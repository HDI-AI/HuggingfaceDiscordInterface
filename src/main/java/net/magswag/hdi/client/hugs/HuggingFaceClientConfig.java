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

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "net.magswag.ai.model")
public class HuggingFaceClientConfig {
  private String baseurl;
  private String id;
  private String token;
  private String defaultResponse;
  private String task;
  private Boolean stream;
  private Map<String, Object> parameters;
  private Map<String, Boolean> options;

  @Bean
  HuggingFaceClient huggingFaceClient() {
    return new HuggingFaceClient(
        baseurl, id, token, defaultResponse, task, stream, parameters, options);
  }

  public void setBaseurl(String baseurl) {
    this.baseurl = baseurl;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setDefaultResponse(String defaultResponse) {
    this.defaultResponse = defaultResponse;
  }

  public void setTask(String task) {
    this.task = task;
  }

  public void setStream(Boolean stream) {
    this.stream = stream;
  }

  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void setOptions(Map<String, Boolean> options) {
    this.options = options;
  }
}
