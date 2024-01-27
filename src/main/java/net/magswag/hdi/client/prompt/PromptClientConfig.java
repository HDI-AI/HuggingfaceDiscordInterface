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

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "net.magswag.ai.prompt")
public class PromptClientConfig {
  private String systemPrompt;
  private String defaultHistory;
  private Boolean allowHistory;
  private String historyDelimiter;
  private Integer historySize;
  private String userPrefix;
  private String userSuffix;

  private String firstChatToken;
  private String aiPrefix;
  private String aiSuffix;
  private String characterName;

  @PostConstruct
  public void init() {
    if (historyDelimiter == null) {
      historyDelimiter = "";
    }
    if (userPrefix == null) {
      userPrefix = "";
    }
    if (userSuffix == null) {
      userSuffix = "";
    }
    if (firstChatToken == null) {
      firstChatToken = "";
    }
    if (aiPrefix == null) {
      aiPrefix = "";
    }
    if (aiSuffix == null) {
      aiSuffix = "";
    }
  }

  @Bean
  PromptClient promptClient() {
    return new PromptClient(
        systemPrompt,
        defaultHistory,
        allowHistory,
        historySize,
        historyDelimiter,
        userPrefix,
        userSuffix,
        firstChatToken,
        aiPrefix,
        aiSuffix,
        characterName);
  }

  public void setSystemPrompt(String systemPrompt) {
    this.systemPrompt = systemPrompt;
  }

  public void setDefaultHistory(String defaultHistory) {
    this.defaultHistory = defaultHistory;
  }

  public void setAllowHistory(Boolean allowHistory) {
    this.allowHistory = allowHistory;
  }

  public void setHistorySize(Integer historySize) {
    this.historySize = historySize;
  }

  public void setHistoryDelimiter(String historyDelimiter) {
    this.historyDelimiter = historyDelimiter;
  }

  public void setUserPrefix(String userPrefix) {
    this.userPrefix = userPrefix;
  }

  public void setUserSuffix(String userSuffix) {
    this.userSuffix = userSuffix;
  }

  public void setFirstChatToken(String firstChatToken) {
    this.firstChatToken = firstChatToken;
  }

  public void setAiPrefix(String aiPrefix) {
    this.aiPrefix = aiPrefix;
  }

  public void setAiSuffix(String aiSuffix) {
    this.aiSuffix = aiSuffix;
  }

  public void setCharacterName(String characterName) {
    this.characterName = characterName;
  }
}
