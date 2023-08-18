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
 * Codec translating short alphanumeric strings to integers and back. For conversion to int (long), strings up to length
 * 6 (13) are supported, with an additional leading sign character for negative values.
 * <p><br>
 * All integers (longs) are valid integer representations.  The string representation of a fully-numeric value is simply
 * the integer's or long's to-string representation.
 * <p><br>
 * For conversion to and from int:
 * <pre>
 *    (V) valid representations are
 *        - fully-numeric values from 0 to 999999 (w/o 0 prefix -- zero prefixed values are considered alphanumeric)
 *        - alphanumeric strings of length 1-5
 *        - alphanumeric strings of length 6 starting with a letter
 *        - alphanumeric strings of length 6 starting with a digit from 0-6
 *        - alphanumeric strings of length 6 starting with a digit and less or equal to "7XIZYJ"
 *        - all of the above, except zero, with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for all other alphanumeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 7 characters
 *        - strings longer than 6 characters and no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - fully-numeric values with alphanumeric '.' sign prefix
 *        - alphanumeric strings with numeric '-' sign prefix
 *        - zero-prefixed strings with numeric '-' sign prefix
 *    --&gt; for more details see {@link AlphaNumericIntCodec}
 * </pre>
 *
 * <p><br>
 * For conversion to and from long:
 * <pre>
 *    (V) valid character sequences are
 *        - a single zero character
 *        - strings with 1-13 digits with no leading zeros
 *        - all alphanumeric strings of length 1-12 with no leading zeros
 *        - all alphanumeric strings of length 13 up to '9HYLDS'
 *        - all of the above, except zero, with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for all other alpha-numeric values
 *    (I) invalid character sequences are for instance
 *        - empty strings
 *        - strings longer than 7 characters
 *        - strings longer than 6 characters and no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - zero-prefixed strings, except for zero itself
 *        - digit only strings with a alphanumeric '.' sign prefix
 *        - alphanumeric strings with at least one letter and a numeric '-' sign prefix
 *    --&gt; for more details see {@link AlphaNumericIntCodec}
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
        return Chars.startsWithSignChar(value);
    }

    @Override
    public String toString() {
        return AlphaNumericCodec.class.getSimpleName();
    }
}
