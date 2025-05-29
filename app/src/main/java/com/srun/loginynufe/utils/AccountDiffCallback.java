package com.srun.loginynufe.utils;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import com.srun.loginynufe.model.Account;
import java.util.Objects;

/**
 * DiffUtil回调实现类，用于比较两个Account对象之间的差异
 * 主要用于RecyclerView的高效数据更新，确定列表项是否需要更新以及更新方式
 */
public class AccountDiffCallback extends DiffUtil.ItemCallback<Account> {
    /**
     * 判断两个Account对象是否代表同一个项目（唯一标识符比较）
     *
     * @param oldItem 旧数据项
     * @param newItem 新数据项
     * @return 当id相同时返回true，表示是同一个账户
     */
    @Override
    public boolean areItemsTheSame(Account oldItem, Account newItem) {
        return oldItem.getId() == newItem.getId();
    }

    /**
     * 检查账户内容是否发生变更（数据完整性比较）
     * 比较以下字段：
     * - 用户名
     * - 区域
     * - 客户端IP（允许null值比较）
     * - 在线设备数量
     * - 登录状态
     *
     * @return 所有字段相同时返回true，表示内容没有变化
     */
    @Override
    public boolean areContentsTheSame(Account oldItem, Account newItem) {
        return oldItem.getUsername().equals(newItem.getUsername())
                && oldItem.getRegion().equals(newItem.getRegion())
                && Objects.equals(oldItem.getClientIp(), newItem.getClientIp())
                && oldItem.getOnlineDevices() == newItem.getOnlineDevices()
                && oldItem.isLoggedIn() == newItem.isLoggedIn();
    }

    /**
     * 获取变更的有效载荷，用于局部更新
     * 当以下字段变更时返回对应的标记：
     * - 客户端IP变更 → "UPDATE_IP"
     * - 在线设备数变更 → "UPDATE_DEVICES"
     *
     * @return 包含变更标记的Bundle，无变更时返回null
     */
    @Nullable
    @Override
    public Object getChangePayload(Account oldItem, Account newItem) {
        Bundle payload = new Bundle();
        if (!Objects.equals(oldItem.getClientIp(), newItem.getClientIp())) {
            payload.putBoolean("UPDATE_IP", true);
        }
        if (oldItem.getOnlineDevices() != newItem.getOnlineDevices()) {
            payload.putBoolean("UPDATE_DEVICES", true);
        }
        return payload.isEmpty() ? null : payload;
    }
}
