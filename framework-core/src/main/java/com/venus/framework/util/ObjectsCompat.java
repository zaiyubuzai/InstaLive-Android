package com.venus.framework.util;

/**
 * java.util.Objects wrapper. 19以上可以直接用Objects
 * <p>
 * Created by ywu on 16/9/7.
 */
public final class ObjectsCompat {

    private ObjectsCompat() {
    }

    /**
     * Returns {@code o} if non-null, or throws {@code NullPointerException}.
     */
    public static <T> T requireNonNull(T o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return o;
    }

    /**
     * Returns {@code o} if non-null, or throws {@code NullPointerException}
     * with the given detail message.
     */
    public static <T> T requireNonNull(T o, String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
        return o;
    }
}
