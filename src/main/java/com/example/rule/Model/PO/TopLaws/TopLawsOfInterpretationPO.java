package com.example.rule.Model.PO.TopLaws;

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
@Table(name = "interpretation_top_laws")
public class TopLawsOfInterpretationPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "doc_id")
    private String docId;

    @Column(name = "laws")
    private String laws;

    public TopLawsOfInterpretationPO setLaws(String laws) {
        this.laws = laws;
        return this;
    }

    public TopLawsOfInterpretationPO setLaws(Iterable<String> laws) {
        this.laws = String.join("|", laws);
        return this;
    }
}
