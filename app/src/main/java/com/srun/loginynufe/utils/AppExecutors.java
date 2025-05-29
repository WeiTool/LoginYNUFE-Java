package com.srun.loginynufe.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 应用全局线程池管理类（单例模式）
 * 提供不同场景的线程执行器：磁盘IO操作、网络请求、主线程任务调度
 */
public class AppExecutors {
    // CPU核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // 单例实例
    private static final AppExecutors INSTANCE = new AppExecutors();

    // 线程池配置
    private final Executor diskIO;       // 数据库操作（单线程）
    private final Executor networkIO;    // 网络请求
    private final Executor mainThread;   // 主线程

    /**
     * 私有化构造方法（实际为public需修正，此处应改为private确保单例严格性）
     * 初始化三个核心线程池配置
     */
    public AppExecutors() {
        diskIO = Executors.newSingleThreadExecutor();
        networkIO = new ThreadPoolExecutor(
                2,                 // 核心线程数
                CPU_COUNT * 2 + 1, // 最大线程数
                30,                // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(128)
        );
        mainThread = new MainThreadExecutor();
    }

    /**
     * 获取单例实例
     *
     * @return 全局唯一的AppExecutors实例
     */
    public static AppExecutors get() {
        return INSTANCE;
    }

    /**
     * 数据库操作执行器
     *
     * @return 单线程顺序执行器（适用于需要事务一致性的场景）
     */
    public Executor diskIO() {
        return diskIO;
    }

    /**
     * 获取网络请求执行器
     *
     * @return 弹性线程池执行器（适合突发性、短生命周期的网络请求任务）
     */
    public Executor networkIO() {
        return networkIO;
    }

    /**
     * 获取主线程执行器
     *
     * @return Android主线程调度器（用于UI更新等必须在主线程执行的操作）
     */
    public Executor mainThread() {
        return mainThread;
    }

    /**
     * 主线程执行器实现类
     * 通过Android Handler机制将任务调度到UI线程
     */
    private static class MainThreadExecutor implements Executor {
        private final android.os.Handler mainThreadHandler =
                new android.os.Handler(android.os.Looper.getMainLooper());

        /**
         * 将任务提交到Android主线程队列
         *
         * @param command 需要主线程执行的任务
         */
        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}