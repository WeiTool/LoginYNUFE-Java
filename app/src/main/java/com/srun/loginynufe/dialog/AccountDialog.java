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

/**
 * 账户编辑对话框 - 用于添加或修改用户账户信息
 * <p>
 * 功能特性：
 * 1. 支持新建账户和编辑现有账户两种模式
 * 2. 包含学号、密码输入及网络区域选择功能
 * 3. 自动生成带区域后缀的用户名（学号@区域代码）
 * 4. 输入字段非空验证
 * 5. 继承原有账户的登录状态和网络信息（编辑模式）
 */
public class AccountDialog extends Dialog {
    private final OnSaveListener listener;
    private TextInputEditText etStudentId, etPassword;
    private RadioGroup radioGroupRegion;
    private final boolean isEditMode;
    private final Account existingAccount;

    /**
     * 构造函数
     *
     * @param context    上下文对象
     * @param listener   保存事件监听器
     * @param isEditMode 是否为编辑模式
     * @param account    被编辑的账户对象（编辑模式时必须提供）
     */
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

        // 编辑模式下预填充现有账户信息
        if (isEditMode && existingAccount != null) {
            prefillExistingAccount(rbCTC, rbYNufe);
        }

        btnSave.setOnClickListener(v -> {
            // 获取输入值
            String studentId = etStudentId.getText() != null ? etStudentId.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // 验证输入
            if (!validateInputs(studentId, password)) {
                return;
            }

            // 创建新账户对象
            Account newAccount = createAccountFromInput(studentId, password);

            // 回调保存逻辑
            listener.onSave(newAccount);
            dismiss();
        });
    }

    /**
     * 预填充现有账户信息
     *
     * @param rbCTC   宿舍区域单选按钮
     * @param rbYNufe 教学区域单选按钮
     */
    private void prefillExistingAccount(RadioButton rbCTC, RadioButton rbYNufe) {
        // 从 username 中提取学号（去掉@后缀）
        String username = existingAccount.getUsername();
        String studentId = username.split("@")[0]; // 分割字符串获取学号
        etStudentId.setText(studentId);
        etPassword.setText(existingAccount.getPassword());

        // 设置区域选择
        String region = existingAccount.getRegion();
        if (region.equals("宿舍区域")) {
            rbCTC.setChecked(true);
        } else if (region.equals("教学区域")) {
            rbYNufe.setChecked(true);
        }
    }

    /**
     * 验证输入字段
     *
     * @param studentId 学号输入
     * @param password  密码输入
     * @return 验证是否通过
     */
    private boolean validateInputs(String studentId, String password) {
        if (TextUtils.isEmpty(studentId)) {
            etStudentId.setError("学号不能为空");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("密码不能为空");
            return false;
        }
        return true;
    }

    /**
     * 从输入创建账户对象
     *
     * @param studentId 学号
     * @param password  密码
     * @return 创建的新账户对象
     */
    private Account createAccountFromInput(String studentId, String password) {
        // 获取选中的区域
        String region = "";
        int selectedId = radioGroupRegion.getCheckedRadioButtonId();
        if (selectedId == R.id.rbCTC) {
            region = "宿舍区域";
        } else if (selectedId == R.id.rbYNufe) {
            region = "教学区域";
        }

        // 生成用户名后缀
        String suffix = region.equals("宿舍区域") ? "@ctc" : "@ynufe";
        String username = studentId + suffix;

        // 创建或更新账户对象
        if (isEditMode && existingAccount != null) {
            // 使用带ID的构造函数，保留原有ID
            Account newAccount = new Account(username, password, region, existingAccount.getId());
            // 同步其他需要保留的字段（如登录状态、IP、在线设备数等）
            newAccount.setLoggedIn(existingAccount.isLoggedIn());
            newAccount.setClientIp(existingAccount.getClientIp());
            newAccount.setOnlineDevices(existingAccount.getOnlineDevices());
            newAccount.setLogs(existingAccount.getLogs());
            return newAccount;
        } else {
            return new Account(username, password, region);
        }
    }

    public interface OnSaveListener {
        void onSave(Account account);
    }
}