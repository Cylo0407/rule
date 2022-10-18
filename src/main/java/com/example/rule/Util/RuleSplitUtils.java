package com.example.rule.Util;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RuleSplitUtils {
    static char[] cnArr = new char[]{'一', '二', '三', '四', '五', '六', '七', '八', '九'};

//    public static List<Pair<String, Boolean>> split(File file) {
//        List<String> texts = new ArrayList<>();
//        try {
//            try (Scanner sc = new Scanner(new FileReader(file))) {
//                while (sc.hasNextLine()) {  //按行读取字符串
//                    texts.add(sc.nextLine());
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return split(texts);
//    }

    public static List<Pair<String, Boolean>> split(List<String> texts) {
        List<Pair<String, Boolean>> res = new ArrayList<>();
        StringBuilder resLine = new StringBuilder();
        int chapter = 1;
        int section = 1;
        int index = 1;
        int currLine;
        boolean isItem = false;
        for (currLine = 0; currLine < texts.size(); currLine++) {
            String tmpLine = texts.get(currLine).replaceAll("　", " ");
            tmpLine = tmpLine.replaceAll(" ", "");
            tmpLine = tmpLine.replaceAll("\\s", "");
            if (tmpLine.startsWith("第一章")) {
                chapter++;
            }
            if (tmpLine.startsWith("第一节")) {
                section++;
            }
            if (tmpLine.startsWith("第一条")) {
                break;
            }
            resLine.append(texts.get(currLine));
            resLine.append("\n");
        }
        for (; currLine < texts.size(); currLine++) {
            String line = texts.get(currLine);
            String tmpLine = line.replaceAll("　", " ");
            tmpLine = tmpLine.replaceAll("\\s", "");
            String mark = "第" + arabicNumToChineseNum(index) + "条";
            if (tmpLine.startsWith("第" + arabicNumToChineseNum(chapter) + "章")) {
                res.add(Pair.of(resLine.toString(), isItem));
                resLine = new StringBuilder(line);
                chapter++;
                isItem = false;
            } else if (tmpLine.startsWith(mark)) {
                res.add(Pair.of(resLine.toString(), isItem));
                resLine = new StringBuilder(line);
                index++;
                isItem = true;
            } else if (tmpLine.startsWith("第" + arabicNumToChineseNum(section) + "节")) {
                res.add(Pair.of(resLine.toString(), isItem));
                resLine = new StringBuilder(line);
                section++;
                isItem = false;
            } else if (tmpLine.startsWith("第一节")) {
                res.add(Pair.of(resLine.toString(), isItem));
                resLine = new StringBuilder(line);
                section = 2;
                isItem = false;
            } else {
                resLine.append(line);
            }
            resLine.append("\n");
        }

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

    public static void main(String[] args) {
        File file = new File("src/File/《贷款担保管理办法（试行）》.doc");
        List<String> texts = DocReadUtils.readWord(file);

        List<Pair<String, Boolean>> splitRes = split(texts);
        for (Pair<String, Boolean> pair : splitRes) {
            System.out.println(pair.getRight());
            System.out.println(pair.getLeft());
        }

//        int num = 111;
//        String num_chinese = arabicNumToChineseNum(num);
//        System.out.println(num_chinese);
    }
}
