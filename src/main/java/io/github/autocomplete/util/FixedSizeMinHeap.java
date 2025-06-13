package io.github.autocomplete.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Реализация кучи фиксированного размера для эффективного поиска top-N элементов
 */
public class FixedSizeMinHeap {
  private final List<Candidate> heap;
  private final int maxSize;

  /**
   * Создает кучу с указанным максимальным размером
   * 
   * @param maxSize Максимальное количество элементов в куче
   */
  public FixedSizeMinHeap(int maxSize) {
    this.maxSize = maxSize;
    this.heap = new ArrayList<>();
  }

  /**
   * Добавляет элемент в кучу, сохраняя свойства кучи
   * 
   * @param candidate Элемент для добавления
   */
  public void add(Candidate candidate) {
    if (heap.size() < maxSize) {
      heap.add(candidate);
      siftUp(heap.size() - 1);
    } else if ((maxSize > 0) && (candidate.compareTo(heap.get(0)) > 0)) {
      heap.set(0, candidate);
      siftDown(0);
    }
  }

  /**
   * Преобразует кучу в отсортированный по убыванию список
   * 
   * @return Отсортированный список элементов
   */
  public List<Candidate> toSortedList() {
    List<Candidate> sorted = new ArrayList<>(heap);
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
