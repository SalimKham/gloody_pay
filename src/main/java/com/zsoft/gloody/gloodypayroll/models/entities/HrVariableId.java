package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HrVariableId implements Serializable {
    private Long contractId;
    private LocalDate month;
}