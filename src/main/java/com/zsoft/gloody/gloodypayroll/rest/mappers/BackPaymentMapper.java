package com.zsoft.gloody.gloodypayroll.rest.mappers;

import com.zsoft.gloody.gloodypayroll.models.entities.BackPayment;
import com.zsoft.gloody.gloodypayroll.rest.dto.BackPaymentDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

@Mapper()
public abstract class BackPaymentMapper {


    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract BackPaymentDto toDto(BackPayment backPayment);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    public abstract BackPayment toEntity(BackPaymentDto backPaymentDto);
}

