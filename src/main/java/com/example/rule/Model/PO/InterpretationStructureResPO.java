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
@Table(name = "interpretation_structure")
public class InterpretationStructureResPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id; //政策解读切分的主键

    @Column(name = "title")
    private String title;

    @Column(name = "doc_id")
    private String doc_id;

    @Column(name = "text")
    private String text;
}
