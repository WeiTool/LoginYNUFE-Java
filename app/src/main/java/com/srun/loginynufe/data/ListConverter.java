package com.srun.loginynufe.data;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 类型转换工具类，用于Room数据库与List<String>类型之间的转换
 * <p>
 * 通过Gson库实现List<String>对象与JSON字符串的相互转换，
 * 使得Room数据库能够存储和读取List<String>类型的数据。
 */
public class ListConverter {
    private static final Gson gson = new Gson();

    /**
     * 将List<String>类型转换为Room可存储的JSON字符串
     *
     * @param list 要转换的字符串列表，允许为null
     * @return 包含列表数据的JSON格式字符串，当输入null时返回null
     */
    @TypeConverter
    public static String fromList(List<String> list) {
        return gson.toJson(list);
    }

    /**
     * 将JSON字符串还原为List<String>类型
     *
     * @param json 包含列表数据的JSON格式字符串，允许为null
     * @return 解析后的字符串列表，当输入null时返回null
     */
    @TypeConverter
    public static List<String> toList(String json) {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}