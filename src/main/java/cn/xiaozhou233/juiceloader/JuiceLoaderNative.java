package cn.xiaozhou233.juiceloader;

public class JuiceLoaderNative {
    // Init jni/jvmti, and register events
    public static native boolean init();

    // Add jar to Bootstrap ClassLoader
    public static native boolean injectJar(String jarPath);

    // Redefine class
    // Notice: classname is the full name of the class, e.g. "java/lang/String"
    public static native boolean redefineClass(Class<?> clazz, byte[] classBytes, int length);
    public static native boolean redefineClassByName(String className, byte[] classBytes, int length);

    // Get Classes
    public static native Class<?>[] getLoadedClasses();

    // Get ClassBytes
    public static native byte[] getClassBytes(Class<?> clazz);
    public static native byte[] getClassBytesByName(String className);
}
