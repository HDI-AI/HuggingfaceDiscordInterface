/*
 *    The Huggingface Discord Interface links Discord users with Huggingface Inference API with the
 *     intention of demonstrating the progress of LLM.
 *     This software is not associated with Discord or Huggingface,
 *     and is intended for educational purposes.
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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
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

  private final boolean strictLeave;
  private final boolean filterChannel;
  private final HuggingFaceClient huggingFaceChat;
  private final List<String> defaultHistory;

  private final String bot;
  private final String user;
  private final String ai;
  private final boolean allowHistory;
  private final int historySize;
  private final int historyDepth;
  private static final Logger logger = LoggerFactory.getLogger(DiscordClient.class);

  public DiscordClient(
      JDA client,
      String channelId,
      String guildId,
      boolean strictLeave,
      boolean filterChannel,
      HuggingFaceClient huggingFaceChat,
      String defaultHistory,
      String bot,
      String user,
      String ai,
      boolean allowHistory,
      Integer historySize) {
    this.client = client;
    this.channelId = channelId;
    this.guildId = guildId;
    this.strictLeave = strictLeave;
    this.filterChannel = filterChannel;
    this.huggingFaceChat = huggingFaceChat;
    this.defaultHistory = List.of(defaultHistory.split("\n"));
    this.bot = bot;
    this.user = user;
    this.ai = ai;
    this.allowHistory = allowHistory;
    this.historySize = Optional.ofNullable(historySize).orElse(10);

    //subtract 2 for the name context, and another 2 for the current message exchange
    this.historyDepth = this.historySize - 4;

    this.client.addEventListener(eventListenerBuilder());
  }

  private ListenerAdapter eventListenerBuilder() {
    return new ListenerAdapter() {
      @Override
      public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        logger.info("guildID:  {}", event.getGuild().getId());
        logger.debug("channelID:  {}", event.getChannel().getId());
        logger.info("messageID:  {}", event.getMessageId());

        // Leave guilds that are no longer allowed
        if (strictLeave && !guildId.equals(event.getGuild().getId())) {
          logger.info("Leaving guild: {}", event.getGuild().getName());

          event
              .getChannel()
              .sendMessage("good bye!")
              .queue(
                  (event1) -> event.getGuild().leave().queue(),
                  error -> {
                    logger.error("Unable to say goodbye", error);
                    event.getGuild().leave().queue();
                  });
          return;
        }
        if (event.getMessage().getMentions().mentionsEveryone()
            || (filterChannel && !event.getChannel().getId().equals(channelId))) {
          return; // Ignore messages from other channels and @everyone mentions
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
            String response = huggingFaceChat.sendPrompt(userPrompt);
            event.getMessage().reply(response).queue();
            logger.info("Sent response to user: {}", response);
          } catch (Exception e) {
            logger.error("Error processing user request", e);
            event.getChannel().sendMessage("I don't have the emotional energy for this").queue();
          }
        }
      }

      public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        logger.info("Joined guild: {}", guild.getName());
        // Leave guilds that try to invite the bot
        if (!guildId.equals(guild.getId())) {
          logger.info("Leaving guild: {}", guild.getName());
          guild.leave().queue();
        }
      }
    };
  }

  private String buildHistoryPrompt(MessageReceivedEvent event) {
    String preprocessedText =
        event.getMessage().getContentRaw().replace(client.getSelfUser().getAsMention(), "").trim();
    String name = event.getAuthor().getName();

    OrderedQueueList<String> chatPrompt = new OrderedQueueList<>(historySize);

    chatPrompt.addAll(defaultHistory);
    if (allowHistory) {
      List<String> previousHistory =
          includeHistory(0, new ArrayList<>(), event.getChannel(), event.getMessage());
      if (!previousHistory.isEmpty()) {
        chatPrompt.addAll(previousHistory);
      }
    }

    //add the user's current message
    chatPrompt.add(String.format("%s: \"%s\".", user, preprocessedText));
    chatPrompt.add(ai + ":");

    //add fake history providing context of the user's name.
    chatPrompt.add(0, String.format("%s: \"My name is %s\".", user, name));
    chatPrompt.add(1, String.format("%s:Hi %s.", ai, name));

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
    if (depth < historyDepth && content.isPresent()) {
      logger.debug("history added: {}", content.get());
      String author =
          Optional.ofNullable(message.getReferencedMessage())
              .map(Message::getAuthor)
              .map(User::getName)
              .map(authorName -> authorName.replace(bot, ai))
              .filter(authorName -> authorName.equals(ai))
              .orElse(user);
      list.add(0, author + ":" + content.get());
      return includeHistory(
          depth + 1,
          list,
          channel,
          channel.retrieveMessageById(message.getReferencedMessage().getId()).complete());
    }
    return list;
  }

  public void start() throws InterruptedException {
    client.awaitReady();
  }
}
