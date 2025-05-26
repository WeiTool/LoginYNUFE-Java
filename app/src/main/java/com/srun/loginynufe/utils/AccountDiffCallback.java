package com.srun.loginynufe.utils;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.srun.loginynufe.model.Account;

import java.util.Objects;

public class AccountDiffCallback extends DiffUtil.ItemCallback<Account> {
    @Override
    public boolean areItemsTheSame(Account oldItem, Account newItem) {
        return oldItem.getId() == newItem.getId();
    }

    @Override
    public boolean areContentsTheSame(Account oldItem, Account newItem) {
        return oldItem.getUsername().equals(newItem.getUsername())
                && oldItem.getRegion().equals(newItem.getRegion())
                && Objects.equals(oldItem.getClientIp(), newItem.getClientIp())
                && oldItem.getOnlineDevices() == newItem.getOnlineDevices()
                && oldItem.isLoggedIn() == newItem.isLoggedIn();
    }

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
