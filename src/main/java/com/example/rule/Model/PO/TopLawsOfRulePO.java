package com.example.rule.Model.PO;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Data
@Entity
@Accessors(chain = true)
@DynamicInsert
@DynamicUpdate
@Table(name = "rule_top_laws")
public class TopLawsOfRulePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //内规上位法集合主键

    @Column(name = "title")
    private String title; //内规标题

    @Column(name = "laws")
    private String laws; //内规标题

    public TopLawsOfRulePO setLaws(String laws) {
        this.laws = laws;
        return this;
    }

    public TopLawsOfRulePO setLaws(Iterable<String> laws) {
        this.laws = String.join("|", laws);
        return this;
    }

}
