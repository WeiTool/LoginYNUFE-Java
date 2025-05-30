package com.srun.loginynufe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 主活动 - 校园网账户管理界面
 * 功能：展示账户列表，支持账户的增删改查、登录登出操作、日志查看等功能
 */
public class MainActivity extends AppCompatActivity implements AccountAdapter.OnItemClickListener {
    private AccountAdapter adapter;
    private AccountDao accountDao;
    private static final String TAG = "MainActivity";

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

    /**
     * 显示添加账户对话框
     * 包含输入验证和数据库插入操作
     */
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

    /**
     * 点击账户项 - 显示编辑对话框
     *
     * @param account 被点击的账户对象
     */
    @Override
    public void onItemClick(Account account) {
        AccountDialog dialog = new AccountDialog(
                this,
                updatedAccount -> {
                    int position = findAccountPosition(updatedAccount);
                    if (position != -1) {
                        // 使用局部刷新更新区域信息
                        adapter.updateItemPartial(position, updatedAccount, "region");
                    }
                    AppExecutors.get().diskIO().execute(() -> accountDao.update(updatedAccount));
                },
                true,
                account
        );
        dialog.show();
    }

    /**
     * 登录操作处理
     * 包含网络请求、结果解析、UI更新和数据库保存
     */
    @Override
    public void onLoginClick(Account account) {
        LoginLogout.performLogin(account, result -> {
            account.addLoginLog(result);

            // 获取当前账户位置
            int position = findAccountPosition(account);

            // 更新IP地址
            if (result.containsKey("client_ip")) {
                String clientIp = (String) result.get("client_ip");
                account.setClientIp(clientIp);

                // 立即更新UI（局部刷新）
                if (position != -1) {
                    adapter.updateItemPartial(position, account, "ip");
                }
            }

            // 更新数据库
            AppExecutors.get().diskIO().execute(() -> {
                accountDao.update(account);
                AppExecutors.get().mainThread().execute(() -> showToast(result, "登录成功", "登录失败"));
            });

            // 延迟1.5秒后获取用户信息（等待服务器更新状态）
            AppExecutors.get().networkIO().execute(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Log.e(TAG, "登录操作线程中断: " + e.getMessage(), e);
                    // 恢复中断状态
                    Thread.currentThread().interrupt();
                }

                Map<String, Object> userInfo = LoginLogout.getRadUserInfo();

                // 处理 online_device_total
                if (userInfo != null && userInfo.containsKey("online_device_total")) {
                    Object value = userInfo.get("online_device_total");
                    if (value != null) {
                        try {
                            int onlineDevices;
                            if (value instanceof String) {
                                onlineDevices = Integer.parseInt((String) value);
                            } else if (value instanceof Number) {
                                onlineDevices = ((Number) value).intValue();
                            } else {
                                throw new NumberFormatException("Unsupported type: " + value.getClass().getName());
                            }
                            account.setOnlineDevices(onlineDevices);

                            // 立即更新UI（局部刷新）
                            if (position != -1) {
                                adapter.updateItemPartial(position, account, "devices");
                            }
                        } catch (NumberFormatException e) {
                            account.setOnlineDevices(0);
                            // 立即更新UI（局部刷新）
                            if (position != -1) {
                                adapter.updateItemPartial(position, account, "devices");
                            }
                            // 使用 Log 记录错误
                            Log.w(TAG, "在线设备数解析失败: " + e.getMessage(), e);
                            AppExecutors.get().mainThread().execute(() ->
                                    Toast.makeText(
                                            MainActivity.this,
                                            "在线设备数解析失败",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    } else {
                        account.setOnlineDevices(0); // 值为null时设为0
                        if (position != -1) {
                            adapter.updateItemPartial(position, account, "devices");
                        }
                    }
                } else {
                    account.setOnlineDevices(0); // 字段不存在时设为0
                    if (position != -1) {
                        adapter.updateItemPartial(position, account, "devices");
                    }
                }

                // 更新数据库（不刷新UI，因为UI已通过局部刷新更新）
                AppExecutors.get().diskIO().execute(() -> accountDao.update(account));
            });
        });
    }

    /**
     * 登出操作处理
     * 清除IP地址，减少在线设备计数
     */
    @Override
    public void onLogoutClick(Account account) {
        int position = findAccountPosition(account);

        LoginLogout.performLogout(account, result -> {
            account.addLogoutLog(result);
            account.setClientIp("");
            account.setOnlineDevices(Math.max(account.getOnlineDevices() - 1, 0));

            // 立即更新UI（局部刷新）
            if (position != -1) {
                adapter.updateItemPartial(position, account, "ip", "devices");
            }

            AppExecutors.get().diskIO().execute(() -> {
                accountDao.update(account);
                AppExecutors.get().mainThread().execute(() -> showToast(result, "登出成功", "登出失败"));
            });
        });
    }

    /**
     * 删除单个账户
     */
    @Override
    public void onDeleteClick(Account account) {
        showDeleteConfirmDialog("确定要删除该账户吗？", () ->
                AppExecutors.get().diskIO().execute(() -> accountDao.delete(account))
        );
    }

    /**
     * 显示操作日志对话框
     * 支持日志查看和清除
     */
    @Override
    public void onShowLogsClick(Account account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("操作日志 - " + account.getUsername().split("@")[0]);

        View view = getLayoutInflater().inflate(R.layout.dialog_logs, null);
        RecyclerView rvLogs = view.findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));

        // 创建日志列表的副本，避免直接操作原始列表
        List<String> logsCopy = new ArrayList<>(account.getLogs());
        LogsAdapter logsAdapter = new LogsAdapter(logsCopy);
        rvLogs.setAdapter(logsAdapter);

        Button btnClear = view.findViewById(R.id.btnClearLogs);
        btnClear.setOnClickListener(v -> {
            // 使用适配器的专用清除方法（高效实现）
            logsAdapter.clearLogs();

            // 在后台更新数据库
            AppExecutors.get().diskIO().execute(() -> {
                // 清除原始日志列表
                account.getLogs().clear();
                accountDao.update(account);

                // 显示Toast提示
                AppExecutors.get().mainThread().execute(() ->
                        Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show()
                );
            });
        });
        builder.setView(view).show();
    }

    //================== 工具方法 ==================//
    private void deleteAllAccounts() {
        AppExecutors.get().diskIO().execute(() -> accountDao.deleteAll());
    }

    /**
     * 解析服务器返回结果并显示对应提示
     *
     * @param result        服务器返回的Map
     * @param successPrefix 成功前缀
     * @param failPrefix    失败前缀
     */
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

    /**
     * 显示带确认的操作对话框
     *
     * @param message       提示信息
     * @param confirmAction 确认回调操作
     */
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

    /**
     * 查找账户在列表中的位置（用于局部刷新）
     *
     * @return 位置索引，-1表示未找到
     */
    private int findAccountPosition(Account account) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            // 使用新的公共方法获取账户
            Account item = adapter.getAccountAt(i);
            if (item.getId() == account.getId()) {
                return i;
            }
        }
        return -1;
    }
}