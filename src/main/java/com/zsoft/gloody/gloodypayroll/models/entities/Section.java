package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.SectionNature;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import com.zsoft.gloody.gloodypayroll.support.StringListConverter;
import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import org.hibernate.annotations.Formula;

import java.util.List;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "section", uniqueConstraints = @UniqueConstraint(name = "section_code_company_unique_constraint", columnNames = {"code", "company_id"}))
public class Section extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code")
    private String code;

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

    @Convert(converter = StringListConverter.class)
    @Column(name = "label_keys")
    private List<String> labelKeys;


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parental_id")
    @ToString.Exclude
    private Section parental;

    @Basic(fetch = FetchType.LAZY)
    @Formula("""
          (
            SELECT EXISTS(SELECT 1   FROM section s LEFT JOIN label_setting lb ON s.id = id AND (s.label_keys LIKE lb.key OR s.label_keys LIKE concat('%##UNIQUE_LIST_SEPARATOR##',lb.key, '%' )
              OR  s.label_keys LIKE CONCAT('%',lb.key, '##UNIQUE_LIST_SEPARATOR##%' ))
                        WHERE    (EXISTS(SELECT 1 FROM payslip_setting_element e where e.section = id) OR
                                  EXISTS(WITH cte AS ( SELECT REGEXP_REPLACE(CONCAT(' ', s2.base_formula, ' ', s2.value_formula, ' ', s2.rate_formula), '[+*()/-]', ' ') AS  combinedFormula FROM section s2
                                  ) SELECT cte.combinedFormula FROM cte WHERE cte.combinedFormula  RLIKE CONCAT(' ', code, ' ') OR cte.combinedFormula RLIKE CONCAT(' LBL_', lb.key, ' ')
                                  )))
)
            """)
    private Boolean referenced;

    public boolean isSalary() {
        return type != null && type.equals(SectionType.SALARY);
    }

    public boolean isTotal() {
        return type != null && type.equals(SectionType.TOTAL);
    }

    public boolean isIntermediate() {
        return type != null && type.equals(SectionType.INTERMEDIATE);
    }

    public boolean isGain() {
        return type != null && type.equals(SectionType.GAIN);
    }

    public boolean isDeduction() {
        return type != null && type.equals(SectionType.DEDUCTION);
    }

    /* public boolean isContribution() {
        return Objects.equals(category, SectionCategory.TAXABLE_CONTRIBUTORY);
    }

    public boolean isTaxationNotContribution() {
        return Objects.equals(category, SectionCategory.TAXABLE_NOT_CONTRIBUTORY);
    }

    public boolean isContributionNotTaxation() {
        return Objects.equals(category, SectionCategory.NOT_TAXABLE_CONTRIBUTORY);
    }

    public boolean notTaxationNotContribution() {
        return Objects.equals(category, SectionCategory.NOT_TAXABLE_NOT_CONTRIBUTORY);
    }

    public boolean isCharge() {
        return Objects.equals(category, SectionCategory.CHARGE);
    }

    public boolean isGrossSalary() {
        return Objects.equals(category, SectionCategory.GROSS);
    }

    public boolean isTaxation() {
        return category != null && category.equals(SectionCategory.TAXATION);
    }*/
}
