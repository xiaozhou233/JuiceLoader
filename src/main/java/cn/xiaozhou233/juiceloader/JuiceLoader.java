package cn.xiaozhou233.juiceloader;

@SuppressWarnings("unused")
public class JuiceLoader {
    // Init jni/jvmti, and register events
    public static native boolean init();

    // Add jar to Bootstrap ClassLoader
    public static native boolean injectJar(String jarPath);
    public static native boolean AddToBootstrapClassLoaderSearch(String jarPath);
    public static native boolean AddToSystemClassLoaderSearch(String jarPath);

    // Define class
    public native static Class<?> defineClass(ClassLoader loader, byte[] bytes);

    // Redefine class
    // Notice: classname is the full name of the class, e.g. "java/lang/String"
    public static native boolean redefineClass(Class<?> clazz, byte[] classBytes, int length);
    public static native boolean redefineClassByName(String className, byte[] classBytes, int length);

    // Get Classes
    public static native Class<?>[] getLoadedClasses();

    // Get ClassBytes
    public static native byte[] getClassBytes(Class<?> clazz);
    public static native byte[] getClassBytesByName(String className);

    // Retransform
    public static native boolean retransformClass(Class<?> clazz, byte[] classBytes, int length);
    public static native boolean retransformClassByName(String className, byte[] classBytes, int length);

    // Get Class
    public static native Class<?> getClassByName(String className);

    // Thread/Inject
    public static native Thread nativeGetThreadByName(String name);
    public static native ClassLoader nativeInjectJarToThread(Thread thread, String jarPath);
    
}
