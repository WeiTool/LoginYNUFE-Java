package com.srun.loginynufe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.srun.loginynufe.R;
import java.util.List;

/**
 * 日志列表适配器 - 用于在RecyclerView中展示网络登录日志数据
 * <p>
 * 功能要点：
 * 1. 继承自RecyclerView.Adapter实现列表数据绑定
 * 2. 使用内部ViewHolder优化视图元素查找
 * 3. 支持动态更新字符串类型的日志数据集合
 */
public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {
    /**
     * 日志数据集合，使用final确保引用不可变
     */
    private final List<String> logs;

    /**
     * 构造方法
     *
     * @param logs 日志数据集合（需非空）
     */
    public LogsAdapter(List<String> logs) {
        this.logs = logs;
    }

    /**
     * 创建ViewHolder实例时调用
     *
     * @param parent   父视图组（RecyclerView自身）
     * @param viewType 视图类型（未使用，保留参数）
     * @return 绑定item_log布局的新ViewHolder
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到指定位置列表项
     *
     * @param holder   要绑定的ViewHolder实例
     * @param position 当前数据项位置（基于0的索引）
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvLog.setText(logs.get(position));
    }

    /**
     * 获取数据项总数
     *
     * @return 日志条目的数量
     */
    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * ViewHolder内部类 - 缓存列表项视图元素
     * <p>
     * 优化点：
     * 1. 通过itemView.findViewById避免重复查找
     * 2. 静态内部类减少内存泄漏风险
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /**
         * 日志内容文本视图，对应布局中的R.id.tvLog
         */
        TextView tvLog;

        /**
         * ViewHolder构造方法
         *
         * @param itemView 列表项根视图（来自item_log.xml）
         */
        public ViewHolder(View itemView) {
            super(itemView);
            tvLog = itemView.findViewById(R.id.tvLog);
        }
    }
}