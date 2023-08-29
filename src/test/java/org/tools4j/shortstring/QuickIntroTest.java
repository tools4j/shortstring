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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuickIntroTest {

    final ShortStringCodec codec = ShortString.ALPHANUMERIC;

    @Test
    void fromStringAndBack() {
        int zero = codec.toInt("0");
        int positive = codec.toInt("123456");
        int negative = codec.toInt("-987654");
        int hello = codec.toInt("HELLO");
        int world = codec.toInt("WORLD");
        int agent = codec.toInt("007");
        long computational = codec.toLong("COMPUTATIONAL");

        assertEquals("123456", codec.toString(positive));
        assertEquals("HELLO", codec.toString(hello));
        assertEquals("WORLD", codec.toString(world));
        assertEquals("007", codec.toString(agent));
        assertEquals("COMPUTATIONAL", codec.toString(computational));

        //for numeric values also
        assertEquals("0", String.valueOf(zero));
        assertEquals("123456", String.valueOf(positive));
        assertEquals("-987654", String.valueOf(negative));

        //negatives for string values
        assertEquals(".HELLO", codec.toString(-hello));
        assertEquals(".WORLD", codec.toString(-world));
        assertEquals(".007", codec.toString(-agent));
        assertEquals(".COMPUTATIONAL", codec.toString(-computational));
    }

    @Test
    void zeroAllocation() {
        int hello = codec.toInt("HELLO");
        int world = codec.toInt("WORLD");
        long computational = codec.toLong("COMPUTATIONAL");

        //conversion to string re-using a string builder
        StringBuilder string = new StringBuilder(codec.maxLongLength() + 1);

        string.setLength(0);
        codec.toString(computational, string);
        assertTrue("COMPUTATIONAL".contentEquals(string));

        string.setLength(0);
        codec.toString(hello, string);
        string.append(' ');
        codec.toString(world, string);
        assertTrue("HELLO WORLD".contentEquals(string));
    }
}
