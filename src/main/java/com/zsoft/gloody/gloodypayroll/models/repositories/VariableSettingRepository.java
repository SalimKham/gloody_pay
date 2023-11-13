package com.zsoft.gloody.gloodypayroll.models.repositories;

import com.zsoft.gloody.gloodypayroll.models.entities.VariableSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariableSettingRepository extends JpaRepository<VariableSetting, Long> {
    Optional<VariableSetting> findByKey(String key);

    boolean existsByKey(String key);
}