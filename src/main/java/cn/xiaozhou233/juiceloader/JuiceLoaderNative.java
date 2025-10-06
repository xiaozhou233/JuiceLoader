package cn.xiaozhou233.juiceloader;

public class JuiceLoaderNative {
    // Init jni/jvmti, and register events
    public native boolean init();

    // Add jar to Bootstrap ClassLoader
    public native boolean injectJar(String jarPath);

    // Redefine class
    // Notice: classname is the full name of the class, e.g. "java/lang/String"
    public native boolean redefineClass(Class<?> clazz, byte[] classBytes, int length);
    public native boolean redefineClass(String className, byte[] classBytes, int length);
}
