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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.stream.Stream;

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

    private enum Chars {
        AlphaNumeric,
        Numeric,
        Hex
    }

    @BeforeEach
    void resetCount() {
        count = 0;
    }

    static Stream<Arguments> sourceCodecs() {
        return Stream.of(
//                Arguments.of(new AlphaPrefixCodec(), AlphaPrefixCodec.MIN_NUMERIC, AlphaPrefixCodec.MAX_NUMERIC),
                Arguments.of(new AlphanumericCodec(), Chars.AlphaNumeric, AlphanumericIntCodec.MIN_NUMERIC, AlphanumericIntCodec.MAX_NUMERIC),
                Arguments.of(new NumericCodec(), Chars.Numeric, Integer.MIN_VALUE, Integer.MAX_VALUE),
                Arguments.of(new HexCodec(), Chars.Hex, Integer.MIN_VALUE, Integer.MAX_VALUE)
        );
    }

    @AfterEach
    void printCount(final TestInfo testInfo) {
        System.out.printf("%s(%s): %s tests\n",
                testInfo.getTestMethod().map(Method::getName).orElse("???"),
                testInfo.getDisplayName(),
                count);
    }

    @Test
    void printSomeInts() {
        final AlphanumericCodec codec = new AlphanumericCodec();
        final Consumer<String> stringPrinter = s -> {
            System.out.println(("       " + s).substring(s.length()) + " --> " + codec.toInt(s));
            count++;
        };
        final IntConsumer intPrinter = i -> {
            System.out.println(("       " + i).substring(("" + i).length()) + " --> " + codec.toString(i));
            count++;
        };

        stringPrinter.accept("00");
        stringPrinter.accept("09");
        stringPrinter.accept("0A");
        stringPrinter.accept("0Z");
        stringPrinter.accept("000");
        stringPrinter.accept("0ZZ");
        stringPrinter.accept("0000");
        stringPrinter.accept("0ZZZ");
        stringPrinter.accept("00000");
        stringPrinter.accept("0ZZZZ");
        stringPrinter.accept("000000");
        stringPrinter.accept("00000A");
        stringPrinter.accept("00000Z");
        stringPrinter.accept("0ZZZZZ");
        stringPrinter.accept("1A");
        stringPrinter.accept("1Z");
        stringPrinter.accept("9Z");
        stringPrinter.accept("10A");
        stringPrinter.accept("10Z");
        stringPrinter.accept("19999Z");
        stringPrinter.accept("10000A");
        stringPrinter.accept("10000Z");
        stringPrinter.accept("10001A");
        stringPrinter.accept("10009Z");
        stringPrinter.accept("99999Z");
        stringPrinter.accept("1A0");
        stringPrinter.accept("6ZZZZZ");
        stringPrinter.accept("7A0000");
        stringPrinter.accept("7W0000");
        stringPrinter.accept("7W9999");
        stringPrinter.accept("7XIZYI");
        stringPrinter.accept("7XIZYJ");
        stringPrinter.accept(".6ZZZZZ");
        stringPrinter.accept(".7A0000");
        stringPrinter.accept(".7W0000");
        stringPrinter.accept(".7W9999");
        stringPrinter.accept(".7XIZYI");
        stringPrinter.accept(".7XIZYJ");
        stringPrinter.accept(".7XIZYK");

        intPrinter.accept(AlphanumericIntCodec.MIN_NUMERIC);
        intPrinter.accept(AlphanumericIntCodec.MAX_NUMERIC);
        intPrinter.accept(AlphanumericIntCodec.MIN_NUMERIC - 1);
        intPrinter.accept(AlphanumericIntCodec.MAX_NUMERIC + 1);
        intPrinter.accept(1617038306 + 1_000_000 - 1);
        intPrinter.accept(1617038306 + 1_000_000);
        intPrinter.accept(1617038306 + 1_000_000 + 1);
        intPrinter.accept(1617038306 + 1_000_000 + 62193780 - 1);
        intPrinter.accept(1617038306 + 1_000_000 + 62193780);
        intPrinter.accept(1617038306 + 1_000_000 + 62193780 + 1);
        intPrinter.accept(-(1617038306 + 1_000_000) + 1);
        intPrinter.accept(-(1617038306 + 1_000_000));
        intPrinter.accept(-(1617038306 + 1_000_000) - 1);
        intPrinter.accept(-(1617038306 + 1_000_000 + 62193780) + 1);
        intPrinter.accept(-(1617038306 + 1_000_000 + 62193780));
        intPrinter.accept(-(1617038306 + 1_000_000 + 62193780) - 1);
        intPrinter.accept(Integer.MIN_VALUE);
        intPrinter.accept(Integer.MAX_VALUE);
    }

    @Test
    void printSomeLongs() {
        final AlphanumericCodec codec = new AlphanumericCodec();
        final Consumer<String> stringPrinter = s -> {
            System.out.println(("                    " + s).substring(s.length()) + " --> " + codec.toLong(s));
            count++;
        };
        final LongConsumer longPrinter = i -> {
            System.out.println(("                    " + i).substring(("" + i).length()) + " --> " + codec.toString(i));
            count++;
        };

        stringPrinter.accept("00");
        stringPrinter.accept("09");
        stringPrinter.accept("0A");
        stringPrinter.accept("0Z");
        stringPrinter.accept("000");
        stringPrinter.accept("0ZZ");
        stringPrinter.accept("0000");
        stringPrinter.accept("0ZZZ");
        stringPrinter.accept("00000");
        stringPrinter.accept("0ZZZZ");
        stringPrinter.accept("000000");
        stringPrinter.accept("00000A");
        stringPrinter.accept("00000Z");
        stringPrinter.accept("0ZZZZZ");
        stringPrinter.accept("0000000");
        stringPrinter.accept("0ZZZZZZ");
        stringPrinter.accept("00000000");
        stringPrinter.accept("0ZZZZZZZ");
        stringPrinter.accept("000000000");
        stringPrinter.accept("0ZZZZZZZZ");
        stringPrinter.accept("0000000000");
        stringPrinter.accept("0ZZZZZZZZZ");
        stringPrinter.accept("00000000000");
        stringPrinter.accept("0ZZZZZZZZZZ");
        stringPrinter.accept("000000000000");
        stringPrinter.accept("0ZZZZZZZZZZZ");
        stringPrinter.accept("1A");
        stringPrinter.accept("1Z");
        stringPrinter.accept("9Z");
        stringPrinter.accept("10A");
        stringPrinter.accept("10Z");
        stringPrinter.accept("19999999999Z");
        stringPrinter.accept("10000000000A");
        stringPrinter.accept("10000000000Z");
        stringPrinter.accept("10000000001A");
        stringPrinter.accept("10000000009Z");
        stringPrinter.accept("99999999999Z");
        stringPrinter.accept("1A0");
        stringPrinter.accept("6ZZZZZZZZZZZ");
        stringPrinter.accept("7A0000000000");
        stringPrinter.accept("7W0000000000");
        stringPrinter.accept("7W9999999999");
        stringPrinter.accept("9ZZZZZZZZZZZ");
        stringPrinter.accept("AAAAAAAAAAAAA");
        stringPrinter.accept("ZZZZZZZZZZZAA");
        stringPrinter.accept("ZZZZZZZZZZZZZ");
        stringPrinter.accept("AAAAAAAAAAAA0");
        stringPrinter.accept("ZZZZZZZZZZZZ9");
        stringPrinter.accept("AAAAAAAAAAA00");
        stringPrinter.accept("RZRYMFXOEDX77");
        stringPrinter.accept(AlphanumericLongCodec.MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12);
        stringPrinter.accept(AlphanumericLongCodec.MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12);

        longPrinter.accept(AlphanumericLongCodec.MIN_NUMERIC);
        longPrinter.accept(AlphanumericLongCodec.MAX_NUMERIC);
        longPrinter.accept(AlphanumericLongCodec.MIN_NUMERIC - 1);
        longPrinter.accept(AlphanumericLongCodec.MAX_NUMERIC + 1);
        longPrinter.accept(3655332746705247317L);
        longPrinter.accept(3655332746705247317L + 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L - 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L - 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L) + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L));
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L) - 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L) + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L));
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382324012512372L) - 1);
        longPrinter.accept(Long.MIN_VALUE);
        longPrinter.accept(Long.MAX_VALUE);
    }

    @Test
    @Disabled//exhaustive test, runs for quite some time (approx. 4-5 min, ~60ns per double-conversion).
    void allInts() {
        final long printInterval = 10_000_000;
        final ShortStringCodec codec = ShortString.ALPHANUMERIC;
        final StringBuilder builder = new StringBuilder(codec.maxIntLength() + 1);
        long printAt = printInterval;
        final long ts = System.nanoTime();
        //noinspection OverflowingLoopIndex
        for (int i = 0; i >= 0; i++) {
            testToFrom(codec, i, builder);
            testToFrom(codec, -(i+1), builder);
            count += 2;
            if (count >= printAt) {
                System.out.println("Tested: " + count);
                printAt += printInterval;
            }
        }
        final long te = System.nanoTime();
        System.out.println("Tested: " + count + ", " + (float)((te-ts) / (0.0 + count)) + "ns/double-conversion");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sourceCodecs")
    void boundaries(final ShortStringCodec codec) {
        final String min;
        final String max;
        final IntUnaryOperator next;
        final IntUnaryOperator prev;
        if (codec instanceof AlphanumericCodec) {
            min = AlphanumericIntCodec.MIN_DIGIT_PREFIXED_ALPHANUMERIC;
            max = AlphanumericIntCodec.MAX_DIGIT_PREFIXED_ALPHANUMERIC;
            next = ch -> ch == '9' ? 'A' : ch == 'Z' ? '0' : ch + 1;
            prev = ch -> ch == '0' ? 'Z' : ch == 'A' ? '9' : ch - 1;
        } else if (codec instanceof NumericCodec) {
            min = NumericCodec.MIN_INT_STRING;
            max = NumericCodec.MAX_INT_STRING;
            next = ch -> ch == '9' ? '0' : ch + 1;
            prev = ch -> ch == '0' ? '9' : ch - 1;
        } else {
            min = HexCodec.MIN_INT_STRING;
            max = HexCodec.MAX_INT_STRING;
            next = ch -> ch == '9' ? 'A' : ch == 'F' ? '0' : ch + 1;
            prev = ch -> ch == '0' ? 'F' : ch == 'A' ? '9' : ch - 1;
        }

        //when + then
        assertEquals(Integer.MIN_VALUE, codec.toInt(min), min);
        assertEquals(Integer.MAX_VALUE, codec.toInt(max), max);
        count += 2;

        //given
        final String beforeMin = next(min, prev, -1);
        final String beforeMax = next(max, prev, -1);
        final String afterMin = next(min, next, +1);
        final String afterMax = next(max, next, +1);
        count += 4;

        //when + then
        assertEquals(Integer.MIN_VALUE + 1, codec.toInt(beforeMin), beforeMin);
        assertEquals(Integer.MAX_VALUE - 1, codec.toInt(beforeMax), beforeMax);
//        assertEquals(Integer.MIN_VALUE + 1, codec.toInt(afterMin), afterMin);
//        assertEquals(Integer.MAX_VALUE - 1, codec.toInt(afterMax), afterMax);
        assertThrows(IllegalArgumentException.class, () -> codec.toInt(afterMin), afterMin);
        assertThrows(IllegalArgumentException.class, () -> codec.toInt(afterMax), afterMax);
        count += 4;
    }

    private static String next(final String value, final IntUnaryOperator nextChar, final int direction) {
        final char[] chars = value.toCharArray();
        final int len = chars.length;
        for (int i = len - 1; i >= 0; i--) {
            final char cur = chars[i];
            final char next = (char)nextChar.applyAsInt(cur);
            chars[i] = next;
            if (Character.compare(next, cur) * direction >= 0) {
                break;
            }
        }
        return new String(chars);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sourceCodecs")
    void randomFromTo(final ShortStringCodec codec, final Chars chars) {
        final Random rnd = new Random();
        final int iterationsPerTest = MAX_LEN == 5 ? 200 : 20;
        final int nFirstChars = 5;
        final int nNextChars = 8;
        final IntUnaryOperator alphaNum0to6 = i -> (i < 7 ? '0' : 'A' - 7) + i;
        final IntUnaryOperator alphaNum = i -> (i < 10 ? '0' : 'A' - 10) + i;
        final CharSupplier firstCharSupplier;
        final CharSupplier nextCharSupplier;
        switch (chars) {
            case AlphaNumeric:
                firstCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum0to6.applyAsInt(rnd.nextInt(33)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(rnd.nextInt(36)) : '\0';
                break;
            case Numeric:
                firstCharSupplier = (charIndex, run) -> run < nFirstChars ? (char)('1' + rnd.nextInt(9)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nFirstChars ? (char)('0' + rnd.nextInt(10)) : '\0';
                break;
            case Hex:
                firstCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(1 + rnd.nextInt(15)) : '\0';
                nextCharSupplier = (charIndex, run) -> run < nNextChars ? (char)alphaNum.applyAsInt(rnd.nextInt(16)) : '\0';
                break;
            default:
                throw new IllegalArgumentException("Unsupported: " + chars);
        }
        count = fromTo(codec, iterationsPerTest, chars, firstCharSupplier, nextCharSupplier);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sourceCodecs")
    void specialFromTo(final ShortStringCodec codec, final Chars chars, final int minNumeric, final int maxNumeric) {
        final int iterationsPerTest = 1;
        final char[] firstChars;
        final char[] nextChars;
        switch (chars) {
            case AlphaNumeric:
                firstChars = new char[]{'0', '1', '6', 'A', 'B', 'Y', 'Z'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'C', 'W', 'Z'};
                break;
            case Numeric:
                firstChars = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
                break;
            case Hex:
                firstChars = new char[]{'1', '2', '9', 'A', 'E', 'F'};
                nextChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'D', 'F'};
                break;
            default:
                throw new IllegalArgumentException("Unsupported: " + chars);
        }
        count = fromTo(codec, iterationsPerTest, chars,
                (charIndex, run) -> run < firstChars.length ? firstChars[run] : '\0',
                (charIndex, run) -> run < nextChars.length ? nextChars[run] : '\0'
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sourceCodecs")
    void specialToFrom(final ShortStringCodec codec, final Chars chars, final int minNumeric, final int maxNumeric) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i <= 1_000_000; i++) {
            testToFrom(codec, i, builder);
            testToFrom(codec, -i, builder);
            testToFrom(codec, maxNumeric - i, builder);
            testToFrom(codec, minNumeric + i, builder);
            testToFrom(codec, Integer.MAX_VALUE - i, builder);
            testToFrom(codec, Integer.MIN_VALUE + i, builder);
            count += 6;
        }
        count--;//one test double counted for zero
    }

    private void testToFrom(final ShortStringCodec codec, final int source, final StringBuilder builder) {
        builder.setLength(0);
        codec.toString(source, builder);
        final int from = codec.toInt(builder);
        if (source != from) {
            assertEquals(source, from, source + " >> " + builder + " >> " + from);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("sourceCodecs")
    void digitsFromTo(final ShortStringCodec codec, final Chars chars, final int minNumeric, final int maxNumeric) {
        final StringBuilder builder = new StringBuilder();
        boolean beforeZero = true;
        int inc = 1;
        for (int intSource = minNumeric; intSource <= maxNumeric && (beforeZero || intSource > 0); intSource += inc) {
            final String strSource = Integer.toString(intSource, chars == Chars.Hex ? 16 : 10).toUpperCase();
            builder.setLength(0);

            assertEquals(strSource, codec.toString(intSource, builder).toString());
            assertEquals(intSource, codec.toInt(strSource));
            count++;

            //let's do small increments around 0 and at the boundaries
            inc = (Math.abs(intSource) <= 1_000_000 || (maxNumeric - Math.abs(intSource) <= 1_000_000)) ?
                    1 : 999;
            beforeZero &= intSource < 0;
        }
    }

    long fromTo(final ShortStringCodec codec,
                final int iterationsPerTest,
                final Chars chars,
                final CharSupplier firstCharSupplier,
                final CharSupplier nextCharSupplier) {
        long count = 0;

        final char[] charArray = new char[7];
        for (int len = 1; len <= MAX_LEN; len++) {
            final int offset = 7 - len;
            charArray[offset - 1] = chars == Chars.AlphaNumeric ? '.' : '-';
            for (int i = 0; i < 26; i++) {
                final char ch = firstCharSupplier.get(0, i);
                if (ch == '\0') {
                    break;
                }
                charArray[offset] = ch;
                count += testFromTo(codec, iterationsPerTest, nextCharSupplier, charArray, 1, len);
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

                if (zeroStr) continue;

                final int negSrc = codec.toInt(neg);
                assertTrue(negSrc < 0, neg + " >> " + negSrc + " < 0");

                result.setLength(0);
                codec.toString(negSrc, result);
                assertTrue(eq(neg, result), neg + " >> " + negSrc + " >> " + result);
                count++;
            }
        }
        return count;
    }

}