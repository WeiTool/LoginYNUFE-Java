package com.srun.loginynufe.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.srun.loginynufe.R;
import com.srun.loginynufe.model.Account;
import com.srun.loginynufe.utils.AccountDiffCallback;

import java.util.List;

public class AccountAdapter extends ListAdapter<Account, AccountAdapter.ViewHolder> {
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Account account);
        void onLoginClick(Account account);
        void onLogoutClick(Account account);
        void onDeleteClick(Account account);
        void onShowLogsClick(Account account);
    }

    public AccountAdapter(OnItemClickListener listener) {
        super(new AccountDiffCallback());
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
        Account account = getItem(position);
        bindBaseData(holder, account);
        setupClickListeners(holder, account);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }

        Account account = getItem(position);
        Bundle payload = (Bundle) payloads.get(0);

        // 局部更新逻辑
        Context context = holder.itemView.getContext();
        if (payload.containsKey("UPDATE_IP")) {
            String ipDisplay = formatIp(account.getClientIp(), context);
            holder.tvIp.setText(ipDisplay);
        }
        if (payload.containsKey("UPDATE_DEVICES")) {
            holder.tvOnlineDevices.setText(formatDevices(account.getOnlineDevices(), context));
        }
    }

    private void bindBaseData(ViewHolder holder, Account account) {
        Context context = holder.itemView.getContext();
        holder.tvUsername.setText(account.getUsername().split("@")[0]);
        holder.tvRegion.setText(account.getRegion());
        holder.tvIp.setText(formatIp(account.getClientIp(), context));
        holder.tvOnlineDevices.setText(formatDevices(account.getOnlineDevices(), context));
    }

    private void setupClickListeners(ViewHolder holder, Account account) {
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(getItem(pos));
            }
        });
        holder.btnLogin.setOnClickListener(v -> listener.onLoginClick(account));
        holder.btnLogout.setOnClickListener(v -> listener.onLogoutClick(account));
        holder.btnLogs.setOnClickListener(v -> listener.onShowLogsClick(account));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(account));
    }

    // 新增Context参数解决holder作用域问题
    private String formatIp(String ip, Context context) {
        return (ip != null && !ip.isEmpty()) ?
                context.getString(R.string.ip_format, ip) :
                context.getString(R.string.ip_not_available);
    }

    private String formatDevices(int count, Context context) {
        return context.getString(R.string.online_devices, count);
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