package com.example.rule.Util;

import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import com.alibaba.fastjson.JSON;


import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class IOUtil {
    public static Object readObject(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(Files.newInputStream(file.toPath()));
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return object;
    }

    public static void writeObject(File file, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    public static ArrayList<String> readLines(File file) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = bufferedReader.readLine();
        while (line != null) {
            lines.add(line);
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return lines;
    }

    public static void writeLines(File file, List<String> lines) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            bufferedWriter.write(line);
        }
        bufferedWriter.close();
    }

    public static List<String> readWordLines(File file) {
        String fileSufName = PathConfig.getFileSufName(file.getName());
        String filePath = file.getPath();
        List<String> linList = new ArrayList<String>();
        String buffer = "";
        try {
            if (fileSufName.equals(".doc")) {
                WordExtractor extractor = new WordExtractor(Files.newInputStream(file.toPath()));
                buffer = extractor.getText();
                extractor.close();
            } else if (fileSufName.equals(".docx")) {
                OPCPackage opcPackage = POIXMLDocument.openPackage(filePath);
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                buffer = extractor.getText();
                extractor.close();
            } else {
                return null;
            }

            if (buffer.length() > 0) {
                //使用回车换行符分割字符串
                String[] contexts = buffer.split("\\r?\\n");
                for (String string : contexts) {
                    if (!string.equals("")) {
                        linList.add(string.trim());
                    }
                }
            }

            return linList;
        } catch (Exception e) {
            System.out.print("error---->" + filePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断文档类型并读取文件中的文本
     *
     * @param file 文件
     * @return 文本内容列表
     */
    public static List<String> getTargetLines(File file) throws IOException {
        String sufName = PathConfig.getFileSufName(file.getName());
        if (sufName.equals(".txt")) {
            return IOUtil.readLines(file);
        } else if (sufName.equals(".doc") || sufName.equals(".docx")) {
            return IOUtil.readWordLines(file);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * 返回目标文件，如果该文件不存在则创建该文件
     *
     * @param filePath 目标文件路径
     * @return 目标文件
     */
    public static File getTargetFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }


    /**
     * 输出json结果文件
     *
     * @param fileName 外规文件名称
     * @param matchResVO 匹配结果
     *
     * @return 目标文件
     */
    public static void createJsonRes(String fileName, MatchResVO matchResVO) {
        String filePath = PathConfig.interpretationJsonPath + '/' + fileName + ".json";
        System.out.println(filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
                Writer writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
                String matchData = JSON.toJSONString(matchResVO);
                writer.write(matchData);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
