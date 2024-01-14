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

package net.magswag.hdi.client.discord;

import java.util.Optional;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.magswag.hdi.client.hugs.HuggingFaceClient;
import net.magswag.hdi.client.prompt.PromptClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordClient {

  private final JDA client;
  private final String channelId;
  private final String guildId;

  private final PromptClient chat;

  private final boolean strictLeave;
  private final boolean filterChannel;
  private final HuggingFaceClient huggingFaceChat;
  private static final Logger logger = LoggerFactory.getLogger(DiscordClient.class);

  public DiscordClient(
      JDA client,
      String channelId,
      String guildId,
      boolean strictLeave,
      boolean filterChannel,
      PromptClient promptClient,
      HuggingFaceClient huggingFaceChat) {
    this.chat = promptClient;
    this.client = client;
    this.channelId = channelId;
    this.guildId = guildId;
    this.strictLeave = strictLeave;
    this.filterChannel = filterChannel;
    this.huggingFaceChat = huggingFaceChat;
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
              .sendMessage("Goodbye cruel world!")
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
            String prompt =
                chat.buildPrompt(
                    client.getSelfUser().getAsMention(), client.getSelfUser().getName(), event);
            String response = huggingFaceChat.sendPrompt(prompt);
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

  public void start() throws InterruptedException {
    client.awaitReady();
  }
}
