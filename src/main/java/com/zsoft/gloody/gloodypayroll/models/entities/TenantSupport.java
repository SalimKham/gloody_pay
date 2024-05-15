package com.zsoft.gloody.gloodypayroll.models.entities;

import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.persistence.*;
import java.io.Serializable;

@SuperBuilder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@MappedSuperclass
/*@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = Long.class)})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")*/
public abstract class TenantSupport extends EntityAudit implements Serializable {
    @Column(name = "tenant_id", updatable = false)
    @Builder.Default
    private Long tenantId = 1L;
}
