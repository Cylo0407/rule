package com.example.rule.Model.PO.RuleStructureRes;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Body.TermBody;

import java.util.List;
import java.util.Map;

public interface RuleStructureResPO {

    Integer getId();

    /**
     * @param sims 键值对<id,similarity>
     * @return 通过键值对找到对应的文本内容
     */
    MatchesBody toMatchesBody(Map<Integer, Double> sims);

    List<TermBody> toTermsFreq();
}
