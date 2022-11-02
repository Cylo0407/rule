package com.example.rule.Service.Impl;

import com.example.rule.Dao.*;
import com.example.rule.Model.PO.*;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.InputSplitUtils;
import com.example.rule.Util.RuleSplitUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.digester.Rule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Transactional
public class StructuredServiceImpl implements StructuredService {

    @Resource
    RuleStructureRepository ruleStructureRepository;
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

    @Override
    public boolean structureRules(List<String> texts) {
        List<Pair<String, Integer>> splitRes = RuleSplitUtils.split(texts);
        String title = "";
        String chapter = "";
        String section = null;
        String text = "";

        Pattern pattern = Pattern.compile("《(.*?)》");
//        TopLawsOfRulePO topLawsOfRulePO = new TopLawsOfRulePO();
        StringBuilder laws = new StringBuilder();


        for (int i = 0; i < splitRes.size(); i++) {
            Pair<String, Integer> split = splitRes.get(i);
            if (split.getRight() == 0) {
                title = split.getLeft();
                topLawsOfRuleRepository.save(new TopLawsOfRulePO().setTitle(title));
            } else if (split.getRight() == 1) {
                chapter = split.getLeft();
                System.out.println(chapter);
                section = null;
                boolean isLast = true;
                for (int j = i + 1; j < splitRes.size(); j++) {
                    if (splitRes.get(j).getRight() == 1) {
                        isLast = false;
                    }
                }
                if (isLast) return true;
            } else if (split.getRight() == 2) section = split.getLeft();
            else if (split.getRight() == 3) {
                text = split.getLeft();

                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String law = matcher.group(1);
                    System.out.println(law);
                    laws.append(law).append('、');
                    TopLawsOfRulePO topLawsOfRulePO = topLawsOfRuleRepository.findByTitle(title);
                    topLawsOfRulePO.setLaws(laws.toString());
                    topLawsOfRuleRepository.save(topLawsOfRulePO);
                }

                RuleStructureResPO ruleStructureResPO = new RuleStructureResPO()
                        .setTitle(title)
                        .setChapter(chapter)
                        .setSection(section)
                        .setText(text);
                ruleStructureRepository.save(ruleStructureResPO);
            }
        }

        return true;
    }


    @Override
    public boolean preDealPenaltyCaseContents(String srcPath, Integer num) {
        System.out.println("start");
        try {
            ArrayList<ArrayList<String>> penaltyCaseInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> penaltyCaseContents = new ArrayList<>();

            BufferedReader caseContextsReader = new BufferedReader(new FileReader(srcPath));
            for (int i = 0; i < num; i++) {
                String line = caseContextsReader.readLine();
                ArrayList<String> caseLine = new ArrayList<>(Arrays.asList(line.split(",")));
                caseLine.replaceAll(s -> s.replace("\"", ""));
                penaltyCaseInfos.add(new ArrayList<>(caseLine.subList(0, 3)));
                penaltyCaseContents.add(InputSplitUtils.dealAndStorePenaltyCaseContent(caseLine.get(3)));
            }

//            InputSplitUtils.writePenaltyContentsToDatabase(penaltyCaseInfos, penaltyCaseContents);
            System.out.println("write");
            for (int i = 0; i < num; i++) {
                Pattern pattern = Pattern.compile("《(.*?)》");
                StringBuffer laws = new StringBuffer();

                String title = penaltyCaseInfos.get(i).get(1);
                System.out.println(title);
                String docId = penaltyCaseInfos.get(i).get(2);
                System.out.println(docId);
                topLawsOfPenaltyCaseRepository.save(new TopLawsOfPenaltyCasePO().setTitle(title).setDocId(docId));
                Matcher titleMatcher = pattern.matcher(title);
                while (titleMatcher.find()) {
                    String law = titleMatcher.group(1);
                    System.out.println(law);
                    laws.append(law).append('、');
                    TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO = topLawsOfPenaltyCaseRepository.findByDocId(docId);
                    topLawsOfPenaltyCaseRepository.save(topLawsOfPenaltyCasePO.setLaws(laws.toString()));
                }
                ArrayList<String> penaltyCaseContent = penaltyCaseContents.get(i);
                System.out.println(penaltyCaseContent.size());
                for (int j = 1; j < penaltyCaseContent.size(); j++) {
                    String context = penaltyCaseContent.get(j);
                    System.out.println(context);

                    Matcher ctxMatcher = pattern.matcher(context);
                    while (ctxMatcher.find()) {
                        String law = ctxMatcher.group(1);
                        System.out.println(law);
                        laws.append(law).append('、');
                        TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO = topLawsOfPenaltyCaseRepository.findByDocId(docId);
                        topLawsOfPenaltyCaseRepository.save(topLawsOfPenaltyCasePO.setLaws(laws.toString()));
                    }

                    PenaltyCaseStructureResPO structureResPO = new PenaltyCaseStructureResPO()
                            .setTitle(title)
                            .setDocId(docId)
                            .setText(context);
                    penaltyCaseStructureRepository.save(structureResPO);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public boolean preDealInterpretationContents(String srcDir, Integer num) {
        System.out.println("start");
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
                        interpretationOfLawsContents.add(InputSplitUtils.dealAndStoreInterpretationOfLawsContent(line));
                    }
                    line = linesReader.readLine();
                }
                interpretationOfLawsInfos.add(interpretationOfLawsInfo);
            }

//            InputSplitUtils.writeInterpretationContentsToDatabase(interpretationOfLawsInfos, interpretationOfLawsContents);
            System.out.println("write");
            for (int i = 0; i < num; i++) {
                Pattern pattern = Pattern.compile("《(.*?)》");
                StringBuffer laws = new StringBuffer();

                String title = interpretationOfLawsInfos.get(i).get(0);
                System.out.println(title);
                String docId = interpretationOfLawsInfos.get(i).get(1);
                System.out.println(docId);
                topLawsOfInterpretationRepository.save(new TopLawsOfInterpretationPO().setTitle(title).setDocId(docId));
                Matcher titleMatcher = pattern.matcher(title);
                while (titleMatcher.find()) {
                    String law = titleMatcher.group(1);
                    System.out.println(law);
                    laws.append(law).append('、');
                    TopLawsOfInterpretationPO topLawsOfInterpretationPO = topLawsOfInterpretationRepository.findByDocId(docId);
                    topLawsOfInterpretationRepository.save(topLawsOfInterpretationPO.setLaws(laws.toString()));
                }
                ArrayList<String> interpretationOfLawsContent = interpretationOfLawsContents.get(i);
                System.out.println(interpretationOfLawsContent.size());
                for (int j = 0; j < interpretationOfLawsContent.size(); j++) {
                    String context = interpretationOfLawsContent.get(j);
                    System.out.println(context);

                    Matcher ctxMatcher = pattern.matcher(context);
                    while (ctxMatcher.find()) {
                        String law = ctxMatcher.group(1);
                        System.out.println(law);
                        laws.append(law).append('、');
                        TopLawsOfInterpretationPO topLawsOfInterpretationPO = topLawsOfInterpretationRepository.findByDocId(docId);
                        topLawsOfInterpretationRepository.save(topLawsOfInterpretationPO.setLaws(laws.toString()));
                    }


                    InterpretationStructureResPO structureResPO = new InterpretationStructureResPO()
                            .setTitle(title)
                            .setDocId(docId)
                            .setText(context);
                    interpretationStructureRepository.save(structureResPO);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

}
