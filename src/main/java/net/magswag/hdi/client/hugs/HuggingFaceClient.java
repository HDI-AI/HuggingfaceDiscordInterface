/*
 *     The Huggingface Discord Interface links Discord users
 *     with Huggingface Inference API with the intention of
 *     demonstrating the progress of LLM.
 *
 *     This software is not associated with Discord or Huggingface,
 *     and is intended for educational purposes.
 *
 *    Copyright (c) 2023-2024.
 *
 *    This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.magswag.hdi.client.hugs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.magswag.hdi.exceptions.HuggingException;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuggingFaceClient {
  private static final Logger logger = LoggerFactory.getLogger(HuggingFaceClient.class);
  private static final Pattern IS_HTTP_CODE = Pattern.compile("^\\d*$");
  private final String baseUrl;
  private final String modelId;
  private final String accessToken;

  private final String defaultResponse;
  public final String task;
  public final Boolean stream;
  public final Map<String, Object> parameters;
  public final Map<String, Boolean> options;

  private final ObjectMapper mapper;

  public HuggingFaceClient(
      String baseUrl,
      String modelId,
      String accessToken,
      String defaultResponse,
      String task,
      Boolean stream,
      Map<String, Object> parameters,
      Map<String, Boolean> options) {
    this.baseUrl = baseUrl;
    this.modelId = modelId;
    this.accessToken = accessToken;
    this.defaultResponse = defaultResponse;
    this.task = task;
    this.stream = stream;
    this.parameters = castParameters(parameters);
    this.options = options;
    this.mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  }

  private Map<String, Object> castParameters(Map<String, Object> parameters) {
    return Optional.ofNullable(parameters)
        .orElse(Collections.emptyMap())
        .entrySet()
        .stream()
        .map(
            entry -> {
              String value = (String) entry.getValue();
              if ("stop".equals(entry.getKey())) {
                return new AbstractMap.SimpleEntry<>(
                    entry.getKey(),
                    Stream.of(value.split("(?<!\\\\),"))
                        .filter(token -> !token.isEmpty())
                        .map(token -> token.replace("\\,", ","))
                        .collect(Collectors.toList()));
              }
              try {
                return new AbstractMap.SimpleEntry<>(entry.getKey(), Integer.parseInt(value));
              } catch (NumberFormatException e0) {
                try {
                  return new AbstractMap.SimpleEntry<>(entry.getKey(), Double.parseDouble(value));
                } catch (NumberFormatException e1) {
                  try {
                    return new AbstractMap.SimpleEntry<>(
                        entry.getKey(), Boolean.parseBoolean(value));
                  } catch (Exception e2) {
                    return entry;
                  }
                }
              }
            })
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public String sendPrompt(String prompt) throws HuggingException {
    HttpClient client = HttpClient.newHttpClient();
    logger.debug("task: " + this.task);
    logger.debug("options: " + this.options);

    try {
      // Build request body
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("inputs", prompt.trim());
      requestBody.put("task", this.task);
      requestBody.put("parameters", parameters);
      requestBody.put("options", this.options);
      requestBody.put("stream", Optional.ofNullable(this.stream).orElse(false));

      // Serialize request body to JSON
      String requestBodyJson = mapper.writeValueAsString(requestBody);

      // Build request
      HttpRequest request =
          HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
              .uri(URI.create(baseUrl + "/models/" + modelId))
              .header("Authorization", "Bearer " + accessToken)
              .header("Content-Type", "application/json")
              .build();

      // Send request and parse response
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      Optional<String> status = Optional.ofNullable(fetchResponseCode(response));

      String responseString = parseResponseBody(response);

      status
          .filter(code -> code.startsWith("2"))
          .orElseThrow(() -> new HuggingException("Cannot reach AI through API"));
      return responseString;
    } catch (InterruptedException | IOException e) {
      logger.error("Error sending or receiving request to Hugging Face API", e);
      throw new HuggingException("Error sending or receiving request to Hugging Face API", e);
    }
  }

  private String parseResponseBody(HttpResponse<String> response)
      throws HuggingException, JsonProcessingException {
    logger.info(
        "response: {}",
        Optional.ofNullable(response.body())
            .orElseThrow(() -> new HuggingException("Missing response body")));

    String responseString =
        Optional.<List<Map<String, Object>>>ofNullable(
                mapper.readValue(response.body(), new TypeReference<>() {}))
            .flatMap(list -> list.stream().findFirst())
            .map(
                map ->
                    String.valueOf(
                        map.getOrDefault(
                            "generated_text", map.getOrDefault("error", "AI_response_unknown"))))
            .filter(responseText -> !"AI_response_unknown".equals(responseText))
            .map(
                responseText -> {
                  var filteredText = responseText;
                  if (null != parameters.get("stop") && parameters.get("stop") instanceof List) {
                    for (Object stopToken : ((List<?>) parameters.get("stop"))) {
                      logger.debug("splitting {} on {}", filteredText, stopToken);
                      filteredText =
                          Arrays.stream(filteredText.split(Pattern.quote((String) stopToken)))
                              .sequential()
                              .findFirst()
                              .orElse("");
                    }
                  }
                  if (filteredText.isBlank()) {
                    filteredText = defaultResponse;
                  }
                  return filteredText.trim();
                })
            .orElseThrow(() -> new HuggingException("Unable to parse the response"));

    logger.debug("Received response: {}", responseString);
    return responseString;
  }

  private String fetchResponseCode(HttpResponse<String> response) {
    Optional<String> status =
        response
            .headers()
            .firstValue(":status")
            .map(String::trim)
            .filter(code -> IS_HTTP_CODE.matcher(code).matches());
    logger.info(
        "Response code: {} ({})",
        status.orElse("<missing>"),
        status
            .map(
                code ->
                    EnglishReasonPhraseCatalog.INSTANCE.getReason(
                        Integer.parseInt(code), Locale.US))
            .orElse("N/A"));
    return status.orElse(null);
  }
}
