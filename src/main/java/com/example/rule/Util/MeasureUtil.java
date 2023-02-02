package com.example.rule.Util;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.VO.MatchResVO;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 度量类，从excel中把标注的那一列读出来，组成一个列表
 * 此处采用大师兄的做法来绘制PR曲线：不以百分比来设置测试点位，而以每一个点作为一个点位（毕竟数据量不大）
 * 关于PR曲线的介绍详见: https://zhuanlan.zhihu.com/p/88896868
 */
public class MeasureUtil {
    public static void main(String[] args) {
        try {
            measure(new File("F:\\DataSet\\银行内规项目数据集\\候选结果集\\候选结果.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void measure(File jsonFile) throws IOException {
        List<MatchResVO> matchResVOList = FileFormatConversionUtil.readJson(jsonFile);
        List<List<Integer>> queriesTags = new ArrayList<>();
        for (MatchResVO query : matchResVOList) {
            List<Integer> tags = new ArrayList<>();
            for (MatchesBody document : query.getRuleMatchRes()) {
                tags.add(document.getRelevance());
            }
            System.out.println(query.getInput_fileName() + "\n" + "AP: " + getAP(tags));
            queriesTags.add(tags);
        }
        System.out.println("MAP: " + getMAP(queriesTags));
    }


    /**
     * AP计算：对PR曲线求积分，输入数据为全测试数据集的标记位tags
     * 由于excel中的内规条目已经按计算相似度进行过排序，因此tags被读取出来时是已经排过序的
     */
    public static double getAP(List<Integer> tags) {
        double sumOfPrecisions = 0.0;
        int currentTags = 0;
        int correctSoFar = 0;
        for (Integer tag : tags) {
            currentTags++;
            if (tag == 1) {
                correctSoFar++;
                sumOfPrecisions += correctSoFar / (double) currentTags;
            }
        }
        double AP = sumOfPrecisions / tags.size();
        return AP;
    }

    /**
     * MAP计算：所有AP的平均值，输入数据为所需要查询的所有输入tags
     */
    public static double getMAP(List<List<Integer>> queriesTags) {
        double sumOfAP = 0.0;
        int zeros = 0;
        for (List<Integer> tags : queriesTags) {
            double AP = getAP(tags);
            sumOfAP += AP;
            if (AP == 0.0) {
                zeros += 1;
            }
        }
        double MAP = sumOfAP / (double) (queriesTags.size() - zeros);
        return MAP;
    }
}
