package com.gc.commons.txt.read.listener;

import com.gc.commons.txt.TxtBaseModel;

/**
 * 读取监听接口
 * @author shizhongming
 * 2020/7/7 10:48 下午
 */
public interface ReadListener<T extends TxtBaseModel> {

    /**
     * 异常处理
     * @param exception 异常信息
     */
    void onException(Exception exception) ;

    /**
     * 处理数据
     * @param data 数据
     */
    void invoke(T data);

    /**
     * 解析完毕执行
     */
    void doAfterAllAnalysed();

    /**
     * 是否有下一行
     * @param lineData 行数据
     * @return 是否有下一行
     */
    boolean hasNext(String lineData);

    /**
     * 分隔符
     * @return 分隔符
     */
    default String separator() {
     return "\t";
    }

    /**
     * 是否读取该行
     * @param lineData 行数据
     * @return 是否读取
     */
    default boolean readLine(String lineData) {
        return true;
    }

    /**
     * 格式化行
     * @param lineData 行
     * @return 格式化后的行
     */
    default String formatLine(String lineData) {
        return lineData;
    }
}
