package com.example.rule.Service.Strategy.StructuredGranularityStrategy;

import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StructuredStrategy {
    /**
     * 对读取的文本行进行分段
     *
     * @param texts 文本行列表
     * @return 按粒度分段的结果
     */
    List<? extends RuleStructureResPO> segmentTextsToPOs(List<String> texts, String title, String department);

    void save(JpaRepository<? extends RuleStructureResPO, Integer> jpaRepository, RuleStructureResPO po);

    void saveAll(JpaRepository<? extends RuleStructureResPO, Integer> jpaRepository, List<? extends RuleStructureResPO> resPOS);
}
