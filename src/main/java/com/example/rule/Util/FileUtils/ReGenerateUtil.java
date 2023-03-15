package com.example.rule.Util.FileUtils;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;
import org.javatuples.Triplet;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReGenerateUtil {
    //获取按章和按条的json结果；
    //取条结果和相应章节结果相似度进行一定运算。
    public static void regenerateSimilarity(String jsonItemDirPath, String jsonChapterDirPath) throws Exception {
        //TODO 路径还需要修改，按章结果和按条结果需要分开存
        File jsonItemDir = new File(jsonItemDirPath);
        File[] itemFiles = jsonItemDir.listFiles();
        File jsonChapterDir = new File(jsonChapterDirPath);
        File[] chapterFiles = jsonChapterDir.listFiles();

        String pattern = "第[一二三四五六七八九十百|1234567890]+条";//正则匹配规则
        Pattern p = Pattern.compile(pattern);

        for (int i = 0; i < Objects.requireNonNull(itemFiles).length; i++) {
            //1.从两个源json文件中分别读取出结果列表
            MatchResVO ruleMatchResVO = ReTagUtil.readSourceJson(itemFiles[i]);
            MatchResVO chapterMatchResVO = ReTagUtil.readSourceJson(Objects.requireNonNull(chapterFiles)[i]);

            //2.将章节匹配结果中相关内规文本保存到chapterTexts列表中
            //按章匹配结果抽取出三元组：Triplet<按章匹配结果中的文本, 相似度, 文本中包含的具体条数>
            List<Triplet<String, Double, Integer>> chapterTexts = new ArrayList<>();
            for (MatchesBody mb : chapterMatchResVO.getRuleMatchRes()) {
                String text = mb.getRule_text();
                Matcher m = p.matcher(text);
                int ruleCnt = 0;//文本中包含的具体条数
                while (m.find()) ruleCnt++;
                chapterTexts.add(new Triplet<>(text, mb.getSimilarity(), ruleCnt));
            }

            //3.遍历按条匹配结果列表，在chapterTexts列表找到相关章节，取其相似度进行运算
            List<MatchesBody> res = new ArrayList<>();
            for (MatchesBody mb : ruleMatchResVO.getRuleMatchRes()) {
                for (Triplet<String, Double, Integer> triplet : chapterTexts) {
                    if (triplet.getValue0().contains(mb.getRule_text())) {
                        Double var = algorithm1(triplet.getValue2(), triplet.getValue1(), mb.getSimilarity());
//                    Double var = algorithm2(triplet.getValue2(), triplet.getValue1(), mb.getSimilarity());
                        mb.setSimilarity(var);
                        break;
                    }
                }
                res.add(mb);
            }
            res.sort((mb1, mb2) -> mb2.getSimilarity().compareTo(mb1.getSimilarity()));

            MatchResVO matchResVO = new MatchResVO();
            matchResVO.setInput_fileName(ruleMatchResVO.getInput_fileName());
            matchResVO.setInput_text(ruleMatchResVO.getInput_text());
            matchResVO.setRuleMatchRes(res);

            //4.将标记完的结果列表输出成新的json文件
            IOUtil.recreateJsonRes(itemFiles[i], matchResVO);
        }
    }

    public static Double algorithm1(int ruleCnt, Double chapter_sim, Double rule_sim) {
//        System.out.println("rule: " + rule_sim);
//        System.out.println("chapter: " + chapter_sim);
        return (chapter_sim / ruleCnt) + (rule_sim - rule_sim / ruleCnt);
    }

    public static Double algorithm2(int ruleCnt, Double chapter_sim, Double rule_sim) {
//        System.out.println("rule: " + rule_sim);
//        System.out.println("chapter: " + chapter_sim);
        return chapter_sim * 0.5 + rule_sim * 0.5;
    }
}
