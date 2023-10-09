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

import static org.tools4j.shortstring.Chars.appendSeq;
import static org.tools4j.shortstring.Chars.charToSeq;
import static org.tools4j.shortstring.Chars.fromHex;
import static org.tools4j.shortstring.Chars.isHex;
import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.toHex;

/**
 * Hex codec that converts shorts, ints, longs to and from their base 16 representation.
 *
 * @see Integer#toHexString(int)
 * @see Long#toHexString(long)
 */
public class HexCodec implements ShortStringCodec {
    /** Default codec instance */
    public static final HexCodec INSTANCE = new HexCodec();
    public static final String MAX_SHORT_STRING = "7FFF";
    public static final String MIN_SHORT_STRING = "-8000";
    public static final String MAX_INT_STRING = "7FFFFFFF";
    public static final String MIN_INT_STRING = "-80000000";
    public static final String MAX_LONG_STRING = "7FFFFFFFFFFFFFFF";
    public static final String MIN_LONG_STRING = "-8000000000000000";
    public static final int MAX_SHORT_STRING_LENGTH = MAX_SHORT_STRING.length();
    public static final int MAX_INT_STRING_LENGTH = MAX_INT_STRING.length();
    public static final int MAX_LONG_STRING_LENGTH = MAX_LONG_STRING.length();

    @Override
    public int maxShortLength() {
        return MAX_SHORT_STRING_LENGTH;
    }

    @Override
    public int maxIntLength() {
        return MAX_INT_STRING_LENGTH;
    }

    @Override
    public int maxLongLength() {
        return MAX_LONG_STRING_LENGTH;
    }

    @Override
    public short toShort(final CharSequence value) {
        final int ival = toInt(value);
        if (ival >= Short.MIN_VALUE && ival <= Short.MAX_VALUE) {
            return (short)ival;
        }
        throw new IllegalArgumentException("Invalid value string (overflow): " + value);
    }

    @Override
    public int toInt(final CharSequence value) {
        return convertToInt(value);
    }

    @Override
    public long toLong(final CharSequence value) {
        return convertToLong(value);
    }

