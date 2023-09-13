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

import static org.tools4j.shortstring.Chars.appendSeq;
import static org.tools4j.shortstring.Chars.charToSeq;
import static org.tools4j.shortstring.Chars.fromAlphanumeric;
import static org.tools4j.shortstring.Chars.fromDigit;
import static org.tools4j.shortstring.Chars.fromLetter;
import static org.tools4j.shortstring.Chars.isDigit;
import static org.tools4j.shortstring.Chars.isLetter;
import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.startsWithSignChar;
import static org.tools4j.shortstring.Chars.toAlphanumeric;
import static org.tools4j.shortstring.Chars.toDigit;
import static org.tools4j.shortstring.Chars.toLetter;
import static org.tools4j.shortstring.Chars.toLetter0;
import static org.tools4j.shortstring.StringLengths.stringLength;

/**
 * Codec translating tiny alphanumeric strings to shorts and back. Strings up to length 3 (or signed 4) are supported.
 * <p><br>
 * All shorts are valid.  The string representation of a fully-numeric value is simply the short's to-string
 * representation.
 * <p><br>
 * Examples of valid and invalid string representations are:
 * <pre>
 *    (V) valid representations are
 *        - fully-numeric values from 0 to 999 (w/o 0 prefix -- zero prefixed values are considered alphanumeric)
 *        - alphanumeric strings of length 1-3 containing only letters
 *        - alphanumeric strings of length 1-2 containing letters or numbers
 *        - alphanumeric strings of length 3 with 2 leading letters
 *        - alphanumeric strings of length 3 with 1 leading letter and less or equal to "R9P"
 *        - all of the above, except zero, with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for all other alphanumeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 4 characters
 *        - strings longer than 3 characters and no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - fully-numeric values with alphanumeric '.' sign prefix
 *        - alphanumeric strings with numeric '-' sign prefix
 *        - zero-prefixed strings with numeric '-' sign prefix
 * </pre>
 * A valid string representation matches exactly one of the following definitions:
 * <pre>
 *     (N0) single zero digit character
 *          - char 1: '0'
 *     (N+) 1-3 digit characters without leading zeros
 *          - char 1: '1'-'9'
 *          - char 2: '0'-'9'
 *          - char 3: '0'-'9'
 *     (N-) 2-4 sign-prefixed digit characters: '-' sign followed by 1-3 digits and no leading zeros
 *          - char 1: '-'
 *          - char 2: '1'-'9'
 *          - char 3: '0'-'9'
 *          - char 4: '0'-'9'
 *     (L+) 1-3 alphanumeric characters, all letters uppercase, first 2 characters a letter
 *          - char 1: 'A'-'Z'
 *          - char 2: 'A'-'Z'
 *          - char 3: '0'-'9', 'A'-'Z'
 *     (L-) 2-4 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first 2 characters a letter
 *          - char 1: '.'
 *          - char 2: 'A'-'Z'
 *          - char 3: 'A'-'Z'
 *          - char 4: '0'-'9', 'A'-'Z'
 *     (A+) 1-3 alphanumeric characters, all letters uppercase, first character a letter, second a digit
 *          - char 1: 'A'-'Z'
 *          - char 2: '0'-'9'
 *          - char 3: '0'-'9', 'A'-'Z'
 *          - length 2 or otherwise value &lt;= 'R9P'
 *     (A-) 2-4 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first character a letter, second a digit
 *          - char 1: '.'
 *          - char 2: 'A'-'Z'
 *          - char 3: '0'-'9'
 *          - char 4: '0'-'9', 'A'-'Z'
 *          - length 2 or otherwise value &lt;= '.R9Q'
 * </pre>
 * The short range is grouped accordingly into the following sections:
 * <pre>
 *  +------+------------------------------------------------------------+-----------------------+---------------+
 *  | Grp  | String range                                               | Short range           | # of elements |
 *  +------+------------------------------------------------------------+-----------------------+---------------+
 *  | (A+) | "A0",..., "Z9", "A00", ..., "P9Z", "R00", ..., "R9P"       |    26,038 -   31,767  |      6,730    |
 *  | (L+) | "A",..., "Z", "AA", ..., "ZZ", "AA0", ..., "ZZZ"           |     1,000 -   26,037  |     25,038    |
 *  | (N+) | "1", "2", ..., "9", "10", ..., "999"                       |         1 -      999  |        999    |
 *  | (N0) | "0"                                                        |                    0  |          1    |
 *  | (N-) | "-1", "-2", ..., "-9", "-10", ..., "-999"                  |      (-1) -    (-999) |        999    |
 *  | (L-) | ".A",..., ".Z", ".A0", ..., ".ZZ", ".AA0", ..., ".ZZZ"     |  (-1,000) - (-26,037) |     25,038    |
 *  | (A-) | ".A0",..., ".Z9", ".A00", ..., ".P9Z", ".R00", ..., ".R9Q" | (-26,038) - (-31,768) |      6,731    |
 *  +------+------------------------------------------------------------+-----------------------+---------------+
 * </pre>
 */
