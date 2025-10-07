package com.zsoft.gloody.gloodypayroll.support;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    public static final String SPLIT_CHAR = "##UNIQUE_LIST_SEPARATOR##";

    @Override
    public String convertToDatabaseColumn(List<String> stringList) {
        return stringList != null && !stringList.isEmpty() ? String.join(SPLIT_CHAR, stringList) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String string) {
        return string != null && !string.isEmpty() ? Arrays.stream(string.split(SPLIT_CHAR)).collect(Collectors.toList()) : Collections.emptyList();
    }
}