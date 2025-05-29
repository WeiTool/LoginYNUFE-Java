package com.srun.loginynufe.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.srun.loginynufe.model.Account;
import java.util.List;

/**
 * 数据访问对象（DAO）接口，用于定义对账户表的数据库操作
 *
 * <p>提供基本的CRUD（创建、读取、更新、删除）操作方法，以及自定义查询操作。
 * 使用Room持久化库实现，与{@link Account}实体类关联。</p>
 */
@Dao
public interface AccountDao {
    /**
     * 向数据库插入一个新的账户
     *
     * @param account 要插入的账户对象，不能为null
     */
    @Insert
    void insert(Account account);

    /**
     * 更新数据库中已存在的账户信息
     *
     * @param account 要更新的账户对象，必须具有相同的主键
     */
    @Update
    void update(Account account);

    /**
     * 从数据库中删除指定的账户
     *
     * @param account 要删除的账户对象
     */
    @Delete
    void delete(Account account);

    /**
     * 查询数据库中的所有账户，并以LiveData形式返回结果
     *
     * <p>返回的LiveData会持续观察数据变化，当数据库中的账户数据发生改变时，
     * 绑定的观察者会自动收到更新通知。</p>
     *
     * @return 包含所有账户的LiveData列表，查询结果为空时返回空列表
     */
    @Query("SELECT * FROM accounts")
    LiveData<List<Account>> getAll();

    /**
     * 删除数据库中的所有账户记录
     *
     * <p>警告：该操作会清空账户表的所有数据，谨慎使用！</p>
     */
    @Query("DELETE FROM accounts")
    void deleteAll();
}
