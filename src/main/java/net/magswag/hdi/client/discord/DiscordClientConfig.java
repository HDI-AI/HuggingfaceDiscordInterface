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

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.magswag.hdi.client.hugs.HuggingFaceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "net.magswag.ai.discord")
public class DiscordClientConfig {

  private String token;
  private String channelID;
  private String guildID;
  private Boolean filterChannel;
  private String activity;
  private String messageHistory;
  private String bot;
  private String user;
  private String ai;

  private Integer historySize;

  @Bean
  @Autowired
  public DiscordClient discordClient(HuggingFaceClient huggingFaceClient) {
    return new DiscordClient(
        JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .setActivity(Activity.playing(activity))
            .build(),
        channelID,
        guildID,
        filterChannel,
        huggingFaceClient,
        messageHistory,
        bot,
        user,
        ai,
        historySize);
  }

  public void setToken(String token) {
    this.token = token;
  }

  public void setChannelID(String channelID) {
    this.channelID = channelID;
  }

  public void setGuildID(String guildID) {
    this.guildID = guildID;
  }

  public void setFilterChannel(Boolean filterChannel) {
    this.filterChannel = filterChannel;
  }

  public void setMessageHistory(String messageHistory) {
    this.messageHistory = messageHistory;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public void setBot(String bot) {
    this.bot = bot;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public void setAi(String ai) {
    this.ai = ai;
  }

  public void setHistorySize(Integer historySize) {
    this.historySize = historySize;
  }
}
