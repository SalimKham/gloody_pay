package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Formula;

@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Entity
@Table(name = "variable_setting")
public class VariableSetting extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "`key`")
    private String key;

    @Column(name = "source")
    private String source;

    @Column(name = "`formula`")
    private String formula;

    @Column(name = "label")
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private VariableSettingGroup group;


    @Column(name = "description")
    private String description;

    @Formula("""
            (
            SELECT EXISTS (
              SELECT 1
              FROM section s
              WHERE s.company_id = company_id AND CONCAT(
                      COALESCE(s.base_formula, ''),
                      COALESCE(s.rate_formula, ''),
                      COALESCE(s.value_formula, '')
                    ) rlike CONCAT('(^|[^A-Z0-9_])', `key`, '([^A-Z0-9_]|$)')
            )
            )
            """)
    private Boolean referenced;
}

