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

/**
 * Codec to translate between the string and integer representation of short character sequences.
 */
public interface ShortStringCodec {
    /**
     * Returns the maximum length of a unsigned int value represented as a string with this codec (+1 for a signed int
     * values).
     * @return the maximum string representation length of an unsigned int value
     */
    int maxIntLength();

    /**
     * Returns the maximum length of a unsigned long value represented as a string with this codec (+1 for a signed long
     * values).
     * @return the maximum string representation length of an unsigned long value
     */
    int maxLongLength();

    /**
     * Converts the provided character sequence to an integer value.
     * @param value the value to convert to an int
     * @return the integer representation of the value
     * @throws IllegalArgumentException if the provided value is not a valid int string for this codec
     * @throws NullPointerException if value is null
     * @see #isConvertibleToInt(CharSequence)
     */
    int toInt(CharSequence value);
    
    /**
     * Converts the provided character sequence to a long value.
     * @param value the value to convert to an long
     * @return the long representation of the value
     * @throws IllegalArgumentException if the provided value is not a valid long string for this codec
     * @throws NullPointerException if value is null
     * @see #isConvertibleToLong(CharSequence)
     */
    long toLong(CharSequence value);

    /**
     * Converts the provided value to its string representation and appends it to {@code dst}.
     * @param value the value to convert
     * @param dst the destination string builder to append to
     * @return the destination string builder
     * @throws NullPointerException if dst is null
     */
    StringBuilder toString(int value, StringBuilder dst);

    /**
     * Converts the provided value to its string representation and appends it to {@code dst}.
     * @param value the value to convert
     * @param dst the destination string builder to append to
     * @return the destination string builder
     * @throws NullPointerException if dst is null
     */
    StringBuilder toString(long value, StringBuilder dst);

    /**
     * Converts the provided value to its string representation and returns it.
     * <p>
     * <b>NOTE: </b> Result and temporary objects are allocated by this method.
     *
     * @param value the value to convert
     * @return the string representation of the value
     */
    default String toString(final int value) {
        return toString(value, new StringBuilder(maxIntLength())).toString();
    }

    /**
     * Converts the provided value to its string representation and returns it.
     * <p>
     * <b>NOTE: </b> Result and temporary objects are allocated by this method.
     *
     * @param value the value to convert
     * @return the string representation of the value
     */
    default String toString(final long value) {
        return toString(value, new StringBuilder(maxIntLength())).toString();
    }

    /**
     * Returns true if the provided character sequence is non-empty and starts with a sign character. Other characters
     * are not parsed or validated.
     *
     * @param value the value to inspect
     * @return true if non-empty and first character is a =sign character
     * @throws NullPointerException if value is null
     */
    boolean startsWithSignChar(CharSequence value);

    /**
     * Returns true if the provided character sequence is a valid representation of an int value for this codec.  Valid
     * sequences can be safely converted without causing an exception.

     * @param value the value to check for convertibility
     * @return true if non-null and convertible to an int by this codec
     */
    boolean isConvertibleToInt(CharSequence value);

    /**
     * Returns true if the provided character sequence is a valid representation of an int value for this codec.  Valid
     * sequences can be safely converted without causing an exception.

     * @param value the value to check for convertibility
     * @return true if non-null and convertible to an int by this codec
     */
    boolean isConvertibleToLong(CharSequence value);
}
