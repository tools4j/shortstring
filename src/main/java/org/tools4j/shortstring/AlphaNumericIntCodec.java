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

import static org.tools4j.shortstring.Chars.indexOfFirstLetter;
import static org.tools4j.shortstring.Chars.isAlphanumeric;
import static org.tools4j.shortstring.Chars.isDigit;
import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.setChar;
import static org.tools4j.shortstring.Chars.startsWithSignChar;
import static org.tools4j.shortstring.StringLengths.stringLength;

/**
 * Codec translating short alphanumeric strings to integers and back. Strings up to length 6 (or signed 7) are supported.
 * <p><br>
 * All integers are valid integer representations.  The string representation of a fully-numeric value is simply the
 * integer's to-string representation.
 * <p><br>
 * Examples of valid and invalid string representations are:
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
 * </pre>
 * A valid string representation match exactly one of the following definitions:
 * <pre>
 *     (N0) single zero digit character
 *          - char 1: '0'
 *     (N+) 1-6 digit characters without leading zeros
 *          - char 1 : '1'-'9'
 *          - char 2+: '0'-'9'
 *     (N-) 2-7 sign-prefixed digit characters: '-' sign followed by 1-6 digits and no leading zeros
 *          - char 1 : '-'
 *          - char 2 : '1'-'9'
 *          - char 3+: '0'-'9'
 *     (A+) 1-6 alphanumeric characters, all letters uppercase, first character a letter
 *          - char  1 : 'A'-'Z'
 *          - chars 2+: '0'-'9', 'A'-'Z'
 *     (A-) 2-7 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first character a letter
 *          - char 1 : '.'
 *          - char 2 : 'A'-'Z'
 *          - char 3+: '0'-'9', 'A'-'Z'
 *     (Z+) 2-6 alphanumeric characters with '0' prefix, all letters uppercase
 *          - char 1: '0'
 *          - chars 2+: '0'-'9', 'A'-'Z'
 *     (Z-) - 3-7 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase
 *          - char 1 : '.'
 *          - char 2: '0'
 *          - char 3+: '0'-'9', 'A'-'Z'
 *     (D+) 1-6 alphanumeric characters, all letters uppercase, first character non-zero digit and at least one letter
 *          - char  1 : '1'-'9'
 *          - char  2+ : '0'-'9', 'A'-'Z' (at least one 'A'-'Z')
 *          - if first char is more than '7'-'9', then length 1-5 or otherwise chars[1-6] &lt;= '7XIZYJ'
 *     (D-) - 2-7 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first character non-zero digit and at least one letter
 *          - char 1 : '.'
 *          - char 2 : '1'-'9'
 *          - char 3+: '0'-'9', 'A'-'Z' (at least one 'A'-'Z')
 *          - if second char is '7'-'9', then length 1-5 or otherwise chars[1-6] &lt;= '7XIZYJ'
 * </pre>
 */
public enum AlphaNumericIntCodec {
    ;
    public static final int MAX_LENGTH_UNSIGNED = 6;
    public static final int MAX_LENGTH_SIGNED = 7;
    /**
     * Length of 1st block containing numeric values up to 6 digits:
     * <pre>
     * 0-999999
     * </pre>
     */
    private static final int NUMERIC_BLOCK_LENGTH = 1_000_000;

    /**
     * Length of 2nd block containing alphanumeric values prefixed with a letter.
     * <pre>
     * [A-Z] + [A-Z][0-9A-Z] + [A-Z][0-9A-Z][0-9A-Z] + ...
     *  26   +      26*36    +      26*36*36         + ...  = 26 * (36^0 + 36^1 + 36^2 + 36^3 + 36^4 + 36^5)
     *                                                      = 1,617,038,306
     */
    private static final int ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH = 1617038306;

