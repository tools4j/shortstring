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

import static java.util.Objects.requireNonNull;

public enum ShortString implements ShortStringCodec {
    NUMERIC(new NumericCodec()),
    HEX(new HexCodec()),
    /**
     * Codec translating short alphanumeric strings to integers and back. For conversion to int (long), strings up to
     * length 6 (13) are supported, with an additional leading sign character for negative values.
     * <p><br>
     * For a detailed specification see {@link AlphanumericCodec}.
     *
     * @see AlphanumericCodec
     * @see AlphanumericIntCodec
     * @see AlphanumericLongCodec
     */
    ALPHANUMERIC(new AlphanumericCodec());

    private final ShortStringCodec codec;
    ShortString(final ShortStringCodec codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public final int maxIntLength() {
        return codec.maxIntLength();
    }

    @Override
    public final int maxLongLength() {
        return codec.maxLongLength();
    }

    @Override
    public final int toInt(final CharSequence value) {
        return codec.toInt(value);
    }

    @Override
    public final long toLong(final CharSequence value) {
        return codec.toLong(value);
    }

    @Override
    public final StringBuilder toString(final int value, final StringBuilder dst) {
        return codec.toString(value, dst);
    }

    @Override
    public final StringBuilder toString(final long value, final StringBuilder dst) {
        return codec.toString(value, dst);
    }

    @Override
    public final boolean startsWithSignChar(final CharSequence value) {
        return codec.startsWithSignChar(value);
    }

    @Override
    public final boolean isConvertibleToInt(final CharSequence value) {
        return codec.isConvertibleToInt(value);
    }

    @Override
    public final boolean isConvertibleToLong(final CharSequence value) {
        return codec.isConvertibleToLong(value);
    }
}
