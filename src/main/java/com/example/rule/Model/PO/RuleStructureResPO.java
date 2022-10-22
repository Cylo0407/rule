package com.example.rule.Model.PO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Data
@Entity
@Accessors(chain = true)
@DynamicInsert
@DynamicUpdate
@Table(name = "rule_structure")
@NoArgsConstructor
public class RuleStructureResPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //内规条例切分的主键

    @Column(name = "title")
    private String title; //内规标题

    @Column(name = "chapter")
    private String chapter; //内规章名

    @Column(name = "section")
    private String section; //内规节名

    @Column(name = "text")
    private String text; //内规具体某一条例内容

}
