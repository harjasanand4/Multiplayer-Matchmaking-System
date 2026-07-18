package com.game.auth;

import java.util.ArrayList;
import java.util.List;

/**
 * This class simulates an audit logger that keeps track of important user actions
 * like login, password changes, and account deletions.
 *
 * In a real-world application, this logger would write these entries to a secure
 * audit database that can be reviewed by system administrators for security purposes.
 *
 * For this course project, we simulate the audit logging by saving logs in memory,
 * and optionally printing them to the console using showLogs() for demonstration.
 * @author: Boya Liu
 * @email: boya.liu@ucalgary.ca
 */
public class AuditLogger {

    // A static list to temporarily store all audit log entries in memory.
    // In a real system, these would be written to a permanent audit database.
    private static List<String> auditLogs = new ArrayList<>();

    /**
     * This method logs an audit event.
     * Each time a sensitive user action occurs (such as login, registration,
     * password change, or account deletion), this method should be called
     * with a detailed message describing the event.
     *
     * In real system this would write to a real audit database.
     *
     * @param message The message describing the user action.
     */
    public static void log(String message) {
        // Get the current system time as a timestamp
        String timestamp = java.time.LocalDateTime.now().toString();

        // Combine timestamp and message into a formatted log entry
        String logEntry = "[AUDIT] " + timestamp + " | " + message;

        // Print the message to console for demonstration purposes (can be removed)
        System.out.println(logEntry); // For demo purposes only

        // Store the log message in the in-memory list (simulating database write)
        auditLogs.add(logEntry);
    }

    /**
     * This method displays all the recorded audit logs to the console.
     * It is mainly used for demonstration and debugging purposes in this project.
     *
     * In real system this would retrieve logs from a secure audit database.
     */
    public static void showLogs() {
        System.out.println("==== Audit Logs Start ====");

        // Check if any logs exist
        if (auditLogs.isEmpty()) {
            System.out.println("No audit logs recorded yet.");
        } else {
            for (String log : auditLogs) {
                System.out.println(log);
            }
        }

        System.out.println("==== Audit Logs End ====");
    }
}
