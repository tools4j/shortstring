[![Continuous Integration](https://github.com/tools4j/shortstring/workflows/Continuous%20Integration/badge.svg)](https://github.com/tools4j/shortstring/actions?query=workflow%3A%22Continuous+Integration%22)
[![Maven Central](https://img.shields.io/maven-central/v/org.tools4j/shortstring.svg)](https://search.maven.org/search?namespace=org.tools4j&name=shortstring)
[![Javadocs](http://www.javadoc.io/badge/org.tools4j/shortstring.svg)](http://www.javadoc.io/doc/org.tools4j/shortstring)
## shortstring
The tools4j shortstring library allows for efficient conversion of short enough strings to and from integers and longs.

Short strings encoded as integers can for instance be used in the following situations:
* Caching strings in memory with a reduced memory footprint and without the need to allocate objects
* A more dynamic alternative to enums, for instance to encode a currency pair or a stock ticker
* A user-friendly alternative to integer identifiers, with the ability to be printed or logged in a human-readable format

The shortstring library avoids object allocations which makes it suitable for latency sensitive applications.   

### Quick Introduction

Sample use of alphanumeric codec:

```java
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
        int plane = codec.toInt("A380");
        int form = codec.toInt("1040ES");
        long computational = codec.toLong("COMPUTATIONAL");

        assertEquals("0", codec.toString(zero));
        assertEquals("123456", codec.toString(positive));
        assertEquals("-987654", codec.toString(negative));
        assertEquals("HELLO", codec.toString(hello));
        assertEquals("WORLD", codec.toString(world));
        assertEquals("007", codec.toString(agent));
        assertEquals("A380", codec.toString(plane));
        assertEquals("1040ES", codec.toString(form));
        assertEquals("COMPUTATIONAL", codec.toString(computational));

        //for numeric values we have also
        assertEquals("0", String.valueOf(zero));
        assertEquals("123456", String.valueOf(positive));
        assertEquals("-987654", String.valueOf(negative));

        //negatives for non-numeric values
        assertEquals(".HELLO", codec.toString(-hello));
        assertEquals(".WORLD", codec.toString(-world));
        assertEquals(".007", codec.toString(-agent));
        assertEquals(".A380", codec.toString(-plane));
        assertEquals(".1040ES", codec.toString(-form));
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
```

This [code example](https://github.com/tools4j/shortstring/tree/master/src/test/java/org/tools4j/shortstring/QuickIntroTest.java) can be found in [tests](https://github.com/tools4j/shortstring/tree/master/src/test/java/org/tools4j/shortstring/).

For more functions and API documentation please consider the [Javadoc API](http://www.javadoc.io/doc/org.tools4j/shortstring).

### Maven/Gradle

#### Maven
```xml
<dependency>
    <groupId>org.tools4j</groupId>
    <artifactId>shortstring</artifactId>
    <version>1.1</version>
</dependency>
```

#### Gradle
```
api "org.tools4j:shortstring:1.1'
```
### Download
You can download binaries, sources and javadoc from maven central:
* [shortstring download](https://search.maven.org/search?namespace=org.tools4j&name=shortstring)

### Javadoc API
The API documentation of the latest release can be found here:
* [Javadoc API](http://www.javadoc.io/doc/org.tools4j/shortstring)