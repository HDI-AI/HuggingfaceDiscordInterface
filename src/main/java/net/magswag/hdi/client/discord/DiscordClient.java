/*
 *    The Huggingface Discord Interface links Discord users with Huggingface Inference API as a demonstration of the progress of LLM.  This software is not associated with Discord or Huggingface, and is intended for educational purposes.
 *
 *    Copyright (c) 2023.
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

package net.magswag.hdi.client.discord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.magswag.hdi.client.hugs.HuggingFaceClient;
import net.magswag.hdi.client.util.OrderedQueueList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordClient {

  private final JDA client;
  private final String channelId;
  private final String guildId;
  private final boolean filterChannel;
  private final HuggingFaceClient huggingFaceChat;

  private final List<String> defaultHistory;
  private static final Logger logger = LoggerFactory.getLogger(DiscordClient.class);

  public DiscordClient(
      JDA client,
      String channelId,
      String guildId,
      boolean filterChannel,
      HuggingFaceClient huggingFaceChat,
      String defaultHistory) {
    this.client = client;
    this.channelId = channelId;
    this.guildId = guildId;
    this.filterChannel = filterChannel;
    this.huggingFaceChat = huggingFaceChat;
    this.defaultHistory = List.of(defaultHistory.split("\n"));
    client.addEventListener(eventListenerBuilder());
  }

  private ListenerAdapter eventListenerBuilder() {
    return new ListenerAdapter() {
      @Override
      public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        logger.info("guildID:  {}", event.getGuild().getId());
        logger.debug("channelID:  {}", event.getChannel().getId());
        logger.info("messageID:  {}", event.getMessageId());

        if (event.getMessage().getMentions().mentionsEveryone()
            || (!event.getGuild().getId().equals(guildId))
            || (filterChannel && !event.getChannel().getId().equals(channelId))) {
          return; // Ignore messages from other channels, discords, and @everyone mentions
        }
        if (client
                .getSelfUser()
                .getName()
                .equals(
                    Optional.ofNullable(event.getMessage().getReferencedMessage())
                        .map(Message::getAuthor)
                        .map(User::getName)
                        .orElse(""))
            || event.getMessage().getContentRaw().contains(client.getSelfUser().getAsMention())) {
          logger.info(
              "Received ping from user {} (bot:{}) in channel {}",
              event.getAuthor().getName(),
              event.getAuthor().isBot(),
              event.getChannel().getName());
          logger.info(
              Optional.ofNullable(event.getMessage().getReferencedMessage())
                  .map(Message::getContentRaw)
                  .orElse("none"));

          try {
            String userPrompt = buildHistoryPrompt(event);
            String response =
                Optional.ofNullable(huggingFaceChat.sendPrompt(userPrompt))
                    .filter(m -> !m.isEmpty())
                    .orElse("*ignores*");
            event.getMessage().reply(response).queue();
            logger.info("Sent response to user: {}", response);
          } catch (Exception e) {
            logger.error("Error processing user request", e);
            event.getChannel().sendMessage("I don't have the emotional energy for this").queue();
          }
        }
      }
    };
  }

  private String buildHistoryPrompt(MessageReceivedEvent event) {
    String preprocessedText =
        event.getMessage().getContentRaw().replace(client.getSelfUser().getAsMention(), "").trim();

    String name = event.getAuthor().getName();
    OrderedQueueList<String> chatPrompt = new OrderedQueueList<>(12);
    chatPrompt.addAll(defaultHistory);
    List<String> previousHistory =
        includeHistory(0, new ArrayList<>(), event.getChannel(), event.getMessage());
    if (!previousHistory.isEmpty()) {
      chatPrompt.addAll(previousHistory);
    }
    chatPrompt.add(String.format("User: \"%s\".", preprocessedText));
    chatPrompt.add("Falcon:");
    chatPrompt.add(0, String.format("User: \"My name is %s\".", name));
    chatPrompt.add(1, String.format("Falcon:Hi %s.", name));
    logger.debug("prompt: {}", chatPrompt);
    return String.join("\n", chatPrompt).trim();
  }

  private List<String> includeHistory(
      int depth, List<String> list, MessageChannelUnion channel, Message message) {
    Optional<String> content =
        Optional.ofNullable(message.getReferencedMessage())
            .map(Message::getContentRaw)
            .filter(text -> !text.isBlank())
            .map(m -> m.replace(client.getSelfUser().getAsMention(), ""));
    if (depth < 8 && content.isPresent()) {
      logger.debug("history added: {}", content.get());
      String author =
          Optional.ofNullable(message.getReferencedMessage())
              .map(Message::getAuthor)
              .map(User::getName)
              .map(authorName -> authorName.replace("AI Felix", "Falcon"))
              .filter(authorName -> authorName.equals("Falcon"))
              .orElse("User");
      list.add(0, author + ":" + content.get());
      return includeHistory(
          depth + 1,
          list,
          channel,
          channel.retrieveMessageById(message.getReferencedMessage().getId()).complete());
    }
    logger.debug("history chain ended");
    return list;
  }

  public void start() throws InterruptedException {
    client.awaitReady();
  }
}
