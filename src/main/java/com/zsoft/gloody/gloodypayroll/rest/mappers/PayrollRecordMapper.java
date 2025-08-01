package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.PayrollRecord;
import com.zsoft.gloody.gloodypayroll.rest.dto.payrollrecords.PayrollRecordDTO;
import org.mapstruct.Mapper;

@Mapper
public interface PayrollRecordMapper {
    PayrollRecordDTO toDto(PayrollRecord payrollRecord);

    PayrollRecord toEntity(PayrollRecordDTO payrollRecordDTO);
}


