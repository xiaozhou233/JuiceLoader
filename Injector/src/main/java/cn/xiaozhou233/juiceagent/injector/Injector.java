package cn.xiaozhou233.juiceagent.injector;

import java.io.*;
import java.util.Scanner;

public class Injector {
    private static boolean isLoadedLibrary = false;
    public static void main(String[] args) {
        if (!isLoadedLibrary) {
            loadLibrary();
            isLoadedLibrary = true;
        }
        Scanner scanner = new Scanner(System.in);

        // InjectorNative injectorNative = new InjectorNative();
        executeJps();

        System.out.println("Input pid: ");
        int pid = scanner.nextInt();

        File libAgentPath = new File("./libagent.dll");
        if (!libAgentPath.exists()) {
            System.out.println("libagent.dll not found, input libagent.dll path: ");
            String libAgentPathStr = scanner.next();
            libAgentPath = new File(libAgentPathStr);
            if (!libAgentPath.exists()) {
                throw new RuntimeException("libagent.dll not found");
            }
        }

        System.out.println("Input config dir (input !c to use libagent dir): ");
        String configDir = scanner.next();
        if (configDir.equals("!c")) {
            //injectorNative.inject(pid, libAgentPath.getAbsolutePath(), libAgentPath.getParent());
            inject(pid, libAgentPath.getAbsolutePath(), libAgentPath.getAbsolutePath().replace("libagent.dll", ""));
        } else {
            //injectorNative.inject(pid, libAgentPath.getAbsolutePath(), configDir);
            inject(pid, libAgentPath.getAbsolutePath(), configDir);
        }
    }

    public static void inject(int pid, String libAgentPath, String configDir) {
        try {
            if (!isLoadedLibrary) {
                loadLibrary();
                isLoadedLibrary = true;
            }
            InjectorNative injectorNative = new InjectorNative();

            injectorNative.inject(pid, libAgentPath, configDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(int pid, String libAgentPath) {
        try {
            if (!isLoadedLibrary) {
                loadLibrary();
                isLoadedLibrary = true;
            }
            InjectorNative injectorNative = new InjectorNative();

            injectorNative.inject(pid, libAgentPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void executeJps() {
        System.out.println("======== Jps ========");
        try {
            ProcessBuilder pb = new ProcessBuilder("jps", "-l");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"))) { // Windows 控制台一般为GBK编码
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("======== Jps ========");
    }

    private static void loadLibrary() {
        if (!isLoadedLibrary) {
            try (InputStream in = Injector.class.getResourceAsStream("/libinjector.dll")) {
                if (in == null) throw new RuntimeException("DLL not found in jar");

                File tempDll = File.createTempFile("libinjector", ".dll");
                tempDll.deleteOnExit();

                try (FileOutputStream out = new FileOutputStream(tempDll)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                System.load(tempDll.getAbsolutePath());
                isLoadedLibrary = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}