package nl.saxion.ptbc.frog;

import java.io.IOException;

public class StartFrogApp {

    /**
     * Starts the external Frog application by launching the executable located
     * at "resources/frog/winfrog/TheFrog.exe".
     * <p>
     * This method uses a ProcessBuilder to start the Frog executable as a separate
     * process. If the executable cannot be started due to an IOException, an error
     * message is printed to the standard error output.
     * <p>
     * Note: The path to the executable is relative and should be valid at runtime.
     */
    public static void startFrogApp() {
        new Thread(() -> {
            try {
                String frogApp = "resources/frog/winfrog/TheFrog.exe";
                ProcessBuilder frogProcess = new ProcessBuilder(frogApp);
                frogProcess.start();
                System.out.println("FrogApp started successfully");
            } catch (IOException e) {
                System.err.println("Error starting FrogApp: " + e.getMessage());
            }
        }).start();
    }
}
