package com.example.rule.Service.Impl;

import com.example.rule.Dao.InterpretationStructureRepository;
import com.example.rule.Dao.PenaltyCaseStructureRepository;
import com.example.rule.Dao.RuleChapterStructureRepository;
import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfInterpretationRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfPenaltyCaseRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfRuleRepository;
import com.example.rule.Model.PO.*;
import com.example.rule.Model.PO.TopLaws.TopLawsOfInterpretationPO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfPenaltyCasePO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.FilePreprocessUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Transactional
public class StructuredServiceImpl implements StructuredService {

    @Resource
    RuleStructureRepository ruleStructureRepository;
    @Resource
    RuleChapterStructureRepository ruleChapterStructureRepository;
    @Resource
    TopLawsOfRuleRepository topLawsOfRuleRepository;
    @Resource
    PenaltyCaseStructureRepository penaltyCaseStructureRepository;
    @Resource
    TopLawsOfPenaltyCaseRepository topLawsOfPenaltyCaseRepository;
    @Resource
    InterpretationStructureRepository interpretationStructureRepository;
    @Resource
    TopLawsOfInterpretationRepository topLawsOfInterpretationRepository;

    /**
     * 结构化内规并提取上位法
     *
     * @param texts doc文本
     * @return true
     */
    @Override
    public boolean structureRules(List<String> texts, String title) {
        // 拿取所有切分后的内规文本
        List<Pair<String, Integer>> splitRulesInfo = FilePreprocessUtil.split(texts);

        ArrayList<RuleStructureResPO> ruleStructureResPOS = new ArrayList<>();
        ArrayList<RuleChapterStructureResPO> ruleChapterStructureResPOS = new ArrayList<>();
        TopLawsOfRulePO topLawsOfRulePO = new TopLawsOfRulePO();

        // TODO 运用一下这部分文字
        String textBeforeChapter = "";
        String chapter = "";
        String section = null;
        String text = "";

        HashSet<String> relatedLaws = new HashSet<>();

        StringBuilder chapter_text = new StringBuilder();
        for (Pair<String, Integer> ruleInfo : splitRulesInfo) {
            switch (ruleInfo.getRight()) {
                case 0:
                    // 非章节内容
                    textBeforeChapter = ruleInfo.getLeft();
                    topLawsOfRulePO.setTitle(title);
                    break;
                case 1:
                    // 第x章
                    //遇到下一章，将之前章节保存
                    if (!chapter.equals("")) {
                        ruleChapterStructureResPOS.add(new RuleChapterStructureResPO()
                                .setTitle(title)
                                .setChapter(chapter)
                                .setText(chapter_text.toString())
                        );
                    }
                    chapter = ruleInfo.getLeft();
                    section = null;
                    chapter_text = new StringBuilder();
                    break;
                case 2:
                    // 第x节
                    section = ruleInfo.getLeft();
                    break;
                case 3:
                    // 第x条
                    text = ruleInfo.getLeft();
                    chapter_text.append(text);
                    findAndStoreArticleTitleFromText(text, relatedLaws);

                    ruleStructureResPOS.add(new RuleStructureResPO()
                            .setTitle(title)
                            .setChapter(chapter)
                            .setSection(section)
                            .setText(text));
                    break;
                default:
                    break;
            }
        }
        if (!chapter.equals("")) {
            ruleChapterStructureResPOS.add(new RuleChapterStructureResPO()
                    .setTitle(title)
                    .setChapter(chapter)
                    .setText(chapter_text.toString())
            );
        }

        ruleChpterStructureResPOS.add(new RuleChpterStructureResPO()
                .setTitle(title)
                .setChapter(chapter)
                .setText(chapter_text.toString())
        );

        topLawsOfRuleRepository.save(topLawsOfRulePO.setLaws(relatedLaws));
        ruleStructureRepository.saveAll(ruleStructureResPOS);
        ruleChapterStructureRepository.saveAll(ruleChapterStructureResPOS);
        return true;
    }


