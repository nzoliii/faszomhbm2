package com.hbm.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe bitset
 *
 * @author mlbv
 */
public class ConcurrentBitSet implements Cloneable {
    private final AtomicLongArray words;
    private final int logicalSize;
    private final LongAdder bitCount = new LongAdder();

    public ConcurrentBitSet(int logicalSize) {
        if (logicalSize < 0) throw new NegativeArraySizeException("logicalSize < 0: " + logicalSize);
        this.logicalSize = logicalSize;
        int wordCount = (logicalSize + 63) >>> 6;
        this.words = new AtomicLongArray(wordCount);
    }

    public ConcurrentBitSet(@NotNull ConcurrentBitSet other) {
        this.logicalSize = other.logicalSize;
        int wordCount = other.words.length();
        this.words = new AtomicLongArray(wordCount);
        for (int i = 0; i < wordCount; i++) {
            this.words.set(i, other.words.get(i));
        }
        this.bitCount.add(other.bitCount.sum());
    }

    @NotNull
    @Contract(value = "_, _ -> new", pure = true)
    public static ConcurrentBitSet fromLongArray(long[] data, int logicalSize) {
        ConcurrentBitSet bitSet = new ConcurrentBitSet(logicalSize);
        int wordsToCopy = Math.min(data.length, bitSet.words.length());
        long totalBits = 0;
        for (int i = 0; i < wordsToCopy; i++) {
            long word = data[i];
            bitSet.words.set(i, word);
            totalBits += Long.bitCount(word);
        }
        bitSet.bitCount.add(totalBits);
        return bitSet;
    }

    @Contract(pure = true)
    public boolean get(int bit) {
        if (bit < 0) throw new IndexOutOfBoundsException("bit < 0: " + bit);
        if (bit >= logicalSize) return false;
        int wordIndex = bit >>> 6;
        long mask = 1L << (bit & 63);
        return (words.get(wordIndex) & mask) != 0;
    }