public enum AlphanumericShortCodec {
    ;
    public static final int MAX_LENGTH_UNSIGNED = 3;
    public static final int MAX_LENGTH_SIGNED = 4;
    /**
     * Length of 1st block containing numeric values up to 6 digits:
     * <pre>
     * 0-999
     * </pre>
     */
    private static final int NUMERIC_BLOCK_LENGTH = 1_000;

    /**
     * Length of 2nd block containing alphanumeric values with no digit at the first 2 positions, which means every
     * string has a prefix of letters.
     * <pre>
     * [A-Z] + [A-Z][A-Z] + [A-Z][A-Z][0-9A-Z]
     *  26   +   26*26    +    26*26*36        = 25,038
     */
    private static final int ALPHANUMERIC_LETTER_PREFIXED_BLOCK_LENGTH = 25038;

    /**
     * Length of 3rd block containing alphanumeric values prefixed with a letter and a digit.
     * <pre>
     * [A-Z][0-9] + [A-Z][0-9][0-9A-Z]
     *   26*10    +    26*10*36         = 9,620
     *
     * We have 26,038 values in the first two blocks and of 2^15 values only 6,730 left.
     * That is, we only support
     * <pre>
     * - 70.0% of all letter prefixed values with a digit after the letter
     * </pre>
     */
    private static final int ALPHANUMERIC_LETTER_DIGIT_PREFIXED_BLOCK_LENGTH = 6730;
    public static final short MAX_NUMERIC = 999;
    public static final short MIN_NUMERIC = -999;
    public static final String MAX_LETTER_DIGIT_PREFIXED_ALPHANUMERIC = "R9P";
    public static final String MIN_LETTER_DIGIT_PREFIXED_ALPHANUMERIC = ".R9Q";

    public static short toShort(final CharSequence value) {
        final int len = value.length();
        final int off = startsWithSignChar(value) ? 1 : 0;
        if (len <= off) {
            throw new IllegalArgumentException(len == 0 ? "Empty value string" : "Invalid sign-only string: " + value);
        }
        if (len - off > MAX_LENGTH_UNSIGNED) {
            throw new IllegalArgumentException("String exceeds max length");
        }
        final SeqType seqType = SeqType.sequenceFor(value);
        int code;
        if (seqType.isNumeric()) {
            code = fromDigit(value.charAt(off), value);
            for (int i = off + 1; i < len; i++) {
                code *= 10;
                code += fromDigit(value.charAt(i), value);
            }
            return (short)(off == 0 ? code : -code);
        }
        final char firstChar = seqType.isLetterPrefixAlphanumeric() ? value.charAt(off) : '\0';
        final char secondChar = len > off + 1 ? value.charAt(off + 1) : '\0';
        if (isLetter(firstChar) && !isDigit(secondChar)) {
            code = fromLetter(firstChar, value);
            if (len > off + 1) {
                code *= 26;
                code += fromLetter(secondChar, value);
                if (len > off + 2) {
                    code *= 36;
                    code += fromAlphanumeric(value.charAt(off + 2), value);
                    code += 26 + 26*26;
                } else {
                    code += 26;
                }
            }
            code += NUMERIC_BLOCK_LENGTH;
            return (short)(off == 0 ? code : -code);
        }
        if (isLetter(firstChar) && isDigit(secondChar)) {
            code = fromLetter(firstChar, value);
            code *= 10;
            code += fromDigit(secondChar, value);
            if (len > off + 2) {
                code *= 36;
                code += fromAlphanumeric(value.charAt(off + 2), value);
                code += 26*10;
            }
            code += NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LETTER_PREFIXED_BLOCK_LENGTH;
            if (code > Short.MAX_VALUE && (off == 0 || code != -Short.MIN_VALUE)) {
                throw new IllegalArgumentException(
                        "Digit-prefixed value exceeds max allowed: " + value + " > " + (off == 0 ?
                                MAX_LETTER_DIGIT_PREFIXED_ALPHANUMERIC : MIN_LETTER_DIGIT_PREFIXED_ALPHANUMERIC
                        ));
            }
            return (short)(off == 0 ? code : -code);
        }
        throw new IllegalArgumentException("Invalid value string: " + value);
    }

