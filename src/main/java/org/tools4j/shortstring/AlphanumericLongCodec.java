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

import org.tools4j.shortstring.Chars.BiAppender;

import static org.tools4j.shortstring.Chars.biSeqToString;
import static org.tools4j.shortstring.Chars.charFromBiSeq;
import static org.tools4j.shortstring.Chars.charToBiSeq1;
import static org.tools4j.shortstring.Chars.charToBiSeq2;
import static org.tools4j.shortstring.Chars.fromAlphanumeric;
import static org.tools4j.shortstring.Chars.fromDigit;
import static org.tools4j.shortstring.Chars.fromLetter;
import static org.tools4j.shortstring.Chars.indexOfFirstDigit;
import static org.tools4j.shortstring.Chars.indexOfFirstLetter;
import static org.tools4j.shortstring.Chars.isAlphanumeric;
import static org.tools4j.shortstring.Chars.leq;
import static org.tools4j.shortstring.Chars.longSeq1;
import static org.tools4j.shortstring.Chars.longSeq2;
import static org.tools4j.shortstring.Chars.lshBiSeq2;
import static org.tools4j.shortstring.Chars.lshSeq;
import static org.tools4j.shortstring.Chars.rshBiSeq1;
import static org.tools4j.shortstring.Chars.rshBiSeq2;
import static org.tools4j.shortstring.Chars.startsWithSignChar;
import static org.tools4j.shortstring.Chars.toAlphanumeric;
import static org.tools4j.shortstring.Chars.toAlphanumeric0;
import static org.tools4j.shortstring.Chars.toDigit;
import static org.tools4j.shortstring.Chars.toLetter;
import static org.tools4j.shortstring.Chars.toLetter0;
import static org.tools4j.shortstring.StringLengths.stringLength;

