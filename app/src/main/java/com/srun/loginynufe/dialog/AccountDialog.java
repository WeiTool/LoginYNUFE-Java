package com.srun.loginynufe.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.google.android.material.textfield.TextInputEditText;
import com.srun.loginynufe.R;
import com.srun.loginynufe.model.Account;

public class AccountDialog extends Dialog {
    private final OnSaveListener listener;
    private TextInputEditText etStudentId, etPassword;
    private Spinner spinnerRegion;
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
        spinnerRegion = findViewById(R.id.spinnerRegion);
        Button btnSave = findViewById(R.id.btnSave);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.regions,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegion.setAdapter(adapter);

        if (isEditMode && existingAccount != null) {
            String studentId = existingAccount.getUsername().split("@")[0];
            etStudentId.setText(studentId);
            etPassword.setText(existingAccount.getPassword());
            ArrayAdapter adapterSpinner = (ArrayAdapter) spinnerRegion.getAdapter();
            int position = adapterSpinner.getPosition(existingAccount.getRegion());
            spinnerRegion.setSelection(position);
        }

        btnSave.setOnClickListener(v -> {
            String studentId = etStudentId.getText() != null ? etStudentId.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String region = spinnerRegion.getSelectedItem() != null ? spinnerRegion.getSelectedItem().toString() : "";

            if (TextUtils.isEmpty(studentId)) {
                etStudentId.setError("学号不能为空");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("密码不能为空");
                return;
            }

            String[] regions = getContext().getResources().getStringArray(R.array.regions);
            String suffix = region.equals(regions[0]) ? "@ctc" : "@ynufe";
            String username = studentId + suffix;
            Account newAccount = new Account(username, password, region);

            if (isEditMode && existingAccount != null) {
                newAccount.setLoggedIn(existingAccount.isLoggedIn());
            }

            listener.onSave(newAccount);
            dismiss();
        });
    }

    public interface OnSaveListener {
        void onSave(Account account);
    }
}