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
 * Character type enum, used by {@link SeqType}/
 */
enum CharType {
    /** Characters: 'A'-'Z' */
    ALPHA,
    /** Character: '0' */
    ZERO_DIGIT,
    /** Characters: '1'-'9' */
    NON_ZERO_DIGIT,
    /** Character: '.' */
    ALPHANUMERIC_SIGN,
    /** Character: '-' */
    NUMERIC_SIGN,
    /** Any character not covered by the other constants */
    OTHER;

    static CharType forChar(final char ch) {
        if ('A' <= ch && ch <= 'Z') {
            return ALPHA;
        }
        if ('0' <= ch && ch <= '9') {
            return ch == '0' ? ZERO_DIGIT : NON_ZERO_DIGIT;
        }
        if (ch == '.') {
            return ALPHANUMERIC_SIGN;
        }
        if (ch == '-') {
            return NUMERIC_SIGN;
        }
        return OTHER;
    }
}
