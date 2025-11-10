package cn.xiaozhou233.juiceagent.injector;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Injector {
    private static boolean isLoadedLibrary = false;
    private static InjectorNative injectorNative = null;

    static {
        try {
            loadLibrary();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library in static initializer", e);
        }
    }

    public static void main(String[] args) {
        System.out.println("[INFO] JuiceAgent Injector");

        int pid = -1;

        StringBuilder sb = new StringBuilder();
        // Use Attach API
        try {
            for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
                sb.append(String.format("[%s] %s%n", vm.id(), vm.displayName()));

                // REMOVE BEFORE RELEASE: auto-detect a special debug target named "loop"
                if ("loop".equals(vm.displayName())) {
                    try {
                        pid = Integer.parseInt(vm.id());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Use JNI to find windows by title (fuzzy)
        sb.append("\n");
        try {
            String[] titles = {"Minecraft", "minecraft", "1.8.9", "Badlion", "Lunar"};
            String[] ignoreTitles = {"Badlion Client", "Badlion Chat"};
            ArrayList<Integer> pids = new ArrayList<>();
            for (String title : titles) {
                InjectorNative.WindowInfo[] list = InjectorNative.findWindowsByTitle(title);
                if (list == null || list.length == 0) {
                    System.out.println("[WARN] No matching windows found.");
                } else {
                    System.out.printf("[INFO] Found %d matching windows:%n", list.length);
                    for (InjectorNative.WindowInfo w : list) {
                        if (pids.contains(w.pid))
                            continue;
                        if (Arrays.stream(ignoreTitles).anyMatch(w.title::contains))
                            continue;
                        pids.add(w.pid);
                        sb.append(String.format("[%s] %s%n", w.pid, w.title));
                    }
                }
            }
        } catch (UnsatisfiedLinkError ule) {
            throw new RuntimeException("Native method findWindowsByTitle not available", ule);
        } catch (Exception ex) {
            System.out.println("[ERROR] Error while calling native findWindowsByTitle: " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("=======================");
        System.out.println(sb);
        System.out.println("=======================");

        // If not auto-detected, ask user for PID
        if (pid == -1) {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("[INPUT] PID: ");
                pid = scanner.nextInt();
            } catch (Exception e) {
                throw new RuntimeException("Invalid PID input", e);
            }
        } else {
            System.out.printf("[INFO] Auto detected PID: %d%n", pid);
        }

        // inject using two-arg overload (agent path only) or three-arg
        File agentFile = new File("libagent.dll");
        String agentPath = agentFile.getAbsolutePath();
        String configDir = new File(".").getAbsolutePath();

        inject(pid, agentPath, configDir);
    }

    /**
     * Wrapper that ensures native library is loaded and calls instance method.
     */
    public static void inject(int pid, String libAgentPath, String configDir) {
        ensureLibraryLoaded();
        try {
            // call instance method on injectorNative
            injectorNative.inject(pid, libAgentPath, configDir);
            System.out.printf("[INFO] Injected %s into PID=%d (configDir=%s)%n", libAgentPath, pid, configDir);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed for pid=" + pid + ", lib=" + libAgentPath, e);
        }
    }

    /**
     * Overloaded inject (two args).
     */
    public static void inject(int pid, String libAgentPath) {
        ensureLibraryLoaded();
        try {
            injectorNative.inject(pid, libAgentPath);
            System.out.printf("[INFO] Injected %s into PID=%d%n", libAgentPath, pid);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed for pid=" + pid + ", lib=" + libAgentPath, e);
        }
    }

    /**
     * Print jps -l output. Uses system default charset (avoids hard-coded GBK).
     */
    public static void executeJps() {
        System.out.println("======== Jps ========");
        ProcessBuilder pb = new ProcessBuilder("jps", "-l");
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute jps", e);
        }
        System.out.println("======== Jps ========");
    }

    /**
     * Ensure native library is loaded and injectorNative instance created.
     */
    private static synchronized void ensureLibraryLoaded() {
        if (!isLoadedLibrary) {
            loadLibrary();
        } else if (injectorNative == null) {
            // if library loaded but instance not created, create it
            injectorNative = new InjectorNative();
        }
    }

    /**
     * Load native library from runtime directory.
     * This method is safe to call multiple times; it will only load once.
     *
     * Comments: avoid InputStream.transferTo() for compatibility with older Java 8.
     */
    private static synchronized void loadLibrary() {
        if (isLoadedLibrary) return;

        try {
            // Get runtime directory
            String runtimeDir = System.getProperty("user.dir");

            File libFile = new File(runtimeDir, "libinjector.dll");
            if (!libFile.exists()) {
                throw new FileNotFoundException("Native library not found: " + libFile.getAbsolutePath());
            }
            // Load native library from temporary file
            System.load(libFile.getAbsolutePath());

            // instantiate native wrapper if needed
            injectorNative = new InjectorNative();

            isLoadedLibrary = true;
            System.out.printf("[INFO] Loaded native library: %s%n", libFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library from resources", e);
        } catch (UnsatisfiedLinkError ule) {
            throw new RuntimeException("Failed to load native library (UnsatisfiedLinkError)", ule);
        }
    }

    /**
     * Utility: try to detect a target Java process by substring of displayName.
     * Not used directly in current main (kept for reuse).
     */
    private static int detectTargetProcess(String matchSubstring) {
        for (VirtualMachineDescriptor vm : VirtualMachine.list()) {
            if (vm.displayName() != null && vm.displayName().contains(matchSubstring)) {
                try {
                    return Integer.parseInt(vm.id());
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return -1;
    }
}
