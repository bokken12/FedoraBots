package common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Random utility stuff
 */
public class Util {
    public static String toString(byte[] b) {
        final AtomicInteger i = new AtomicInteger(); // It doesn't need to be atomic but it works...
        return Stream.generate(() -> String.valueOf(b[i.getAndIncrement()] & 0xFF))
            .limit(b.length)
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
