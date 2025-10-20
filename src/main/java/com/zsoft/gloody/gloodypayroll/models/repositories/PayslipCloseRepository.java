package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.PayslipClose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Repository
@Transactional
public interface PayslipCloseRepository extends JpaRepository<PayslipClose, Long> {
    @Modifying
    void deleteByContractIdAndMonth(Long contractId, LocalDate month);
}