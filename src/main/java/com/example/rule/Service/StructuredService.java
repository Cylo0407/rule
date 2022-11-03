package com.example.rule.Service;

import java.util.List;

public interface StructuredService {
    /**
     * 结构化内规
     *
     * @param texts doc文本
     */
    boolean structureRules(List<String> texts, String title);

    /**
     * 结构化指定数目的处罚案例文本
     *
     * @param srcPath 存储案例的文件路径
     * @param num     获取文本的条数
     */
    boolean preDealPenaltyCaseContents(String srcPath, Integer num);

    /**
     * 结构化指定数目的政策解读文本
     *
     * @param srcDir 存储解读文本的目录
     * @param num    获取文本的数量
     */
    boolean preDealInterpretationContents(String srcDir, Integer num);
}
