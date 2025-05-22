package com.srun.loginynufe.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.srun.loginynufe.model.Account;

import java.util.List;

@Dao
public interface AccountDao {
    @Insert
    void insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM accounts")
    List<Account> getAll();

    @Query("DELETE FROM accounts")
    void deleteAll();
}
