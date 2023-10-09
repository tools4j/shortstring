/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 tools4j.org (Marco Terzer)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.shortstring;

import java.io.IOException;

/**
 * Contains utility methods for chars and char sequences.
 */
enum Chars {
    ;
    private static final char[] HEX_DIGITS = {
            '0' , '1' , '2' , '3' , '4' , '5' , '6' , '7' ,
            '8' , '9' , 'A' , 'B' , 'C' , 'D' , 'E' , 'F'
    };


    /**
     * Note: a shorter string is always considered before a longer string.
     * @param a sequence a
     * @param b sequence b
     * @return true if a {@code a <= b}
     */
    static boolean leq(final long a, final long b) {
        return a <= b;
    }

    /**
     * Note: a shorter string is always considered before a longer string.
     * @param a1 first part of sequence a
     * @param a2 second part of sequence a
     * @param b sequence b
     * @return true if a {@code a1|a2 <= b}
     */
    static boolean leq(final long a1, final long a2, final CharSequence b) {
        final long b1 = longSeq1(b);
        return a1 < b1 || (a1 == b1 && a2 <= longSeq2(b));
    }

    static boolean leq(final CharSequence a, final CharSequence b) {
        final int len = a.length();
        if (len < b.length()) {
            return true;
        }
        if (len > b.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            final int cmp;
            if ((cmp = Character.compare(a.charAt(i), b.charAt(i))) != 0) {
                return cmp < 0;
            }
        }
        //equal
        return true;
    }

