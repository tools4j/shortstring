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

/**
 * Provides static utility methods to determine the length of the string representation for integers and longs.
 */
enum StringLengths {
    ;
    private final static int[] MAX_INTS = {
            9, 99, 999, 9_999, 99_999, 999_999, 9_999_999, 99_999_999, 999_999_999,
            Integer.MAX_VALUE };
    private final static long[] MAX_LONGS = {
            9, 99, 999, 9_999, 99_999, 999_999, 9_999_999, 99_999_999, 999_999_999,
            9_999_999_999L, 99_999_999_999L, 999_999_999_999L,
            9_999_999_999_999L, 99_999_999_999_999L, 999_999_999_999_999L,
            9_999_999_999_999_999L, 99_999_999_999_999_999L, 999_999_999_999_999_999L,
            Long.MAX_VALUE};

    /**
     * Returns the length of the string representation of the provided non-negative value x.
     * If x is negative 0 is returned.
     *
     * @param x the integer input
     * @return the string length of x, or 0 if x is negative
     */
    static int stringLength(final int x) {
        assert x >= 0;
        for (int i = 0; ; i++) {
            if (x <= MAX_INTS[i]) {
                return i + 1;
            }
        }
    }
    /**
     * Returns the length of the string representation of the provided non-negative value x.
     * If x is negative 0 is returned.
     *
     * @param x the long input
     * @return the string length of x, or 0 if x is negative
     */
    static int stringLength(final long x) {
        assert x >= 0;
        for (int i = 0; ; i++) {
            if (x <= MAX_LONGS[i]) {
                return i + 1;
            }
        }
    }
}
