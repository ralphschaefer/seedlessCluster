package my.clusterDemp;

import java.lang.reflect.Field;

public class KRWarning {
    public static void disable() {
        try { // Turn off illegal access log messages.
            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            Object unsafe = unsafeField.get(null);
            Long offset = (Long)unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, loggerField);
            unsafeClass.getMethod("putObjectVolatile", Object.class, long.class, Object.class) //
                .invoke(unsafe, loggerClass, offset, null);
        } catch (Throwable ex) {
            // WARN("Failed to disable illegal access warning:", ex);
        }
    }
}