    /**
     * Length of 3rd block containing alphanumeric values with a leading zero digit (w/o '0' itself which is numeric).
     * <pre>
     * [0][0-9A-Z] + [0][0-9A-Z][0-9A-Z] + [0][0-9A-Z][0-9A-Z][0-9A-Z] + ...
     *       36    +         36*36       +           36*36*36          + ...  = 36^1 + 36^2 + 36^3 + 36^4 + 36^5
     *                                                                        = 62,193,780
     */
    private static final int ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH = 62193780;
    /**
     * Length of 4th block containing alphanumeric values with a leading non-zero digit.
     * To be non-numeric, the block must have a letter somewhere.
     * We order by the letter position from the end:
     * <pre>
     * L(n)   : letter at n-th position of string with n chars
     * L(n-i) : letter at (n-i)-th position of string with n chars
     *
     * L(n)   = [1-9][A-Z] + [1-9][0-9][A-Z] + ... + [1-9][0-9][0-9][0-9][0-9][A-Z] = 9 * (10^0 + 10^1 + 10^2 + 10^3 + 10^4) * 26
     *                                                                              = (10^5 - 1) * 26
     *                                                                              =   99,999   * 26
     *                                                                              = 2,599,974
     * L(n-1) = [1-9][A-Z][0-9A-Z] + [1-9][0-9][A-Z][0-9A-Z] + ... + [1-9][0-9][0-9][0-9][A-Z][0-9A-Z]
     *                                                                              = (10^4 - 1) * 26 * 36
     *                                                                              =   9,999   * 26 * 36
     *                                                                              = 9,359,064
     *  ...
     * L(n-4) = [1-9][A-Z][0-9A-Z][0-9A-Z][0-9A-Z][0-9A-Z]                          = 9 * 26 * 36^4
     *                                                                              = 393,030,144
     *
     * L(n-i) = [1-9] | [0-9]^[0..(4-i)] | [A-Z][0-9A-Z]^i                          = (10^(5-i) - 1) * 26 * 36^i
     *
     * We get:
     *    L(n)   =   2,599,974
     *    L(n-1) =   9,359,064
     *    L(n-2) =  33,662,304
     *    L(n-3) = 120,092,544
     *    L(n-4) = 393,030,144
     *    --------------------
     *    L(*)   = 558,744,030
     *
     * </pre>
     * All 5 blocks have a total length of 558,744,030, but we have only 467,251,561 values left.
     * We support them in ascending order, that is, we only support
     * <pre>
     * - 83.6% of all digit prefixed values
     * or
     * - 76.7% values of length 6 with a leading digit and a letter right after that, and
     * - 100% of all other digit prefixed values
     * </pre>
     *
     * @see #ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTH
     */
    private static final int[] ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTHS = {
            2599974, 9359064, 33662304, 120092544, 558744030};
    /**
     * The actual length of the 4th block containing alphanumeric values with a leading non-zero digit, cutting out
     * some elements of the L(n-4) sub-block.
     *
     * @see #ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTHS
     */
    private static final int ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTH = Integer.MAX_VALUE
            - NUMERIC_BLOCK_LENGTH - ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH
            - ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH;

    public static final int MAX_NUMERIC = 999_999;
    public static final int MIN_NUMERIC = -999_999;
    public static final String MAX_DIGIT_PREFIXED_ALPHANUMERIC = "7XIZYJ";
    public static final String MIN_DIGIT_PREFIXED_ALPHANUMERIC = ".7XIZYK";

