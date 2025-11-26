package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSettingAccount;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.support.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "section", uniqueConstraints ={
        @UniqueConstraint(name = "section_code_company_unique_constraint", columnNames = {"code", "company_id"}),
        @UniqueConstraint(name = "section_identifier_company_unique_constraint", columnNames = {"code", "company_id"})
})
public class Section extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "identifier", nullable = false)
    private Integer identifier;

    @Column(name = "label")
    private String label;
    @Column(name = "description")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private SectionType type;

    @Column(name = "base_formula")
    private String baseFormula;

    @Column(name = "rate_percent")
    private boolean ratePercent;
    @Column(name = "rate_formula")
    private String rateFormula;

    @Column(name = "value_formula")
    private String valueFormula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category", nullable = false)
    @ToString.Exclude
    private SectionCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature")
    private SectionNature nature;

    @OneToOne(fetch = FetchType.LAZY)
    private AccountingSettingAccount accounting;

    @Convert(converter = StringListConverter.class)
    @Column(name = "label_keys")
    private List<String> labelKeys;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parental_id")
    @ToString.Exclude
    private Section parental;


    @Builder.Default
    @OneToMany(targetEntity = SectionVariable.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payslip_section_id", nullable = false)
    @ToString.Exclude
    private List<SectionVariable> variables= new ArrayList<>();

    @Basic(fetch = FetchType.LAZY)
    @Formula("""
       (
            SELECT EXISTS(SELECT 1   FROM section s LEFT JOIN label_setting lb ON s.id = id AND (s.label_keys LIKE lb.key OR s.label_keys LIKE concat('%##UNIQUE_LIST_SEPARATOR##',lb.key, '%' )
              OR  s.label_keys LIKE CONCAT('%',lb.key, '##UNIQUE_LIST_SEPARATOR##%' ))
                        WHERE    (EXISTS(SELECT 1 FROM payslip_setting_element e where e.section = id) OR
                                  EXISTS(WITH cte AS ( SELECT REGEXP_REPLACE(CONCAT(' ', COALESCE(s2.base_formula, ''), ' ', COALESCE(s2.value_formula, ''), ' ', COALESCE(s2.rate_formula, '')), '[+*()/-]', ' ') AS  combinedFormula FROM section s2
                                  ) SELECT cte.combinedFormula FROM cte WHERE cte.combinedFormula  RLIKE CONCAT(' ', code, ' ') OR cte.combinedFormula RLIKE CONCAT(' LBL_', lb.key, ' ')
                                  )))
)
            """)
    private Boolean referenced;


}
