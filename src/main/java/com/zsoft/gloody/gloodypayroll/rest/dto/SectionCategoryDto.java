package com.zsoft.gloody.gloodypayroll.rest.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SectionCategoryDto {
    private long id;
    private long company;
    private String code;
    private String name;
}
