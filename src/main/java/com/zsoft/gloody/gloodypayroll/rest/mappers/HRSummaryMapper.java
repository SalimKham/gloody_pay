package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.HRMonthlyData;
import com.zsoft.gloody.gloodypayroll.rest.dto.HRMonthlySummaryDTO;
import org.mapstruct.Mapper;

@Mapper
public interface HRSummaryMapper {
    HRMonthlySummaryDTO toDto(HRMonthlyData monthlyData);
    HRMonthlyData toEntity(HRMonthlySummaryDTO dto);
}


