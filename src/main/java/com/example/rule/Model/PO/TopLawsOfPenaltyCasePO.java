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
@Table(name = "penalty_case_top_laws")
public class TopLawsOfPenaltyCasePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //处罚案例库上位法集合主键

    @Column(name = "title")
    private String title; //处罚案例库标题

    @Column(name = "doc_id")
    private String docId; //处罚案例库标题

    @Column(name = "laws")
    private String laws; //处罚案例库标题
}
