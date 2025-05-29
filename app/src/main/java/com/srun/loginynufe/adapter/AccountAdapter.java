package com.srun.loginynufe.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.srun.loginynufe.R;
import com.srun.loginynufe.model.Account;
import com.srun.loginynufe.utils.AccountDiffCallback;
import java.util.List;

/**
 * 账户列表适配器（基于RecyclerView实现）
 *
 * <p>使用ListAdapter配合DiffUtil实现高效数据更新，处理账户项的展示和交互逻辑</p>
 */
public class AccountAdapter extends ListAdapter<Account, AccountAdapter.ViewHolder> {
    private final OnItemClickListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface OnItemClickListener {
        /**
         * 列表项点击事件监听接口
         */
        void onItemClick(Account account);

        /**
         * 当登录按钮被点击时触发
         *
         * @param account 关联的账户对象
         */
        void onLoginClick(Account account);

        /**
         * 当登出按钮被点击时触发
         *
         * @param account 关联的账户对象
         */
        void onLogoutClick(Account account);

        /**
         * 当删除按钮被点击时触发
         *
         * @param account 待删除的账户对象
         */
        void onDeleteClick(Account account);

        /**
         * 当日志按钮被点击时触发
         *
         * @param account 关联的账户对象
         */
        void onShowLogsClick(Account account);
    }

    /**
     * 构造方法
     *
     * @param listener 列表项事件监听器实例
     */
    public AccountAdapter(OnItemClickListener listener) {
        super(new AccountDiffCallback());
        this.listener = listener;
    }

    /**
     * 获取指定位置的账户对象
     *
     * @param position 列表位置索引
     * @return 对应位置的账户对象
     */
    public Account getAccountAt(int position) {
        return getItem(position);
    }

    /**
     * 创建ViewHolder实例
     *
     * @param parent   父容器视图组
     * @param viewType 视图类型
     * @return 创建好的ViewHolder实例
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定基础数据到ViewHolder
     *
     * @param holder   目标ViewHolder
     * @param position 数据位置索引
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Account account = getItem(position);
        bindBaseData(holder, account);
        setupClickListeners(holder, account);
    }

    /**
     * 带payload的绑定方法（支持局部更新）
     *
     * @param holder   目标ViewHolder
     * @param position 数据位置索引
     * @param payloads 更新载荷集合
     */
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
        if (payload.containsKey("UPDATE_REGION")) {
            holder.tvRegion.setText(account.getRegion());
        }
    }

    /**
     * 绑定基础数据到视图组件
     *
     * @param holder  目标ViewHolder
     * @param account 数据源账户对象
     */
    private void bindBaseData(ViewHolder holder, Account account) {
        Context context = holder.itemView.getContext();
        holder.tvUsername.setText(account.getUsername().split("@")[0]);
        holder.tvRegion.setText(account.getRegion());
        holder.tvIp.setText(formatIp(account.getClientIp(), context));
        holder.tvOnlineDevices.setText(formatDevices(account.getOnlineDevices(), context));
    }

    /**
     * 设置视图组件的点击监听器
     *
     * @param holder  关联的ViewHolder
     * @param account 数据源账户对象
     */
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

    /**
     * 格式化IP地址显示
     *
     * @param ip      原始IP字符串
     * @param context Android上下文对象
     * @return 格式化后的IP地址字符串
     */
    private String formatIp(String ip, Context context) {
        return (ip != null && !ip.isEmpty()) ?
                context.getString(R.string.ip_format, ip) :
                context.getString(R.string.ip_not_available);
    }

    /**
     * 格式化在线设备数显示
     *
     * @param count   设备数量
     * @param context Android上下文对象
     * @return 格式化后的设备数字符串
     */
    private String formatDevices(int count, Context context) {
        return context.getString(R.string.online_devices, count);
    }

    /**
     * 局部更新指定位置的列表项
     *
     * @param position 需要更新的位置索引
     * @param account  更新后的账户对象
     * @param fields   需要更新的字段集合
     */
    // 局部刷新方法
    public void updateItemPartial(int position, Account account, String... fields) {
        mainHandler.post(() -> {
            Bundle payload = new Bundle();
            for (String field : fields) {
                switch (field) {
                    case "ip":
                        payload.putString("UPDATE_IP", account.getClientIp());
                        break;
                    case "devices":
                        payload.putInt("UPDATE_DEVICES", account.getOnlineDevices());
                        break;
                    case "region":
                        payload.putString("UPDATE_REGION", account.getRegion());
                        break;
                }
            }
            notifyItemChanged(position, payload);
        });
    }

    /**
     * ViewHolder内部类（承载列表项视图组件）
     */
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