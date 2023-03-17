package com.example.rule.Service.Strategy.StructuredGranularityStrategy;

import com.example.rule.Dao.RuleItemStructureRepository;
import com.example.rule.Model.PO.RuleStructureRes.RuleItemStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.FileUtils.FileStructuredUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public class ItemStructuredStrategy implements StructuredStrategy {

    private int itemId = 1;

    @Override
    public List<? extends RuleStructureResPO> segmentTextsToPOs(List<String> texts, String title, String department) {
        ArrayList<RuleItemStructureResPO> ruleItemStructureResPOS = new ArrayList<>();

        // 新建条结构
        RuleItemStructureResPO itemPO = new RuleItemStructureResPO();
        String section = "";
        String chapter = "";
        StringBuilder ruleItemText = new StringBuilder();

        boolean beginItem = false;

        boolean isItem;
        boolean isSection;
        boolean isChapter;
        // 获取每条文本的内容
        for (String text : texts) {
            text = FileStructuredUtil.cleansedText(text);

            if (FileStructuredUtil.isAppendix(text)) {
                // 不录入附则内容
                break;
            }

            isItem = FileStructuredUtil.isStartWithItemTitle(text);
            isSection = FileStructuredUtil.isStartWithSectionTitle(text);
            isChapter = FileStructuredUtil.isStartWithChapterTitle(text);

            if (isItem | isSection | isChapter) {
                // 当前条目已经录入完毕，存储条目并创建一个新的条目
                if (!ruleItemText.toString().equals("") && beginItem) {
                    itemPO.setId(this.itemId++)
                            .setTitle(title)
                            .setSection(section)
                            .setChapter(chapter)
                            .setDepartment(department)
                            .setText(ruleItemText.toString());
                    ruleItemStructureResPOS.add(itemPO);
                    itemPO = new RuleItemStructureResPO();
                    ruleItemText = new StringBuilder();
                }
                if (isItem) {
                    if (!beginItem) {
                        beginItem = true;
                        ruleItemText = new StringBuilder();
                    }
                    ruleItemText.append(text);
                }
                if (isSection) {
                    section = FileStructuredUtil.getSectionTitle(text);
                }
                if (isChapter) {
                    chapter = FileStructuredUtil.getChapterTitle(text);
                    section = "";
                }
            } else {
                // 如果当前条没有结束，添加新的内容到条中
                ruleItemText.append(text);
            }
        }
        // 保存最后一条
        if (!ruleItemText.toString().equals("")) {
            itemPO.setId(this.itemId++)
                    .setTitle(title)
                    .setSection(section)
                    .setChapter(chapter)
                    .setDepartment(department)
                    .setText(ruleItemText.toString());
            ruleItemStructureResPOS.add(itemPO);
        }

        return ruleItemStructureResPOS;
    }

    @Override
    public void save(JpaRepository<? extends RuleStructureResPO, Integer> ruleItemStructureRepository, RuleStructureResPO po) {
        ((RuleItemStructureRepository) ruleItemStructureRepository).save((RuleItemStructureResPO) po);
    }

    @Override
    public void saveAll(JpaRepository<? extends RuleStructureResPO, Integer> ruleItemStructureRepository, List<? extends RuleStructureResPO> resPOS) {
        ((RuleItemStructureRepository) ruleItemStructureRepository).saveAll((List<RuleItemStructureResPO>) resPOS);
    }
}
