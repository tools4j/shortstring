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

import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.setChar;
import static org.tools4j.shortstring.StringLengths.stringLength;

/**
 * Alphanumeric codec translating between string and long representation of a value.  This codec supports
 * alphanumeric strings of length 12 (or signed 13).
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
public enum AlphaNumericLongCodec {
    ;
    public static final int MAX_LENGTH_UNSIGNED = 13;
    public static final int MAX_LENGTH_SIGNED = 14;
    /**
     * Length of first block containing numeric values up to 13 digits:
     * <pre>
     * 0-9999999999999
     * </pre>
     */
    private static final long NUMERIC_BLOCK_LENGTH = 10_000_000_000_000L;
    public static final long MIN_NUMERIC = -(NUMERIC_BLOCK_LENGTH - 1);
    public static final long MAX_NUMERIC = NUMERIC_BLOCK_LENGTH - 1;
    /**
     * Length of second block containing alphanumeric values up to 12 digits:
     * <pre>
     * [1-9A-Z] + [1-9A-Z][0-9A-Z] + [A-Z][0-9A-Z][0-9A-Z] + ...
     *    35    +       35*36      +      35*36*36         + ... + 35*36^11 = 4,738,381,338,321,616,895
     * </pre>
     */
    private static final long ALPHANUMERIC_12_BLOCK_LENGTH = 35L*(1L + 36L*(1L + 36L*(1L + 36L*(1L + 36*(1L + 36L*(1L + 36L*(1L + 36*(1L + 36L*(1L + 36L*(1L + 36L*(1L + 36L)))))))))));
    /**
     * Length of third block containing alphanumeric values of length 13 allowing digits only for the last 2.
     * <pre>
     * [A-Z]^11 * [0-9A-Z]^2  =
     *   26^11  *    36^2     = 4,756,766,455,136,157,696
     * </pre>
     *
     * but we have only 4,484,980,698,533,158,912 left, so we can encode ~94.29% of all values of length 13.
     */
    private static final long ALPHANUMERIC_13_BLOCK_LENGTH = Long.MAX_VALUE - NUMERIC_BLOCK_LENGTH - ALPHANUMERIC_12_BLOCK_LENGTH;

    public static final String MAX_ALPHANUMERIC = "ZZZZZZZZZxxxx";
    public static final String MIN_ALPHANUMERIC = ".ZZZZZZZZZxxxx";

    public static long toLong(final CharSequence value) {
        final CharSeq seq = CharSeq.sequenceFor(value);
        final int len = value.length();
        final int off = seq.isSigned() ? 1 : 0;
        long code;
        if (seq.isNumeric()) {
            code = fromDigit(value.charAt(off), value);
            for (int i = off + 1; i < len; i++) {
                code *= 10;
                code += fromDigit(value.charAt(i), value);
            }
            return off == 0 ? code : -code;
        }
        if (seq.isAlphanumeric() && (len == 12 && off == 0 || len == 13 && off != 0)) {
            code = fromAlphanumeric0(value.charAt(off), value);
            for (int i = off + 1; i < len; i++) {
                code *= 36;
                code += 35 + fromAlphanumeric(value.charAt(i), value);
            }
            code += NUMERIC_BLOCK_LENGTH;
        } else if (seq.isAlphanumeric() && (len == 13 && off == 0 || len == 14 && off != 0)
                && CharSeq.sequenceFor(value, len - 2).isAlphaOnly()
        ) {
            code = fromAlpha(value.charAt(off), value);
            for (int i = off + 1; i < len - 2; i++) {
                code *= 26;
                code += fromAlpha(value.charAt(i), value);
            }
            code += fromAlphanumeric(value.charAt(off + len - 2), value);
            code *= 36;
            code += fromAlphanumeric(value.charAt(off + len - 1), value);
            code *= 36;
            code += NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_12_BLOCK_LENGTH;
        } else {
            throw new IllegalArgumentException(len == 0 ? "Empty value string" : "Invalid value string: " + value);
        }
        assert code >= 0 || code == Long.MIN_VALUE;
        return off == 0 ? code : -code;
    }

    public static StringBuilder toString(final long value, final StringBuilder dst) {
        final char sign;
        long val = Math.abs(value);
        int off = dst.length();
        int start = off;
        if (value > -NUMERIC_BLOCK_LENGTH && value < NUMERIC_BLOCK_LENGTH) {
            sign = '-';
            final int len = stringLength(val);
            for (int i = len - 1; i >= 0; i--) {
                final char ch = toDigit(val);
                setChar(ch, dst, off + i);
                val /= 10;
                if (val == 0) {
                    start = off + i;
                    break;
                }
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_12_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_12_BLOCK_LENGTH)) {
            sign = '.';
            val -= NUMERIC_BLOCK_LENGTH;
            for (int i = 11; i >= 0; i--) {
                if (val < 35) {
                    final char ch = toAlphanumeric0(val);
                    setChar(ch, dst, off + i);
                    start = off + i;
                    break;
                }
                val -= 35;
                final char ch = toAlphanumeric(val);
                setChar(ch, dst, off + i);
                val /= 36;
            }
        } else {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_12_BLOCK_LENGTH);
            for (int i = 12; i >= 11; i--) {
                final char ch = toAlphanumeric(val);
                setChar(ch, dst, off + i);
                val /= 36;
            }
            for (int i = 10; i >= 0; i--) {
                final char ch = toAlphanumeric(val);
                setChar(ch, dst, off + i);
                val /= 26;
            }
        }
        if (value < 0) {
            start--;
            if (start < off) {
                dst.insert(off, sign);
                return dst;
            }
            dst.setCharAt(start, sign);
        }
        return dst.delete(off, start);
    }

    public static boolean isConvertibleToLong(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_LENGTH_SIGNED) {
            return false;
        }
        final CharSeq seq = CharSeq.sequenceFor(value);
        switch (seq) {
            case NUMERIC_UNSIGNED:
            case ALPHA_ONLY_UNSIGNED:
            case DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED:
                return len <= MAX_LENGTH_UNSIGNED;
            case NUMERIC_SIGNED:
            case ALPHA_ONLY_SIGNED:
            case DIGIT_PREFIXED_ALPHANUMERIC_SIGNED:
                return true;
            case ALPHA_PREFIXED_ALPHANUMERIC_UNSIGNED:
                return len < MAX_LENGTH_UNSIGNED || (len == MAX_LENGTH_UNSIGNED &&
                                CharSeq.sequenceFor(value, len - 2).isAlphaOnly() &&
                                leq(value, MAX_ALPHANUMERIC));
            case ALPHA_PREFIXED_ALPHANUMERIC_SIGNED:
                return len < MAX_LENGTH_SIGNED || (/*len == MAX_LENGTH_SIGNED &&*/
                        CharSeq.sequenceFor(value, len - 2).isAlphaOnly() &&
                                leq(value, MIN_ALPHANUMERIC));
            default:
                return false;
        }
    }

    private static char toAlphanumeric0(final long value) {
        final long code = value % 35;
        return (char)(code + (code < 9 ? '0' : 'A' - 9));
    }

    private static char toAlphanumeric(final long value) {
        final long code = value % 36;
        return (char)(code + (code < 10 ? '0' : 'A' - 10));
    }

    private static char toDigit(final long value) {
        final long code = value % 10;
        return (char)(code + '0');
    }

    private static int fromAlpha(final char ch, final CharSequence seq) {
        if ('A' <= ch && ch <= 'Z') {
            return ch - 'A';
        }
        throw new IllegalArgumentException("Illegal first character '" + ch + "' in value string: " + seq);
    }

    private static int fromDigit(final char ch, final CharSequence seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " + seq);
    }

    private static int fromAlphanumeric(final char ch, final CharSequence seq) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'Z') {
            return 10 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " + seq);
    }
    private static int fromAlphanumeric0(final char ch, final CharSequence seq) {
        if ('1' <= ch && ch <= '9') {
            return ch - '1';
        }
        if ('A' <= ch && ch <= 'Z') {
            return 9 + ch - 'A';
        }
        throw new IllegalArgumentException("Illegal character '" + ch + "' in value string: " + seq);
    }
}
