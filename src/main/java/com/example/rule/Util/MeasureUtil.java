package com.example.rule.Util;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.FileUtils.FileFormatConversionUtil;
import com.example.rule.Util.FileUtils.ReTagUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 度量类，从excel中把标注的那一列读出来，组成一个列表
 * 此处采用大师兄的做法来绘制PR曲线：不以百分比来设置测试点位，而以每一个点作为一个点位（毕竟数据量不大）
 * 关于PR曲线的介绍详见: https://zhuanlan.zhihu.com/p/88896868
 */
public class MeasureUtil {
    public static void main(String[] args) {
        try {
//            measure(new File("F:\\DataSet\\22银行内规项目候选数据集\\候选结果集\\候选结果.txt"));
            ReTagUtil.reTag(PathConfig.interpretationJsonPath, PathConfig.excelPath);
            measure(IOUtil.getTargetDir(PathConfig.interpretationJsonPath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 输入一个json文件/文件列表，度量其AP和MAP
     *
     * @param file json文件/目录位置
     * @throws IOException IO异常
     */
    public static void measure(File file) throws IOException {
        List<MatchResVO> matchResVOList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] jsonFiles = file.listFiles();
            for (File f : Objects.requireNonNull(jsonFiles)) {
                if (f.isFile()) {
                    matchResVOList.addAll(FileFormatConversionUtil.readJson(f));
                }
            }
        } else {
            matchResVOList.addAll(FileFormatConversionUtil.readJson(file));
        }
        List<List<Integer>> queriesTags = new ArrayList<>();
        for (MatchResVO query : matchResVOList) {
            List<Integer> tags = new ArrayList<>();
            for (MatchesBody document : query.getRuleMatchRes()) {
                tags.add(document.getRelevance());
            }
            System.out.println(query.getInput_fileName());
            System.out.println("AP 100%: " + getAP(tags));
            System.out.println("AP 50%: " + getAP(tags, 0.5));
            System.out.println("AP 20%: " + getAP(tags, 0.2));
            System.out.println("AP 5%: " + getAP(tags, 0.05));
            System.out.println("AP 1%: " + getAP(tags, 0.01));
            queriesTags.add(tags);
        }
//        System.out.println("MAP 100%: " + getMAP(queriesTags));
//        System.out.println("MAP 50%: " + getMAP(queriesTags, 0.5));
//        System.out.println("MAP 20%: " + getMAP(queriesTags, 0.2));
//        System.out.println("MAP 5%: " + getMAP(queriesTags, 0.05));
//        System.out.println("MAP 1%: " + getMAP(queriesTags, 0.01));
    }


    /**
     * AP计算：对PR曲线求积分，输入数据为全测试数据集的标记位tags
     * 由于excel中的内规条目已经按计算相似度进行过排序，因此tags被读取出来时是已经排过序的
     */
    public static double getAP(List<Integer> tags) {
        return getAP(tags, 1.0);
    }

    public static double getAP(List<Integer> tags, Double prop) {
        double sumOfPrecisions = 0.0;
        int currentTags = 0;
        int correctSoFar = 0;
        for (int i = 0; i < Math.round(tags.size() * prop); i++) {
            Integer tag = tags.get(i);
            currentTags++;
            if (tag == 1) {
                correctSoFar++;
                sumOfPrecisions += correctSoFar / (double) currentTags;
            }
        }
        double AP = sumOfPrecisions / Math.round(tags.size() * prop);
        return AP;
    }

    /**
     * MAP计算：所有AP的平均值，输入数据为所需要查询的所有输入tags
     */
    public static double getMAP(List<List<Integer>> queriesTags) {
        return getMAP(queriesTags, 1.0);
    }

    public static double getMAP(List<List<Integer>> queriesTags, Double prop) {
        double sumOfAP = 0.0;
        int zeros = 0;
        for (List<Integer> tags : queriesTags) {
            double AP = getAP(tags, prop);
            sumOfAP += AP;
            if (AP == 0.0) {
                zeros += 1;
            }
        }
        double MAP = sumOfAP / (double) (queriesTags.size() - zeros);
        return MAP;
    }
}