/**
 * Alphanumeric codec translating between string and long representation of a value.  This codec supports all
 * alphanumeric strings of length 12 (or signed 13), and most length 13 (or signed 14) strings with at least 11 leading
 * letters.
 * <p><br>
 * All longs are valid.  The string representation of a fully-numeric value is simply the long's to-string
 * representation.
 * <p><br>
 * Examples of valid and invalid string representations are:
 * <pre>
 *    (V) valid representations are
 *        - fully-numeric values from 0 to 9,999,999,999,999 (w/o 0 prefix -- zero prefixed values are considered alphanumeric)
 *        - alphanumeric strings of length 1-12
 *        - alphanumeric strings of length 13 with no digits or with a single digit at the end
 *        - alphanumeric strings of length 13 with the first digit at position 12 and less or equal to "RZRYMFXOEDX77"
 *        - all of the above, except zero, with a sign prefix, where
 *            '-' is the sign for fully-numeric values
 *            '.' is the sign for all other alphanumeric values
 *    (I) invalid representations are for instance
 *        - empty strings
 *        - strings longer than 14 characters
 *        - strings longer than 13 characters if they have no sign prefix
 *        - strings containing non-alphanumeric characters other than the sign prefix
 *        - fully-numeric values with alphanumeric '.' sign prefix
 *        - alphanumeric strings with numeric '-' sign prefix
 *        - zero-prefixed strings with numeric '-' sign prefix
 * </pre>
 * A valid string representation matches exactly one of the following definitions:
 * <pre>
 *     (N0) single zero digit character
 *          - char 1: '0'
 *     (N+) 1-13 digit characters without leading zeros
 *          - char 1 : '1'-'9'
 *          - char 2+: '0'-'9'
 *     (N-) 2-14 sign-prefixed digit characters: '-' sign followed by 1-13 digits and no leading zeros
 *          - char 1 : '-'
 *          - char 2 : '1'-'9'
 *          - char 3+: '0'-'9'
 *     (A+) 1-12 alphanumeric characters, all letters uppercase, first character a letter
 *          - char  1 : 'A'-'Z'
 *          - chars 2+: '0'-'9', 'A'-'Z'
 *     (A-) 2-13 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first character a letter
 *          - char 1 : '.'
 *          - char 2 : 'A'-'Z'
 *          - char 3+: '0'-'9', 'A'-'Z'
 *     (Z+) 2-12 alphanumeric characters with '0' prefix, all letters uppercase
 *          - char 1: '0'
 *          - chars 2+: '0'-'9', 'A'-'Z'
 *     (Z-) 3-13 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase
 *          - char 1 : '.'
 *          - char 2: '0'
 *          - char 3+: '0'-'9', 'A'-'Z'
 *     (D+) 2-12 alphanumeric characters, all letters uppercase, first character non-zero digit and at least one letter
 *          - char  1 : '1'-'9'
 *          - char  2+ : '0'-'9', 'A'-'Z' (at least one 'A'-'Z')
 *     (D-) 3-13 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first character non-zero digit and at least one letter
 *          - char 1 : '.'
 *          - char 2 : '1'-'9'
 *          - char 3+: '0'-'9', 'A'-'Z' (at least one 'A'-'Z')
 *     (L+) 13 alphanumeric characters, all letters uppercase, first 11 characters a letter
 *          - char  1-11 : 'A'-'Z'
 *          - char 12-13 : '0'-'9', 'A'-'Z'
 *          - if character 12 is a digit, then value &lt;= 'RZRYMFXOEDX77'
 *     (L-) 14 sign-prefixed alphanumeric characters, '.' as sign prefix, all letters uppercase, first 11 characters after the sign a letter
 *          - char 1 : '.'
 *          - char 2-12 : 'A'-'Z'
 *          - char 13-14 : '0'-'9', 'A'-'Z'
 *          - if character 13 is a digit, then value &lt;= '.RZRYMFXOEDX78'
 * </pre>
 * The long range is grouped accordingly into the following sections:
 * <pre>
 *  +------+----------------------------------------------------------------+-------------------------------------------------------------+---------------------------+
 *  | Grp  | String range                                                   | Long range                                                  | # of elements             |
 *  +------+----------------------------------------------------------------+-------------------------------------------------------------+---------------------------+
 *  | (L+) | "AAAAAAAAAAAAA", ..., "AAAAAAAAAAA00", ..., "RZRYMFXOEDX77"    |   4,873,772,662,273,663,092  -   9,223,372,036,854,775,807  | 4,349,599,374,581,112,716 |
 *  | (D+) | "1A", "1B", ..., "10A", ..., "1A0", ..., "9ZZZZZZZZZZZ"        |   3,655,332,746,705,247,318  -   4,873,772,662,273,663,091  | 1,218,439,915,568,415,774 |
 *  | (Z+) | "00", "01", ..., "0A", ..., "007", ..., "0ZZZZZZZZZZZ"         |   3,519,950,422,753,201,122  -   3,655,332,746,705,247,317  |   135,382,323,952,046,196 |
 *  | (A+) | "A", "B", ..., "A0", "A1", ..., "ZZZZZZZZZZZZ"                 |          10,000,000,000,000  -   3,519,950,422,753,201,121  | 3,519,940,422,753,201,122 |
 *  | (N+) | "1", "2", ..., "9", "10", ..., "9999999999999"                 |                           1  -           9,999,999,999,999  |         9,999,999,999,999 |
 *  | (N0) | "0"                                                            |                                                          0  |                         1 |
 *  | (N-) | "-1", "-2", ..., "-9", "-10", ..., "-9999999999999"            |                         (-1) -         (-9,999,999,999,999) |         9,999,999,999,999 |
 *  | (A-) | ".A", ".B", ..., ".A0", ".A1", ..., ".ZZZZZZZZZZZZ"            |        (-10,000,000,000,000) - (-3,519,950,422,753,201,121) | 3,519,940,422,753,201,122 |
 *  | (Z-) | ".00", ".01", ..., ".0A", ..., ".007", ..., ".0ZZZZZZZZZZZ"    | (-3,519,950,422,753,201,122) - (-3,655,332,746,705,247,317) |   135,382,323,952,046,196 |
 *  | (D-) | ".1A", ".1B", ..., ".10A", ..., ".1A0", ..., ".9ZZZZZZZZZZZ"   | (-3,655,332,746,705,247,318) - (-4,873,772,662,273,663,091) | 1,218,439,915,568,415,774 |
 *  | (L-) | ".AAAAAAAAAAAAA", ..., ".AAAAAAAAAAA00", ..., ".RZRYMFXOEDX78" | (-4,873,772,662,273,663,092) - (-9,223,372,036,854,775,808) | 4,349,599,374,581,112,717 |
 *  +------+----------------------------------------------------------------+-------------------------------------------------------------+---------------------------+
 * </pre>
 */
