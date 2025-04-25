package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipSettingElement;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface PayslipElementSettingRepository extends JpaRepository<PayslipSettingElement, Long> {
    @Query("SELECT NEW com.zsoft.gloody.gloodypayroll.rest.dto.PayslipFunctionElement(pse.payslipSetting.employee, pse.section.code, pse.payslipSetting.paymentDate, pse.value)  FROM PayslipSettingElement  pse WHERE pse.section.code in :codes " +
            "AND pse.payslipSetting.employee in :resource_ids and pse.payslipSetting.closed")
    List<PayslipFunctionElement> findAllByEmployeeIdAndClosed(@Param("resource_ids") Set<Long> resourceIds, @Param("codes") List<String> codes);
}