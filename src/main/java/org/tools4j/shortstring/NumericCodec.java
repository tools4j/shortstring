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
import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.toDigit;
import static org.tools4j.shortstring.StringLengths.stringLength;

/**
 * Numeric only codec simply using the equivalent of {@link Short#toString(short)}  {@link Integer#toString(int)} and
 * {@link Long#toString(long)} as string representation for short, int and long, respectively.
 *
 * @see Short#toString(short)
 * @see Integer#toString(int)
 * @see Long#toString(long)
 */
public class NumericCodec implements ShortStringCodec {
    /** Default codec instance */
    public static final NumericCodec INSTANCE = new NumericCodec();
    public static final String MAX_SHORT_STRING = Integer.toString(Short.MAX_VALUE);
    public static final String MIN_SHORT_STRING = Integer.toString(Short.MIN_VALUE);
    public static final String MAX_INT_STRING = Integer.toString(Integer.MAX_VALUE);
    public static final String MIN_INT_STRING = Integer.toString(Integer.MIN_VALUE);
    public static final String MAX_LONG_STRING = Long.toString(Long.MAX_VALUE);
    public static final String MIN_LONG_STRING = Long.toString(Long.MIN_VALUE);
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
     * Same parsing logic as {@link Integer#parseInt(String, int)} with base 10.
     *
     * @param value the value to parse
     * @return the integer representation of value
     * @throws IllegalArgumentException if value is not numeric or too large to fit in an integer
     */
    public static int convertToInt(final CharSequence value) {
        //see Integer.toString(int, int) with radix=10
        int result = 0;
        boolean negative = false;
        int i = 0, len = value.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            final char firstChar = value.charAt(0);
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
            multmin = limit / 10;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                final char ch = value.charAt(i++);
                digit = Character.digit(ch, 10);
                if (digit < 0) {
                    throw new IllegalArgumentException("Illegal character '" + ch + "' in value: " + value);
                }
                if (result < multmin) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result *= 10;
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
     * Same parsing logic as {@link Long#parseLong(String, int)} base 10.
     *
     * @param value the value to parse
     * @return the long representation of value
     * @throws IllegalArgumentException if value is not numeric or too large to fit in a long
     */
    public static long convertToLong(final CharSequence value) {
        //see Integer.toString(int, int) with radix=10
        long result = 0;
        boolean negative = false;
        int i = 0, len = value.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            final char firstChar = value.charAt(0);
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
            multmin = limit / 10;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                final char ch = value.charAt(i++);
                digit = Character.digit(ch, 10);
                if (digit < 0) {
                    throw new IllegalArgumentException("Illegal character '" + ch + "' in value: " + value);
                }
                if (result < multmin) {
                    throw new IllegalArgumentException("Invalid value string (overflow): " + value);
                }
                result *= 10;
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
        return dst.append(value);
    }

    public static int shortToString(final short value, final Appendable dst) {
        return longToString(value, dst);
    }

    public static StringBuilder intToString(final int value, final StringBuilder dst) {
        return dst.append(value);
    }

    public static int intToString(final int value, final Appendable dst) {
        return longToString(value, dst);
    }

    public static StringBuilder longToString(final long value, final StringBuilder dst) {
        return dst.append(value);
    }

    public static int longToString(final long value, final Appendable dst) {
        try {
            if (value == Long.MIN_VALUE) {
                dst.append("-9223372036854775808");
                return 20;
            }
            long val;
            if (value >= 0) {
                val = value;
            } else {
                dst.append('-');
                val = -value;
            }
            int len = stringLength(val);
            if (len > 16) {
                final long msb = val / 1_0000_0000_0000_0000L;
                append(msb, len - 16, dst);
                val -= msb * 1_0000_0000_0000_0000L;
                len = 16;
            }
            if (len > 8) {
                final long msb = val / 1_0000_0000L;
                append(msb, len - 8, dst);
                val -= msb * 1_0000_0000L;
                len = 8;
            }
            append(val, len, dst);
            return len + (value < 0 ? 1 : 0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void append(final long value, final int len, final Appendable dst) {
        assert value >= 0 && value < 1_0000_0000L;
        long val = value;
        long seq = 0;
        for (int i = len - 1; i >= 0; i--) {
            final char ch = toDigit(val);
            seq = charToSeq(seq, i, ch);
            val /= 10;
        }
        appendSeq(seq, dst);
    }

    @Override
    public boolean isConvertibleToShort(final CharSequence value) {
        return isValidShortString(value);
    }

    @Override
    public boolean isConvertibleToInt(final CharSequence seq) {
        return isValidIntString(seq);
    }

    @Override
    public boolean isConvertibleToLong(final CharSequence value) {
        return isValidLongString(value);
    }

    public static boolean isValidShortString(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_SHORT_STRING_LENGTH + 1) {
            return false;
        }
        final SeqType seq = SeqType.sequenceFor(value);
        switch (seq) {
            case NUMERIC_UNSIGNED:
                return len < MAX_SHORT_STRING_LENGTH ||
                        (len == MAX_SHORT_STRING_LENGTH && leq(value, MAX_SHORT_STRING));
            case NUMERIC_SIGNED:
                return len <= MAX_SHORT_STRING_LENGTH ||
                        (len == MAX_SHORT_STRING_LENGTH + 1 && leq(value, MIN_SHORT_STRING));
            default:
                return false;
        }
    }

    public static boolean isValidIntString(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_INT_STRING_LENGTH + 1) {
            return false;
        }
        final SeqType seq = SeqType.sequenceFor(value);
        switch (seq) {
            case NUMERIC_UNSIGNED:
                return len < MAX_INT_STRING_LENGTH ||
                        (len == MAX_INT_STRING_LENGTH && leq(value, MAX_INT_STRING));
            case NUMERIC_SIGNED:
                return len <= MAX_INT_STRING_LENGTH ||
                        (len == MAX_INT_STRING_LENGTH + 1 && leq(value, MIN_INT_STRING));
            default:
                return false;
        }
    }

    public static boolean isValidLongString(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_LONG_STRING_LENGTH + 1) {
            return false;
        }
        final SeqType encoding = SeqType.sequenceFor(value);
        switch (encoding) {
            case NUMERIC_UNSIGNED:
                return len < MAX_LONG_STRING_LENGTH ||
                        (len == MAX_LONG_STRING_LENGTH && leq(value, MAX_LONG_STRING));
            case NUMERIC_SIGNED:
                return len <= MAX_LONG_STRING_LENGTH ||
                        (len == MAX_LONG_STRING_LENGTH + 1 && leq(value, MIN_LONG_STRING));
            default:
                return false;
        }
    }

    @Override
    public boolean startsWithSignChar(final CharSequence value) {
        return hasSignCharPrefix(value);
    }

    public static boolean hasSignCharPrefix(final CharSequence value) {
        return value.length() > 0 && value.charAt(0) == '-';
    }

    @Override
    public String toString() {
        return NumericCodec.class.getSimpleName();
    }
}
