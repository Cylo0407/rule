package com.example.rule.Util;


import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputUtils {


    public InputUtils() {

    }

    /**
     * 预处理并存储一行案例处罚
     *
     * @param line 一行文本
     * @return 文本中的案例处罚部分
     */
    public ArrayList<String> dealAndStorePenaltyCaseContent(String line) {
        ArrayList<String> res = new ArrayList<>();
        res.add(line);
        return res;
//        return new ArrayList<>(Arrays.asList(line.split("[:\\s]")));
    }

    /**
     * 预处理指定数目的处罚案例文本
     *
     * @param srcPath 存储案例的文件路径
     * @param num     获取文本的条数
     */
    public void preDealPenaltyCaseContents(String srcPath, Integer num) {
        try {
            ArrayList<ArrayList<String>> penaltyCaseInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> penaltyCaseContents = new ArrayList<>();

            BufferedReader caseContextsReader = new BufferedReader(new FileReader(srcPath));
            for (int i = 0; i < num; i++) {
                String line = caseContextsReader.readLine();
                ArrayList<String> caseLine = new ArrayList<>(Arrays.asList(line.split(",")));
                caseLine.replaceAll(s -> s.replace("\"", ""));
                penaltyCaseInfos.add(new ArrayList<>(caseLine.subList(0, 3)));
                penaltyCaseContents.add(dealAndStorePenaltyCaseContent(caseLine.get(3)));
            }
            writePenaltyContentsToDatabase(penaltyCaseInfos, penaltyCaseContents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取一行文本中的政策解读部分
     *
     * @param line 一行文本
     * @return 文本中的政策解读部分
     */
    public ArrayList<String> dealAndStoreInterpretationOfLawsContent(String line) {
        //去掉text:前缀以及附件后缀以及空格
        line = line.replace(" ", "").replace("text:", "");

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
    private String formMatch(String line, Matcher formMatcher) {
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
     * 预处理指定数目的政策解读文本
     *
     * @param srcDir 存储解读文本的目录
     * @param num    获取文本的数量
     */
    public void preDealInterpretationOfLawsContents(String srcDir, Integer num) {
        try {
            ArrayList<ArrayList<String>> interpretationOfLawsInfos = new ArrayList<>();
            ArrayList<ArrayList<String>> interpretationOfLawsContents = new ArrayList<>();

            File directory = new File(srcDir);
            File[] interpretations = directory.listFiles();
            for (int i = 0; i < num; i++) {
                ArrayList<String> interpretationOfLawsInfo = new ArrayList<>();
                LineNumberReader linesReader = new LineNumberReader(new FileReader(Objects.requireNonNull(interpretations)[i]));
                String line = linesReader.readLine();
                while (line != null) {
                    if (line.startsWith("title:")) {
                        interpretationOfLawsInfo.add(line.replace("title:", ""));
                    } else if (line.startsWith("docId:")) {
                        interpretationOfLawsInfo.add(line.replace("docId:", ""));
                    } else if (line.startsWith("text:")) {
                        interpretationOfLawsContents.add(dealAndStoreInterpretationOfLawsContent(line));
                    }
                    line = linesReader.readLine();
                }
                interpretationOfLawsInfos.add(interpretationOfLawsInfo);
            }
            writeInterpretationContentsToDatabase(interpretationOfLawsInfos, interpretationOfLawsContents);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //TODO 将待解读的文本拆分，存入数据库表中

    /**
     * 把处罚案例写入数据库
     *
     * @param penaltyInfo     处罚案例信息，每个ArrayList按序存有对应数据库的case_id,case_title和doc_id。存入penalty_case表
     * @param penaltyContents 处罚案例内容，每个ArrayList下存放着拆分后的案例文本，对应数据库的content。和doc_id一起存入penalty_content表
     */
    private void writePenaltyContentsToDatabase(ArrayList<ArrayList<String>> penaltyInfo, ArrayList<ArrayList<String>> penaltyContents) {
        System.out.println("Penalty Case has stored.");
    }

    /**
     * 把法规解读写入数据库
     *
     * @param interpretationInfo     法规解读信息，每个ArrayList按序存有interpretation_title和doc_id。存入interpretation_text表
     * @param interpretationContents 法规解读内容，每个ArrayList下存放着拆分后的解读文本，对应数据库的content。和doc_id一起存入interpretation_content表
     */
    private void writeInterpretationContentsToDatabase(ArrayList<ArrayList<String>> interpretationInfo, ArrayList<ArrayList<String>> interpretationContents) {
        System.out.println("Interpretation of laws has stored.");
    }


    public static void main(String[] args) {
        InputUtils inputUtils = new InputUtils();
        inputUtils.preDealPenaltyCaseContents("F:\\魔鬼的力量\\_A研究生资料\\面向互联网+助教材料\\外部数据\\第4组迭代一\\第四组casebase最新数据\\caseBase数据库\\punishment.csv", 200);
        inputUtils.preDealInterpretationOfLawsContents("F:\\魔鬼的力量\\_A研究生资料\\面向互联网+助教材料\\外部数据\\data_小组10_主题六\\data\\Interpretation", 10);
        System.out.println("Done");
    }

}