public enum AlphanumericLongCodec {
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
     * Length of 2nd block containing alphanumeric values up to length 12 prefixed with a letter.
     * <pre>
     * [A-Z] + [A-Z][0-9A-Z] + [A-Z][0-9A-Z][0-9A-Z] + ...
     *  26   +      26*36    +      26*36*36         + ...  = 26 * (36^0 + 36^1 + 36^2 + 36^3 + 36^4 + 36^5 + 36^6 + 36^7 + 36^8 + 36^9 + 36^10 + 36^11)
     *                                                      = 3,519,940,422,753,201,122
     */
    private static final long ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH = 3519940422753201122L;

    /**
     * Length of 3rd block containing alphanumeric values up to length 12 with a leading zero digit (w/o '0' itself
     * which is numeric).
     * <pre>
     * [0][0-9A-Z] + [0][0-9A-Z][0-9A-Z] + [0][0-9A-Z][0-9A-Z][0-9A-Z] + ...
     *       36    +         36*36       +           36*36*36          + ...  = 36^1 + 36^2 + 36^3 + 36^4 + 36^5 + 36^6 + 36^7 + 36^8 + 36^9 + 36^10 + 36^11
     *                                                                        = 135,382,323,952,046,196
     */
    private static final long ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH = 135382323952046196L;

