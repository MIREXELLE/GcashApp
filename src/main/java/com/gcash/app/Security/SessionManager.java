package com.gcash.app.Security;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import com.gcash.app.util.AppConfig;

public class SessionManager {
    private static final Map<Integer, Session> sessions = new HashMap<>();

    public static class Session {
        private final int userId;
        private LocalDateTime lastActivity;

        public Session(int userId) {
            this.userId = userId;
            this.lastActivity = LocalDateTime.now();
        }

        public int getUserId() {
            return userId;
        }

        public LocalDateTime getLastActivity() {
            return lastActivity;
        }

        public void updateActivity() {
            this.lastActivity = LocalDateTime.now();
        }

        public boolean isExpired() {
            return lastActivity.plusMinutes(AppConfig.SESSION_TIMEOUT_MINUTES)
                    .isBefore(LocalDateTime.now());
        }
    }

    public static boolean createSession(int userId) {
        if (userId <= 0) {
            return false;
        }

        sessions.put(userId, new Session(userId));
        return true;
    }

    public static boolean isSessionValid(int userId) {
        Session session = sessions.get(userId);
        if (session == null) {
            return false;
        }

        if (session.isExpired()) {
            sessions.remove(userId);
            return false;
        }

        session.updateActivity();
        return true;
    }

    public static boolean invalidateSession(int userId) {
        return sessions.remove(userId) != null;
    }
}