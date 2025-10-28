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
@Table(name = "label_setting", uniqueConstraints = @UniqueConstraint(name = "label_setting_unique_constraint", columnNames = "company_id,`key`"))
public class LabelSetting extends CompanySupport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "`key`")
    private String key;

    @OneToOne
    @JoinColumn(name = "parent_id")
    private LabelSetting  parentLabel;

    @Column(name = "label")
    private String label;

    @Column(name = "color")
    private String color;

    @Column(name = "description")
    private String description;

    @Formula("""
            (
            SELECT EXISTS (
                SELECT 1
                FROM section s
                WHERE s.company_id = company_id AND  CONCAT(
                              COALESCE(s.base_formula, ''),
                              ',',
                              COALESCE(s.rate_formula, ''),
                              ',',
                              COALESCE(s.label_keys, ''),
                              ',',
                              COALESCE(s.value_formula, '')
                      ) rlike CONCAT('(^|[^A-Z0-9_])', `key`, '([^A-Z0-9_]|$)')
            )
            OR EXISTS(SELECT 1 FROM label_setting ls WHERE ls.parent_id = id)
            )
            """)
    private Boolean referenced;
}
