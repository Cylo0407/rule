package com.example.rule.Model.Config;

public class PathConfig {

    public static String rulesPath = "/Users/cyl/Downloads/内规外化/数据集/标准版内规";

    public static String interpretationInputPath = "/Users/cyl/Downloads/内规外化/数据集/最终外规数据/政策解读/Interpretation";
    public static String sourceDocInputPath = "/Users/cyl/Downloads/内规外化/数据集/最终外规数据/政策解读/SourceDoc";
    public static String lawsJsonInputPath = "/Users/cyl/Downloads/内规外化/数据集/最终外规数据/laws_json";
    public static String regulationsJsonInputPath = "/Users/cyl/Downloads/内规外化/数据集/最终外规数据/regulations_json";

    public static String interpretationJsonPath = "/Users/cyl/Downloads/内规外化/json结果/政策解读/Interpretation";
    public static String lawsJsonPath = "/Users/cyl/Downloads/内规外化/json结果/laws_json";
    public static String regulationsJsonPath = "/Users/cyl/Downloads/内规外化/json结果/regulations_json";


    public static String termsInfoCache = "src/main/resources/terms_info_cache";

    public static String termsFrequencyCache = "terms_frequency_cache.txt";

    public static String termsTFIDFCache = "terms_TFIDF_cache.txt";

    public static String getFileMainName(String fileName) {
        return fileName.replaceFirst("\\..*", "");
    }

    public static String getFileSufName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

}
