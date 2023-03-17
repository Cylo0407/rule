package com.example.rule.Service.Strategy.StructuredGranularityStrategy;

import com.example.rule.Dao.RuleArticleStructureRepository;
import com.example.rule.Model.PO.RuleStructureRes.RuleArticleStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.FileUtils.FileStructuredUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public class ArticleStructuredStrategy implements StructuredStrategy {

    private int articleId = 1;

    @Override
    public List<? extends RuleStructureResPO> segmentTextsToPOs(List<String> texts, String title, String department) {
        ArrayList<RuleArticleStructureResPO> ruleArticleStructureResPOS = new ArrayList<>();

        // 新建文章结构
        RuleArticleStructureResPO articlePO = new RuleArticleStructureResPO();
        StringBuilder ruleArticleText = new StringBuilder();

        // 清洗全篇文章的内容
        for (String text : texts) {
            text = FileStructuredUtil.cleansedText(text);
            if (FileStructuredUtil.isAppendix(text)) {
                // 不录入附则内容
                break;
            } else {
                // 如果还未读到附则，则添加内容到文章中
                ruleArticleText.append(text);
            }
        }
        // 保存全文
        if (!ruleArticleText.toString().equals("")) {
            articlePO.setId(this.articleId++)
                    .setTitle(title)
                    .setDepartment(department)
                    .setText(ruleArticleText.toString());
            ruleArticleStructureResPOS.add(articlePO);
        }

        return ruleArticleStructureResPOS;
    }

    @Override
    public void save(JpaRepository<? extends RuleStructureResPO, Integer> ruleArticleStructureRepository, RuleStructureResPO po) {
        ((RuleArticleStructureRepository) ruleArticleStructureRepository).save((RuleArticleStructureResPO) po);
    }

    @Override
    public void saveAll(JpaRepository<? extends RuleStructureResPO, Integer> ruleArticleStructureRepository, List<? extends RuleStructureResPO> resPOS) {
        ((RuleArticleStructureRepository) ruleArticleStructureRepository).saveAll((List<RuleArticleStructureResPO>) resPOS);
    }
}
