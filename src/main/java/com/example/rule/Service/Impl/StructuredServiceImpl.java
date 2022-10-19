package com.example.rule.Service.Impl;

import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Model.PO.StructureResPO;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.RuleSplitUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
@Transactional
public class StructuredServiceImpl implements StructuredService {

    @Resource
    RuleStructureRepository ruleStructureRepository;

    @Override
    public boolean structureRules(List<String> texts) {
        List<Pair<String, Integer>> splitRes = RuleSplitUtils.split(texts);
        String title = "";
        String chapter = "";
        String section = null;
        String text = "";

        for (Pair<String, Integer> split : splitRes) {
            if (split.getRight() == 0) title = split.getLeft();
            else if (split.getRight() == 1) {
                chapter = split.getLeft();
                section = null;
            } else if (split.getRight() == 2) section = split.getLeft();
            else if (split.getRight() == 3) {
                text = split.getLeft();
                StructureResPO structureResPO = new StructureResPO()
                        .setTitle(title)
                        .setChapter(chapter)
                        .setSection(section)
                        .setText(text);
                ruleStructureRepository.save(structureResPO);
            }
        }

        return true;
    }
}
