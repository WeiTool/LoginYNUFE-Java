package com.srun.loginynufe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.srun.loginynufe.R;
import com.srun.loginynufe.model.Account;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
    private final List<Account> accounts;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Account account);

        void onLoginClick(Account account);

        void onLogoutClick(Account account);

        void onDeleteClick(int position);

        void onShowLogsClick(Account account);
    }

    public AccountAdapter(List<Account> accounts, OnItemClickListener listener) {
        this.accounts = accounts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Account account = accounts.get(position);
        bindBaseData(holder, account);
        setupClickListeners(holder, position, account);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        Account account = accounts.get(position);
        bindBaseData(holder, account);

        // 局部更新逻辑
        if (!payloads.isEmpty()) {
            for (Object payload : payloads) {
                if (payload instanceof String && payload.equals("UPDATE_IP_AND_STATUS")) {
                    String ipDisplay;
                    if (account.getClientIp() != null && !account.getClientIp().isEmpty()) {
                        ipDisplay = holder.itemView.getContext().getString(R.string.ip_format, account.getClientIp());
                    } else {
                        ipDisplay = holder.itemView.getContext().getString(R.string.ip_not_available);
                    }
                    holder.tvIp.setText(ipDisplay);
                    holder.tvOnlineDevices.setText(
                            holder.itemView.getContext().getString(R.string.online_devices, account.getOnlineDevices())
                    );
                }
            }
        }

        setupClickListeners(holder, position, account);
    }

    private void bindBaseData(ViewHolder holder, Account account) {
        holder.tvUsername.setText(account.getUsername().split("@")[0]);
        holder.tvRegion.setText(account.getRegion());
        String ipDisplay;
        if (account.getClientIp() != null && !account.getClientIp().isEmpty()) {
            ipDisplay = holder.itemView.getContext().getString(R.string.ip_format, account.getClientIp());
        } else {
            ipDisplay = holder.itemView.getContext().getString(R.string.ip_not_available);
        }
        holder.tvIp.setText(ipDisplay);
        holder.tvOnlineDevices.setText(
                holder.itemView.getContext().getString(R.string.online_devices, account.getOnlineDevices())
        );
    }

    private void setupClickListeners(ViewHolder holder, int position, Account account) {
        // 绑定最新数据和位置
        holder.btnLogin.setOnClickListener(v -> listener.onLoginClick(account));
        holder.btnLogout.setOnClickListener(v -> listener.onLogoutClick(account));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
        holder.btnLogs.setOnClickListener(v -> listener.onShowLogsClick(account));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername, tvRegion, tvIp, tvOnlineDevices;
        public MaterialButton btnLogin, btnLogout;
        public ImageButton btnDelete, btnLogs;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            tvIp = itemView.findViewById(R.id.tvIp);
            tvOnlineDevices = itemView.findViewById(R.id.tvOnlineDevices);
            btnLogin = itemView.findViewById(R.id.btnLogin);
            btnLogout = itemView.findViewById(R.id.btnLogout);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnLogs = itemView.findViewById(R.id.btn_logs);
        }
    }
}