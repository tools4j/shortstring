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

enum SeqType {
    NUMERIC_UNSIGNED,
    NUMERIC_SIGNED,
    LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED,
    LETTER_PREFIXED_ALPHANUMERIC_SIGNED,
    DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED,
    DIGIT_PREFIXED_ALPHANUMERIC_SIGNED,
    INVALID;

    public boolean isSigned() {
        return this == NUMERIC_SIGNED ||
                this == LETTER_PREFIXED_ALPHANUMERIC_SIGNED ||
                this == DIGIT_PREFIXED_ALPHANUMERIC_SIGNED;
    }

    public boolean isNumeric() {
        return this == NUMERIC_UNSIGNED || this == NUMERIC_SIGNED;
    }

    public boolean isLetterPrefixAlphanumeric() {
        return this == LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED || this == LETTER_PREFIXED_ALPHANUMERIC_SIGNED;
    }

    public boolean isDigitPrefixAlphanumeric() {
        return this == DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED || this == DIGIT_PREFIXED_ALPHANUMERIC_SIGNED;
    }

    public boolean isAlphanumeric() {
        return isLetterPrefixAlphanumeric() || isDigitPrefixAlphanumeric();
    }
    static SeqType sequenceFor(final CharSequence seq) {
        return sequenceFor(seq, seq.length());
    }

    static SeqType sequenceFor(final CharSequence seq, final int length) {
        if (length == 0) {
            return INVALID;
        }
        final CharType charType = CharType.forChar(seq.charAt(0));
        if (length == 1) {
            return forSingleChar(charType);
        }
        SeqType type = forMultiChar(charType, seq.charAt(1));
        for (int i = 2; i < length; i++) {
            type = type.thenChar(seq.charAt(i));
        }
        if (length == 2 && type == DIGIT_PREFIXED_ALPHANUMERIC_SIGNED) {
            //".0' to ".9" are all invalid, but ".0' is valid
            return INVALID;
        }
        return type;
    }

    private SeqType thenChar(final char ch) {
        switch (this) {
            case NUMERIC_UNSIGNED://fallthrough
                if ('0' <= ch && ch <= '9') {
                    return this;
                }
                if ('A' <= ch && ch <= 'Z') {
                    return DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                return INVALID;
            case NUMERIC_SIGNED:
                if ('0' <= ch && ch <= '9') {
                    return this;
                }
                if ('A' <= ch && ch <= 'Z') {
                    return DIGIT_PREFIXED_ALPHANUMERIC_SIGNED;
                }
                return INVALID;
            case LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED:  //fallthrough
            case LETTER_PREFIXED_ALPHANUMERIC_SIGNED:    //fallthrough
            case DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED:  //fallthrough
            case DIGIT_PREFIXED_ALPHANUMERIC_SIGNED:
                if (('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9')) {
                    return this;
                }
                return INVALID;
            default:
                return INVALID;
        }
    }


    private static SeqType forSingleChar(final CharType charType) {
        switch (charType) {
            case ALPHA:
                return LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED;
            case ZERO_DIGIT://fallthrough
            case NON_ZERO_DIGIT:
                return NUMERIC_UNSIGNED;
            default:
                return INVALID;
        }
    }

    private static SeqType forMultiChar(final CharType first, final char then) {
        switch (first) {
            case ALPHA:
                if ('A' <= then && then <= 'Z') {
                    return LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                if ('0' <= then && then <= '9') {
                    return LETTER_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                return INVALID;
            case ZERO_DIGIT:
                if ('0' <= then && then <= '9') {
                    //zero prefixed value is treated as alphanumeric
                    return DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                if ('A' <= then && then <= 'Z') {
                    return DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                return INVALID;
            case NON_ZERO_DIGIT:
                if ('0' <= then && then <= '9') {
                    return NUMERIC_UNSIGNED;
                }
                if ('A' <= then && then <= 'Z') {
                    return DIGIT_PREFIXED_ALPHANUMERIC_UNSIGNED;
                }
                return INVALID;
            case ALPHANUMERIC_SIGN:
                if ('A' <= then && then <= 'Z') {
                    return LETTER_PREFIXED_ALPHANUMERIC_SIGNED;
                }
                if ('0' <= then && then <= '9') {
                    return DIGIT_PREFIXED_ALPHANUMERIC_SIGNED;
                }
                return INVALID;
            case NUMERIC_SIGN:
                if ('1' <= then && then <= '9') {
                    return NUMERIC_SIGNED;
                }
                return INVALID;
            default:
                return INVALID;
        }
    }
}
