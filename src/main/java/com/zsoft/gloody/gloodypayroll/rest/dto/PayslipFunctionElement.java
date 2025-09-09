package com.zsoft.gloody.gloodypayroll.rest.dto;

import java.time.LocalDate;

public record PayslipFunctionElement(Long resourceId, String code, LocalDate date, double value) { }
