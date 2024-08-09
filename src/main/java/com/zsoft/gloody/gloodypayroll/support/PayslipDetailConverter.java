package com.zsoft.gloody.gloodypayroll.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.zsoft.gloody.gloodypayroll.rest.dto.PayslipDetailDto;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class PayslipDetailConverter implements AttributeConverter<PayslipDetailDto, String> {
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Override
    public String convertToDatabaseColumn(PayslipDetailDto detailDto) {
        ObjectWriter ow = jacksonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).writer().withDefaultPrettyPrinter();
        try {
            return ow.writeValueAsString(detailDto);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public PayslipDetailDto convertToEntityAttribute(String jsonString) {
        try {

            return jacksonObjectMapper.readValue(jsonString, PayslipDetailDto.class);
        } catch (Exception ignored) {
        }
        return null;
    }
}