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

import static org.tools4j.shortstring.Chars.biSeqLength;
import static org.tools4j.shortstring.Chars.biSeqToString;
import static org.tools4j.shortstring.Chars.lshSeq;
import static org.tools4j.shortstring.Chars.rshSeq;
import static org.tools4j.shortstring.Chars.seqLength;
import static org.tools4j.shortstring.Chars.seqToString;
import static org.tools4j.shortstring.Chars.subBiSeq;
import static org.tools4j.shortstring.Chars.subSeq;

/**
 * Codec translating short alphanumeric strings to integers and back. For conversion to (short, int, long), strings up
 * to lengths (3, 6, 13) are supported, with an additional leading sign character for negative values.
 * <p><br>
 * All integer numbers (shot, int or long) are valid.  The string representation of a fully-numeric value is simply the
 * number's to-string representation.
 * <p><br>
 * Examples of valid and invalid string representations are:
 * <pre>
 *    (V) valid representations are for instance
 *        - fully-numeric values without leading zeroes in the following ranges
 *             (short) -- 0 to 999
 *              (int)  -- 0 to 999,999
 *             (long)  -- 0 to 9,999,999,999,999
 *        - alphanumeric strings of the following lengths
 *             (short) -- 1-3  : without digit in first 2 positions
 *              (int)  -- 1-6  : letter prefixed
 *             (long)  -- 1-12 : all alphanumeric, and 13 if letter-only or single-digit postfix
 *        - values with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for alphanumeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 4 characters (short), 7 characters (int) and 14 characters (long)
 *        - unsigned strings longer than 3 characters (short), 6 characters (int) and 13 characters (long)
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - fully-numeric values with alphanumeric '.' sign prefix
 *        - alphanumeric strings with numeric '-' sign prefix
 *        - zero-prefixed strings with numeric '-' sign prefix
 *
 *    --&gt; for more details see
 *        - {@link AlphanumericShortCodec}
 *        - {@link AlphanumericIntCodec}
 *        - {@link AlphanumericLongCodec}
 * </pre>
 *
 * @see AlphanumericShortCodec
 * @see AlphanumericIntCodec
 * @see AlphanumericLongCodec
 */
public class AlphanumericCodec implements ShortStringCodec {
    public static final AlphanumericCodec INSTANCE = new AlphanumericCodec();

