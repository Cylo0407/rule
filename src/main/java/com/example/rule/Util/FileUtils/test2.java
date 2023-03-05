package com.example.rule.Util.FileUtils;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class test2 {//按章节运行json文件标记

    public static void main(String[] args) throws Exception {
        String jsonDirPath = PathConfig.interpretationJsonPath;//存放未处理json结果的文件夹
        File dir = new File(jsonDirPath);
        File[] fs = dir.listFiles();
        String excelFilePath = PathConfig.excelPath;//候选结果列表.xls

        for (File f : fs) {
            //1.从候选结果excel文件中读取出标记数据
            List<MatchResVO> excelList = FileFormatConversionUtil.readExcel(new File(excelFilePath));
            //2.从源json文件中读取出结果列表
            MatchResVO matchResVO = test.readSourceJson(f);
            //3.对结果列表进行标记
            for (MatchResVO mrv : excelList) {
                String name1 = matchResVO.getInput_fileName().trim();
                String name2 = mrv.getInput_fileName().trim();
                if (!name1.equals(name2)) continue;

                System.out.println(mrv.getInput_fileName());

                //3.1 将相关内规文本保存到一个单独的列表中
                List<String> revelenceRules = new ArrayList<>();
                for (MatchesBody mb : mrv.getRuleMatchRes()) {
                    if (mb.getRelevance() == 1)
                        revelenceRules.add(mb.getRule_text());
                }

                //3.2 遍历结果列表，如果结果文本章节内的条例包含在revelenceRules列表中，则整体章节记为相关
                for (String text : revelenceRules) {
                    for (MatchesBody mb : matchResVO.getRuleMatchRes()) {
                        if (mb.getRelevance() != 1)
                            if (mb.getRule_text().contains(text)) mb.setRelevance(1);
                    }
                }
            }
            //4.将标记完的结果列表输出成新的json文件
            String fileName = f.getName().split("\\.")[0];
            IOUtil.createJsonRes(fileName, matchResVO);
        }
    }
}
