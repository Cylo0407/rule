package com.example.rule.Service.Strategy.StructuredGranularityStrategy;

import com.example.rule.Dao.RuleChapterStructureRepository;
import com.example.rule.Model.PO.RuleStructureRes.RuleChapterStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.FileUtils.FileStructuredUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public class ChapterStructuredStrategy implements StructuredStrategy {

    private int chapterId = 1;

    @Override
    public List<? extends RuleStructureResPO> segmentTextsToPOs(List<String> texts, String title, String department) {
        ArrayList<RuleChapterStructureResPO> ruleChapterStructureResPOS = new ArrayList<>();

        // 新建章结构
        RuleChapterStructureResPO chapterPO = new RuleChapterStructureResPO();
        String chapter = "";
        StringBuilder ruleChapterText = new StringBuilder();

        boolean beginChapter = false;

        boolean isChapter;
        // 获取每章文本的内容
        for (String text : texts) {
            text = FileStructuredUtil.cleansedText(text);

            if (FileStructuredUtil.isAppendix(text)) {
                // 不录入附则内容
                break;
            }

            isChapter = FileStructuredUtil.isStartWithChapterTitle(text);

            if (isChapter) {
                // 当前章已经录入完毕，存储章并创建一个新的章
                if (!ruleChapterText.toString().equals("") && beginChapter) {
                    chapterPO.setId(this.chapterId++)
                            .setTitle(title)
                            .setChapter(chapter)
                            .setDepartment(department)
                            .setText(ruleChapterText.toString());
                    ruleChapterStructureResPOS.add(chapterPO);
                    chapterPO = new RuleChapterStructureResPO();
                    ruleChapterText = new StringBuilder();
                }
                if (!beginChapter) {
                    beginChapter = true;
                    ruleChapterText = new StringBuilder();
                }
                chapter = FileStructuredUtil.getChapterTitle(text);
            } else {
                // 如果当前章没有结束，添加新的内容到条中
                ruleChapterText.append(text);
            }
        }
        // 保存最后一章
        if (!ruleChapterText.toString().equals("")) {
            chapterPO.setId(this.chapterId++)
                    .setTitle(title)
                    .setChapter(chapter)
                    .setDepartment(department)
                    .setText(ruleChapterText.toString());
            ruleChapterStructureResPOS.add(chapterPO);
        }

        return ruleChapterStructureResPOS;
    }

    @Override
    public void save(JpaRepository<? extends RuleStructureResPO, Integer> ruleChapterStructureRepository, RuleStructureResPO po) {
        ((RuleChapterStructureRepository) ruleChapterStructureRepository).save((RuleChapterStructureResPO) po);
    }

    @Override
    public void saveAll(JpaRepository<? extends RuleStructureResPO, Integer> ruleChapterStructureRepository, List<? extends RuleStructureResPO> resPOS) {
        ((RuleChapterStructureRepository) ruleChapterStructureRepository).saveAll((List<RuleChapterStructureResPO>) resPOS);
    }
}