    @Override
    public int maxShortLength() {
        return AlphanumericShortCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int maxIntLength() {
        return AlphanumericIntCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public int maxLongLength() {
        return AlphanumericLongCodec.MAX_LENGTH_UNSIGNED;
    }

    @Override
    public short toShort(final CharSequence value) {
        return AlphanumericShortCodec.toShort(value);
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
    public StringBuilder toString(final short value, final StringBuilder dst) {
        return AlphanumericShortCodec.toString(value, dst);
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
    public int toString(final short value, final Appendable appendable) {
        return AlphanumericShortCodec.toString(value, appendable);
    }

    @Override
    public int toString(final int value, final Appendable appendable) {
        return AlphanumericIntCodec.toString(value, appendable);
    }

    @Override
    public int toString(final long value, final Appendable appendable) {
        return AlphanumericLongCodec.toString(value, appendable);
    }

    public short intToShort(final int value) {
        final long seq = AlphanumericIntCodec.toSeq(value);
        final int len = seqLength(seq);
        return AlphanumericShortCodec.toShort((int) seq, len);
    }

    public short longToShort(final long value) {
        final long seq1 = AlphanumericLongCodec.toSeq1(value);
        final long seq2 = AlphanumericLongCodec.toSeq2(value);
        final int len = biSeqLength(seq1, seq2);
        return AlphanumericShortCodec.toShort((int)seq1, len);
    }

    public int longToInt(final long value) {
        final long seq1 = AlphanumericLongCodec.toSeq1(value);
        final long seq2 = AlphanumericLongCodec.toSeq2(value);
        final int len = biSeqLength(seq1, seq2);
        return AlphanumericIntCodec.toInt(seq1, len);
    }

    public int concatShortsToInt(final short valueA, final short valueB) {
        final int seqA = AlphanumericShortCodec.toSeq(valueA);
        final int seqB = AlphanumericShortCodec.toSeq(valueB);
        final int lenA = seqLength(seqA);
        final int lenB = seqLength(seqB);
        final long seqAB = lshSeq(0xffffffffL & seqB, lenA) | (0xffffffffL & seqA);
        return AlphanumericIntCodec.toInt(seqAB, lenA + lenB);
    }

    public long concatIntsToLong(final int valueA, final int valueB) {
        final long seqA = AlphanumericIntCodec.toSeq(valueA);
        final long seqB = AlphanumericIntCodec.toSeq(valueB);
        final int lenA = seqLength(seqA);
        final int lenB = seqLength(seqB);
        final long rshB = rshSeq(seqB, Long.BYTES - lenA);
        final long lshB = lshSeq(seqB, lenA);
        return AlphanumericLongCodec.toLong(lshB | seqA, rshB, lenA + lenB);
    }

    public short substringOfIntToShort(final int value, final int start) {
        return substringOfIntToShort(value, start, Integer.MAX_VALUE);
    }

    public short substringOfIntToShort(final int value, final int start, final int end) {
        final long seq = AlphanumericIntCodec.toSeq(value);
        final int len = seqLength(seq);
        final int posStart = start >= 0 ? start : len + start;
        final int posEnd = end == Integer.MAX_VALUE ? len : end >= 0 ? end : len + end;
        if (posStart < 0 || posEnd > len || posStart >= posEnd) {
            throw new IllegalArgumentException("Invalid start/end indices: " +
                    seqToString(seq) + "[" + start + ":" + end + "]");
        }
        final int maxLen = maxShortLength() + (posStart == 0 && Chars.startsWithSignChar(seq) ? 1 : 0);
        if (posEnd - posStart > maxLen) {
            throw new IllegalArgumentException("Substring exceeds max short length: " +
                    seqToString(seq) + "[" + start + ":" + end + "] exceeds " + maxLen);
        }
        final long subSeq = subSeq(seq, posStart, posEnd);
        return AlphanumericShortCodec.toShort((int) subSeq, posEnd - posStart);
    }

    public int substringOfLongToInt(final long value, final int start) {
        return substringOfLongToInt(value, start, Integer.MAX_VALUE);
    }

    public int substringOfLongToInt(final long value, final int start, final int end) {
        if (start >= 0 && start < end) {
            if (end <= Long.BYTES) {
                final long seq1 = AlphanumericLongCodec.toSeq1(value);
                if (end <= seqLength(seq1)) {
                    final long subSeq = subSeq(seq1, start, end);
                    return AlphanumericIntCodec.toInt(subSeq, end - start);
                }
            }
            if (start >= Long.BYTES) {
                final long seq2 = AlphanumericLongCodec.toSeq2(value);
                final int len2 = seqLength(seq2);
                final int start2 = start - Long.BYTES;
                final int end2 = end == Integer.MAX_VALUE ? len2 : end - Long.BYTES;
                if (end2 <= len2) {
                    final long subSeq = subSeq(seq2, start2, end2);
                    return AlphanumericIntCodec.toInt(subSeq, end2 - start2);
                }
            }
        }
        final long seq1 = AlphanumericLongCodec.toSeq1(value);
        final long seq2 = AlphanumericLongCodec.toSeq2(value);
        final int len = biSeqLength(seq1, seq2);
        final int posStart = start >= 0 ? start : len + start;
        final int posEnd = end == Integer.MAX_VALUE ? len : end >= 0 ? end : len + end;
        if (posStart < 0 || posEnd > len || posStart >= posEnd) {
            throw new IllegalArgumentException("Invalid start/end indices: " +
                    biSeqToString(seq1, seq2) + "[" + start + ":" + end + "]");
        }
        final int maxLen = maxIntLength() + (posStart == 0 && Chars.startsWithSignChar(seq1) ? 1 : 0);
        if (posEnd - posStart > maxLen) {
            throw new IllegalArgumentException("Substring exceeds max int length: " +
                    biSeqToString(seq1, seq2) + "[" + start + ":" + end + "] exceeds " + maxLen);
        }
        final long subSeq = subBiSeq(seq1, seq2, posStart, posEnd);
        return AlphanumericIntCodec.toInt(subSeq, posEnd - posStart);
    }

    @Override
    public boolean isConvertibleToShort(final CharSequence value) {
        return AlphanumericShortCodec.isConvertibleToShort(value);
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