    static boolean eq(final CharSequence a, final CharSequence b) {
        final int len;
        if ((len = a.length()) != b.length()) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    static boolean isDigit(final char ch) {
        return '0' <= ch && ch <= '9';
    }

    static boolean isLetter(final char ch) {
        return 'A' <= ch && ch <= 'Z';
    }

    static boolean isAlphanumeric(final char ch) {
        return isDigit(ch) || isLetter(ch);
    }

    static boolean isAlphanumeric(final CharSequence seq) {
        return isAlphanumeric(seq, 0, seq.length());
    }

    static boolean isAlphanumeric(final long seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (!isAlphanumeric(charFromSeq(seq, i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isAlphanumeric(final long seq1, final long seq2, final int start, final int end) {
        return isAlphanumeric(seq1, start, Math.min(Long.BYTES, end)) &&
                isAlphanumeric(seq2, Math.max(Long.BYTES, start), end);
    }

    static boolean isAlphanumeric(final CharSequence seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (!isAlphanumeric(seq.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static int indexOfFirstLetter(final long seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (isLetter(charFromSeq(seq, i))) {
                return i;
            }
        }
        return -1;
    }

    static int indexOfFirstLetter(final long seq1, final long seq2, final int start, final int end) {
        final int end1 = Math.min(Long.BYTES, end);
        for (int i = start; i < end1; i++) {
            if (isLetter(charFromSeq(seq1, i))) {
                return i;
            }
        }
        final int start2 = Math.max(0, start - Long.BYTES);
        final int end2 = Math.max(0, end - Long.BYTES);
        for (int i = start2; i < end2; i++) {
            if (isLetter(charFromSeq(seq2, i))) {
                return i + Long.BYTES;
            }
        }
        return -1;
    }

    static int indexOfFirstDigit(final long seq1, final long seq2, final int start, final int end) {
        final int end1 = Math.min(Long.BYTES, end);
        for (int i = start; i < end1; i++) {
            if (isDigit(charFromSeq(seq1, i))) {
                return i;
            }
        }
        final int start2 = Math.max(0, start - Long.BYTES);
        final int end2 = Math.max(0, end - Long.BYTES);
        for (int i = start2; i < end2; i++) {
            if (isDigit(charFromSeq(seq2, i))) {
                return i + Long.BYTES;
            }
        }
        return -1;
    }

    static int indexOfFirstLetter(final CharSequence seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (isLetter(seq.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    static int indexOfFirstDigit(final CharSequence seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (isDigit(seq.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    static boolean isNumeric(final CharSequence seq) {
        final int len = seq.length();
        if (len == 0) {
            return false;
        }
        int i = 0;
        char first = seq.charAt(i++);
        if (first == '-') {
            if (len == 1) {
                return false;
            }
            seq.charAt(i++);
        }
        if (!isDigit(first)) {
            return false;
        }
        if (first == '0') {
            return len == 1;
        }
        while (i < len) {
            if (!isDigit(seq.charAt(i++))) {
                return false;
            }
        }
        return true;
    }

    static boolean isSignChar(final char ch) {
        return ch == '.' || ch == '-';
    }

    static boolean startsWithSignChar(final long seq) {
        return isSignChar(charFromSeq(seq, 0));
    }

    static boolean startsWithSignChar(final CharSequence seq) {
        return seq.length() > 0 && isSignChar(seq.charAt(0));
    }

    static char toLetter0(final int value) {
        assert 0 <= value && value < 26;
        return (char)(value + 'A');
    }

    static char toLetter0(final long value) {
        assert 0 <= value && value < 26;
        return (char)(value + 'A');
    }
    static char toLetter(final int value) {
        final int code = value % 26;
        return (char)(code + 'A');
    }

    static char toLetter(final long value) {
        final long code = value % 26;
        return (char)(code + 'A');
    }

    static char toAlphanumeric0(final int value) {
        assert 0 <= value && value < 36;
        return (char)(value + (value < 10 ? '0' : 'A' - 10));
    }

    static char toAlphanumeric0(final long value) {
        assert 0 <= value && value < 36;
        return (char)(value + (value < 10 ? '0' : 'A' - 10));
    }

    static char toAlphanumeric(final int value) {
        final int code = value % 36;
        return (char)(code + (code < 10 ? '0' : 'A' - 10));
    }

    static char toAlphanumeric(final long value) {
        final long code = value % 36;
        return (char)(code + (code < 10 ? '0' : 'A' - 10));
    }

    static char toDigit(final int value) {
        final int code = value % 10;
        return (char)(code + '0');
    }

    static char toHex(final int value) {
        return HEX_DIGITS[value & 0xf];
    }

    static char toHex(final long value) {
        return toHex((int)value);
    }

    static boolean isHex(final char ch) {
        return ('0' <= ch && ch <= '9') || ('A' <= ch && ch <= 'F');
    }
    static int fromHex(final char ch, final CharSequence value) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return 10 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal hex character '" + ch + "' in value string: " + value);
    }

    static char toDigit(final long value) {
        final long code = value % 10;
        return (char)(code + '0');
    }

    static int fromLetter(final char ch, final long seq) {
        if ('A' <= ch && ch <= 'Z') {
            return ch - 'A';
        }
        throw new IllegalArgumentException("Illegal letter character '" + ch + "' in value string: " +
                seqToString(seq));
    }

    static int fromLetter(final char ch, final long seq1, final long seq2) {
        if ('A' <= ch && ch <= 'Z') {
            return ch - 'A';
        }
        throw new IllegalArgumentException("Illegal letter character '" + ch + "' in value string: " +
                biSeqToString(seq1, seq2));
    }

    static int fromLetter(final char ch, final CharSequence seq) {
        if ('A' <= ch && ch <= 'Z') {
            return ch - 'A';
        }
        throw new IllegalArgumentException("Illegal letter character '" + ch + "' in value string: " + seq);
    }

    static int fromAlphanumeric(final char ch, final long seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'Z') {
            return 10 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " +
                seqToString(seq));
    }

    static int fromAlphanumeric(final char ch, final long seq1, final long seq2) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'Z') {
            return 10 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " +
                biSeqToString(seq1, seq2));
    }

    static int fromAlphanumeric(final char ch, final CharSequence seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'Z') {
            return 10 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " + seq);
    }

    static int fromDigit(final char ch, final long seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException("Illegal digit character '" + ch + "' in value string: " +
                seqToString(seq));
    }

    static int fromDigit(final char ch, final long seq1, final long seq2) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException("Illegal digit character '" + ch + "' in value string: " +
                biSeqToString(seq1, seq2));
    }
    static int fromDigit(final char ch, final CharSequence seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException("Illegal digit character '" + ch + "' in value string: " + seq);
    }

    static int charToSeq(final int seq, final int index, final char ch) {
        return seq | ((ch & 0xff) << (index << 3));
    }

    static long charToSeq(final long seq, final int index, final char ch) {
        return seq | ((ch & 0xffL) << (index << 3));
    }

    static long charToBiSeq1(final long seq1, final int index, final char ch) {
        return index < 8 ? charToSeq(seq1, index, ch) : seq1;
    }

    static long charToBiSeq2(final long seq2, final int index, final char ch) {
        return index < 8 ? seq2 : charToSeq(seq2, index - Long.BYTES, ch);
    }

    static char charFromSeq(final int seq, final int index) {
        return (char)((seq >>> (index << 3)) & 0xff);
    }

    static char charFromSeq(final long seq, final int index) {
        return (char)((seq >>> (index << 3)) & 0xffL);
    }

    static char charFromBiSeq(final long seq1, final long seq2, final int index) {
        return index < Long.BYTES ? charFromSeq(seq1, index) : charFromSeq(seq2, index - Long.BYTES);
    }

    static int seqLength(final int seq) {
        int tmp, val = seq;
        int n = Integer.BYTES;
        if ((tmp = val >>> 16) == 0) n -= 2; else val = tmp;
        if ((tmp = val >>> 8) == 0) n -= 1; else val = tmp;
        return val == 0 ? 0 : n;
    }

    static int seqLength(final long seq) {
        long tmp, val = seq;
        int n = Long.BYTES;
        if ((tmp = val >>> 32) == 0) n -= 4; else val = tmp;
        if ((tmp = val >>> 16) == 0) n -= 2; else val = tmp;
        if ((tmp = val >>> 8) == 0) n -= 1; else val = tmp;
        return val == 0 ? 0 : n;
    }

    static int biSeqLength(final long seq1, final long seq2) {
        return seq2 == 0 ? seqLength(seq1) : Long.BYTES + seqLength(seq2);
    }

    static int intSeq(final CharSequence seq) {
        final int len = seq.length();
        if (len > Integer.BYTES) {
            throw new IllegalArgumentException("String exceeds max length: " + seq);
        }
        int intSeq = 0;
        for (int i = 0; i < len; i++) {
            intSeq = charToSeq(intSeq, i, seq.charAt(i));
        }
        return intSeq;
    }

    static long longSeq(final CharSequence seq) {
        return longSeq(seq, 0, seq.length());
    }

    static long longSeq(final CharSequence seq, final int offset, final int len) {
        if (len > Long.BYTES) {
            throw new IllegalArgumentException("Length exceeds max sequence length: " + len);
        }
        long longSeq = 0;
        for (int i = 0; i < len; i++) {
            longSeq = charToSeq(longSeq, i, seq.charAt(i + offset));
        }
        return longSeq;
    }

    static long longSeq1(final CharSequence seq) {
        return longSeq(seq, 0, Math.min(Long.BYTES, seq.length()));
    }

    static long longSeq2(final CharSequence seq) {
        final int len = seq.length();
        return len > Long.BYTES ? longSeq(seq, Long.BYTES, len - Long.BYTES) : 0L;
    }

    static long lshSeq(final long seq, final int shift) {
        return seq << (shift << 3);
    }

    static long rshSeq(final long seq, final int shift) {
        return seq >>> (shift << 3);
    }

    static long subSeq(final long seq, final int start, final int end) {
        final long masked = seq & (0xffffffffffffffffL >>> (Long.SIZE - (end << 3)));
        return rshSeq(masked, start);
    }

    static long subBiSeq(final long seq1, final long seq2, final int start, final int end) {
        final long masked1 = start < Long.BYTES ? subSeq(seq1, 0, Math.min(end, Long.BYTES)) : 0L;
        final long masked2 = end > Long.BYTES ? subSeq(seq2, 0, end - Long.BYTES) : 0L;
        return rshBiSeq1(masked1, masked2, start);
    }

    static long lshBiSeq1(final long seq1, final int shift) {
        return shift < Long.BYTES ? lshSeq(seq1, shift) : 0L;
    }

    static long lshBiSeq2(final long seq1, final long seq2, final int shift) {
        if (shift < Long.BYTES) {
            final int bitShift = (shift << 3);
            return (seq2 << bitShift) | (seq1 >>> (Long.SIZE - bitShift));
        } else {
            final int bitShift = ((shift - Long.BYTES) << 3);
            return seq1 << bitShift;
        }
    }

    static long rshBiSeq1(final long seq1, final long seq2, final int shift) {
        if (shift < Long.BYTES) {
            final int bitShift = (shift << 3);
            return (seq1 >>> bitShift) | (seq2 << (Long.SIZE - bitShift));
        } else {
            final int bitShift = ((shift - Long.BYTES) << 3);
            return seq2 >>> bitShift;
        }
    }

    static long rshBiSeq2(final long seq2, final int shift) {
        return shift < Long.BYTES ? rshSeq(seq2, shift) : 0L;
    }

    static StringBuilder appendSeq(final long seq, final StringBuilder dst) {
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq, i);
            if (ch == '\0') {
                return dst;
            }
            dst.append(ch);
        }
        return dst;
    }
    static int appendSeq(final long seq, final Appendable appendable) {
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq, i);
            if (ch == '\0') {
                return i;
            }
            append(appendable, ch);
        }
        return Long.BYTES;
    }
    static int appendBiSeq(final long seq1, final long seq2, final StringBuilder dst) {
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq1, i);
            if (ch == '\0') {
                return i;
            }
            dst.append(ch);
        }
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq2, i);
            if (ch == '\0') {
                return i + Long.BYTES;
            }
            dst.append(ch);
        }
        return Long.BYTES + Long.BYTES;
    }
    static int appendBiSeq(final long seq1, final long seq2, final Appendable appendable) {
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq1, i);
            if (ch == '\0') {
                return i;
            }
            append(appendable, ch);
        }
        for (int i = 0; i < Long.BYTES; i++) {
            final char ch = charFromSeq(seq2, i);
            if (ch == '\0') {
                return i + Long.BYTES;
            }
            append(appendable, ch);
        }
        return Long.BYTES + Long.BYTES;
    }

    static CharSequence seqToString(final long seq) {
        return appendSeq(seq, new StringBuilder(Long.BYTES + 1));
    }

    static CharSequence biSeqToString(final long seq1, final long seq2) {
        final StringBuilder builder = new StringBuilder(Long.BYTES + Long.BYTES + 1);
        appendBiSeq(seq1, seq2, builder);
        return builder;
    }

    private static void append(final Appendable appendable, final char ch) {
        try {
            appendable.append(ch);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    interface BiAppender<T> {
        long append(long seq1, long seq2, T target);
    }
}
