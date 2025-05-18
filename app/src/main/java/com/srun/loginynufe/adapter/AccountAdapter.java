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
        holder.tvUsername.setText(account.getUsername().split("@")[0]);
        holder.tvRegion.setText(account.getRegion());

        String ipDisplay = (account.getClientIp() != null && !account.getClientIp().isEmpty())
                ? "IP: " + account.getClientIp()
                : "IP: 未获取";
        holder.tvIp.setText(ipDisplay);

        // 绑定点击事件
        holder.btnLogin.setOnClickListener(v -> listener.onLoginClick(account));
        holder.btnLogout.setOnClickListener(v -> {
            account.setClientIp(null);
            notifyItemChanged(position);
            listener.onLogoutClick(account);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(currentPosition);
            }
        });

        holder.btnLogs.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onShowLogsClick(accounts.get(currentPos));
            }
        });

        holder.itemView.setOnClickListener(v -> listener.onItemClick(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername, tvRegion, tvIp;
        public MaterialButton btnLogin, btnLogout;
        public ImageButton btnDelete, btnLogs;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRegion = itemView.findViewById(R.id.tvRegion);
            tvIp = itemView.findViewById(R.id.tvIp);
            btnLogin = itemView.findViewById(R.id.btnLogin);
            btnLogout = itemView.findViewById(R.id.btnLogout);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnLogs = itemView.findViewById(R.id.btn_logs);
        }
    }
}