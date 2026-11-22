/**
 * Hash Set implementation using separate chaining.
 *
 * Internal structure: an array of "buckets", where each bucket
 * is a linked list of entries that hash to the same index.
 *
 * When the load factor (size/capacity) exceeds a threshold,
 * the table is resized (doubled) and all entries are rehashed.
 */
public class SimpleHashSet<T> {
    private static final int DEFAULT_CAPACITY = 16;
    // Load factor threshold — when exceeded, we resize.
    // 0.75 is the standard value (same as java.util.HashMap).
    // Higher = less memory but more collisions.
    // Lower = more memory but fewer collisions.
    private static final float LOAD_FACTOR = 0.75f;
    private class Entry {
        T key;
        Entry next; // Next entry in the chain (for collision handling)
        Entry(T key, Entry next) {
            this.key = key;
            this.next = next;
        }
    }
    private Entry[] table;
    private int size;
    public SimpleHashSet() {
        this(DEFAULT_CAPACITY);
    }
    public SimpleHashSet(int capacity) {
        table = new Entry[capacity];
        size = 0;
    }
    /**
     * Computes the bucket index for a given key.
     *
     * Key detail: hashCode() can return negative values,
     * so we use Math.abs() to ensure a non-negative index.
     * Then we use modulo to fit within the array bounds.
     *
     * The "& (capacity - 1)" trick works only when capacity
     * is a power of 2, and is faster than modulo.
     */
    private int hash(T key) {
        if (key == null) return 0;
        // Spread the hash bits to reduce collisions.
        // This is similar to what HashMap does internally:
        // XOR the high bits into the low bits.
        int h = key.hashCode();
        h ^= (h >>> 16);
        return Math.abs(h) & (table.length - 1);
    }
    /**
     * Adds an element to the set.
     * @return true if the element was not already present
     */
    public boolean add(T element) {
        // Check if we need to resize BEFORE inserting
        if ((float) size / table.length >= LOAD_FACTOR) {
            resize();
        }
        int index = hash(element);
        Entry bucket = table[index];
        // Check if element already exists in this bucket's chain
        while (bucket != null) {
            if (java.util.Objects.equals(bucket.key, element)) {
                return false; // Already in set
            }
            bucket = bucket.next;
        }
        // Insert at the HEAD of the chain (O(1) insertion).
        // Prepending is simpler than appending (no need to find the tail).
        table[index] = new Entry(element, table[index]);
        size++;
        return true;
    }
    /**
     * Checks if the set contains the given element. O(1) average, O(n) worst.
     */
    public boolean contains(T element) {
        int index = hash(element);
        Entry bucket = table[index];
        while (bucket != null) {
            if (java.util.Objects.equals(bucket.key, element)) {
                return true;
            }
            bucket = bucket.next;
        }
        return false;
    }
    /**
     * Removes an element from the set.
     *
     * We need to find the entry AND its predecessor in the chain,
     * because singly-linked list removal requires updating the
     * previous node's 'next' pointer.
     */
    public boolean remove(T element) {
        int index = hash(element);
        Entry current = table[index];
        Entry prev = null;
        while (current != null) {
            if (java.util.Objects.equals(current.key, element)) {
                if (prev == null) {
                    // Removing the head of the chain
                    table[index] = current.next;
                } else {
                    // Bypass the removed node
                    prev.next = current.next;
                }
                size--;
                return true;
            }
            prev = current;
            current = current.next;
        }
        return false;
    }
    /**
     * Doubles the table size and rehashes all entries.
     *
     * Why double? Because we need the capacity to remain a power of 2
     * (so the "& (capacity - 1)" trick works). Doubling preserves this.
     *
     * Amortized cost: each element is rehashed O(log n) times total
     * across all resizes, giving O(1) amortized insertion.
     */
    private void resize() {
        Entry[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        table = new Entry[newCapacity];
        // Rehash every entry into the new table.
        // Note: we can't just copy references — the hash indices
        // change because the table size changed.
        for (Entry bucket : oldTable) {
            while (bucket != null) {
                Entry next = bucket.next; // Save next before we modify bucket.next
                int newIndex = hash(bucket.key);
                // Prepend to new bucket chain
                bucket.next = table[newIndex];
                table[newIndex] = bucket;
                bucket = next;
            }
        }
    }
    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
}