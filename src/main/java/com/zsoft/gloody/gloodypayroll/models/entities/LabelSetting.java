package com.zsoft.gloody.gloodypayroll.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
}
