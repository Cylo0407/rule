package com.example.rule.Util.FileUtils;


import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePreprocessUtil {

    static char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九'};

    /**
     * 预处理并存储一行案例处罚
     *
     * @param line 一行文本
     * @return 文本中的案例处罚部分
     */
    public static ArrayList<String> dealAndStorePenaltyCaseContent(String line) {
        ArrayList<String> penaltyCaseContents = new ArrayList<>(Arrays.asList(line.split("[:\\s]")));
        return integrateContents(penaltyCaseContents);
//        return new ArrayList<>(Arrays.asList(line.split("[:\\s]")));
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
        ArrayList<String> lines = new ArrayList<>();
        lines.add(line);
        return lines;

//        //按三种不同的序号格式为文本添加分隔符
//        Pattern form1 = Pattern.compile("[一二三四五六七八九十]、");
//        Matcher form1matcher = form1.matcher(line);
//        if (form1matcher.find()) {
//            line = formMatch(line, form1matcher);
//        } else {
//            Pattern form2 = Pattern.compile("[一二三四五六七八九十]是");
//            Matcher form2matcher = form2.matcher(line);
//            if (form2matcher.find()) {
//                line = formMatch(line, form2matcher);
//            } else {
//                Pattern form3 = Pattern.compile("\\([一二三四五六七八九十]\\)");
//                Matcher form3matcher = form3.matcher(line);
//                if (form3matcher.find()) {
//                    line = formMatch(line, form3matcher);
//                }
//            }
//        }
//
//        Pattern pattern = Pattern.compile("附：.*");
//        Matcher matcher = pattern.matcher(line);
//
//        ArrayList<String> interpretationContents = new ArrayList<>(Arrays.asList(matcher.replaceAll("").split("[:\\s]")));
//        return integrateContents(interpretationContents);
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

    private static ArrayList<String> integrateContents(ArrayList<String> contents) {
        ArrayList<String> integratedContents = new ArrayList<>(contents);
//        StringBuilder sb = new StringBuilder();
//        for (String content : contents) {
//            if (sb.length() < 50) {
//                sb.append(content);
//            } else {
//                integratedContents.add(sb.toString());
//                sb = new StringBuilder(content);
//            }
//        }
        return integratedContents;
    }

    public static List<Pair<String, Integer>> split(List<String> texts) {
        List<Pair<String, Integer>> res = new ArrayList<>();
        StringBuilder resLine = new StringBuilder();
        int chapter = 1;
        int section = 1;
        int index = 1;
        int currLine;
//        boolean isItem = false;
        Integer type = 0;
        for (currLine = 0; currLine < texts.size() - 1; currLine++) {
            String tmpLine = texts.get(currLine).replaceAll("　", " ");
            tmpLine = tmpLine.replaceAll(" ", "");
            tmpLine = tmpLine.replaceAll("\\s", "");

            String nextLine = texts.get(currLine + 1).replaceAll("　", " ");
            nextLine = nextLine.replaceAll(" ", "");
            nextLine = nextLine.replaceAll("\\s", "");

            if (nextLine.startsWith("第一章") || nextLine.startsWith("第一节") || nextLine.startsWith("第一条")) {
                resLine.append(texts.get(currLine));
                resLine.append('\n');
                currLine++;
                break;
            }
        }
        for (; currLine < texts.size(); currLine++) {
            String line = texts.get(currLine);
            String tmpLine = line.replaceAll("　", " ");
            tmpLine = tmpLine.replaceAll("\\s", "");
            String mark = "第" + arabicNumToChineseNum(index) + "条";
            if (tmpLine.startsWith("第" + arabicNumToChineseNum(chapter) + "章")) {
                if (tmpLine.endsWith("附则")) {
                    System.out.println("delete 附则");
                    break;
                }
                res.add(Pair.of(resLine.toString(), type));
                resLine = new StringBuilder(line);
                chapter++;
//                isItem = false;
                type = 1;
            } else if (tmpLine.startsWith(mark)) {
                res.add(Pair.of(resLine.toString(), type));
                resLine = new StringBuilder(line);
                index++;
//                isItem = true;
                type = 3;
            } else if (tmpLine.startsWith("第" + arabicNumToChineseNum(section) + "节")) {
                res.add(Pair.of(resLine.toString(), type));
                resLine = new StringBuilder(line);
                section++;
//                isItem = false;
                type = 2;
            } else if (tmpLine.startsWith("第一节")) {
                res.add(Pair.of(resLine.toString(), type));
                resLine = new StringBuilder(line);
                section = 2;
//                isItem = false;
                type = 2;
            } else if (tmpLine.startsWith("附件") || tmpLine.startsWith("附表")) {
                resLine.deleteCharAt(resLine.length() - 1);
                break;
            } else {
                if ((!checkcountname(line)) && currLine + 1 >= texts.size()) {
                    break;
                }
                resLine.append(line);
            }
            resLine.append("\n");
        }

        res.add(Pair.of(resLine.toString(), type));
        return res;
    }

    /**
     * 将数字转换为中文数字， 这里只写到了百
     *
     * @param intInput
     * @return
     */
    private static String arabicNumToChineseNum(int intInput) {
        String si = String.valueOf(intInput);
        String sd = "";
        if (si.length() == 1) {
            if (intInput == 0) {
                return sd;
            }
            sd += cnArr[intInput - 1];
            return sd;
        } else if (si.length() == 2) {
            if (si.substring(0, 1).equals("1")) {
                sd += "十";
                if (intInput % 10 == 0) {
                    return sd;
                }
            } else
                sd += (cnArr[intInput / 10 - 1] + "十");
            sd += arabicNumToChineseNum(intInput % 10);
        } else if (si.length() == 3) {
            sd += (cnArr[intInput / 100 - 1] + "百");
            if (String.valueOf(intInput % 100).length() < 2) {
                if (intInput % 100 == 0) {
                    return sd;
                }
                sd += "零";
            } else if (intInput % 100 < 20) {
                sd += "一";
            }
            sd += arabicNumToChineseNum(intInput % 100);
        }
        return sd;
    }

    private static boolean checkcountname(String countname) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(countname);
        if (m.find()) {
            return true;
        }
        return false;
    }


//    public static void main(String[] args) {
//        preDealPenaltyCaseContents("/Users/cyl/Downloads/第4组迭代一/caseBase数据库/punishment.csv", 50);
//        preDealInterpretationOfLawsContents("/Users/cyl/Downloads/data/Interpretation", 10);
//        System.out.println("Done");
//    }

}
