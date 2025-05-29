package com.srun.loginynufe.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import com.srun.loginynufe.model.Account;

/**
 * Room 数据库抽象类，用于管理应用程序的账户数据存储
 * <p>
 * 此类使用 Room 持久化库创建并管理 SQLite 数据库，采用单例模式保证全局唯一实例。
 * 数据库配置说明：
 * - 包含的实体：Account（账户实体类）
 * - 当前数据库版本：1
 * - 不导出 schema 文件（适用于不需要迁移历史的场景）
 * - 使用 ListConverter 类型转换器处理复杂数据类型
 */
@Database(entities = {Account.class}, version = 1, exportSchema = false)
@TypeConverters(ListConverter.class)
public abstract class AppDatabase extends RoomDatabase {
    /**
     * 获取 Account 实体对应的数据访问对象 (DAO)
     *
     * @return AccountDao 实例，用于执行账户相关的数据库操作
     */
    public abstract AccountDao accountDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * 获取数据库单例实例（双重检查锁模式实现线程安全）
     *
     * @param context 应用上下文，用于数据库构建
     * @return AppDatabase 唯一实例
     * <p>
     * 实现特点：
     * 1. 首次调用时使用 Room 构建器创建数据库：
     * - 指定上下文对象
     * - 数据库类类型
     * - 数据库文件名称为 "User.db"
     * 2. 使用 synchronized 代码块保证线程安全
     * 3. 双重非空检查提升性能，避免不必要的同步开销
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "User.db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
