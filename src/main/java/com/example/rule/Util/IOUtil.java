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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        IOUtil.getTargetDir(file.getParentFile());
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    public static void getTargetDir(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    public static void recreateJsonRes(File jsonFile, MatchResVO matchResVO) throws IOException {
        IOUtil.createJsonRes(jsonFile, matchResVO);
    }

    /**
     * 输出json结果文件
     *
     * @param outputJsonFile 输出的json文件
     * @param matchResVO     匹配结果
     */
    public static void createJsonRes(File outputJsonFile, MatchResVO matchResVO) throws IOException {
        Writer writer = new OutputStreamWriter(Files.newOutputStream(outputJsonFile.toPath()), StandardCharsets.UTF_8);
        List<MatchResVO> matchResVOWrapper = new ArrayList<>();
        matchResVOWrapper.add(matchResVO);
        String matchData = JSON.toJSONString(matchResVOWrapper, true);
        writer.write(matchData);
        writer.flush();
        writer.close();
    }

    public static void clearTermsInfoCache() {
        File termsInfoCacheDir = new File(PathConfig.termsInfoCache);
        File[] termsInfoCaches = termsInfoCacheDir.listFiles();
        for (File cache : Objects.requireNonNull(termsInfoCaches)) {
            cache.delete();
        }
    }
}