    /**
     * 结构化指定数目的处罚案例文本
     *
     * @param srcPath 存储案例的文件路径
     * @param num     获取文本的条数
     * @return true
     */
    @Override
    public boolean preDealPenaltyCaseContents(String srcPath, Integer num) {
        try {
            ArrayList<ArrayList<String>> penaltyCaseInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> penaltyCaseContents = new ArrayList<>();

            BufferedReader caseContextsReader = new BufferedReader(new FileReader(srcPath));
            for (int i = 0; i < num; i++) {
                String line = caseContextsReader.readLine();
                ArrayList<String> caseLine = new ArrayList<>(Arrays.asList(line.split(",")));
                caseLine.replaceAll(s -> s.replace("\"", ""));
                penaltyCaseInfos.add(new ArrayList<>(caseLine.subList(0, 3)));
                penaltyCaseContents.add(FilePreprocessUtil.dealAndStorePenaltyCaseContent(caseLine.get(3)));
            }

            ArrayList<TopLawsOfPenaltyCasePO> topLawsOfPenaltyCasePOS = new ArrayList<>();
            ArrayList<PenaltyCaseStructureResPO> penaltyCaseStructureResPOS = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO = new TopLawsOfPenaltyCasePO();

                String title = penaltyCaseInfos.get(i).get(1);
                String docId = penaltyCaseInfos.get(i).get(2);
                topLawsOfPenaltyCasePO.setTitle(title).setDocId(docId);

                HashSet<String> relatedLaws = new HashSet<>();
                findAndStoreArticleTitleFromText(title, relatedLaws);

                ArrayList<String> penaltyCaseContent = penaltyCaseContents.get(i);
                for (String context : penaltyCaseContent) {
                    findAndStoreArticleTitleFromText(context, relatedLaws);
                    penaltyCaseStructureResPOS.add(new PenaltyCaseStructureResPO()
                            .setTitle(title)
                            .setDocId(docId)
                            .setText(context));
                }
                topLawsOfPenaltyCasePOS.add(topLawsOfPenaltyCasePO.setLaws(relatedLaws));
            }

            topLawsOfPenaltyCaseRepository.saveAll(topLawsOfPenaltyCasePOS);
            penaltyCaseStructureRepository.saveAll(penaltyCaseStructureResPOS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * TODO 去除关于相关法规提取的功能并移入ExtractController中
     *
     * @param srcDir 存储解读文本的目录
     * @param num    获取文本的数量
     * @return true
     */
    @Override
    public boolean preDealInterpretationContents(String srcDir, Integer num) {
        try {
            ArrayList<ArrayList<String>> interpretationOfLawsInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> interpretationOfLawsContents = new ArrayList<>();

            File directory = new File(srcDir);
            File[] interpretations = directory.listFiles();
            for (int i = 0; i < num; i++) {
                ArrayList<String> interpretationOfLawsInfo = new ArrayList<>();
                LineNumberReader linesReader = new LineNumberReader(new FileReader(Objects.requireNonNull(interpretations)[i]));
                String line = linesReader.readLine();
                while (line != null) {
                    if (line.startsWith("title:")) {
                        interpretationOfLawsInfo.add(line.replace("title:", ""));
                    } else if (line.startsWith("docId:")) {
                        interpretationOfLawsInfo.add(line.replace("docId:", ""));
                    } else if (line.startsWith("text:")) {
                        interpretationOfLawsContents.add(FilePreprocessUtil.dealAndStoreInterpretationOfLawsContent(line));
                    }
                    line = linesReader.readLine();
                }
                interpretationOfLawsInfos.add(interpretationOfLawsInfo);
            }

            ArrayList<TopLawsOfInterpretationPO> topLawsOfInterpretationPOS = new ArrayList<>();
            ArrayList<InterpretationStructureResPO> interpretationStructureResPOS = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                TopLawsOfInterpretationPO topLawsOfInterpretationPO = new TopLawsOfInterpretationPO();

                String title = interpretationOfLawsInfos.get(i).get(0);
                String docId = interpretationOfLawsInfos.get(i).get(1);
                topLawsOfInterpretationPO.setTitle(title).setDocId(docId);

                HashSet<String> relatedLaws = new HashSet<>();
                findAndStoreArticleTitleFromText(title, relatedLaws);

                ArrayList<String> interpretationOfLawsContent = interpretationOfLawsContents.get(i);
                for (String context : interpretationOfLawsContent) {
                    findAndStoreArticleTitleFromText(context, relatedLaws);
                    interpretationStructureResPOS.add(new InterpretationStructureResPO()
                            .setTitle(title)
                            .setDocId(docId)
                            .setText(context));
                }
                topLawsOfInterpretationPOS.add(topLawsOfInterpretationPO.setLaws(relatedLaws));
            }

            topLawsOfInterpretationRepository.saveAll(topLawsOfInterpretationPOS);
            interpretationStructureRepository.saveAll(interpretationStructureResPOS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }


    /**
     * 查找文本中的文章名并存储
     *
     * @param text          待查找的文本
     * @param articleTitles 文章标题集合
     */
    private void findAndStoreArticleTitleFromText(String text, HashSet<String> articleTitles) {
        Pattern pattern = Pattern.compile("《[^》]+》");
        Matcher titleMatcher = pattern.matcher(text);
        while (titleMatcher.find()) {
            String law = titleMatcher.group();
            if (law.length() > 4) {
                articleTitles.add(law);
            }
        }
    }

}