    /**
     * Same conversion logic as {@link Integer#parseInt(String, int)} with base 16.
     *
     * @param value the value to parse
     * @return the integer representation of value
     * @throws IllegalArgumentException if value is not numeric or too large to fit in an integer
     */
    public static int convertToInt(final CharSequence value) {
        //see Integer.parseInt(int, int) with radix=16
        int result = 0;
        boolean negative = false;
        int i = 0, len = value.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = value.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw new IllegalArgumentException("Illegal first character '" + firstChar + "' in value: " + value);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw new IllegalArgumentException("Invalid value string: " + value);
                i++;
            }
            multmin = limit / 16;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = fromHex(value.charAt(i++), value);
                if (result < multmin) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result *= 16;
                if (result < limit + digit) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result -= digit;
            }
        } else {
            throw new IllegalArgumentException("Empty value string");
        }
        return negative ? result : -result;
    }

    /**
     * Same parsing logic as {@link Long#parseLong(String, int)} base 16.
     *
     * @param value the value to parse
     * @return the long representation of value
     * @throws IllegalArgumentException if value is not numeric or too large to fit in a long
     */
    public static long convertToLong(final CharSequence value) {
        //see Long.parseLong(int, int) with radix=16
        long result = 0;
        boolean negative = false;
        int i = 0, len = value.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = value.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw new IllegalArgumentException("Illegal first character '" + firstChar + "' in value: " + value);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw new IllegalArgumentException("Invalid value string: " + value);
                i++;
            }
            multmin = limit / 16;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = fromHex(value.charAt(i++), value);
                if (result < multmin) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result *= 16;
                if (result < limit + digit) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result -= digit;
            }
        } else {
            throw new IllegalArgumentException("Empty value string");
        }
        return negative ? result : -result;
    }

    @Override
    public StringBuilder toString(final short value, final StringBuilder dst) {
        return shortToString(value, dst);
    }

    @Override
    public StringBuilder toString(final int value, final StringBuilder dst) {
        return intToString(value, dst);
    }

    @Override
    public StringBuilder toString(final long value, final StringBuilder dst) {
        return longToString(value, dst);
    }

    @Override
    public int toString(final short value, final Appendable appendable) {
        return shortToString(value, appendable);
    }

    @Override
    public int toString(final int value, final Appendable appendable) {
        return intToString(value, appendable);
    }

    @Override
    public int toString(final long value, final Appendable appendable) {
        return longToString(value, appendable);
    }

    public static StringBuilder shortToString(final short value, final StringBuilder dst) {
        final int mag = Short.SIZE - Integer.numberOfLeadingZeros(Math.abs(value) << 16);
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, dst);
    }

    public static int shortToString(final short value, final Appendable appendable) {
        final int mag = Short.SIZE - Integer.numberOfLeadingZeros(Math.abs(value) << 16);
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, appendable);
    }

    public static StringBuilder intToString(final int value, final StringBuilder dst) {
        //see Integer.toHexString(int)
        final int mag = Integer.SIZE - Integer.numberOfLeadingZeros(Math.abs(value));
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, dst);
    }

    public static int intToString(final int value, final Appendable appendable) {
        //see Integer.toHexString(int)
        final int mag = Integer.SIZE - Integer.numberOfLeadingZeros(Math.abs(value));
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, appendable);
    }

    public static StringBuilder longToString(final long value, final StringBuilder dst) {
        //see Long.toHexString(int)
        final int mag = Long.SIZE - Long.numberOfLeadingZeros(Math.abs(value));
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, dst);
    }

    public static int longToString(final long value, final Appendable appendable) {
        //see Long.toHexString(int)
        final int mag = Long.SIZE - Long.numberOfLeadingZeros(Math.abs(value));
        final int len = Math.max(((mag + 3) / 4), 1);
        return toHexString(value, len, value < 0, appendable);
    }

    private static StringBuilder toHexString(final long value, final int len, final boolean neg, final StringBuilder dst) {
        if (neg) {
            dst.append('-');
        }
        final int offset = dst.length();
        dst.setLength(offset + len);
        int charPos = offset + len;
        long val = Math.abs(value);
        do {
            dst.setCharAt(--charPos, toHex(val));
            val >>>= 4;
        } while (val != 0 && charPos > offset);
        return dst;
    }

    private static int toHexString(final long value, final int length, final boolean neg, final Appendable dst) {
        try {
            if (neg) {
                dst.append('-');
            }
            long val = Math.abs(value);
            int len = length;
            if (len > 8) {
                final long msb = val >>> 32;
                append((int)msb, len - 8, dst);
                val &= 0xffffffffL;
                len = 8;
            }
            append((int)val, len, dst);
            return len + (neg ? 1 : 0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void append(final int value, final int len, final Appendable dst) {
        int val = value;
        long seq = 0;
        for (int i = len - 1; i >= 0; i--) {
            final char ch = toHex(val);
            seq = charToSeq(seq, i, ch);
            val >>>= 4;
        }
        appendSeq(seq, dst);
    }

    @Override
    public boolean isConvertibleToShort(final CharSequence value) {
        return isConvertible(value, MAX_SHORT_STRING, MIN_SHORT_STRING);
    }

    @Override
    public boolean isConvertibleToInt(final CharSequence value) {
        return isConvertible(value, MAX_INT_STRING, MIN_INT_STRING);
    }

    @Override
    public boolean isConvertibleToLong(final CharSequence value) {
        return isConvertible(value, MAX_LONG_STRING, MIN_LONG_STRING);
    }

    private static boolean isConvertible(final CharSequence seq, final String maxValue, final String minValue) {
        final int len = seq.length();
        if (len < 1) {
            return false;
        }
        final int signOff = seq.charAt(0) == '-' ? 1 : 0;
        final String max = signOff == 0 ? maxValue : minValue;
        if (len > max.length() || len <= signOff) {
            return false;
        }
        for (int i = signOff; i < len; i++) {
            if (!isHex(seq.charAt(i))) {
                return false;
            }
        }
        if (len < max.length()) {
            return true;
        }
        return leq(seq, max);
    }

    @Override
    public boolean startsWithSignChar(final CharSequence value) {
        return startsWithSignCharacter(value);
    }

    public static boolean startsWithSignCharacter(final CharSequence value) {
        return value.length() > 0 && value.charAt(0) == '-';
    }

    @Override
    public String toString() {
        return HexCodec.class.getSimpleName();
    }
}