    public void set(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = words.get(wordIndex);
            long newWord = oldWord | mask;
            if (oldWord == newWord) return;
            if (words.compareAndSet(wordIndex, oldWord, newWord)) {
                bitCount.increment();
                return;
            }
        }
    }

    public void set(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (1L << (toIndex & 63)) - 1L;
        if (toIndex % 64 == 0) endMask = -1L;

        if (startWord == endWord) {
            setBits(startWord, startMask & endMask);
        } else {
            setBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) {
                setBits(i, -1L);
            }
            if (endMask != 0) setBits(endWord, endMask);
        }
    }

    private void setBits(int wordIdx, long mask) {
        if (mask == 0) return;
        while (true) {
            long oldWord = words.get(wordIdx);
            long newWord = oldWord | mask;
            if (oldWord == newWord) return;
            if (words.compareAndSet(wordIdx, oldWord, newWord)) {
                bitCount.add(Long.bitCount(newWord ^ oldWord));
                return;
            }
        }
    }

    public void clear(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long mask = ~(1L << (bit & 63));
        while (true) {
            long oldWord = words.get(wordIndex);
            long newWord = oldWord & mask;
            if (oldWord == newWord) return;
            if (words.compareAndSet(wordIndex, oldWord, newWord)) {
                bitCount.decrement();
                return;
            }
        }
    }

    public void clear(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (1L << (toIndex & 63)) - 1L;
        if (toIndex % 64 == 0) endMask = -1L; // Correct mask for full word

        if (startWord == endWord) {
            clearBits(startWord, startMask & endMask);
        } else {
            clearBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) {
                clearBits(i, -1L);
            }
            if (endMask != 0) clearBits(endWord, endMask);
        }
    }

    private void clearBits(int wordIdx, long mask) {
        if (mask == 0) return;
        while (true) {
            long oldWord = words.get(wordIdx);
            long newWord = oldWord & ~mask;
            if (oldWord == newWord) return;
            if (words.compareAndSet(wordIdx, oldWord, newWord)) {
                bitCount.add(-Long.bitCount(oldWord ^ newWord));
                return;
            }
        }
    }

    public void clear() {
        clear(0, logicalSize);
    }

    public void flip(int bit) {
        if (bit < 0 || bit >= logicalSize) return;
        int wordIndex = bit >>> 6;
        long mask = 1L << (bit & 63);
        while (true) {
            long oldWord = words.get(wordIndex);
            long newWord = oldWord ^ mask;
            if (words.compareAndSet(wordIndex, oldWord, newWord)) {
                bitCount.add((oldWord & mask) == 0 ? 1 : -1);
                return;
            }
        }
    }

    public void flip(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > logicalSize || fromIndex > toIndex) throw new IndexOutOfBoundsException();
        if (fromIndex == toIndex) return;
        int startWord = fromIndex >>> 6;
        int endWord = (toIndex - 1) >>> 6;
        long startMask = -1L << (fromIndex & 63);
        long endMask = (1L << (toIndex & 63)) - 1L;
        if (toIndex % 64 == 0) endMask = -1L; // Correct mask for full word

        if (startWord == endWord) {
            flipBits(startWord, startMask & endMask);
        } else {
            flipBits(startWord, startMask);
            for (int i = startWord + 1; i < endWord; i++) {
                flipBits(i, -1L);
            }
            if (endMask != 0) flipBits(endWord, endMask);
        }
    }

    private void flipBits(int wordIdx, long mask) {
        if (mask == 0) return;
        while (true) {
            long oldWord = words.get(wordIdx);
            long newWord = oldWord ^ mask;
            if (words.compareAndSet(wordIdx, oldWord, newWord)) {
                bitCount.add(Long.bitCount(mask) - 2 * Long.bitCount(mask & oldWord));
                return;
            }
        }
    }

    @Contract(pure = true)
    public int nextSetBit(int from) {
        if (from < 0) from = 0;
        int wordIndex = from >>> 6;
        if (wordIndex >= words.length()) return -1;
        long word = words.get(wordIndex) & (~0L << (from & 63));
        while (true) {
            if (word != 0) {
                int idx = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return (idx < logicalSize) ? idx : -1;
            }
            wordIndex++;
            if (wordIndex >= words.length()) return -1;
            word = words.get(wordIndex);
        }
    }

    @Contract(pure = true)
    public int nextClearBit(int from) {
        if (from < 0) throw new IndexOutOfBoundsException("from < 0: " + from);
        if (from >= this.logicalSize) return from;

        int wordIndex = from >>> 6;
        if (wordIndex >= words.length()) return from;

        long word = ~words.get(wordIndex) & (-1L << (from & 63));

        while (true) {
            if (word != 0) {
                int idx = (wordIndex << 6) + Long.numberOfTrailingZeros(word);
                return Math.min(idx, this.logicalSize);
            }
            wordIndex++;
            if (wordIndex >= words.length()) {
                return this.logicalSize;
            }
            word = ~words.get(wordIndex);
        }
    }

    @Contract(pure = true)
    public int previousSetBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSize) from = logicalSize - 1;
        if (from < 0) return -1;

        int wordIndex = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = words.get(wordIndex) & mask;

        while (true) {
            if (word != 0) {
                return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
            }
            wordIndex--;
            if (wordIndex < 0) return -1;
            word = words.get(wordIndex);
        }
    }

    @Contract(pure = true)
    public int previousClearBit(int from) {
        if (from < 0) return -1;
        if (from >= logicalSize) from = logicalSize - 1;
        if (from < 0) return -1;

        int wordIndex = from >>> 6;
        long mask = ~0L >>> (63 - (from & 63));
        long word = ~words.get(wordIndex) & mask;

        while (true) {
            if (word != 0) {
                return (wordIndex << 6) + (63 - Long.numberOfLeadingZeros(word));
            }
            wordIndex--;
            if (wordIndex < 0) return -1;
            word = ~words.get(wordIndex);
        }
    }

    @Contract(pure = true)
    public boolean isEmpty() {
        return bitCount.sum() == 0;
    }

    @Contract(pure = true)
    public long cardinality() {
        return bitCount.sum();
    }

    @Contract(pure = true)
    public int length() {
        if (isEmpty()) return 0;
        return previousSetBit(logicalSize - 1) + 1;
    }

    @Contract(pure = true)
    public int size() {
        return words.length() * 64;
    }

    @Contract(pure = true)
    public int logicalSize() {
        return logicalSize;
    }

    @Contract(pure = true)
    public boolean intersects(@NotNull ConcurrentBitSet set) {
        if (logicalSize != set.logicalSize) throw new IllegalArgumentException("Different sizes");
        for (int i = 0; i < this.words.length(); i++) {
            if ((words.get(i) & set.words.get(i)) != 0) return true;
        }
        return false;
    }

    public void and(@NotNull ConcurrentBitSet set) {
        if (logicalSize != set.logicalSize) throw new IllegalArgumentException("Different sizes");
        for (int i = 0; i < this.words.length(); i++) {
            long otherWord = set.words.get(i);
            while (true) {
                long oldWord = words.get(i);
                long newWord = oldWord & otherWord;
                if (oldWord == newWord) break;
                if (words.compareAndSet(i, oldWord, newWord)) {
                    bitCount.add(-Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    public void or(@NotNull ConcurrentBitSet set) {
        if (logicalSize != set.logicalSize) throw new IllegalArgumentException("Different sizes");
        int wordsInCommon = Math.min(this.words.length(), set.words.length());
        for (int i = 0; i < wordsInCommon; i++) {
            long otherWord = set.words.get(i);
            while (true) {
                long oldWord = words.get(i);
                long newWord = oldWord | otherWord;
                if (oldWord == newWord) break;
                if (words.compareAndSet(i, oldWord, newWord)) {
                    bitCount.add(Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    public void xor(@NotNull ConcurrentBitSet set) {
        if (logicalSize != set.logicalSize) throw new IllegalArgumentException("Different sizes");
        int wordsInCommon = Math.min(this.words.length(), set.words.length());
        for (int i = 0; i < wordsInCommon; i++) {
            long otherWord = set.words.get(i);
            while (true) {
                long oldWord = words.get(i);
                long newWord = oldWord ^ otherWord;
                if (oldWord == newWord) break;
                if (words.compareAndSet(i, oldWord, newWord)) {
                    bitCount.add(Long.bitCount(otherWord) - 2 * Long.bitCount(otherWord & oldWord));
                    break;
                }
            }
        }
    }

    public void andNot(@NotNull ConcurrentBitSet set) {
        if (logicalSize != set.logicalSize) throw new IllegalArgumentException("Different sizes");
        int wordsInCommon = Math.min(this.words.length(), set.words.length());
        for (int i = 0; i < wordsInCommon; i++) {
            long otherWord = set.words.get(i);
            while (true) {
                long oldWord = words.get(i);
                long newWord = oldWord & ~otherWord;
                if (oldWord == newWord) break;
                if (words.compareAndSet(i, oldWord, newWord)) {
                    bitCount.add(-Long.bitCount(oldWord ^ newWord));
                    break;
                }
            }
        }
    }

    @Contract(pure = true)
    public long[] toLongArray() {
        long[] result = new long[words.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = words.get(i);
        }
        return result;
    }

    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcurrentBitSet other)) return false;
        if (logicalSize != other.logicalSize) return false;
        for (int i = 0; i < words.length(); i++) {
            if (words.get(i) != other.words.get(i)) return false;
        }
        return true;
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        long h = 1234;
        for (int i = words.length() - 1; i >= 0; i--) {
            h ^= words.get(i) * (i + 1);
        }
        return (int) ((h >> 32) ^ h);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        int i = nextSetBit(0);
        if (i != -1) {
            sb.append(i);
            while (true) {
                i = nextSetBit(i + 1);
                if (i == -1) break;
                sb.append(", ").append(i);
            }
        }
        sb.append('}');
        return sb.toString();
    }

    @NotNull
    @Override
    @Contract(value = "-> new", pure = true)
    public ConcurrentBitSet clone() throws CloneNotSupportedException {
        return new ConcurrentBitSet(this);
    }
}