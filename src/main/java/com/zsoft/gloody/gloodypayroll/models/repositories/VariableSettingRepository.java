package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface VariableSettingRepository extends JpaRepository<VariableSetting, Long> {
    boolean existsByKeyAndSourceAndCompany(String key, String source, Long company);
    boolean existsBySourceAndCompany(String source, Long company);
    void deleteBySource(String key);
    void deleteAllBySourceIsNotNullAndCompany(Long company);
    Page<VariableSetting> findAllByLabelLikeOrKeyLike(String label, String key, Pageable pageable);
}