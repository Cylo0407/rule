# 2023.3.4修改

### 1、修改MatchesBody实体结构

1. 添加字段 rule_id : 内规id逐渐、rule_chapter : 内规条例所属章节；

2. 修改MatchesBody实体构造函数；

3. 修改`RuleChapterStructureResPO.java`文件中`public MatchesBody toMatchesBody(Map<Integer, Double> sims)`方法；

4. 修改`RuleItemStructureResPO.java`文件中`public MatchesBody toMatchesBody(Map<Integer, Double> sims)`方法；

5. 修改`RuleArticleStructureResPO.java`文件中`public MatchesBody toMatchesBody(Map<Integer, Double> sims)`方法；

6. 修改`FileFormatConversionUtil.java`文件中`public static List<MatchResVO> readExcel(File excelFile)`方法。

   ```java
   ruleItemStructureResPO = ruleStructureRepository.findByText(rule_text);
   Integer id = ruleItemStructureResPO.getId();
   String chapter = ruleItemStructureResPO.getChapter();
   MatchesBody matchesBody = 
     	new MatchesBody(similarity, id, rule_fileName, chapter, rule_text, relevance);
   ```

### 2、增加Util.FileUtils.test工具类，用于标记json结果
