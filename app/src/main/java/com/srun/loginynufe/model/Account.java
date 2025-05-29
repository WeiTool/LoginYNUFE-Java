package com.srun.loginynufe.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.srun.loginynufe.data.ListConverter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 用户账户实体类，对应数据库中的"accounts"表
 * 用于存储用户登录凭证、区域信息、登录状态及操作日志等
 */
@Entity(tableName = "accounts")
@TypeConverters(ListConverter.class)
public class Account {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "encrypted_password")
    private String password;

    @ColumnInfo(name = "region")
    private String region;

    @ColumnInfo(name = "client_ip")
    private String clientIp;

    @ColumnInfo(name = "is_logged_in")
    private boolean isLoggedIn;

    @ColumnInfo(name = "online_devices")
    private int onlineDevices = 0;

    @ColumnInfo(name = "logs")
    private List<String> logs = new ArrayList<>();

    public Account() {
    }

    @Ignore
    public Account(String username, String password, String region, int id) {
        this.username = username;
        this.password = password;
        this.region = region;
        this.id = id;
    }

    @Ignore
    public Account(String username, String password, String region) {
        this.username = username;
        this.password = password;
        this.region = region;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
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

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public List<String> getLogs() {
        return logs;
    }

    public int getOnlineDevices() {
        return onlineDevices;
    }

    public void setOnlineDevices(int onlineDevices) {
        this.onlineDevices = onlineDevices;
    }

    /**
     * 添加登录操作日志
     *
     * @param result 包含登录响应参数的Map，包含success、message等字段
     */
    public void addLoginLog(Map<String, Object> result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder log = new StringBuilder("=== 登录记录 ===\n")
                .append(sdf.format(new Date())).append("\n");

        for (Map.Entry<String, Object> entry : result.entrySet()) {
            log.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        this.logs.add(log.toString());
    }

    /**
     * 添加登出操作日志
     *
     * @param result 包含登出响应参数的Map，包含success、message等字段
     */
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