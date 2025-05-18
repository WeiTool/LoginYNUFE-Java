package com.srun.loginynufe;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import com.srun.loginynufe.data.AccountManager;
import com.srun.loginynufe.dialog.AccountDialog;
import com.srun.loginynufe.encryption.LoginLogout;
import com.srun.loginynufe.model.Account;
import com.srun.loginynufe.adapter.LogsAdapter;
import com.srun.loginynufe.utils.VersionChecker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AccountAdapter.OnItemClickListener {
    private AccountAdapter adapter;
    private List<Account> accounts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 加载数据
        accounts = AccountManager.getAccounts(this);
        adapter = new AccountAdapter(accounts, this);
        recyclerView.setAdapter(adapter);

        // FAB点击事件
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddAccountDialog());

        com.google.android.material.button.MaterialButton btnDeleteAll = findViewById(R.id.btnDeleteAll);
        btnDeleteAll.setOnClickListener(v ->
                showDeleteConfirmDialog("确定要删除所有账户吗？", () -> {
                    accounts.clear();
                    AccountManager.saveAccounts(this, accounts);
                    adapter.notifyDataSetChanged();
                })
        );

        VersionChecker.checkNewVersion(this);
    }

    private void showAddAccountDialog() {
        AccountDialog dialog = new AccountDialog(
                this,
                account -> {
                    accounts.add(account);
                    AccountManager.saveAccounts(this, accounts);
                    adapter.notifyItemInserted(accounts.size() - 1);
                },
                false,
                null
        );
        dialog.show();
    }

    @Override
    public void onItemClick(Account account) {
        AccountDialog dialog = new AccountDialog(
                this,
                newAccount -> {
                    int index = accounts.indexOf(account);
                    if (index != -1) {
                        accounts.set(index, newAccount);
                        AccountManager.saveAccounts(this, accounts);
                        adapter.notifyItemChanged(index);
                    }
                },
                true,
                account
        );
        dialog.show();
    }

    @Override
    public void onLoginClick(Account account) {
        LoginLogout.performLogin(account, result -> {
            // 将 toast 信息直接合并到原始 result 中
            result.put("toast_message", "ok".equalsIgnoreCase(getStringFromMap(result, "error", "unknown_error"))
                    ? "登录成功：" + getStringFromMap(result, "suc_msg", "操作成功")
                    : "登录失败：" + getStringFromMap(result, "error_msg", "未知错误"));

            // 仅添加一次登录日志
            account.addLoginLog(result);
            AccountManager.saveAccounts(this, accounts);

            if ("ok".equalsIgnoreCase(getStringFromMap(result, "error", ""))) {
                account.setLoggedIn(true);
                adapter.notifyItemChanged(accounts.indexOf(account));
                Toast.makeText(this, getStringFromMap(result, "toast_message", ""), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getStringFromMap(result, "toast_message", ""), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onLogoutClick(Account account) {
        LoginLogout.performLogout(account, result -> {
            // 将 toast 信息合并到原始 result
            result.put("toast_message", "ok".equalsIgnoreCase(getStringFromMap(result, "error", "unknown_error"))
                    ? "登出成功：" + getStringFromMap(result, "suc_msg", "操作成功")
                    : "登出失败：" + getStringFromMap(result, "error_msg", "未知错误"));

            // 仅添加一次登出日志
            account.addLogoutLog(result);
            AccountManager.saveAccounts(this, accounts);

            if ("ok".equalsIgnoreCase(getStringFromMap(result, "error", ""))) {
                account.setLoggedIn(false);
                adapter.notifyItemChanged(accounts.indexOf(account));
                Toast.makeText(this, getStringFromMap(result, "toast_message", ""), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getStringFromMap(result, "toast_message", ""), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        showDeleteConfirmDialog("确定要删除该账户吗？", () -> {
            accounts.remove(position);
            AccountManager.saveAccounts(this, accounts);
            adapter.notifyItemRemoved(position);
        });
    }

    @Override
    public void onShowLogsClick(Account account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("详细操作日志 - " + account.getUsername().split("@")[0]);

        View view = getLayoutInflater().inflate(R.layout.dialog_logs, null);
        RecyclerView rvLogs = view.findViewById(R.id.rvLogs);
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        LogsAdapter adapter = new LogsAdapter(account.getLogs()); // 创建适配器
        rvLogs.setAdapter(adapter);

        Button btnClear = view.findViewById(R.id.btnClearLogs);
        btnClear.setOnClickListener(v -> {
            account.getLogs().clear();
            AccountManager.saveAccounts(this, accounts);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "日志已清除", Toast.LENGTH_SHORT).show();
        });

        builder.setView(view);
        builder.show();
    }

    private void showDeleteConfirmDialog(String message, Runnable confirmAction) {
        new AlertDialog.Builder(this)
                .setTitle("操作确认")
                .setMessage(message)
                .setPositiveButton("确定", (dialog, which) -> confirmAction.run())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 安全地从 Map 中获取字符串字段值
     *
     * @param map          数据源
     * @param key          字段名
     * @param defaultValue 默认值
     * @return 字段值（或默认值）
     */
    private String getStringFromMap(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        // 处理不同数据类型（如服务器返回数字时转为字符串）
        return (value != null) ? value.toString() : defaultValue;
    }
}