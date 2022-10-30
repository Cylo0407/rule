package com.example.rule.Service.Impl;

import com.example.rule.Dao.InterpretationStructureRepository;
import com.example.rule.Dao.PenaltyCaseStructureRepository;
import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Model.PO.InterpretationStructureResPO;
import com.example.rule.Model.PO.PenaltyCaseStructureResPO;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.InputSplitUtils;
import com.example.rule.Util.RuleSplitUtils;
import net.bytebuddy.asm.Advice;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Service
@Transactional
public class StructuredServiceImpl implements StructuredService {

    @Resource
    RuleStructureRepository ruleStructureRepository;

    @Resource
    PenaltyCaseStructureRepository penaltyCaseStructureRepository;

    @Resource
    InterpretationStructureRepository interpretationStructureRepository;

    @Override
    public boolean structureRules(List<String> texts) {
        List<Pair<String, Integer>> splitRes = RuleSplitUtils.split(texts);
        String title = "";
        String chapter = "";
        String section = null;
        String text = "";

        for (int i = 0; i < splitRes.size(); i++) {
            Pair<String, Integer> split = splitRes.get(i);
            if (split.getRight() == 0) title = split.getLeft();
            else if (split.getRight() == 1) {
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
                RuleStructureResPO structureResPO = new RuleStructureResPO()
                        .setTitle(title)
                        .setChapter(chapter)
                        .setSection(section)
                        .setText(text);
                ruleStructureRepository.save(structureResPO);
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
                String title = penaltyCaseInfos.get(i).get(1);
                System.out.println(title);
                String docId = penaltyCaseInfos.get(i).get(2);
                System.out.println(docId);
                ArrayList<String> penaltyCaseContent = penaltyCaseContents.get(i);
                System.out.println(penaltyCaseContent.size());
                for (int j = 1; j < penaltyCaseContent.size(); j++) {
                    String context = penaltyCaseContent.get(j);
                    System.out.println(context);
                    PenaltyCaseStructureResPO structureResPO = new PenaltyCaseStructureResPO()
                            .setTitle(title)
                            .setDoc_id(docId)
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
                String title = interpretationOfLawsInfos.get(i).get(0);
                System.out.println(title);
                String docId = interpretationOfLawsInfos.get(i).get(1);
                System.out.println(docId);
                ArrayList<String> interpretationOfLawsContent = interpretationOfLawsContents.get(i);
                System.out.println(interpretationOfLawsContent.size());
                for (int j = 0; j < interpretationOfLawsContent.size(); j++) {
                    String context = interpretationOfLawsContent.get(j);
                    System.out.println(context);
                    InterpretationStructureResPO structureResPO = new InterpretationStructureResPO()
                            .setTitle(title)
                            .setDoc_id(docId)
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
