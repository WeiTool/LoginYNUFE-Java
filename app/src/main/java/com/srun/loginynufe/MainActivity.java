package com.srun.loginynufe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.srun.loginynufe.adapter.AccountAdapter;
import com.srun.loginynufe.data.AppDatabase;
import com.srun.loginynufe.data.AccountDao;
import com.srun.loginynufe.dialog.AccountDialog;
import com.srun.loginynufe.encryption.LoginLogout;
import com.srun.loginynufe.model.Account;
import com.srun.loginynufe.adapter.LogsAdapter;
import com.srun.loginynufe.utils.AppExecutors;
import com.srun.loginynufe.utils.VersionChecker;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AccountAdapter.OnItemClickListener {
    private AccountAdapter adapter;
    private AccountDao accountDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化 Room 数据库
        AppDatabase db = AppDatabase.getInstance(this);
        accountDao = db.accountDao();

        // 初始化 RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器并提交数据
        adapter = new AccountAdapter(this);
        recyclerView.setAdapter(adapter);

        // 观察数据库变化
        accountDao.getAll().observe(this, accounts -> adapter.submitList(accounts));

        // FAB 添加账户
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddAccountDialog());

        // 删除所有账户
        com.google.android.material.button.MaterialButton btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(v ->
                showDeleteConfirmDialog("确定要删除所有账户吗？", this::deleteAllAccounts)
        );

        VersionChecker.checkNewVersion(this);
    }

    private void showAddAccountDialog() {
        AccountDialog dialog = new AccountDialog(
                this,
                newAccount -> AppExecutors.get().diskIO().execute(() -> {
                    try {
                        accountDao.insert(newAccount);
                        AppExecutors.get().mainThread().execute(() -> Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show());
                    } catch (Exception e) {
                        AppExecutors.get().mainThread().execute(() ->
                                Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                }),
                false,
                null
        );
        dialog.show();
    }

    // ================== 接口方法实现 ==================
    @Override
    public void onItemClick(Account account) {
        AccountDialog dialog = new AccountDialog(
                this,
                updatedAccount -> AppExecutors.get().diskIO().execute(() -> accountDao.update(updatedAccount)),
                true,
                account
        );
        dialog.show();
    }

    @Override
    public void onLoginClick(Account account) {
        LoginLogout.performLogin(account, result -> {
            account.addLoginLog(result);
            AppExecutors.get().diskIO().execute(() -> {
                accountDao.update(account);
                AppExecutors.get().mainThread().execute(() -> showToast(result, "登录成功", "登录失败"));
            });

            AppExecutors.get().networkIO().execute(() -> {
                Map<String, Object> userInfo = LoginLogout.getRadUserInfo();

                if (userInfo != null && userInfo.containsKey("online_device_total")) {
                    Object value = userInfo.get("online_device_total");
                    if (value instanceof String) {
                        String onlineTotal = (String) value;
                        try {
                            int onlineDevices = Integer.parseInt(onlineTotal);
                            account.setOnlineDevices(onlineDevices);
                        } catch (NumberFormatException e) {
                            account.setOnlineDevices(0);
                            AppExecutors.get().mainThread().execute(() ->
                                    Toast.makeText(
                                            MainActivity.this,
                                            "在线设备数解析失败",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    } else {
                        account.setOnlineDevices(0);
                    }
                } else {
                    account.setOnlineDevices(0);
                }

                // 更新数据库
                AppExecutors.get().diskIO().execute(() -> accountDao.update(account));
            });
        });
    }

    @Override
    public void onLogoutClick(Account account) {
        LoginLogout.performLogout(account, result -> {
            account.addLogoutLog(result);
            account.setClientIp("");
            account.setOnlineDevices(Math.max(account.getOnlineDevices() - 1, 0));

            AppExecutors.get().diskIO().execute(() -> {
                accountDao.update(account);
                AppExecutors.get().mainThread().execute(() -> showToast(result, "登出成功", "登出失败"));
            });
        });
    }

    @Override
    public void onDeleteClick(Account account) {
        showDeleteConfirmDialog("确定要删除该账户吗？", () ->
                AppExecutors.get().diskIO().execute(() -> accountDao.delete(account))
        );
    }

    @Override
    public void onShowLogsClick(Account account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("操作日志 - " + account.getUsername().split("@")[0]);

        View view = getLayoutInflater().inflate(R.layout.dialog_logs, null);
        RecyclerView rvLogs = view.findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        LogsAdapter logsAdapter = new LogsAdapter(account.getLogs());
        rvLogs.setAdapter(logsAdapter);

        Button btnClear = view.findViewById(R.id.btnClearLogs);
        btnClear.setOnClickListener(v ->
                AppExecutors.get().diskIO().execute(() -> {
                    account.getLogs().clear();
                    accountDao.update(account);
                    AppExecutors.get().mainThread().execute(() -> Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show());
                })
        );
        builder.setView(view).show();
    }

    // ================== 其他方法 ==================
    private void deleteAllAccounts() {
        AppExecutors.get().diskIO().execute(() -> accountDao.deleteAll());
    }

    private void showToast(Map<String, Object> result, String successPrefix, String failPrefix) {
        String error = getStringFromMap(result, "error", "").toLowerCase();
        String sucMsg = getStringFromMap(result, "suc_msg", "").toLowerCase();

        String message;

        if (sucMsg.contains("ip_already_online_error")) {
            message = "当前IP已经在线";
        } else if (error.contains("failed to connect to /172.16.130.31:80")) {
            message = "请连接校园网并确保可以访问登录页面";
        } else if (sucMsg.contains("e2901: (third party -1)status_err")) {
            message = "该账户已欠费";
        } else {
            message = getStringFromMap(result, "toast_message",
                    "ok".equalsIgnoreCase(error) ? successPrefix : failPrefix
            );
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteConfirmDialog(String message, Runnable confirmAction) {
        new AlertDialog.Builder(this)
                .setTitle("操作确认")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> confirmAction.run())
                .setNegativeButton("取消", null)
                .show();
    }

    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        return (value != null) ? value.toString() : defaultValue;
    }
}