    /**
     * Length of 4th block containing alphanumeric values up to length 12 with a leading non-zero digit.
     * To be non-numeric, the block must have a letter somewhere.
     * We order by the letter position from the end:
     * <pre>
     * L(n)   : letter at n-th position of string with n chars
     * L(n-i) : letter at (n-i)-th position of string with n chars
     *
     * L(n)   = [1-9][A-Z] + [1-9][0-9][A-Z] + ... + [1-9][0-9]...[0-9][A-Z] = 9 * (10^0 + 10^1 + ... + 10^3 + 10^10) * 26
     *                                                                       =  (10^11 - 1)   * 26
     *                                                                       = 99,999,999,999 * 26
     *                                                                       = 2,599,999,999,974
     * L(n-1) = [1-9][A-Z][0-9A-Z] + [1-9][0-9][A-Z][0-9A-Z] + ... + [1-9][0-9][0-9][0-9][A-Z][0-9A-Z]
     *                                                                       =  (10^10 - 1)  * 26 * 36
     *                                                                       = 9,999,999,999 * 26 * 36
     *                                                                       = 9,359,999,999,064
     *  ...
     * L(n-4) = [1-9][A-Z][0-9A-Z]...[0-9A-Z]                                = 9 * 26 * 36^10
     *                                                                       = 855,541,074,974,736,384
     *
     * L(n-i) = [1-9] | [0-9]^[0..(4-i)] | [A-Z][0-9A-Z]^i                   = (10^(11-i) - 1) * 26 * 36^i
     *
     * @see #ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH
     */
    private static final long[] ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTHS = {
            (100000000000L - 1) * 26,
            (10000000000L - 1) * 26 * 36,
            (1000000000L - 1) * 26 * 36 * 36,
            (100000000L - 1) * 26 * 36 * 36 * 36,
            (10000000L - 1) * 26 * 36 * 36 * 36 * 36,
            (1000000L - 1) * 26 * 36 * 36 * 36 * 36 * 36,
            (100000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36,
            (10000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36,
            (1000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36,
            (100L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36,
            (10L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36};
    /**
     * Total length of the 4th block containing alphanumeric values up to length 12 with a leading non-zero digit.
     *
     * @see #ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTHS
     */
    private static final long ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH =
            (100000000000L - 1) * 26 +
            (10000000000L - 1) * 26 * 36 +
            (1000000000L - 1) * 26 * 36 * 36 +
            (100000000L - 1) * 26 * 36 * 36 * 36 +
            (10000000L - 1) * 26 * 36 * 36 * 36 * 36 +
            (1000000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 +
            (100000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 +
            (10000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 +
            (1000L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 +
            (100L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 +
            (10L - 1) * 26 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36 * 36;

    /**
     * Length of 8th block containing alphanumeric values of length 13 with
     * <pre>
     *    (0) only letters
     *    (1) a single digit at the last position
     *    (2) a digit at the second last position (and possibly on the last as well)
     * </pre>
     *
     * We have
     * <pre>
     *    (0) [A-Z]...[A-Z][A-Z][A-Z]    = 26^13           = 2,481,152,873,203,736,576
     *    (1) [A-Z]...[A-Z][A-Z][0-9]    = 26^12 * 10      =   954,289,566,616,821,760
     *    (2) [A-Z]...[A-Z][0-9][0-9A-Z] = 26^11 * 10 * 36 = 1,321,324,015,315,599,360
     *                                               Total = 4,756,766,455,136,157,696
     * </pre>
     * However, we have only 4,349,599,374,581,112,716 values left, hence we support
     * <pre>
     * - 0% of values of length 13 with a digit before position 12
     * - 69.2% of values of length 13 with a digit at position 12
     * - 91.4% of values of length 13 with no digit or with a digit at or after position 12
     * - 100% of all values of length 13 without a digit or with a digit at position 13
     * </pre>
     */
    private static final long[] ALPHANUMERIC_EQ_13_BLOCK_LENGTHS = {
            (26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L),
            (26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L) * 10L,
            (26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L * 26L) * 10L * 36L
    };

    public static final String MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12 = "RZRYMFXOEDX77";
    public static final String MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12 = ".RZRYMFXOEDX78";

    public static long toLong(final CharSequence value) {
        return toLong(longSeq1(value), longSeq2(value), value.length());
    }

    static long toLong(final long seq1, final long seq2, final int len) {
        final int off = startsWithSignChar(seq1) ? 1 : 0;
        if (len <= off) {
            throw new IllegalArgumentException(len == 0 ? "Empty value string" :
                    "Invalid sign-only string: " + biSeqToString(seq1, seq2));
        }
        if (len - off > MAX_LENGTH_UNSIGNED) {
            throw new IllegalArgumentException("String exceeds max length: " + biSeqToString(seq1, seq2));
        }
        final SeqType seqType = SeqType.sequenceFor(seq1, seq2);
        long code;
        if (seqType.isNumeric()) {
            code = fromDigit(charFromBiSeq(seq1, seq2, off), seq1, seq2);
            for (int i = off + 1; i < len; i++) {
                code *= 10;
                code += fromDigit(charFromBiSeq(seq1, seq2, i), seq1, seq2);
            }
            return off == 0 ? code : -code;
        }
        if (len - off <= 12) {
            if (seqType.isLetterPrefixAlphanumeric()) {
                code = fromLetter(charFromBiSeq(seq1, seq2, off), seq1, seq2);
                for (int i = off + 1; i < len; i++) {
                    code *= 36;
                    code += 26 + fromAlphanumeric(charFromBiSeq(seq1, seq2, i), seq1, seq2);
                }
                code += NUMERIC_BLOCK_LENGTH;
                return off == 0 ? code : -code;
            }
            final char firstChar = seqType.isDigitPrefixAlphanumeric() ? charFromBiSeq(seq1, seq2, off) : '\0';
            if (firstChar == '0') {
                code = fromAlphanumeric(charFromBiSeq(seq1, seq2, off + 1), seq1, seq2);
                for (int i = off + 2; i < len; i++) {
                    code *= 36;
                    code += 36 + fromAlphanumeric(charFromBiSeq(seq1, seq2, i), seq1, seq2);
                }
                code += NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH;
                return off == 0 ? code : -code;
            }
            if ('1' <= firstChar && firstChar <= '9') {
                final int indexOfFirstLetter = indexOfFirstLetter(seq1, seq2, off + 1, len);
                assert indexOfFirstLetter >= off + 1;
                code = firstChar - '1';
                for (int i = off + 1; i < indexOfFirstLetter; i++) {
                    code *= 10;
                    code += 9 + fromDigit(charFromBiSeq(seq1, seq2, i), seq1, seq2);
                }
                code *= 26;
                code += fromLetter(charFromBiSeq(seq1, seq2, indexOfFirstLetter), seq1, seq2);
                for (int i = indexOfFirstLetter + 1; i < len; i++) {
                    code *= 36;
                    code += fromAlphanumeric(charFromBiSeq(seq1, seq2, i), seq1, seq2);
                }
                code += NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH;
                final int subBlockIndex = len - indexOfFirstLetter - 1;
                for (int i = 0; i < subBlockIndex; i++) {
                    code += ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTHS[i];
                }
                return off == 0 ? code : -code;
            }
            throw new IllegalArgumentException("Invalid value string: " + biSeqToString(seq1, seq2));
        }
        final int indexOfFirstDigit = indexOfFirstDigit(seq1, seq2, off, len);
        if ((indexOfFirstDigit < 0 || indexOfFirstDigit - off >= 11) && seqType.isLetterPrefixAlphanumeric()) {
            final int indexOfLastLetterBeforeFirstDigit = (indexOfFirstDigit < 0 ? len : indexOfFirstDigit) - 1;
            code = fromLetter(charFromBiSeq(seq1, seq2, off), seq1, seq2);
            for (int i = off + 1; i <= indexOfLastLetterBeforeFirstDigit; i++) {
                code *= 26;
                code += fromLetter(charFromBiSeq(seq1, seq2, i), seq1, seq2);
            }
            long subBlockAddOn = 0;
            if (indexOfLastLetterBeforeFirstDigit + 1 < len) {
                code *= 10;
                code += fromDigit(charFromBiSeq(seq1, seq2, indexOfLastLetterBeforeFirstDigit + 1), seq1, seq2);
                subBlockAddOn += ALPHANUMERIC_EQ_13_BLOCK_LENGTHS[0];
            }
            if (indexOfLastLetterBeforeFirstDigit + 2 < len) {
                code *= 36;
                code += fromAlphanumeric(charFromBiSeq(seq1, seq2, indexOfLastLetterBeforeFirstDigit + 2), seq1, seq2);
                subBlockAddOn += ALPHANUMERIC_EQ_13_BLOCK_LENGTHS[1];
            }
            code += NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH
                    + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH
                    + subBlockAddOn;
            if (code < 0) {
                if (code != Long.MIN_VALUE || !isConvertibleToLong(seq1, seq2, len)) {
                    throw new IllegalArgumentException("Alphanumeric 13-char value exceeds max allowed: " +
                            biSeqToString(seq1, seq2) + " > " +
                            (off == 0 ? MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12 :  MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12)
                    );
                }
            }
            return off == 0 ? code : -code;
        }
        throw new IllegalArgumentException((seqType.isAlphanumeric()
                ? "Alphanumeric 13-char value must not have a digit before position 12: "
                : "Invalid value string: ") + biSeqToString(seq1, seq2));
    }

    public static StringBuilder toString(final long value, final StringBuilder dst) {
        toString(value, dst, Chars::appendBiSeq);
        return dst;
    }

    public static int toString(final long value, final Appendable appendable) {
        return (int)toString(value, appendable, Chars::appendBiSeq);
    }

    static long toSeq1(final long value) {
        return toString(value, null, (seq1, seq2, target) -> seq1);
    }

    static long toSeq2(final long value) {
        return toString(value, null, (seq1, seq2, target) -> seq2);
    }

    private static <T> long toString(final long value, final T target, final BiAppender<? super T> appender) {
        final char sign;
        long val = Math.abs(value);
        int start = 0;
        long seq1 = 0;
        long seq2 = 0;
        if (value > -NUMERIC_BLOCK_LENGTH && value < NUMERIC_BLOCK_LENGTH) {
            sign = '-';
            final int len = stringLength(val);
            for (int i = len - 1; i >= 0; i--) {
                final char ch = toDigit(val);
                seq1 = charToBiSeq1(seq1, i, ch);
                seq2 = charToBiSeq2(seq2, i, ch);
                val /= 10;
                if (val == 0) {
                    start = i;
                    break;
                }
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= NUMERIC_BLOCK_LENGTH;
            for (int i = 11; i >= 0; i--) {
                if (val < 26) {
                    final char ch = toLetter0(val);
                    seq1 = charToBiSeq1(seq1, i, ch);
                    seq2 = charToBiSeq2(seq2, i, ch);
                    start = i;
                    break;
                }
                val -= 26;
                final char ch = toAlphanumeric(val);
                seq1 = charToBiSeq1(seq1, i, ch);
                seq2 = charToBiSeq2(seq2, i, ch);
                val /= 36;
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH);
            for (int i = 11; i >= 1; i--) {
                if (val < 36) {
                    final char ch = toAlphanumeric0(val);
                    seq1 = charToBiSeq1(seq1, i, ch);
                    seq2 = charToBiSeq2(seq2, i, ch);
                    seq1 = charToBiSeq1(seq1, i - 1, '0');
                    seq2 = charToBiSeq2(seq2, i - 1, '0');
                    start = i - 1;
                    break;
                }
                val -= 36;
                final char ch = toAlphanumeric(val);
                seq1 = charToBiSeq1(seq1, i, ch);
                seq2 = charToBiSeq2(seq2, i, ch);
                val /= 36;
            }
        } else if (value > -(NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH) &&
                value < (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH)) {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH);
            int subBlockIndex = -1;
            for (int i = 0; i < ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTHS.length; i++) {
                final long blockLength = ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTHS[i];
                if (val < blockLength) {
                    subBlockIndex = i;
                    break;
                }
                val -= blockLength;
            }
            assert subBlockIndex >= 0;
            for (int i = 0; i < subBlockIndex; i++) {
                final char ch = toAlphanumeric(val);
                seq1 = charToBiSeq1(seq1, 11 - i, ch);
                seq2 = charToBiSeq2(seq2, 11 - i, ch);
                val /= 36;
            }
            final char letter = toLetter(val);
            seq1 = charToBiSeq1(seq1, 11 - subBlockIndex, letter);
            seq2 = charToBiSeq2(seq2, 11 - subBlockIndex, letter);
            val /= 26;
            for (int i = subBlockIndex + 1; i <= 11; i++) {
                if (val < 9) {
                    final char ch = (char)(val + '1');
                    seq1 = charToBiSeq1(seq1, 11 - i, ch);
                    seq2 = charToBiSeq2(seq2, 11 - i, ch);
                    start = 11 - i;
                    break;
                }
                val -= 9;
                final char ch = toDigit(val);
                seq1 = charToBiSeq1(seq1, 11 - i, ch);
                seq2 = charToBiSeq2(seq2, 11 - i, ch);
                val /= 10;
            }
        } else {
            sign = '.';
            val -= (NUMERIC_BLOCK_LENGTH + ALPHANUMERIC_LE_12_LETTER_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_ZERO_PREFIXED_BLOCK_LENGTH + ALPHANUMERIC_LE_12_DIGIT_PREFIXED_BLOCK_LENGTH);
            int subBlockIndex = -1;
            for (int i = 0; i < ALPHANUMERIC_EQ_13_BLOCK_LENGTHS.length; i++) {
                final long blockLength = ALPHANUMERIC_EQ_13_BLOCK_LENGTHS[i];
                if (val < blockLength) {
                    subBlockIndex = i;
                    break;
                }
                val -= blockLength;
            }
            assert subBlockIndex >= 0;
            char ch;
            if (subBlockIndex > 1) {
                ch = toAlphanumeric(val);
                seq1 = charToBiSeq1(seq1, 12, ch);
                seq2 = charToBiSeq2(seq2, 12, ch);
                val /= 36;
            }
            if (subBlockIndex > 0) {
                ch = toDigit(val);
                seq1 = charToBiSeq1(seq1, 12 - (subBlockIndex - 1), ch);
                seq2 = charToBiSeq2(seq2, 12 - (subBlockIndex - 1), ch);
                val /= 10;
            }
            for (int i = subBlockIndex; i <= 12; i++) {
                ch = toLetter(val);
                seq1 = charToBiSeq1(seq1, 12 - i, ch);
                seq2 = charToBiSeq2(seq2, 12 - i, ch);
                val /= 26;
            }
            assert val == 0;
        }
        if (value < 0) {
            if (start > 0) {
                start--;
            } else {
                //left shift by 1 byte
                seq2 = lshBiSeq2(seq1, seq2, 1);
                seq1 = lshSeq(seq1, 1);
            }
            seq1 = charToBiSeq1(seq1, start, sign);
            seq2 = charToBiSeq2(seq2, start, sign);
        }
        if (start > 0) {
            //right shift by (start) bytes
            seq1 = rshBiSeq1(seq1, seq2, start);
            seq2 = rshBiSeq2(seq2, start);
        }
        return appender.append(seq1, seq2, target);
    }

    public static boolean isConvertibleToLong(final CharSequence value) {
        return isConvertibleToLong(longSeq1(value), longSeq2(value), value.length());
    }

    private static boolean isConvertibleToLong(final long seq1, final long seq2, final int len) {
        if (len < 1 || len > MAX_LENGTH_SIGNED) {
            return false;
        }
        final boolean signed = startsWithSignChar(seq1);
        if (signed) {
            if (len < 2) return false;
            if (len < MAX_LENGTH_SIGNED) return isAlphanumeric(seq1, seq2, 1, len);
        } else {
            if (len < MAX_LENGTH_UNSIGNED) return isAlphanumeric(seq1, seq2, 0, len);
        }
        final int off = signed ? 1 : 0;
        if (!isAlphanumeric(seq1, seq2, off, len)) {
            return false;
        }
        final int indexOfFirstDigit = indexOfFirstDigit(seq1, seq2, off, len);
        if ((indexOfFirstDigit < 0 || indexOfFirstDigit - off == 12)) {
            return true;
        }
        if ((indexOfFirstDigit - off < 11)) {
            return false;
        }
        return signed ?
                leq(seq1, seq2, MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12) :
                leq(seq1, seq2, MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12);
    }

}
