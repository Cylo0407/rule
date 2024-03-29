package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Model.VO.MatchResVO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Map;

public interface RetrieveStrategy {

    List<? extends RuleStructureResPO> findAll(JpaRepository<? extends RuleStructureResPO, Integer> jpaRepository);

    /**
     * 获取tf-idf列表
     *
     * @param model 使用的计算模型
     * @return tf-idf Map
     */
    Map<Integer, List<TermBody>> getTFIDFList(List<? extends RuleStructureResPO> ruleStructureResPOS, IR_Model model);

    void outputJson(String fileName, MatchResVO matchResVO);


}
