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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tools4j.shortstring.Chars.eq;

/**
 * Unit test for {@link ShortStringCodec} and implementations.
 */
class ShortStringCodecTest {

    private static final int MAX_LEN = 6;
    private long count;

    @FunctionalInterface
    private interface CharSupplier {
        char get(int charIndex, int run);
    }

    @BeforeEach
    void resetCount() {
        count = 0;
    }

    static Stream<Arguments> codecSpecs() {
        return Arrays.stream(CodecSpec.values()).map(Arguments::of);
    }

    @AfterEach
    void printCount(final TestInfo testInfo) {
        System.out.printf("%s(%s): %s tests\n",
                testInfo.getTestMethod().map(Method::getName).orElse("???"),
                testInfo.getDisplayName(),
                count);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    void boundaries(final CodecSpec spec) {
        final ShortStringCodec codec = spec.codec;

        //when + then
        assertEquals(Short.MIN_VALUE, codec.toShort(spec.minShortString), spec.minShortString);
        assertEquals(Short.MAX_VALUE, codec.toShort(spec.maxShortString), spec.maxShortString);
        assertEquals(Integer.MIN_VALUE, codec.toInt(spec.minIntString), spec.minIntString);
        assertEquals(Integer.MAX_VALUE, codec.toInt(spec.maxIntString), spec.maxIntString);
        assertEquals(Long.MIN_VALUE, codec.toLong(spec.minLongString), spec.minLongString);
        assertEquals(Long.MAX_VALUE, codec.toLong(spec.maxLongString), spec.maxLongString);
        count += 6;

        //given
        final String shortBeforeMin = next(spec.minShortString, spec.prevChar, -1);
        final String shortBeforeMax = next(spec.maxShortString, spec.prevChar, -1);
        final String intBeforeMin = next(spec.minIntString, spec.prevChar, -1);
        final String intBeforeMax = next(spec.maxIntString, spec.prevChar, -1);
        final String longBeforeMin = next(spec.minLongString, spec.prevChar, -1);
        final String longBeforeMax = next(spec.maxLongString, spec.prevChar, -1);

        //when + then
        assertEquals(Short.MIN_VALUE + 1, codec.toShort(shortBeforeMin), shortBeforeMin);
        assertEquals(Short.MAX_VALUE - 1, codec.toShort(shortBeforeMax), shortBeforeMax);
        assertEquals(Integer.MIN_VALUE + 1, codec.toInt(intBeforeMin), intBeforeMin);
        assertEquals(Integer.MAX_VALUE - 1, codec.toInt(intBeforeMax), intBeforeMax);
        assertEquals(Long.MIN_VALUE + 1, codec.toLong(longBeforeMin), longBeforeMin);
        assertEquals(Long.MAX_VALUE - 1, codec.toLong(longBeforeMax), longBeforeMax);
        count += 6;

        //given
        final String shortAfterMin = next(spec.minShortString, spec.nextChar, +1);
        final String shortAfterMax = next(spec.maxShortString, spec.nextChar, +1);
        final String intAfterMin = next(spec.minIntString, spec.nextChar, +1);
        final String intAfterMax = next(spec.maxIntString, spec.nextChar, +1);
        final String longAfterMin = next(spec.minLongString, spec.nextChar, +1);
        final String longAfterMax = next(spec.maxLongString, spec.nextChar, +1);

        //when + then
        assertThrows(IllegalArgumentException.class, () -> codec.toShort(shortAfterMin), shortAfterMin);
        assertThrows(IllegalArgumentException.class, () -> codec.toShort(shortAfterMax), shortAfterMax);
        assertThrows(IllegalArgumentException.class, () -> codec.toInt(intAfterMin), intAfterMin);
        assertThrows(IllegalArgumentException.class, () -> codec.toInt(intAfterMax), intAfterMax);
        assertThrows(IllegalArgumentException.class, () -> codec.toLong(longAfterMin), longAfterMin);
        assertThrows(IllegalArgumentException.class, () -> codec.toLong(longAfterMax), longAfterMax);
        count += 6;
    }

    private static String next(final String value, final CharUnaryOperator nextChar, final int direction) {
        final char[] chars = value.toCharArray();
        final int len = chars.length;
        for (int i = len - 1; i >= 0; i--) {
            final char cur = chars[i];
            final char next = nextChar.apply(cur);
            chars[i] = next;
            if (Character.compare(next, cur) * direction >= 0) {
                break;
            }
        }
        return new String(chars);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    void intRandomFromTo(final CodecSpec spec) {
        final Random rnd = new Random();
        final int iterationsPerTest = MAX_LEN == 5 ? 100 : 10;
        final int nFirstChars = 5;
        final int nNextChars = 8;
        final IntUnaryOperator alphaNum0to6 = i -> (i < 7 ? '0' : 'A' - 7) + i;
        final IntUnaryOperator alphaNum = i -> (i < 10 ? '0' : 'A' - 10) + i;
        final CharSupplier firstCharSupplier;
        final CharSupplier nextCharSupplier;
        switch (spec) {
            case ALPHANUMERIC:
                firstCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum0to6.applyAsInt(rnd.nextInt(33)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(rnd.nextInt(36)) : '\0';
                break;
            case NUMERIC:
                firstCharSupplier = (charIndex, run) -> run < nFirstChars ? (char)('1' + rnd.nextInt(9)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nFirstChars ? (char)('0' + rnd.nextInt(10)) : '\0';
                break;
            case HEX:
                firstCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(1 + rnd.nextInt(15)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(rnd.nextInt(16)) : '\0';
                break;
            default:
                throw new IllegalArgumentException("Unsupported: " + spec);
        }
        count = fromTo(spec, iterationsPerTest, firstCharSupplier, nextCharSupplier);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void specialFromTo(final CodecSpec spec) {
        final int iterationsPerTest = 1;
        final char[] firstChars;
        final char[] nextChars;
        switch (spec) {
            case ALPHANUMERIC:
                firstChars = new char[]{'0', '1', '6', 'A', 'B', 'Y', 'Z'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'C', 'W', 'Z'};
                break;
            case NUMERIC:
                firstChars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
                break;
            case HEX:
                firstChars = new char[]{'1', '2', '9', 'A', 'E', 'F'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'D', 'F'};
                break;
            default:
                throw new IllegalArgumentException("Unsupported: " + spec);
        }
        count = fromTo(spec, iterationsPerTest,
                (charIndex, run) -> run < firstChars.length ? firstChars[run] : '\0',
                (charIndex, run) -> run < nextChars.length ? nextChars[run] : '\0'
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void shortToFrom(final CodecSpec spec) {
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            final short val = (short)i;
            count += testToFrom(codec, val, builder);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void intRandomToFrom(final CodecSpec spec) {
        final Random rnd = new Random();
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 1_000_000; i++) {
            final int value = rnd.nextInt();
            count += testToFrom(codec, value, builder);
            count += testToFrom(codec, -value, builder);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void longRandomToFrom(final CodecSpec spec) {
        final Random rnd = new Random();
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 1_000_000; i++) {
            final long value = rnd.nextLong();
            count += testToFrom(codec, value, builder);
            count += testToFrom(codec, -value, builder);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void intSpecialToFrom(final CodecSpec spec) {
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 1_000_000; i++) {
            count += testToFrom(codec, i, builder);
            count += testToFrom(codec, -i, builder);
            count += testToFrom(codec, spec.maxNumericInt - i, builder);
            count += testToFrom(codec, spec.minNumericInt + i, builder);
            count += testToFrom(codec, Integer.MAX_VALUE - i, builder);
            count += testToFrom(codec, Integer.MIN_VALUE + i, builder);
        }
        count -= 2;//double counted zero, 2x
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    @SuppressWarnings("unused")
    void longSpecialToFrom(final CodecSpec spec) {
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 1_000_000; i++) {
            count += testToFrom(codec, i, builder);
            count += testToFrom(codec, -i, builder);
            count += testToFrom(codec, spec.maxNumericLong - i, builder);
            count += testToFrom(codec, spec.minNumericLong + i, builder);
            count += testToFrom(codec, Integer.MAX_VALUE - i, builder);
            count += testToFrom(codec, Integer.MIN_VALUE + i, builder);
            count += testToFrom(codec, Long.MAX_VALUE - i, builder);
            count += testToFrom(codec, Long.MIN_VALUE + i, builder);
        }
        count -= 2;//double counted zero, 2x
    }

    static int testToFrom(final ShortStringCodec codec, final short source, final StringBuilder builder) {
        builder.setLength(0);
        codec.toString(source, builder);
        final short fromBuilder = codec.toShort(builder);
        if (source != fromBuilder) {
            assertEquals(source, fromBuilder, source + " >> " + builder + " >> " + fromBuilder);
        }
        builder.setLength(0);
        codec.toString(source, (Appendable) builder);
        final short fromAppendable = codec.toShort(builder);
        if (source != fromAppendable) {
            assertEquals(source, fromAppendable, source + " >> " + builder + " >> " + fromAppendable);
        }
        return 2;
    }

    static int testToFrom(final ShortStringCodec codec, final int source, final StringBuilder builder) {
        builder.setLength(0);
        codec.toString(source, builder);
        final int fromBuilder = codec.toInt(builder);
        if (source != fromBuilder) {
            assertEquals(source, fromBuilder, source + " >> " + builder + " >> " + fromBuilder);
        }
        builder.setLength(0);
        codec.toString(source, (Appendable)builder);
        final int fromAppendable = codec.toInt(builder);
        if (source != fromAppendable) {
            assertEquals(source, fromAppendable, source + " >> " + builder + " >> " + fromAppendable);
        }
        return 2;
    }

    static int testToFrom(final ShortStringCodec codec, final long source, final StringBuilder builder) {
        builder.setLength(0);
        codec.toString(source, builder);
        final long fromBuilder = codec.toLong(builder);
        if (source != fromBuilder) {
            assertEquals(source, fromBuilder, source + " >> " + builder + " >> " + fromBuilder);
        }
        builder.setLength(0);
        codec.toString(source, (Appendable)builder);
        final long fromAppendable = codec.toLong(builder);
        if (source != fromAppendable) {
            assertEquals(source, fromAppendable, source + " >> " + builder + " >> " + fromAppendable);
        }
        return 2;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codecSpecs")
    void intDigitsFromTo(final CodecSpec spec) {
        final ShortStringCodec codec = spec.codec;
        final StringBuilder builder = new StringBuilder();
        boolean beforeZero = true;
        int inc = 1;
        for (int intSource = spec.minNumericInt; intSource <= spec.maxNumericInt && (beforeZero || intSource > 0); intSource += inc) {
            final String strSource = Integer.toString(intSource, spec == CodecSpec.HEX ? 16 : 10).toUpperCase();
            builder.setLength(0);

            assertEquals(strSource, codec.toString(intSource, builder).toString());
            assertEquals(intSource, codec.toInt(strSource));
            count++;

            //let's do small increments around 0 and at the boundaries
            inc = (Math.abs(intSource) <= 1_000_000 || (spec.maxNumericInt - Math.abs(intSource) <= 1_000_000)) ?
                    1 : 999;
            beforeZero &= intSource < 0;
        }
    }

    long fromTo(final CodecSpec spec,
                final int iterationsPerTest,
                final CharSupplier firstCharSupplier,
                final CharSupplier nextCharSupplier) {
        long count = 0;

        final char[] charArray = new char[7];
        for (int len = 1; len <= MAX_LEN; len++) {
            final int offset = 7 - len;
            charArray[offset - 1] = spec == CodecSpec.ALPHANUMERIC ? '.' : '-';
            for (int i = 0; i < 26; i++) {
                final char ch = firstCharSupplier.get(0, i);
                if (ch == '\0') {
                    break;
                }
                charArray[offset] = ch;
                count += testFromTo(spec.codec, iterationsPerTest, nextCharSupplier, charArray, 1, len);
            }
        }
        return count;
    }

    private long testFromTo(final ShortStringCodec codec,
                            final int iterationsPerTest,
                            final CharSupplier nextCharSuppliers,
                            final char[] chars, final int index, final int len) {
        long count = 0;
        final int offset = 7 - len;
        if (index < len) {
            for (int k = 0; k < 36; k++) {
                final char ch = nextCharSuppliers.get(index, k);
                if (ch == '\0') {
                    break;
                }
                chars[offset + index] = ch;
                count += testFromTo(codec, iterationsPerTest, nextCharSuppliers, chars, index + 1, len);
            }
        } else {
            final String pos = String.valueOf(chars, offset, len);
            final boolean num = org.tools4j.shortstring.Chars.isNumeric(pos);
            final boolean zeroStr = "0".equals(pos);
            final String neg = num ? '-' + pos : String.valueOf(chars, offset - 1, len + 1);
            final StringBuilder result = new StringBuilder();

            for (int i = 0; i < iterationsPerTest; i++) {
                final int posSrc = codec.toInt(pos);
                assertTrue(posSrc >= 0, pos + " >> " + posSrc + " >= 0");
                assertTrue(posSrc > 0 || zeroStr, pos + " >> " + posSrc + " > 0 || " + pos + " == \"0\"");

                result.setLength(0);
                codec.toString(posSrc, result);
                assertTrue(eq(pos, result), pos + " >> " + posSrc + " >> " + result);
                count++;

                result.setLength(0);
                codec.toString(posSrc, (Appendable) result);
                assertTrue(eq(pos, result), pos + " >> " + posSrc + " >> " + result);
                count++;

                if (zeroStr) continue;

                final int negSrc = codec.toInt(neg);
                assertTrue(negSrc < 0, neg + " >> " + negSrc + " < 0");

                result.setLength(0);
                codec.toString(negSrc, result);
                assertTrue(eq(neg, result), neg + " >> " + negSrc + " >> " + result);
                count++;

                result.setLength(0);
                codec.toString(negSrc, (Appendable) result);
                assertTrue(eq(neg, result), neg + " >> " + negSrc + " >> " + result);
                count++;
            }
        }
        return count;
    }

    @FunctionalInterface
    interface CharUnaryOperator {
        char apply(char ch);
    }

    private enum CodecSpec {
        ALPHANUMERIC(ShortString.ALPHANUMERIC,
                AlphanumericShortCodec.MIN_NUMERIC, AlphanumericShortCodec.MAX_NUMERIC,
                AlphanumericIntCodec.MIN_NUMERIC, AlphanumericIntCodec.MAX_NUMERIC,
                AlphanumericLongCodec.MIN_NUMERIC, AlphanumericLongCodec.MAX_NUMERIC,
                AlphanumericShortCodec.MIN_LETTER_DIGIT_PREFIXED_ALPHANUMERIC, AlphanumericShortCodec.MAX_LETTER_DIGIT_PREFIXED_ALPHANUMERIC,
                AlphanumericIntCodec.MIN_DIGIT_PREFIXED_ALPHANUMERIC, AlphanumericIntCodec.MAX_DIGIT_PREFIXED_ALPHANUMERIC,
                AlphanumericLongCodec.MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12, AlphanumericLongCodec.MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12,
                ch -> (char)(ch == '9' ? 'A' : ch == 'Z' ? '0' : ch + 1),
                ch -> (char)(ch == '0' ? 'Z' : ch == 'A' ? '9' : ch - 1)),
        NUMERIC(ShortString.NUMERIC,
                Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE,
                NumericCodec.MIN_SHORT_STRING, NumericCodec.MAX_SHORT_STRING,
                NumericCodec.MIN_INT_STRING, NumericCodec.MAX_INT_STRING,
                NumericCodec.MIN_LONG_STRING, NumericCodec.MAX_LONG_STRING,
                ch -> (char)(ch == '9' ? '0' : ch + 1),
                ch -> (char)(ch == '0' ? '9' : ch - 1)),
        HEX(ShortString.HEX,
                Short.MIN_VALUE, Short.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE,
                HexCodec.MIN_SHORT_STRING, HexCodec.MAX_SHORT_STRING,
                HexCodec.MIN_INT_STRING, HexCodec.MAX_INT_STRING,
                HexCodec.MIN_LONG_STRING, HexCodec.MAX_LONG_STRING,
                ch -> (char)(ch == '9' ? 'A' : ch == 'F' ? '0' : ch + 1),
                ch -> (char)(ch == '0' ? 'F' : ch == 'A' ? '9' : ch - 1));
        CodecSpec(final ShortStringCodec codec,
                  final short minNumericShort, final short maxNumericShort,
                  final int minNumericInt, final int maxNumericInt,
                  final long minNumericLong, final long maxNumericLong,
                  final String minShortString,
                  final String maxShortString,
                  final String minIntString,
                  final String maxIntString,
                  final String minLongString,
                  final String maxLongString,
                  final CharUnaryOperator nextChar,
                  final CharUnaryOperator prevChar
        ) {
            this.codec = requireNonNull(codec);
            this.minNumericShort = minNumericShort;
            this.maxNumericShort = maxNumericShort;
            this.minNumericInt = minNumericInt;
            this.maxNumericInt = maxNumericInt;
            this.minNumericLong = minNumericLong;
            this.maxNumericLong = maxNumericLong;
            this.minShortString = requireNonNull(minShortString);
            this.maxShortString = requireNonNull(maxShortString);
            this.minIntString = requireNonNull(minIntString);
            this.maxIntString = requireNonNull(maxIntString);
            this.minLongString = requireNonNull(minLongString);
            this.maxLongString = requireNonNull(maxLongString);
            this.nextChar = requireNonNull(nextChar);
            this.prevChar = requireNonNull(prevChar);
        }

        final ShortStringCodec codec;
        final short minNumericShort;
        final short maxNumericShort;
        final int minNumericInt;
        final int maxNumericInt;
        final long minNumericLong;
        final long maxNumericLong;
        final String minShortString;
        final String maxShortString;
        final String minIntString;
        final String maxIntString;
        final String minLongString;
        final String maxLongString;
        final CharUnaryOperator nextChar;
        final CharUnaryOperator prevChar;
    }
}