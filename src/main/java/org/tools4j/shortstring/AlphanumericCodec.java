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
 * Examples of valid and invalid string representations are:
 * <pre>
 *    (V) valid representations are
 *        - fully-numeric values from 0 to 999,999 (int) and 0 to 9,999,999,999,999 (long), without 0 prefix
 *        - alphanumeric strings of length 1-5 (int) and 1-12 (long)
 *        - values with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for alphanumeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 7 characters (int) and 14 characters (long)
 *        - strings longer than 6 characters (int) and 13 characters (long) if they have no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - fully-numeric values with alphanumeric '.' sign prefix
 *        - alphanumeric strings with numeric '-' sign prefix
 *        - zero-prefixed strings with numeric '-' sign prefix
 *
 *    --&gt; for more details see {@link AlphanumericIntCodec} and {@link AlphanumericLongCodec}
 * </pre>
 *
 * @see AlphanumericIntCodec
 * @see AlphanumericLongCodec
 */
public class AlphanumericCodec implements ShortStringCodec {
    @Override
    public int maxIntLength() {
        return AlphanumericIntCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int maxLongLength() {
        return AlphanumericLongCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int toInt(final CharSequence value) {
        return AlphanumericIntCodec.toInt(value);
    }

    @Override
    public long toLong(final CharSequence value) {
        return AlphanumericLongCodec.toLong(value);
    }

    @Override
    public StringBuilder toString(final int value, final StringBuilder dst) {
        return AlphanumericIntCodec.toString(value, dst);
    }

    @Override
    public StringBuilder toString(final long value, final StringBuilder dst) {
        return AlphanumericLongCodec.toString(value, dst);
    }

    @Override
    public boolean isConvertibleToInt(final CharSequence value) {
        return AlphanumericIntCodec.isConvertibleToInt(value);
    }

    @Override
    public boolean isConvertibleToLong(final CharSequence value) {
        return AlphanumericLongCodec.isConvertibleToLong(value);
    }

    @Override
    public boolean startsWithSignChar(final CharSequence value) {
        return Chars.startsWithSignChar(value);
    }

    @Override
    public String toString() {
        return AlphanumericCodec.class.getSimpleName();
    }
}
