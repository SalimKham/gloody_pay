package com.zsoft.gloody.gloodypayroll.models.entities.accounting;


import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "accounting_setting_account")
public class AccountingSettingAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;


    @Column(name = "title")
    private String title;

    @Column(name = "detailed_by_employee")
    private Boolean detailedByEmployee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AccountingSettingAccountType type;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_setting_group_id", nullable = false, insertable = false, updatable = false)
    @ToString.Exclude
    private AccountingSettingGroup group;

    @Formula("(SELECT exists(SELECT s.id FROM section s where s.accounting_id = id))")
    private Boolean referenced;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountingSettingAccount validationModelItem = (AccountingSettingAccount) o;
        if (validationModelItem.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), validationModelItem.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
