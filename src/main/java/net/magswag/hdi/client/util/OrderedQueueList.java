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

package net.magswag.hdi.client.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OrderedQueueList<E> extends ArrayList<E> {

  private int limit = 10;

  public OrderedQueueList(int initialCapacity) {
    super(initialCapacity);
    this.limit = initialCapacity;
  }

  public OrderedQueueList() {
    super();
  }

  @Override
  public boolean add(E e) {
    while (size() >= limit) {
      remove(0);
    }
    return super.add(e);
  }

  @Override
  public void add(int index, E element) {
    remove(index);
    super.add(index, element);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    removeRange(index, size() - 1);
    return super.addAll(index, c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    if (size() > 0) {
      int lastIndex;
      if (size() >= c.size()) {
        lastIndex = c.size() - 1;
      } else {
        lastIndex = size() - 1;
      }
      removeRange(0, lastIndex);
    }
    if (c.size() > limit) {
      return super.addAll(((List<? extends E>) c).subList(c.size() - 1 - limit, c.size() - 1));
    }
    return super.addAll(c);
  }
}
