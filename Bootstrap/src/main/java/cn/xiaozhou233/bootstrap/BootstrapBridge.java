package cn.xiaozhou233.bootstrap;

public final class BootstrapBridge {
    private static volatile LoaderBridge provider;

    public static void setProvider(LoaderBridge p) {
        provider = p;
    }

    public static LoaderBridge getProvider() {
        return provider;
    }

    private BootstrapBridge() { }
}
