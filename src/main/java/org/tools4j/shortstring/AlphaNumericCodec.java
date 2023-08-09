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
 * Alphanumeric codec translating between string and integral representation of a command value.  This codec supports
 * alphanumeric strings of length 6 (or signed 7).
 * <p><br>
 * All integers are valid integer representations.  The string representation of a fully-numeric value is simply the
 * integer's to-string representation.
 * <p><br>
 * Examples of valid and invalid string representations are:
 * <pre>
 *    (V) valid representations are
 *        - a single zero character
 *        - strings with 1-6 digits with no leading zeros
 *        - all alphanumeric strings of length 1-5 with no leading zeros
 *        - all alphanumeric strings of length 1-6 with first char a letter
 *        - all alphanumeric strings of length 1-5 with first char a digit, or 6 chars and value at most '9HYLDS'
 *        - all of the above, except zero, with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for all other alpha-numeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 7 characters
 *        - strings longer than 6 characters and no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - zero-prefixed strings, except for zero itself
 *        - digit only strings with a alphanumeric '.' sign prefix
 *        - alphanumeric strings with at least one letter and a numeric '-' sign prefix
 * </pre>
 * A valid string representation follows one of the following definitions:
 * <pre>
 *     (A+) - 1-6 alphanumeric characters, all letters uppercase and no leading zeros
 *          - char  1 : 'A'-'Z', '1'-'9'
 *          - chars 2+: 'A'-'Z', '0'-'9'
 *          - if first char is '9', then length 1-5 or if length 6, then chars[1-6] &lt;= '9HYLDS'
 *     (A-) - 2-7 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase and no leading zeros
 *          - char 1 : '.'
 *          - char 2 : 'A'-'Z", '1'-'9'
 *          - char 3+: 'A'-'Z', '0'-'9'
 *          - at least one char: 'A'-'Z'
 *          - if second char is '9', then length 2-6 or if length 7, then chars[2-7] &lt;= '9HYLDT'
 *     (Z)  - single zero digit character
 *          - char 1: '0'
 *     (N+) - 1-6 digit characters without leading zeros
 *          - char 1 : '1'-'9'
 *          - char 2+: '0'-'9'
 *     (N-) - 2-7 sign-prefixed digit characters: '-' sign followed by 1-6 digits but no leading zeros
 *          - char 1 : '-'
 *          - char 2 : '1'-'9'
 *          - char 3+: '0'-'9'
 * </pre>
 */
public class AlphaNumericCodec implements ShortStringCodec {
    public static final AlphaNumericCodec INSTANCE = new AlphaNumericCodec();

    @Override
    public int maxIntLength() {
        return AlphaNumericIntCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int maxLongLength() {
        return AlphaNumericLongCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int toInt(final CharSequence value) {
        return AlphaNumericIntCodec.toInt(value);
    }

    @Override
    public long toLong(final CharSequence value) {
        return AlphaNumericLongCodec.toLong(value);
    }

    @Override
    public StringBuilder toString(final int value, final StringBuilder dst) {
        return AlphaNumericIntCodec.toString(value, dst);
    }

    @Override
    public StringBuilder toString(final long value, final StringBuilder dst) {
        return AlphaNumericLongCodec.toString(value, dst);
    }

    @Override
    public boolean isConvertibleToInt(final CharSequence value) {
        return AlphaNumericIntCodec.isConvertibleToInt(value);
    }

    @Override
    public boolean isConvertibleToLong(final CharSequence value) {
        return AlphaNumericLongCodec.isConvertibleToLong(value);
    }

    @Override
    public boolean startsWithSignChar(final CharSequence value) {
        return CharType.startsWithSignChar(value);
    }

    @Override
    public String toString() {
        return AlphaNumericCodec.class.getSimpleName();
    }
}
