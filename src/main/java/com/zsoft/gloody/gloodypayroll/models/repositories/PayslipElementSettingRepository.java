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
            "AND pse.payslipSetting.employee IN :employee_ids and pse.payslipSetting.closed")
    List<PayslipSettingElement> findAllByEmployeeIdInAndClosed(@Param("employee_ids") List<Long> employeeIds,  @Param("codes") List<String> codes);
}