package com.srun.loginynufe.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.srun.loginynufe.model.Account;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_ACCOUNTS = "accounts";

    public static void saveAccounts(Context context, List<Account> accounts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(accounts);
        prefs.edit().putString(KEY_ACCOUNTS, json).apply();
    }

    public static List<Account> getAccounts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_ACCOUNTS, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<Account>>(){}.getType();
        return new Gson().fromJson(json, type);
    }
}