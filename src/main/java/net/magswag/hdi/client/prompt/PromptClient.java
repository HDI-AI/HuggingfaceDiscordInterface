/*
 *     The Huggingface Discord Interface links Discord users
 *     with Huggingface Inference API with the intention of
 *     demonstrating the progress of LLM.
 *
 *     This software is not associated with Discord or Huggingface,
 *     and is intended for educational purposes.
 *
 *    Copyright (c) 2024.
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

package net.magswag.hdi.client.prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.magswag.hdi.util.OrderedQueueList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PromptClient {

  private static final Logger logger = LoggerFactory.getLogger(PromptClient.class);

  private final String systemPrompt;
  private final List<String> defaultHistory;
  private final boolean allowHistory;
  private final int historySize;
  private final int historyDepth;
  private final String historyDelimiter;
  private final String userPrefix;
  private final String userSuffix;

  private final String firstChatToken;
  private final String aiPrefix;
  private final String aiSuffix;
  private final String characterName;

  public PromptClient(
      String systemPrompt,
      String defaultHistory,
      boolean allowHistory,
      Integer historySize,
      String historyDelimiter,
      String userPrefix,
      String userSuffix,
      String firstChatToken,
      String aiPrefix,
      String aiSuffix,
      String characterName) {
    this.systemPrompt = systemPrompt;
    this.defaultHistory = List.of(defaultHistory.split(","));
    this.allowHistory = allowHistory;
    this.historySize = Optional.ofNullable(historySize).orElse(10);
    this.historyDelimiter = historyDelimiter;
    this.userPrefix = userPrefix;
    this.userSuffix = userSuffix;
    this.firstChatToken = firstChatToken;
    this.aiPrefix = aiPrefix;
    this.aiSuffix = aiSuffix;
    this.characterName = characterName;
    //subtract 1 for the current message exchange
    this.historyDepth = this.historySize - 1;
  }

  public final String buildPrompt(String selfMention, String selfName, MessageReceivedEvent event) {
    return String.format(
        this.systemPrompt + buildHistoryPrompt(selfMention, selfName, event),
        characterName,
        event.getAuthor().getName());
  }

  private String buildHistoryPrompt(
      String selfMention, String selfName, MessageReceivedEvent event) {
    String preprocessedText =
        event.getMessage().getContentRaw().replace(selfMention, "").replace("%", "").trim();
    OrderedQueueList<String> chatPrompt = new OrderedQueueList<>(historySize);
    chatPrompt.addAll(defaultHistory);
    if (allowHistory) {
      List<String> previousHistory =
          includeHistory(
              0, selfMention, selfName, new ArrayList<>(), event.getChannel(), event.getMessage());
      if (!previousHistory.isEmpty()) {
        chatPrompt.addAll(previousHistory);
      }
    }

    //add the user's current message
    chatPrompt.add(addUserChatToken(preprocessedText) + firstChatToken);

    logger.debug("prompt: {}", chatPrompt);
    return String.join(historyDelimiter, chatPrompt).trim();
  }

  private List<String> includeHistory(
      int depth,
      String selfMention,
      String selfName,
      List<String> list,
      MessageChannelUnion channel,
      Message message) {
    Optional<String> content =
        Optional.ofNullable(message.getReferencedMessage())
            .map(Message::getContentRaw)
            .filter(text -> !text.isBlank())
            .map(m -> m.replace(selfMention, "").replace("%", ""));
    if (depth < historyDepth && content.isPresent()) {
      logger.debug("history added: {}", content.get());
      boolean isAI =
          Optional.ofNullable(message.getReferencedMessage())
              .map(Message::getAuthor)
              .map(User::getName)
              .filter(selfName::equals)
              .isPresent();
      if (isAI) {
        list.add(0, addAIChatToken(content.get()));
      } else {
        list.add(0, addUserChatToken(content.get()));
      }
      return includeHistory(
          depth + 1,
          selfMention,
          selfName,
          list,
          channel,
          channel.retrieveMessageById(message.getReferencedMessage().getId()).complete());
    }
    return list;
  }

  private String addAIChatToken(String content) {
    return this.aiPrefix + content + this.aiSuffix;
  }

  private String addUserChatToken(String content) {
    return this.userPrefix + content + this.userSuffix;
  }
}
