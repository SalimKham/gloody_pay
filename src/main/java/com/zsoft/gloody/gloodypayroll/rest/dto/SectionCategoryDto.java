package com.zsoft.gloody.gloodypayroll.rest.dto;
import com.zsoft.gloody.gloodypayroll.rest.dto.section.SectionDto;
import lombok.*;

import java.util.Set;

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
    private Set<SectionDto> sections;
}
