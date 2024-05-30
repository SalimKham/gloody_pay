package com.zsoft.gloody.gloodypayroll.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@SuperBuilder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "section_category", uniqueConstraints = @UniqueConstraint(name = "code_company_unique_constraint", columnNames = {"code", "company_id"}))
public class SectionCategory extends CompanySupport{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "code")
    private String code;
    @Column(name = "name")
    private String name;
    @OneToMany(mappedBy = "category")
    @OrderBy("code asc")
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<Section> sections = new HashSet<>();
}
