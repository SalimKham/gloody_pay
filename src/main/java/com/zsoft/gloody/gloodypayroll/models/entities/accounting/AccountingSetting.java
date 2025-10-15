package com.zsoft.gloody.gloodypayroll.models.entities.accounting;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zsoft.gloody.gloodypayroll.models.entities.CompanySupport;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "accounting_setting")
public class AccountingSetting extends CompanySupport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(targetEntity = AccountingSettingGroup.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "accounting_setting_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<AccountingSettingGroup> groups = null;
}
