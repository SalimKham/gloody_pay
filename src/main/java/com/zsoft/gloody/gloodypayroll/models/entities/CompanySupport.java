package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.io.Serializable;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@MappedSuperclass
@FilterDef(name = "companyFilter", parameters = {@ParamDef(name = "companyId", type = long.class)})
@Filter(name = "companyFilter", condition = "company_id = :companyId")
public abstract class CompanySupport extends EntityAudit implements Serializable {
    @Column(name = "company_id", updatable = false)
    @Builder.Default
    private Long company = 1L;
}
