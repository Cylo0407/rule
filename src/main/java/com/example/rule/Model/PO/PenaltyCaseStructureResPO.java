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
@Table(name = "penalty_case_structure")
@NoArgsConstructor
public class PenaltyCaseStructureResPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //处罚案例条例切分的主键

    @Column(name = "title")
    private String title; //处罚案例标题

    @Column(name = "doc_id")
    private String doc_id; //处罚案例文本号

    @Column(name = "text")
    private String text; //处罚案例具体某一条例内容
}
