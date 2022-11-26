package com.example.rule.Util;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

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
        return lines;
    }

    public static void writeLines(File file, List<String> lines) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        for (String line : lines) {
            bufferedWriter.write(line);
        }
        bufferedWriter.close();
    }

    public static List<String> readWord(File file) {
        String fileName = file.getName();
        String filePath = file.getPath();
        List<String> linList = new ArrayList<String>();
        String buffer = "";
        try {
            if (fileName.endsWith(".doc")) {
                InputStream is = new FileInputStream(file);
                WordExtractor ex = new WordExtractor(is);
                buffer = ex.getText();
                ex.close();

                if (buffer.length() > 0) {
                    //使用回车换行符分割字符串
                    String[] arry = buffer.split("\\r?\\n");
                    for (String string : arry) {
                        if (!string.equals(""))
                            linList.add(string.trim());
                    }
                }
            } else if (fileName.endsWith(".docx")) {
                OPCPackage opcPackage = POIXMLDocument.openPackage(filePath);
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                buffer = extractor.getText();
                extractor.close();

                if (buffer.length() > 0) {
                    //使用换行符分割字符串
                    String[] arry = buffer.split("\\r?\\n");
                    for (String string : arry) {
                        if (!string.equals(""))
                            linList.add(string.trim());
                    }
                }
            } else {
                return null;
            }

            return linList;
        } catch (Exception e) {
            System.out.print("error---->" + filePath);
            e.printStackTrace();
            return null;
        }
    }
}
