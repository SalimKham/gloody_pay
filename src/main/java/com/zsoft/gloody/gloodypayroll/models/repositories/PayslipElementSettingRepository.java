package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PayslipElementSettingRepository extends JpaRepository<PayslipSettingElement, Long> {
    @Query("SELECT pse from PayslipSettingElement  pse WHERE pse.section.code in :codes " +
            "AND pse.payslipSetting.contract = :contract_id and pse.payslipSetting.closed")
    List<PayslipSettingElement> findAllByEmployeeIdAndClosed(@Param("contract_id") Long contractId,  @Param("codes") List<String> codes);
}