    public static int toInt(final CharSequence value) {
        final int len = value.length();
        final int off = Chars.startsWithSignChar(value) ? 1 : 0;
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
            return off == 0 ? code : -code;
        }
        if (seqType.isLetterPrefixAlphanumeric()) {
            code = fromLetter(value.charAt(off), value);
            for (int i = off + 1; i < len; i++) {
                code *= 36;
                code += 26 + fromAlphanumeric(value.charAt(i), value);
            }
            code += NUMERIC_BLOCK_LENGTH;
            return off == 0 ? code : -code;
        }
        final char firstChar = seqType.isDigitPrefixAlphanumeric() ? value.charAt(off) : '\0';
        if (firstChar == '0') {
            code = fromAlphanumeric(value.charAt(off + 1), value);
            for (int i = off + 2; i < len; i++) {
                code *= 36;
                code += 36 + fromAlphanumeric(value.charAt(i), value);
            }
            code += NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH;
            return off == 0 ? code : -code;
        }
        if ('1' <= firstChar && firstChar <= '9') {
            final int indexOfFirstLetter = indexOfFirstLetter(value, off + 1, len);
            assert indexOfFirstLetter >= off + 1;
            code = firstChar - '1';
            for (int i = off + 1; i < indexOfFirstLetter; i++) {
                code *= 10;
                code += 9 + fromDigit(value.charAt(i), value);
            }
            code *= 26;
            code += fromLetter(value.charAt(indexOfFirstLetter), value);
            for (int i = indexOfFirstLetter + 1; i < len; i++) {
                code *= 36;
                code += fromAlphanumeric(value.charAt(i), value);
            }
            code += NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH + ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH;
            final int subBlockIndex = len - indexOfFirstLetter - 1;
            for (int i = 0; i < subBlockIndex; i++) {
                code += ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTHS[i];
            }
            if (code < 0) {
                if (code != Integer.MIN_VALUE || !isConvertibleToInt(value)) {
                    throw new IllegalArgumentException(
                            " digit-prefixed value exceeds max allowed: " + value + (off == 0
                                    ? (" > " + MAX_DIGIT_PREFIXED_ALPHANUMERIC)
                                    : (" < " + MIN_DIGIT_PREFIXED_ALPHANUMERIC)));
                }
            }
            return off == 0 ? code : -code;
        }
        throw new IllegalArgumentException("Invalid value string: " + value);
    }

    public static StringBuilder toString(final int value, final StringBuilder dst) {
        final char sign;
        int val = Math.abs(value);
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
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= NUMERIC_BLOCK_LENGTH;
            for (int i = 5; i >= 0; i--) {
                if (val < 26) {
                    final char ch = toLetter0(val);
                    setChar(ch, dst, off + i);
                    start = off + i;
                    break;
                }
                val -= 26;
                final char ch = toAlphaNumeric(val);
                setChar(ch, dst, off + i);
                val /= 36;
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH + ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH + ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH);
            for (int i = 5; i >= 1; i--) {
                if (val < 36) {
                    final char ch = toAlphaNumeric0(val);
                    setChar(ch, dst, off + i);
                    setChar('0', dst, off + i - 1);
                    start = off + i - 1;
                    break;
                }
                val -= 36;
                final char ch = toAlphaNumeric(val);
                setChar(ch, dst, off + i);
                val /= 36;
            }
        } else {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHA_NUMERIC_LETTER_PREFIXED_BLOCK_LENGTH + ALPHA_NUMERIC_ZERO_PREFIXED_BLOCK_LENGTH);
            int subBlockIndex = -1;
            for (int i = 0; i < ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTHS.length; i++) {
                final int blockLength = ALPHA_NUMERIC_DIGIT_PREFIXED_BLOCK_LENGTHS[i];
                if (val < blockLength) {
                    subBlockIndex = i;
                    break;
                }
                val -= blockLength;
            }
            assert subBlockIndex >= 0;
            for (int i = 0; i < subBlockIndex; i++) {
                final char ch = toAlphaNumeric(val);
                setChar(ch, dst, off + 5 - i);
                val /= 36;
            }
            final char letter = toLetter(val);
            setChar(letter, dst, off + 5 - subBlockIndex);
            val /= 26;
            for (int i = subBlockIndex + 1; i <= 5; i++) {
                if (val < 9) {
                    final char ch = (char)(val + '1');
                    setChar(ch, dst, off + 5 - i);
                    start = off + 5 - i;
                    break;
                }
                val -= 9;
                final char ch = toDigit(val);
                setChar(ch, dst, off + 5 - i);
                val /= 10;
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

    public static boolean isConvertibleToInt(final CharSequence value) {
        final int len = value.length();
        if (len < 1 || len > MAX_LENGTH_SIGNED) {
            return false;
        }
        final boolean signed = startsWithSignChar(value);
        if (signed) {
            if (len < 2) return false;
            if (len < MAX_LENGTH_SIGNED) return isAlphanumeric(value, 1, len);
        } else {
            if (len < MAX_LENGTH_UNSIGNED) return isAlphanumeric(value, 0, len);
        }
        final int off = signed ? 1 : 0;
        if (!isAlphanumeric(value, off, len)) {
            return false;
        }
        if (!isDigit(value.charAt(off))) {
            return true;
        }
        return signed ?
                leq(value, MIN_DIGIT_PREFIXED_ALPHANUMERIC) :
                leq(value, MAX_DIGIT_PREFIXED_ALPHANUMERIC);
    }

    private static char toLetter0(final int value) {
        assert 0 <= value && value < 26;
        return (char)(value + 'A');
    }

    private static char toAlphaNumeric0(final int value) {
        assert 0 <= value && value < 36;
        return (char)(value + (value < 10 ? '0' : 'A' - 10));
    }

    private static char toLetter(final int value) {
        final int code = value % 26;
        return (char)(code + 'A');
    }

    private static char toAlphaNumeric(final int value) {
        final int code = value % 36;
        return (char)(code + (code < 10 ? '0' : 'A' - 10));
    }

    private static char toDigit(final int value) {
        final int code = value % 10;
        return (char)(code + '0');
    }

    private static int fromLetter(final char ch, final CharSequence seq) {
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
}