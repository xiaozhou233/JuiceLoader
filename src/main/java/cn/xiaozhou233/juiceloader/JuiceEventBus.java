package cn.xiaozhou233.juiceloader;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JuiceEventBus {
    private static final List<Event> listeners = new CopyOnWriteArrayList<>();

    public static byte[] postClassFileLoadHook(
            Class<?> beingTransformedClass,
            ClassLoader classLoader,
            String className,
            Object protectionDomain,
            int bytesLength,
            byte[] bytes) {

        byte[] current = bytes;
        for (Event listener : listeners) {
            try {
                byte[] res = listener.onClassFileLoad(beingTransformedClass, classLoader, className, protectionDomain, current);
                if (res != null) {
                    // return a defensive copy to avoid native-side alias issues
                    current = Arrays.copyOf(res, res.length);
                }
            } catch (Throwable t) {
                System.err.println("[Java EventBus] listener error: " + t);
            }
        }
        return current;
    }


    public static void register(Event listener) {
        System.out.println("[Java EventBus] Registering listener " + listener.getClass());
        listeners.add(listener);
    }

    public static void unregister(Event listener) {
        System.out.println("[Java EventBus] Unregistering listener " + listener.getClass());
        listeners.remove(listener);
    }
}
