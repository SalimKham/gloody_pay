package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.models.enums.SectionCategory;
import com.zsoft.gloody.gloodypayroll.models.enums.SectionType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "section")
public class Section extends TenantSupport {
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

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private SectionCategory category;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parental_id")
    @ToString.Exclude
    private Section parental;

    public boolean isSalary() {
        return type != null && type.equals(SectionType.SALARY);
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

    public boolean isContribution() {
        return category != null && category.equals(SectionCategory.TAXABLE_CONTRIBUTORY);
    }

    public boolean isTaxationNotContribution() {
        return category != null && category.equals(SectionCategory.TAXABLE_NOT_CONTRIBUTORY);
    }

    public boolean isContributionNotTaxation() {
        return category != null && category.equals(SectionCategory.NOT_TAXABLE_CONTRIBUTORY);
    }

    public boolean notTaxationNotContribution() {
        return category != null && category.equals(SectionCategory.NOT_TAXABLE_NOT_CONTRIBUTORY);
    }

    public boolean isCharge() {
        return category != null && category.equals(SectionCategory.CHARGE);
    }

    public boolean isGrossSalary() {
        return category != null && category.equals(SectionCategory.GROSS);
    }

    public boolean isTaxation() {
        return category != null && category.equals(SectionCategory.TAXATION);
    }
}
