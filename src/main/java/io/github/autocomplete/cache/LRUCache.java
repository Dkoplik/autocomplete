package io.github.autocomplete.cache;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация LRU-кеша для использования в AutocompleteProvider
 */
public class LRUCache<K, V> {
  private static class Node<K, V> {
    K key;
    V value;
    Node<K, V> prev;
    Node<K, V> next;

    Node(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }

  private final int capacity;
  private final Map<K, Node<K, V>> map;
  private Node<K, V> head;
  private Node<K, V> tail;

  /**
   * Создает LRU-кеш указанного размера
   * 
   * @param capacity размер кеша
   * 
   * @throws IllegalArgumentException Если capacity меньше 0
   */
  public LRUCache(int capacity) {
    if (capacity < 0) {
      throw new IllegalArgumentException("capacity cannot be less than 0");
    }

    this.capacity = capacity;
    this.map = new HashMap<>(capacity * 2);
  }

  /**
   * Получить значение из кеша по ключу
   * 
   * @param key ключ элемента
   * 
   * @return значение элемента
   * 
   * @throws IllegalArgumentException Если key равен null
   */
  public V get(K key) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }

    Node<K, V> node = map.get(key);
    if (node == null)
      return null;

    moveToHead(node);
    return node.value;
  }

  /**
   * Добавить новую пару ключ-значение в кеш. Если кеш полон, то вытесняет давно использованный
   * элемент
   * 
   * @param key ключ для кешируемого значения
   * 
   * @param value кешируемое значение
   * 
   * @throws IllegalArgumentException Если key равен null
   */
  public void put(K key, V value) {
    if (key == null) {
      throw new IllegalArgumentException("key cannot be null");
    }

    Node<K, V> node = map.get(key);
    if (node != null) {
      node.value = value;
      moveToHead(node);
      return;
    }

    node = new Node<>(key, value);
    map.put(key, node);
    addToFront(node);

    if (map.size() > capacity) {
      removeTail();
    }
  }

  /**
   * Очистить содержимое кеша.
   */
  public void clear() {
    map.clear();
    head = null;
    tail = null;
  }

  private void addToFront(Node<K, V> node) {
    node.next = head;
    node.prev = null;

    if (head != null) {
      head.prev = node;
    }

    head = node;

    if (tail == null) {
      tail = head;
    }
  }

  private void moveToHead(Node<K, V> node) {
    if (node == head)
      return;

    if (node.prev != null) {
      node.prev.next = node.next;
    }
    if (node.next != null) {
      node.next.prev = node.prev;
    }

    if (node == tail) {
      tail = node.prev;
    }

    addToFront(node);
  }

  private void removeTail() {
    if (tail == null)
      return;

    map.remove(tail.key);

    if (tail.prev != null) {
      tail.prev.next = null;
    } else {
      head = null;
    }

    tail = tail.prev;
  }
}
