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

enum Chars {
    ;
    static void setChar(final char ch, final StringBuilder dst, final int dstIndex) {
        if (dstIndex < dst.length()) {
            dst.setCharAt(dstIndex, ch);
        } else {
            while (dstIndex > dst.length()) {
                dst.append('0');
            }
            dst.append(ch);
        }
    }

    /**
     * Note: a shorter string is always considered before a longer string.
     * @param a sequence a
     * @param b sequence b
     * @return true if a {@code a <= b}
     */
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

    static boolean isAlphanumeric(final CharSequence seq, final int start, final int end) {
        for (int i = start; i < end; i++) {
            if (!isAlphanumeric(seq.charAt(i))) {
                return false;
            }
        }
        return true;
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

    static boolean startsWithSignChar(final CharSequence seq) {
        return seq.length() > 0 && isSignChar(seq.charAt(0));
    }
}
