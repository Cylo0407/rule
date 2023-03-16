package com.example.rule.Util.FileUtils;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;
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
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FileFormatConversionUtil {
    public static void jsonFilesToExcelFiles(File jsonDir) throws IOException {
        if (jsonDir.exists()) {
            if (jsonDir.isDirectory()) {
                String excelDir = jsonDir.getPath() + "ExcelList";
                IOUtil.getTargetDir(new File(excelDir));
                for (File jsonFile : Objects.requireNonNull(jsonDir.listFiles())) {
                    String excelPath = excelDir + File.separator + PathConfig.getFileMainName(jsonFile.getName()) + ".xlsx";
                    jsonFileToExcelFile(jsonFile, excelPath);
                }
            }
        }
    }

    public static void excelFilesToJsonFiles(File excelDir) throws IOException {
        if (excelDir.exists()) {
            if (excelDir.isDirectory()) {
                String jsonDir = excelDir.getPath().replace("ExcelList", "");
                IOUtil.getTargetDir(new File(jsonDir));
                for (File excelFile : Objects.requireNonNull(excelDir.listFiles())) {
                    String jsonPath = jsonDir + File.separator + PathConfig.getFileMainName(excelFile.getName()) + ".json";
                    excelFileToJsonFile(excelFile, jsonPath);
                }
            }
        }
    }

    public static void jsonFileToExcelFile(File jsonFile, String excelFilePath) throws IOException {
        writeXlsxExcel(readJson(jsonFile), excelFilePath);
    }

    public static void excelFileToJsonFile(File excelFile, String jsonFilePath) throws IOException {
        writeJson(readXlsxExcel(excelFile), jsonFilePath);
    }

    public static List<MatchResVO> readXlsExcel(File xlsFile) throws IOException, BiffException {
        Workbook workbook = Workbook.getWorkbook(Files.newInputStream(xlsFile.toPath()));

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
                MatchesBody matchesBody = new MatchesBody(similarity, null, null, rule_fileName, rule_text, relevance);
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

    public static List<MatchResVO> readXlsxExcel(File xlsxFile) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(xlsxFile.toPath()));

        List<MatchResVO> matchResVOList = new ArrayList<>();
        int sheetNum = workbook.getNumberOfSheets();
        for (int index = 0; index < sheetNum; index++) {
            XSSFSheet sheet = workbook.getSheetAt(index);
            String input_fileName = sheet.getRow(0).getCell(0).getStringCellValue();
            String input_text = sheet.getRow(0).getCell(1).getStringCellValue();
            List<MatchesBody> matchesBodyList = new ArrayList<>();
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                Integer relevance = (int) sheet.getRow(i).getCell(0).getNumericCellValue();
                Double similarity = sheet.getRow(i).getCell(1).getNumericCellValue();
                Integer rule_id = (int) sheet.getRow(i).getCell(2).getNumericCellValue();
                String department = sheet.getRow(i).getCell(3).getStringCellValue();
                String rule_fileName = sheet.getRow(i).getCell(4).getStringCellValue();
                String rule_text = sheet.getRow(i).getCell(5).getStringCellValue();
                MatchesBody matchesBody = new MatchesBody(similarity, rule_id, department, rule_fileName, rule_text, relevance);
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

    public static void writeXlsExcel(List<MatchResVO> matchResVOList, String xlsFilePath) throws IOException, WriteException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File(xlsFilePath));

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

    public static void writeXlsxExcel(List<MatchResVO> matchResVOList, String xlsxFilePath) throws IOException {
        File xlsxFile = IOUtil.getTargetFile(xlsxFilePath);
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (MatchResVO matchResVO : matchResVOList) {
            XSSFSheet sheet = workbook.createSheet();
            int col = 0;
            int row = 0;
            XSSFRow xssfRow;
            XSSFCell xssfCell;

            xssfRow = sheet.createRow(row);
            xssfCell = xssfRow.createCell(col);
            xssfCell.setCellValue(matchResVO.getInput_fileName());
            col = 1;

            xssfCell = xssfRow.createCell(col);
            xssfCell.setCellValue(matchResVO.getInput_text());
            row++;

            for (MatchesBody matchesBody : matchResVO.getRuleMatchRes()) {
                try {
                    xssfRow = sheet.createRow(row);
                    col = 0;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getRelevance());
                    col++;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getSimilarity());
                    col++;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getRule_id());
                    col++;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getRule_department());
                    col++;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getRule_fileName());
                    col++;

                    xssfCell = xssfRow.createCell(col);
                    xssfCell.setCellValue(matchesBody.getRule_text());
                    row++;
                } catch (Exception e) {
                    System.out.println(matchResVO.getInput_fileName());
                }

            }
        }
        workbook.write(Files.newOutputStream(xlsxFile.toPath()));
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
            jsonFilesToExcelFiles(new File("F:\\DataSet\\JsonOutput\\Test\\TestArticle"));
//            excelFilesToJsonFiles(new File("F:\\DataSet\\JsonOutput\\Test\\TestArticleExcelList"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
