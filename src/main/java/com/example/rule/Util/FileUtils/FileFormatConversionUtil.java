package com.example.rule.Util.FileUtils;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.VO.MatchResVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FileFormatConversionUtil {
    public static void jsonFileToExcelFile(File jsonFile, String excelFileName) throws WriteException, IOException {
        writeExcel(readJson(jsonFile), excelFileName);
    }

    public static void excelFileToJsonFile(File excelFile, String jsonFileName) throws IOException, BiffException {
        writeJson(readExcel(excelFile), jsonFileName);
    }

    public static List<MatchResVO> readExcel(File excelFile) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(Files.newInputStream(excelFile.toPath()));

        List<MatchResVO> matchResVOList = new ArrayList<>();
        int sheetNum = workbook.getNumberOfSheets();
        for (int index = 0; index < sheetNum; index++) {
            Sheet sheet = workbook.getSheet(index);
            String input_fileName = sheet.getCell(0, 0).getContents();
            String input_text = sheet.getCell(1, 0).getContents();
            List<MatchesBody> matchesBodyList = new ArrayList<>();
            for (int i = 1; i < sheet.getRows(); i++) {
                Integer relevance = Integer.valueOf(sheet.getCell(0, i).getContents());
                Double similarity = ((NumberCell) sheet.getCell(1, i)).getValue();
                String rule_fileName = sheet.getCell(2, i).getContents();
                String rule_text = sheet.getCell(3, i).getContents();
                MatchesBody matchesBody = new MatchesBody(similarity, null, rule_fileName, rule_text, relevance);
                matchesBodyList.add(matchesBody);
            }
            MatchResVO matchResVO = new MatchResVO();
            matchResVO.setInput_fileName(input_fileName);
            matchResVO.setInput_text(input_text);
            matchResVO.setRuleMatchRes(matchesBodyList);
            matchResVOList.add(matchResVO);
        }
        workbook.close();
        return matchResVOList;
    }

    public static List<MatchResVO> readJson(File jsonFile) throws IOException {
        JsonReader jsonReader = new JsonReader(new InputStreamReader(Files.newInputStream(jsonFile.toPath()), StandardCharsets.UTF_8));
        Gson gson = new GsonBuilder().create();

        List<MatchResVO> matchResVOList = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            MatchResVO matchResVO = gson.fromJson(jsonReader, MatchResVO.class);
            matchResVOList.add(matchResVO);
        }
        jsonReader.close();
        return matchResVOList;
    }

    public static void writeExcel(List<MatchResVO> matchResVOList, String excelFileName) throws IOException, WriteException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File(excelFileName));

        int sheetNum = 0;
        for (MatchResVO matchResVO : matchResVOList) {
            WritableSheet sheet = workbook.createSheet(String.valueOf(sheetNum), sheetNum);
            int col = 0;
            int row = 0;

            Label input_fileName = new Label(col, row, matchResVO.getInput_fileName());
            sheet.addCell(input_fileName);
            col = 1;

            Label input_text = new Label(col, row, matchResVO.getInput_text());
            sheet.addCell(input_text);
            row++;

            for (MatchesBody matchesBody : matchResVO.getRuleMatchRes()) {
                col = 0;

                Number relevance = new Number(col, row, matchesBody.getRelevance());
                sheet.addCell(relevance);
                col++;

                Number similarity = new Number(col, row, matchesBody.getSimilarity());
                sheet.addCell(similarity);
                col++;

                Label rule_fileName = new Label(col, row, matchesBody.getRule_fileName());
                sheet.addCell(rule_fileName);
                col++;

                Label rule_text = new Label(col, row, matchesBody.getRule_text());
                sheet.addCell(rule_text);
                row++;
            }
            sheetNum++;
        }
        workbook.write();
        workbook.close();
    }

    public static void writeJson(List<MatchResVO> matchResVOList, String jsonFileName) throws IOException {
        Writer writer = Files.newBufferedWriter(Paths.get(jsonFileName), StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        gson.toJson(matchResVOList, writer);
        writer.close();
    }

    public static void main(String[] args) {
        try {
//            jsonFileToExcelFile(new File("F:\\DataSet\\银行内规项目数据集\\候选结果集\\候选结果.txt"), "F:\\DataSet\\银行内规项目数据集\\候选结果集\\候选结果.xls");
            excelFileToJsonFile(new File("F:\\DataSet\\银行内规项目数据集\\候选结果集\\候选结果_1128.xls"), "F:\\DataSet\\银行内规项目数据集\\候选结果集\\候选结果2.txt");
        } catch (IOException | BiffException e) {
            throw new RuntimeException(e);
        }
    }
}
