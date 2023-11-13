package com.zsoft.gloody.gloodypayroll.models.entities;

import lombok.*;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Period {
    private LocalDate start;
    private LocalDate end;
}
