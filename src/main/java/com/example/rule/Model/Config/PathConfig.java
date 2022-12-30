package com.example.rule.Model.Config;

public class PathConfig {

    public static String rulesPath = "F:\\DataSet\\A标准版内规";

    public static String inputPath = "F:\\DataSet\\A政策解读库\\Interpretation";

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
