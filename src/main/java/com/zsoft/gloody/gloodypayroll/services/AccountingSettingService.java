package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.models.entities.accounting.AccountingSetting;
import com.zsoft.gloody.gloodypayroll.models.enums.AccountingSettingAccountType;
import com.zsoft.gloody.gloodypayroll.models.repositories.AccountingSettingRepository;
import com.zsoft.gloody.gloodypayroll.rest.dto.accounting.AccountingSettingDTO;
import com.zsoft.gloody.gloodypayroll.rest.mappers.accounting.AccountingSettingMapper;
import com.zsoft.gloody.gloodypayroll.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
@Transactional
public class AccountingSettingService {
    private final AccountingSettingRepository accountingSettingRepository;
    private final AccountingSettingMapper accountingSettingMapper;

    public AccountingSettingDTO save(AccountingSettingDTO validationModelDTO) {
        AccountingSetting accountingSetting = accountingSettingMapper.toEntity(validationModelDTO);
        accountingSetting = accountingSettingRepository.save(accountingSetting);
        return accountingSettingMapper.toDto(accountingSetting);
    }

    @Transactional(readOnly = true)
    public Optional<AccountingSettingDTO> findOne() {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto);
    }


    @Transactional(readOnly = true)
    public Optional<AccountingSettingDTO> getAccountingMonthlyTotals(LocalDate month) {
        return accountingSettingRepository.findTopOrderByIdDesc()
                .map(accountingSettingMapper::toDto)
                .map(accountingSetting -> {
                    AtomicReference<Double> accountingSettingDebitTotal = new AtomicReference<>(0.0);
                    AtomicReference<Double> accountingSettingCreditTotal = new AtomicReference<>(0.0);
                    List<Object[]> accountingTotals =  accountingSettingRepository.getAccountingSectionsMonthlyTotal(month, SecurityUtils.getCurrentCompanyId());
                    Map<Long, Double> totals = accountingTotals == null ? new HashMap<>() : accountingTotals
                            .stream()
                            .collect(Collectors.toMap(objects -> (Long) objects[0], objects -> (Double)objects[1]));
                    if (accountingSetting.getGroups() != null) {
                       accountingSetting.getGroups().forEach(group -> {
                           AtomicReference<Double> debitTotal = new AtomicReference<>(0.0);
                           AtomicReference<Double> creditTotal = new AtomicReference<>(0.0);
                           if (group.getAccounts()!=null) {
                               group.getAccounts().forEach(account -> {
                                   Double accountMonthlyTotal = totals.getOrDefault(account.getId(), 0.0);
                                   account.setMonthlyTotal(accountMonthlyTotal);
                                   if (Objects.equals(account.getType(), AccountingSettingAccountType.DEBIT)) {
                                       debitTotal.updateAndGet(v -> v + accountMonthlyTotal);
                                   } else {
                                       creditTotal.updateAndGet(v -> v + accountMonthlyTotal);
                                   }
                               });
                           }
                           group.setDebitMonthlyTotal(debitTotal.get());
                           group.setCreditMonthlyTotal(creditTotal.get());
                           accountingSettingDebitTotal.updateAndGet(v -> v + group.getDebitMonthlyTotal());
                           accountingSettingCreditTotal.updateAndGet(v -> v + group.getCreditMonthlyTotal());
                       });
                    }
                    accountingSetting.setCreditMonthlyTotal(accountingSettingCreditTotal.get());
                    accountingSetting.setDebitMonthlyTotal(accountingSettingDebitTotal.get());
                    return accountingSetting;
                });
    }

    @Transactional(readOnly = true)
    public Page<AccountingSettingDTO> findAll(Pageable pageable) {
        return accountingSettingRepository.findAll(pageable)
                .map(accountingSettingMapper::toDto);
    }

    public void delete(Long id) {
        accountingSettingRepository.deleteById(id);
    }
}

