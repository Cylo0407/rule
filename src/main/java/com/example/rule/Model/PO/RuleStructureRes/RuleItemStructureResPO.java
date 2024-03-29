package com.example.rule.Model.PO.RuleStructureRes;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Util.TermProcessingUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Accessors(chain = true)
@DynamicInsert
@DynamicUpdate
@Table(name = "rule_structure")
@NoArgsConstructor
public class RuleItemStructureResPO implements Serializable, RuleStructureResPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //内规条例切分的主键

    @Column(name = "department")
    private String department; //内规所属部门

    @Column(name = "title")
    private String title; //内规标题

    @Column(name = "chapter")
    private String chapter; //内规章名

    @Column(name = "section")
    private String section; //内规节名

    @Column(name = "text")
    private String text; //内规具体某一条例内容

    @Override
    public MatchesBody toMatchesBody(Map<Integer, Double> sims) {
        Double similarity = sims.get(this.id);
        return new MatchesBody(similarity, this.id, this.department, this.title, this.text, 0);
    }


    @Override
    public List<TermBody> toTermsFreq() {
        return TermProcessingUtil.calTermFreq(this.getTitle() + this.getText());
    }
}
