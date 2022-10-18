package com.example.rule.Util;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class DocReadUtils {

    public static void main(String[] args) {
        File file = new File("src/File/《贷款担保管理办法（试行）》.doc");
//        String path = "src/File/《贷款担保管理办法（试行）》.doc";
        List<String> texts = readWord(file);
        System.out.println(texts.size());
        for (String text : texts)
            System.out.println(text);
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
                        linList.add(string);
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
                        linList.add(string);
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
