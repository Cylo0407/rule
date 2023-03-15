package com.example.rule.Service.Impl;

import com.example.rule.Model.Config.NumberConfig;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Service.MeasureService;
import com.example.rule.Util.FileUtils.ReGenerateUtil;
import com.example.rule.Util.FileUtils.ReTagUtil;
import com.example.rule.Util.IOUtil;
import com.example.rule.Util.MeasureUtil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class MeasureServiceImpl implements MeasureService {
    @Override
    public void doMeasure(String granularity) {
        try {
            // 拿到上一个生成json的文件夹并重新标记
            ReTagUtil.reTag(PathConfig.interpretationJsonPath + granularity, PathConfig.excelPath);
            MeasureUtil.measure(IOUtil.getTargetFile(PathConfig.interpretationJsonPath + granularity));
            IOUtil.clearTermsInfoCache();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reGenerateItemThenMeasure() {
        try {
            // 对前两次执行的结果进行相似度重算
            ReGenerateUtil.regenerateSimilarity(
                    PathConfig.interpretationJsonPath + "Item",
                    PathConfig.interpretationJsonPath + "Chapter"
            );
            doMeasure("Item");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reGenerateChapterThenMeasure() {

    }
}
