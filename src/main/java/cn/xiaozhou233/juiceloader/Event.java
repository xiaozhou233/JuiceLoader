package cn.xiaozhou233.juiceloader;

public interface Event {
    byte[] onClassFileLoad(Class<?> clazz, ClassLoader loader, String name, Object pd, byte[] bytes);
}
