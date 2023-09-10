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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tools4j.shortstring.AlphanumericIntCodec.MAX_DIGIT_PREFIXED_ALPHANUMERIC;
import static org.tools4j.shortstring.AlphanumericIntCodec.MIN_DIGIT_PREFIXED_ALPHANUMERIC;
import static org.tools4j.shortstring.AlphanumericLongCodec.MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12;
import static org.tools4j.shortstring.AlphanumericLongCodec.MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12;
import static org.tools4j.shortstring.ShortStringCodecTest.testToFrom;

/**
 * Unit test for {@link AlphanumericCodec}.
 */
class AlphanumericCodecTest {

    //under test
    final ShortStringCodec codec = ShortString.ALPHANUMERIC;

    @Test
    void printSomeInts() {
        final Consumer<String> stringPrinter = s -> {
            System.out.println(("       " + s).substring(s.length()) + " --> " + codec.toInt(s));
        };
        final IntConsumer intPrinter = i -> {
            System.out.println(("           " + i).substring(("" + i).length()) + " --> " + codec.toString(i));
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
    void someInts() {
        assertEquals(1618038306, codec.toInt("00"));
        assertEquals(1618038315, codec.toInt("09"));
        assertEquals(1618038316, codec.toInt("0A"));
        assertEquals(1618038341, codec.toInt("0Z"));
        assertEquals(1618038342, codec.toInt("000"));

        assertEquals(1618039637, codec.toInt("0ZZ"));
        assertEquals(1618039638, codec.toInt("0000"));
        assertEquals(1618086293, codec.toInt("0ZZZ"));
        assertEquals(1618086294, codec.toInt("00000"));
        assertEquals(1619765909, codec.toInt("0ZZZZ"));
        assertEquals(1619765910, codec.toInt("000000"));
        assertEquals(1619765920, codec.toInt("00000A"));
        assertEquals(1619765945, codec.toInt("00000Z"));
        assertEquals(1680232085, codec.toInt("0ZZZZZ"));
        assertEquals(1680232086, codec.toInt("1A"));
        assertEquals(1680232111, codec.toInt("1Z"));
        assertEquals(1680232319, codec.toInt("9Z"));
        assertEquals(1680232320, codec.toInt("10A"));
        assertEquals(1680232345, codec.toInt("10Z"));
        assertEquals(1680752059, codec.toInt("19999Z"));
        assertEquals(1680492060, codec.toInt("10000A"));
        assertEquals(1680492085, codec.toInt("10000Z"));
        assertEquals(1680492086, codec.toInt("10001A"));
        assertEquals(1680492319, codec.toInt("10009Z"));
        assertEquals(1682832059, codec.toInt("99999Z"));
        assertEquals(1682832060, codec.toInt("1A0"));
        assertEquals(2107966067, codec.toInt("6ZZZZZ"));
        assertEquals(2107966068, codec.toInt("7A0000"));
        assertEquals(2144917620, codec.toInt("7W0000"));
        assertEquals(2145349521, codec.toInt("7W9999"));
        assertEquals(2147483646, codec.toInt("7XIZYI"));
        assertEquals(Integer.MAX_VALUE, codec.toInt("7XIZYJ"));
        assertEquals(Integer.MAX_VALUE, codec.toInt(MAX_DIGIT_PREFIXED_ALPHANUMERIC));

        assertEquals(-2107966067, codec.toInt(".6ZZZZZ"));
        assertEquals(-2107966068, codec.toInt(".7A0000"));
        assertEquals(-2144917620, codec.toInt(".7W0000"));
        assertEquals(-2145349521, codec.toInt(".7W9999"));
        assertEquals(Integer.MIN_VALUE + 2, codec.toInt(".7XIZYI"));
        assertEquals(Integer.MIN_VALUE + 1, codec.toInt(".7XIZYJ"));
        assertEquals(Integer.MIN_VALUE, codec.toInt(".7XIZYK"));

        assertEquals(AlphanumericIntCodec.MIN_NUMERIC, codec.toInt("-999999"));
        assertEquals(AlphanumericIntCodec.MAX_NUMERIC, codec.toInt("999999"));
        assertEquals("-999999", codec.toString(AlphanumericIntCodec.MIN_NUMERIC));
        assertEquals("999999", codec.toString(AlphanumericIntCodec.MAX_NUMERIC));
        assertEquals(".A", codec.toString(AlphanumericIntCodec.MIN_NUMERIC - 1));
        assertEquals("A", codec.toString(AlphanumericIntCodec.MAX_NUMERIC + 1));
        assertEquals("ZZZZZZ", codec.toString(1617038306 + 1_000_000 - 1));
        assertEquals("00", codec.toString(1617038306 + 1_000_000));
        assertEquals("01", codec.toString(1617038306 + 1_000_000 + 1));
        assertEquals("0ZZZZZ", codec.toString(1617038306 + 1_000_000 + 62193780 - 1));
        assertEquals("1A", codec.toString(1617038306 + 1_000_000 + 62193780));
        assertEquals("1B", codec.toString(1617038306 + 1_000_000 + 62193780 + 1));
        assertEquals(".ZZZZZZ", codec.toString(-(1617038306 + 1_000_000) + 1));
        assertEquals(".00", codec.toString(-(1617038306 + 1_000_000)));
        assertEquals(".01", codec.toString(-(1617038306 + 1_000_000) - 1));
        assertEquals(".0ZZZZZ", codec.toString(-(1617038306 + 1_000_000 + 62193780) + 1));
        assertEquals(".1A", codec.toString(-(1617038306 + 1_000_000 + 62193780)));
        assertEquals(".1B", codec.toString(-(1617038306 + 1_000_000 + 62193780) - 1));
        assertEquals(".7XIZYJ", codec.toString(Integer.MIN_VALUE + 1));
        assertEquals(".7XIZYK", codec.toString(Integer.MIN_VALUE));
        assertEquals(MIN_DIGIT_PREFIXED_ALPHANUMERIC, codec.toString(Integer.MIN_VALUE));
        assertEquals("7XIZYI", codec.toString(Integer.MAX_VALUE - 1));
        assertEquals("7XIZYJ", codec.toString(Integer.MAX_VALUE));
        assertEquals(MAX_DIGIT_PREFIXED_ALPHANUMERIC, codec.toString(Integer.MAX_VALUE));
    }

    @Test
    void printSomeLongs() {
        final Consumer<String> stringPrinter = s -> {
            System.out.println(("                    " + s).substring(s.length()) + " --> " + codec.toLong(s));
        };
        final LongConsumer longPrinter = i -> {
            System.out.println(("                    " + i).substring(("" + i).length()) + " --> " + codec.toString(i));
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
        stringPrinter.accept(MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12);

        longPrinter.accept(AlphanumericLongCodec.MIN_NUMERIC);
        longPrinter.accept(AlphanumericLongCodec.MAX_NUMERIC);
        longPrinter.accept(AlphanumericLongCodec.MIN_NUMERIC - 1);
        longPrinter.accept(AlphanumericLongCodec.MAX_NUMERIC + 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L - 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L - 1);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L);
        longPrinter.accept(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L) + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L));
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L) - 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L) + 1);
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L));
        longPrinter.accept(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L) - 1);
        longPrinter.accept(Long.MIN_VALUE);
        longPrinter.accept(Long.MAX_VALUE);
    }

    @Test
    void someLongs() {
        assertEquals(3519950422753201122L, codec.toLong("00"));
        assertEquals(3519950422753201131L, codec.toLong("09"));
        assertEquals(3519950422753201132L, codec.toLong("0A"));
        assertEquals(3519950422753201157L, codec.toLong("0Z"));
        assertEquals(3519950422753201158L, codec.toLong("000"));
        assertEquals(3519950422753202453L, codec.toLong("0ZZ"));
        assertEquals(3519950422753202454L, codec.toLong("0000"));
        assertEquals(3519950422753249109L, codec.toLong("0ZZZ"));
        assertEquals(3519950422753249110L, codec.toLong("00000"));
        assertEquals(3519950422754928725L, codec.toLong("0ZZZZ"));
        assertEquals(3519950422754928726L, codec.toLong("000000"));
        assertEquals(3519950422754928736L, codec.toLong("00000A"));
        assertEquals(3519950422754928761L, codec.toLong("00000Z"));
        assertEquals(3519950422815394901L, codec.toLong("0ZZZZZ"));
        assertEquals(3519950422815394902L, codec.toLong("0000000"));
        assertEquals(3519950424992177237L, codec.toLong("0ZZZZZZ"));
        assertEquals(3519950424992177238L, codec.toLong("00000000"));
        assertEquals(3519950503356341333L, codec.toLong("0ZZZZZZZ"));
        assertEquals(3519950503356341334L, codec.toLong("000000000"));
        assertEquals(3519953324466248789L, codec.toLong("0ZZZZZZZZ"));
        assertEquals(3519953324466248790L, codec.toLong("0000000000"));
        assertEquals(3520054884422917205L, codec.toLong("0ZZZZZZZZZ"));
        assertEquals(3520054884422917206L, codec.toLong("00000000000"));
        assertEquals(3523711042862980181L, codec.toLong("0ZZZZZZZZZZ"));
        assertEquals(3523711042862980182L, codec.toLong("000000000000"));
        assertEquals(3655332746705247317L, codec.toLong("0ZZZZZZZZZZZ"));
        assertEquals(3655332746705247318L, codec.toLong("1A"));
        assertEquals(3655332746705247343L, codec.toLong("1Z"));
        assertEquals(3655332746705247551L, codec.toLong("9Z"));
        assertEquals(3655332746705247552L, codec.toLong("10A"));
        assertEquals(3655332746705247577L, codec.toLong("10Z"));
        assertEquals(3655333266705247291L, codec.toLong("19999999999Z"));
        assertEquals(3655333006705247292L, codec.toLong("10000000000A"));
        assertEquals(3655333006705247317L, codec.toLong("10000000000Z"));
        assertEquals(3655333006705247318L, codec.toLong("10000000001A"));
        assertEquals(3655333006705247551L, codec.toLong("10000000009Z"));
        assertEquals(3655335346705247291L, codec.toLong("99999999999Z"));
        assertEquals(3655335346705247292L, codec.toLong("1A0"));
        assertEquals(4588592303948750963L, codec.toLong("6ZZZZZZZZZZZ"));
        assertEquals(4588592303948750964L, codec.toLong("7A0000000000"));
        assertEquals(4669027789630136436L, codec.toLong("7W0000000000"));
        assertEquals(4669967944657581201L, codec.toLong("7W9999999999"));
        assertEquals(4873772662273663091L, codec.toLong("9ZZZZZZZZZZZ"));
        assertEquals(4873772662273663092L, codec.toLong("AAAAAAAAAAAAA"));
        assertEquals(7354925535477398992L, codec.toLong("ZZZZZZZZZZZAA"));
        assertEquals(7354925535477399667L, codec.toLong("ZZZZZZZZZZZZZ"));
        assertEquals(7354925535477399668L, codec.toLong("AAAAAAAAAAAA0"));
        assertEquals(8309215102094221427L, codec.toLong("ZZZZZZZZZZZZ9"));
        assertEquals(8309215102094221428L, codec.toLong("AAAAAAAAAAA00"));
        assertEquals(9223372036854775807L, codec.toLong("RZRYMFXOEDX77"));
        assertEquals(Long.MAX_VALUE, codec.toLong("RZRYMFXOEDX77"));
        assertEquals(Long.MAX_VALUE, codec.toLong(MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12));
        assertEquals(-9223372036854775808L, codec.toLong(".RZRYMFXOEDX78"));
        assertEquals(Long.MIN_VALUE, codec.toLong(".RZRYMFXOEDX78"));
        assertEquals(Long.MIN_VALUE, codec.toLong(MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12));

        assertEquals(AlphanumericLongCodec.MIN_NUMERIC, codec.toLong("-9999999999999"));
        assertEquals(AlphanumericLongCodec.MAX_NUMERIC, codec.toLong("9999999999999"));
        assertEquals("-9999999999999", codec.toString(AlphanumericLongCodec.MIN_NUMERIC));
        assertEquals("9999999999999", codec.toString(AlphanumericLongCodec.MAX_NUMERIC));

        assertEquals(".A", codec.toString(AlphanumericLongCodec.MIN_NUMERIC - 1));
        assertEquals("A", codec.toString(AlphanumericLongCodec.MAX_NUMERIC + 1));
        assertEquals("0ZZZZZZZZZZZ", codec.toString(3655332746705247317L));
        assertEquals("1A", codec.toString(3655332746705247317L + 1));
        assertEquals("ZZZZZZZZZZZZ", codec.toString(3519940422753201122L + 10_000_000_000_000L - 1));
        assertEquals("00", codec.toString(3519940422753201122L + 10_000_000_000_000L));
        assertEquals("01", codec.toString(3519940422753201122L + 10_000_000_000_000L + 1));
        assertEquals("0ZZZZZZZZZZZ", codec.toString(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L - 1));
        assertEquals("1A", codec.toString(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L));
        assertEquals("1B", codec.toString(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L + 1));
        assertEquals(".ZZZZZZZZZZZZ", codec.toString(-(3519940422753201122L + 10_000_000_000_000L) + 1));
        assertEquals(".00", codec.toString(-(3519940422753201122L + 10_000_000_000_000L)));
        assertEquals(".01", codec.toString(-(3519940422753201122L + 10_000_000_000_000L) - 1));
        assertEquals(".0ZZZZZZZZZZZ", codec.toString(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L) + 1));
        assertEquals(".1A", codec.toString(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L)));
        assertEquals(".1B", codec.toString(-(3519940422753201122L + 10_000_000_000_000L + 135382323952046196L) - 1));
        assertEquals(".RZRYMFXOEDX78", codec.toString(Long.MIN_VALUE));
        assertEquals(MIN_ALPHANUMERIC_13_WITH_DIGIT_AT_12, codec.toString(Long.MIN_VALUE));
        assertEquals("RZRYMFXOEDX77", codec.toString(Long.MAX_VALUE));
        assertEquals(MAX_ALPHANUMERIC_13_WITH_DIGIT_AT_12, codec.toString(Long.MAX_VALUE));

        assertEquals("0ZZZZZZZZZZZ", codec.toString(3655332746705247317L));
        assertEquals("1A", codec.toString(3655332746705247318L));
        assertEquals("1Z", codec.toString(3655332746705247343L));
        assertEquals("9Z", codec.toString(3655332746705247551L));
        assertEquals("10A", codec.toString(3655332746705247552L));
        assertEquals("10Z", codec.toString(3655332746705247577L));
        assertEquals("19999999999Z", codec.toString(3655333266705247291L));
        assertEquals("10000000000A", codec.toString(3655333006705247292L));
        assertEquals("10000000000Z", codec.toString(3655333006705247317L));
        assertEquals("10000000001A", codec.toString(3655333006705247318L));
        assertEquals("10000000009Z", codec.toString(3655333006705247551L));
        assertEquals("99999999999Z", codec.toString(3655335346705247291L));
        assertEquals("1A0", codec.toString(3655335346705247292L));
        assertEquals("6ZZZZZZZZZZZ", codec.toString(4588592303948750963L));
        assertEquals("7A0000000000", codec.toString(4588592303948750964L));
        assertEquals("7W0000000000", codec.toString(4669027789630136436L));
        assertEquals("7W9999999999", codec.toString(4669967944657581201L));
        assertEquals("9ZZZZZZZZZZZ", codec.toString(4873772662273663091L));
        assertEquals("AAAAAAAAAAAAA", codec.toString(4873772662273663092L));
    }

    @Test
    void randomInts() {
        final int n = 10_000_000;
        final Random random = new Random();
        final StringBuilder builder = new StringBuilder(codec.maxIntLength() + 1);
        long count = 0;
        final long ts = System.nanoTime();
        for (int i = 0; i < n; i++) {
            final int value = random.nextInt();
            testToFrom(codec, value, builder);
            count++;
        }
        final long te = System.nanoTime();
        System.out.println("Tested: " + count + ", " + (float)((te-ts) / (0.0 + count)) + "ns/double-conversion");
    }

    @Test
    void randomLongs() {
        final int n = 10_000_000;
        final Random random = new Random();
        final StringBuilder builder = new StringBuilder(codec.maxLongLength() + 1);
        long count = 0;
        final long ts = System.nanoTime();
        for (int i = 0; i < n; i++) {
            final long value = random.nextLong();
            testToFrom(codec, value, builder);
            count++;
        }
        final long te = System.nanoTime();
        System.out.println("Tested: " + count + ", " + (float)((te-ts) / (0.0 + count)) + "ns/double-conversion");
    }

    @Test
    @Disabled//exhaustive test, runs for quite some time (approx. 4-5 min, ~60ns per double-conversion).
    void allInts() {
        final ShortStringCodec theCodec = codec;
        final long printInterval = 10_000_000;
        final StringBuilder builder = new StringBuilder(theCodec.maxIntLength() + 1);
        long count = 0;
        long printAt = printInterval;
        final long ts = System.nanoTime();
        //noinspection OverflowingLoopIndex
        for (int i = 0; i >= 0; i++) {
            testToFrom(theCodec, i, builder);
            testToFrom(theCodec, -(i+1), builder);
            count += 2;
            if (count >= printAt) {
                System.out.println("Tested: " + count);
                printAt += printInterval;
            }
        }
        final long te = System.nanoTime();
        System.out.println("Tested: " + count + ", " + (float)((te-ts) / (0.0 + count)) + "ns/double-conversion");
    }

}