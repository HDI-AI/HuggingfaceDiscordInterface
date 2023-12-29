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

package net.magswag.hdi.exceptions;

public class HuggingException extends Exception {
  public HuggingException() {
    super();
  }

  public HuggingException(String message) {
    super(message);
  }

  public HuggingException(String message, Throwable cause) {
    super(message, cause);
  }

  public HuggingException(Throwable cause) {
    super(cause);
  }
}
