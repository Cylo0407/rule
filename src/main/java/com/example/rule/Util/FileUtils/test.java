package com.example.rule.Util.FileUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class test {

    public static void main(String[] args) throws Exception {
        String jsonDirPath = PathConfig.interpretationJsonPath;//存放未处理json结果的文件夹
        File dir = new File(jsonDirPath);
        File[] fs = dir.listFiles();
        String excelFilePath = PathConfig.excelPath;//候选结果列表.xls

        for (File f : fs) {
            //1.从候选结果excel文件中读取出标记数据
            List<MatchResVO> excelList = FileFormatConversionUtil.readExcel(new File(excelFilePath));
            //2.从未处理json文件中读取出结果列表
            MatchResVO matchResVO = readSourceJson(f);
            //3.对结果列表进行标记
            for (MatchResVO mrv : excelList) {
                if (!matchResVO.getInput_fileName().equals(mrv.getInput_fileName())) continue;

                System.out.println(mrv.getInput_fileName());

                //3.1 将相关内规文本保存到一个单独的列表中
                List<String> revelenceRules = new ArrayList<>();
                for (MatchesBody mb : mrv.getRuleMatchRes()) {
                    if (mb.getRelevance() == 1) revelenceRules.add(mb.getRule_text());
                }

                //3.2 遍历结果列表，如果其中结果文本在revelenceRules列表中，则记为相关
                for (MatchesBody mb : matchResVO.getRuleMatchRes()) {
                    if (revelenceRules.contains(mb.getRule_text())) {
                        mb.setRelevance(1);
                        System.out.println(mb.getRule_text());
                    }
                }
            }
            //4.将标记完的结果列表输出成新的json文件
            String fileName = f.getName().split("\\.")[0];
            IOUtil.createJsonRes(fileName, matchResVO);
        }
    }

    private static MatchResVO readSourceJson(File file) throws Exception {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(new FileInputStream(file), "Utf-8");
        int ch = 0;
        StringBuffer sb = new StringBuffer();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        String jsonStr = sb.toString();
        JSONObject jsonObject = JSON.parseObject(jsonStr);
        String input_fileName = jsonObject.getString("input_fileName");
        String input_text = jsonObject.getString("input_text");
        String resList = jsonObject.getString("ruleMatchRes");  //读取结果列表
        List<MatchesBody> ruleMatchRes = JSON.parseArray(resList, MatchesBody.class);   //转化成列表

        MatchResVO matchResVO = new MatchResVO();
        matchResVO.setInput_fileName(input_fileName);
        matchResVO.setInput_text(input_text);
        matchResVO.setRuleMatchRes(ruleMatchRes);

        return matchResVO;
    }
}
