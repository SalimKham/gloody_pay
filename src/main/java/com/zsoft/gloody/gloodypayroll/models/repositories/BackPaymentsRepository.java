package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.BackPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BackPaymentsRepository extends JpaRepository<BackPayment, Long> {
    List<BackPayment> findAllByContractIdAndMonthAndSectionCodeIsNotNullAndSourceIsNotNull(Long contractId, LocalDate month);
     List<BackPayment> findAllByMonthAndSectionCodeIsNullAndSourceIsNull(LocalDate month);
}
