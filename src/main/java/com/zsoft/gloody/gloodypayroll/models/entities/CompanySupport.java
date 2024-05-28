package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.io.Serializable;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@MappedSuperclass
public abstract class CompanySupport extends EntityAudit implements Serializable {
    @Column(name = "company_id", updatable = false)
    @Builder.Default
    private Long company = 1L;
}
