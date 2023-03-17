package com.example.rule.Util.FileUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileStructuredUtil {
    private static final Pattern itemPattern = Pattern.compile("^第[一二三四五六七八九十百]+条");
    private static final Pattern sectionPattern = Pattern.compile("^第[一二三四五六七八九十百]+节");
    private static final Pattern chapterPattern = Pattern.compile("^第[一二三四五六七八九十百]+章");

    public static String cleansedText(String text) {
        return text.trim().replaceAll("\\s+", " ").replaceAll(" ", "");
    }

    public static boolean isStartWithItemTitle(String text) {
        Matcher itemMatcher = itemPattern.matcher(text);
        return itemMatcher.find();
    }

    public static boolean isStartWithSectionTitle(String text) {
        Matcher sectionMatcher = sectionPattern.matcher(text);
        return sectionMatcher.find();
    }

    public static boolean isStartWithChapterTitle(String text) {
        Matcher chapterMatcher = chapterPattern.matcher(text);
        return chapterMatcher.find();
    }

    public static String getItemTitle(String text) {
        Matcher itemMatcher = itemPattern.matcher(text);
        if (itemMatcher.find()) {
            return itemMatcher.group();
        } else {
            return "";
        }
    }

    public static String getSectionTitle(String text) {
        Matcher sectionMatcher = sectionPattern.matcher(text);
        if (sectionMatcher.find()) {
            return sectionMatcher.group();
        } else {
            return "";
        }
    }

    public static String getChapterTitle(String text) {
        Matcher chapterMatcher = chapterPattern.matcher(text);
        if (chapterMatcher.find()) {
            return chapterMatcher.group();
        } else {
            return "";
        }
    }

    public static boolean isAppendix(String text) {
        return text.endsWith("附则");
    }

}