    public static StringBuilder toString(final short value, final StringBuilder dst) {
        return appendSeq(toSeq(value), dst);
    }

    public static int toString(final short value, final Appendable appendable) {
        return appendSeq(toSeq(value), appendable);
    }

    private static int toSeq(final short value) {
        final char sign;
        int val = Math.abs(value);
        int start = 0;
        int seq = 0;
        if (value > -NUMERIC_BLOCK_LENGTH && value < NUMERIC_BLOCK_LENGTH) {
            sign = '-';
            final int len = stringLength(val);
            for (int i = len - 1; i >= 0; i--) {
                final char ch = toDigit(val);
                seq = charToSeq(seq, i, ch);
                val /= 10;
                if (val == 0) {
                    start = i;
                    break;
                }
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LETTER_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LETTER_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= NUMERIC_BLOCK_LENGTH;
            if (val < 26) {
                final char ch = toLetter0(val);
                seq = charToSeq(seq, 2, ch);
                start = 2;
            } else {
                val -= 26;
                if (val < 26 * 26) {
                    seq = charToSeq(seq, 2, toLetter(val));
                    val /= 26;
                    seq = charToSeq(seq, 1, toLetter0(val));
                    start = 1;
                } else {
                    val -= (26 * 26);
                    seq = charToSeq(seq, 2, toAlphanumeric(val));
                    val /= 36;
                    seq = charToSeq(seq, 1, toLetter(val));
                    val /= 26;
                    seq = charToSeq(seq, 0, toLetter0(val));
                }
            }
        } else {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LETTER_PREFIXED_BLOCK_LENGTH);
            if (val < 26 * 10) {
                seq = charToSeq(seq, 2, toDigit(val));
                val /= 10;
                seq = charToSeq(seq, 1, toLetter0(val));
                start = 1;
            } else {
                val -= (26 * 10);
                seq = charToSeq(seq, 2, toAlphanumeric(val));
                val /= 36;
                seq = charToSeq(seq, 1, toDigit(val));
                val /= 10;
                seq = charToSeq(seq, 0, toLetter0(val));
            }
        }
        if (value < 0) {
            if (start > 0) {
                start--;
            } else {
                seq = (seq << 8);
            }
            seq = charToSeq(seq, start, sign);
        }
        return (seq >>> (start << 3));
    }

    public static boolean isConvertibleToShort(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_LENGTH_SIGNED) {
            return false;
        }
        final SeqType seqType = SeqType.sequenceFor(value);
        switch (seqType) {
            case NUMERIC_UNSIGNED:
                return len <= MAX_LENGTH_UNSIGNED;
            case NUMERIC_SIGNED:
                return true;
            case LETTER_PREFIXED_ALPHANUMERIC_SIGNED:
                if (len < 2) return true;
                final char ch2 = value.charAt(1);
                return isLetter(ch2) || leq(value, MAX_LETTER_DIGIT_PREFIXED_ALPHANUMERIC);
            case LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED:
                if (len < 3) return true;
                final char ch3 = value.charAt(2);
                return isLetter(ch3) || leq(value, MIN_LETTER_DIGIT_PREFIXED_ALPHANUMERIC);
            default:
                return false;
        }
    }

}
