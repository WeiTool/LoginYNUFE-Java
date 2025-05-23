package com.srun.loginynufe.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.srun.loginynufe.R;
import com.srun.loginynufe.model.Account;

public class AccountDialog extends Dialog {
    private final OnSaveListener listener;
    private TextInputEditText etStudentId, etPassword;
    private RadioGroup radioGroupRegion;
    private final boolean isEditMode;
    private final Account existingAccount;

    public AccountDialog(Context context, OnSaveListener listener, boolean isEditMode, Account account) {
        super(context);
        this.listener = listener;
        this.isEditMode = isEditMode;
        this.existingAccount = account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_account);

        etStudentId = findViewById(R.id.etStudentId);
        etPassword = findViewById(R.id.etPassword);
        radioGroupRegion = findViewById(R.id.radioGroupRegion);
        RadioButton rbCTC = findViewById(R.id.rbCTC);
        RadioButton rbYNufe = findViewById(R.id.rbYNufe);
        Button btnSave = findViewById(R.id.btnSave);


        if (isEditMode && existingAccount != null) {
            // 从 username 中提取学号（去掉@后缀）
            String username = existingAccount.getUsername();
            String studentId = username.split("@")[0]; // 分割字符串获取学号
            etStudentId.setText(studentId);

            // 填充密码
            etPassword.setText(existingAccount.getPassword());

            // 设置区域选中状态（原有逻辑）
            String region = existingAccount.getRegion();
            if (region.equals("宿舍区域")) {
                rbCTC.setChecked(true);
            } else if (region.equals("教学区域")) {
                rbYNufe.setChecked(true);
            }
        }

        btnSave.setOnClickListener(v -> {
            String studentId = etStudentId.getText() != null ? etStudentId.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // 获取选中的区域
            String region = "";
            int selectedId = radioGroupRegion.getCheckedRadioButtonId();
            if (selectedId == R.id.rbCTC) {
                region = "宿舍区域";
            } else if (selectedId == R.id.rbYNufe) {
                region = "教学区域";
            }

            // 输入验证
            if (TextUtils.isEmpty(studentId)) {
                etStudentId.setError("学号不能为空");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("密码不能为空");
                return;
            }

            // 生成用户名后缀
            String suffix = region.equals("宿舍区域") ? "@ctc" : "@ynufe";
            String username = studentId + suffix;

            // 创建或更新账户对象
            Account newAccount;
            if (isEditMode && existingAccount != null) {
                // 使用带ID的构造函数，保留原有ID
                newAccount = new Account(username, password, region, existingAccount.getId());
                // 同步其他需要保留的字段（如登录状态、IP、在线设备数等）
                newAccount.setLoggedIn(existingAccount.isLoggedIn());
                newAccount.setClientIp(existingAccount.getClientIp());
                newAccount.setOnlineDevices(existingAccount.getOnlineDevices());
                newAccount.setLogs(existingAccount.getLogs());
            } else {
                newAccount = new Account(username, password, region);
            }

            // 回调保存逻辑
            listener.onSave(newAccount);
            dismiss();
        });
    }

    public interface OnSaveListener {
        void onSave(Account account);
    }
}