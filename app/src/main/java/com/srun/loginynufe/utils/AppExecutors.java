package com.srun.loginynufe.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AppExecutors {
    // CPU核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // 单例实例
    private static final AppExecutors INSTANCE = new AppExecutors();

    // 线程池配置
    private final Executor diskIO;       // 数据库操作（单线程）
    private final Executor networkIO;    // 网络请求
    private final Executor mainThread;   // 主线程

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

    public static AppExecutors get() {
        return INSTANCE;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final android.os.Handler mainThreadHandler =
                new android.os.Handler(android.os.Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}