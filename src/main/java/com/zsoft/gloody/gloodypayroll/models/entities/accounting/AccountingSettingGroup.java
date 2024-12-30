package com.zsoft.gloody.gloodypayroll.models.entities.accounting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "accounting_setting_group")
public class AccountingSettingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="name", nullable = false)
    private String name;

    @OneToMany(targetEntity = AccountingSettingAccount.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "accounting_setting_group_id", nullable = false)
    @ToString.Exclude
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<AccountingSettingAccount> accounts = Collections.emptyList();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounting_setting_id", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    private AccountingSetting validationModel;

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountingSettingGroup accountingSettingGroup = (AccountingSettingGroup) o;
        if (accountingSettingGroup.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), accountingSettingGroup.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
