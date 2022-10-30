package com.example.rule.Util;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputSplitUtils {

    /**
     * 预处理并存储一行案例处罚
     *
     * @param line 一行文本
     * @return 文本中的案例处罚部分
     */
    public static ArrayList<String> dealAndStorePenaltyCaseContent(String line) {
        return new ArrayList<>(Arrays.asList(line.split("[:\\s]")));
    }

    /**
     * 获取一行文本中的政策解读部分
     *
     * @param line 一行文本
     * @return 文本中的政策解读部分
     */
    public static ArrayList<String> dealAndStoreInterpretationOfLawsContent(String line) {
        //去掉text:前缀以及附件后缀以及空格
        line = line.replace(" ", "").replace("text:", "").replace(" ", "");

        //按三种不同的序号格式为文本添加分隔符
        Pattern form1 = Pattern.compile("[一二三四五六七八九十]、");
        Matcher form1matcher = form1.matcher(line);
        if (form1matcher.find()) {
            line = formMatch(line, form1matcher);
        } else {
            Pattern form2 = Pattern.compile("[一二三四五六七八九十]是");
            Matcher form2matcher = form2.matcher(line);
            if (form2matcher.find()) {
                line = formMatch(line, form2matcher);
            } else {
                Pattern form3 = Pattern.compile("\\([一二三四五六七八九十]\\)");
                Matcher form3matcher = form3.matcher(line);
                if (form3matcher.find()) {
                    line = formMatch(line, form3matcher);
                }
            }
        }

        Pattern pattern = Pattern.compile("附：.*");
        Matcher matcher = pattern.matcher(line);
        return new ArrayList<>(Arrays.asList(matcher.replaceAll("").split("[:\\s]")));
    }

    /**
     * 按格式处理解读文本
     *
     * @param line        原始字符串
     * @param formMatcher 匹配的格式
     * @return 更新后的字符串
     */
    public static String formMatch(String line, Matcher formMatcher) {
        int start = 0;//起始匹配的位置
        int count = 0;//已经匹配的次数
        while (formMatcher.find(start)) {
            start = formMatcher.start();
            line = line.substring(0, start + count) + " " + line.substring(start + count);
            start++;
            count++;
        }
        return line;
    }

    /**
     * 把法规解读写入数据库
     *
     * @param interpretationInfo     法规解读信息，每个ArrayList按序存有interpretation_title和doc_id。存入interpretation_text表
     * @param interpretationContents 法规解读内容，每个ArrayList下存放着拆分后的解读文本，对应数据库的content。和doc_id一起存入interpretation_content表
     */
    public static void writeInterpretationContentsToDatabase(ArrayList<ArrayList<String>> interpretationInfo, ArrayList<ArrayList<String>> interpretationContents) {
        System.out.println("Interpretation of laws has stored.");
    }


//    public static void main(String[] args) {
//        preDealPenaltyCaseContents("/Users/cyl/Downloads/第4组迭代一/caseBase数据库/punishment.csv", 50);
//        preDealInterpretationOfLawsContents("/Users/cyl/Downloads/data/Interpretation", 10);
//        System.out.println("Done");
//    }

}
