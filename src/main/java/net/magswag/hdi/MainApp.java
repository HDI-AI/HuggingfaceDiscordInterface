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

package net.magswag.hdi;

import net.magswag.hdi.client.discord.DiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApp implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
  @Autowired DiscordClient discordClient;

  public static void main(String[] args) {
    SpringApplication.run(MainApp.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    discordClient.start();
    logger.info("Hugging Face Discord Bot started successfully!");
  }
}
