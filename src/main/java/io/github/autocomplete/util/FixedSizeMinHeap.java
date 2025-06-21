package io.github.autocomplete.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реализация кучи фиксированного размера для эффективного поиска top-N элементов
 */
public class FixedSizeMinHeap {
  private final List<WordFrequency> heap;
  private final int maxSize;

  /**
   * Создает кучу с указанным максимальным размером
   *
   * @param maxSize Максимальное количество элементов в куче
   *
   * @throws IllegalArgumentException Если maxSize меньше 0
   */
  public FixedSizeMinHeap(int maxSize) {
    if (maxSize < 0) {
      throw new IllegalArgumentException("maxSize cannot be less than 0");
    }

    this.maxSize = maxSize;
    this.heap = new ArrayList<>();
  }

  /**
   * Добавляет элемент в кучу, сохраняя свойства кучи
   *
   * @param wordFrequency Элемент для добавления
   *
   * @throws IllegalArgumentException Если wordFrequency равен null
   */
  public void add(WordFrequency wordFrequency) {
    if (wordFrequency == null) {
      throw new IllegalArgumentException("wordFrequency cannot be null");
    }

    if (heap.size() < maxSize) {
      heap.add(wordFrequency);
      siftUp(heap.size() - 1);
    } else if ((maxSize > 0) && (wordFrequency.compareTo(heap.get(0)) > 0)) {
      heap.set(0, wordFrequency);
      siftDown(0);
    }
  }

  /**
   * Преобразует кучу в отсортированный по убыванию список
   *
   * @return Отсортированный список элементов
   */
  public List<WordFrequency> toSortedList() {
    List<WordFrequency> sorted = new ArrayList<>(heap);
    Collections.sort(sorted, Collections.reverseOrder());
    return sorted;
  }

  /**
   * Поднимает элемент вверх по куче, восстанавливая свойства кучи
   *
   * @param index Индекс элемента для подъема
   */
  private void siftUp(int index) {
    while (index > 0) {
      int parentIndex = (index - 1) / 2;
      if (heap.get(index).compareTo(heap.get(parentIndex)) < 0) {
        Collections.swap(heap, index, parentIndex);
        index = parentIndex;
      } else {
        break;
      }
    }
  }

  /**
   * Опускает элемент вниз по куче, восстанавливая свойства кучи
   *
   * @param index Индекс элемента для опускания
   */
  private void siftDown(int index) {
    int smallest = index;
    int left = 2 * index + 1;
    int right = 2 * index + 2;
    int size = heap.size();

    if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0) {
      smallest = left;
    }
    if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0) {
      smallest = right;
    }

    if (smallest != index) {
      Collections.swap(heap, index, smallest);
      siftDown(smallest);
    }
  }
}
