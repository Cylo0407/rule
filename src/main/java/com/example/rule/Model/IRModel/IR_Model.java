package com.example.rule.Model.IRModel;

import com.example.rule.Model.Body.TermBody;

import java.util.List;
import java.util.Map;

public interface IR_Model {

    /**
     *
     * @param inputTermsBodies 输入内容的Term信息列表
     * @param rulesTermsBodies 文本库的Term信息列表
     */
    void calTermsWeight(List<TermBody> inputTermsBodies, Map<Integer, List<TermBody>> rulesTermsBodies);
}
