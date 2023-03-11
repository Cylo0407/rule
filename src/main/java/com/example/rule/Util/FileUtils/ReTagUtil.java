package com.example.rule.Util.FileUtils;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReTagUtil {
    public static void main(String[] args) throws Exception {
        ReTagUtil.reTag(PathConfig.interpretationJsonPath, PathConfig.excelPath);
    }

    /**
     * 对生成的新列表重新标记
     *
     * @param jsonDirPath   存放未处理json结果的文件夹
     * @param excelFilePath 候选结果列表.xls
     */
    public static void reTag(String jsonDirPath, String excelFilePath) throws Exception {
        File jsonDir = new File(jsonDirPath);
        File[] jsonDirList = jsonDir.listFiles();
        for (File f : Objects.requireNonNull(jsonDirList)) {
            //1.从候选结果excel文件中读取出标记数据
            List<MatchResVO> excelList = FileFormatConversionUtil.readExcel(new File(excelFilePath));
            //2.从未处理json文件中读取出结果列表
            MatchResVO matchResVO = readSourceJson(f);
            //3.对结果列表进行标记
            for (MatchResVO mrv : excelList) {
                String name1 = matchResVO.getInput_fileName().trim();
                String name2 = mrv.getInput_fileName().trim();
                if (!name1.equals(name2)) {
                    continue;
                }

                //3.1 将相关内规文本保存到一个单独的列表中
                List<String> relevanceRules = new ArrayList<>();
                for (MatchesBody mb : mrv.getRuleMatchRes()) {
                    if (mb.getRelevance() == 1) {
                        relevanceRules.add(mb.getRule_text());
                    }
                }

//                3.2 遍历结果列表，如果其中结果文本在relevanceRules列表中，则记为相关
//                for (MatchesBody mb : matchResVO.getRuleMatchRes()) {
//                    if (relevanceRules.contains(mb.getRule_text())) {
//                        mb.setRelevance(1);
//                    }
//                }
                for (String text : relevanceRules) {
                    for (MatchesBody mb : matchResVO.getRuleMatchRes()) {
                        if (mb.getRelevance() != 1) {
                            if (mb.getRule_text().contains(text)) {
                                mb.setRelevance(1);
                            }
                        }
                    }
                }

                int reTagNum = 0;
                for (MatchesBody mb : matchResVO.getRuleMatchRes()) {
                    if (mb.getRelevance() == 1) {
                        reTagNum++;
                    }
                }

                System.out.println(matchResVO.getInput_fileName() + " 中包含" + matchResVO.getRuleMatchRes().size() +
                        "条返回结果和" + reTagNum + "条相关结果");
            }
            //4.将标记完的结果列表输出成新的json文件
            String fileName = PathConfig.getFileMainName(f.getName());
            IOUtil.createJsonRes(fileName, matchResVO);
        }
    }


    public static MatchResVO readSourceJson(File jsonFile) throws Exception {
        return FileFormatConversionUtil.readJson(jsonFile).get(0);
    }
}
