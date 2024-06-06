package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;

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


    @Column(name = "description")
    private String description;
}
