package com.srun.loginynufe.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Account {
    private final String username; // 学号+区域（如202305006401@ctc）
    private final String password;
    private final String region;
    private String clientIp;
    private boolean isLoggedIn;
    private List<String> logs = new ArrayList<>();

    public Account(String username, String password, String region) {
        this.username = username;
        this.password = password;
        this.region = region;
        this.isLoggedIn = false;
    }

    // Getters 和 Setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRegion() {
        return region;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void addLoginLog(Map<String, Object> result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder log = new StringBuilder("=== 登录记录 ===\n")
                .append(sdf.format(new Date())).append("\n");

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            log.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        this.logs.add(log.toString());
    }

    public void addLogoutLog(Map<String, Object> result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder log = new StringBuilder("=== 登出记录 ===\n")
                .append(sdf.format(new Date())).append("\n");

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            log.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        this.logs.add(log.toString());
    }
}