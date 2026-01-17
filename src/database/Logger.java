package database;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Logger {
    private static final String LOG_FILE = "files/login_activity.txt";
    private static final String SESSION_LOG_FILE = "files/session_log.txt";

    // Store active login sessions
    private static final Map<String, LocalDateTime> activeSessions = new HashMap<>();

    // Generic logging (already present)
    public static void log(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(LocalDateTime.now() + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to login log: " + e.getMessage());
        }
    }

    // Log login time
    public static void logLogin(String username) {
        LocalDateTime loginTime = LocalDateTime.now();
        activeSessions.put(username, loginTime);
        log("User (" + username + ") logged in at " + loginTime);
    }

    // Log logout time and session duration
    public static void logLogout(String username) {
        LocalDateTime logoutTime = LocalDateTime.now();
        LocalDateTime loginTime = activeSessions.get(username);

        if (loginTime != null) {
            Duration session = Duration.between(loginTime, logoutTime);
            long minutes = session.toMinutes();

            String sessionLog = "User: " + username +
                    " | Login: " + loginTime +
                    " | Logout: " + logoutTime +
                    " | Session Duration: " + minutes + " minute(s)";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("files/session_log.txt", true))) {
                writer.write(sessionLog);
                writer.newLine();
            } catch (IOException e) {
                System.err.println("Failed to write session log: " + e.getMessage());
            }

            log("User '" + username + "' logged out at " + logoutTime + " (Session: " + minutes + " min)");
            activeSessions.remove(username);
        } else {
            log("Logout attempted for user '" + username + "' but no login time was found.");
        }
    }

}
