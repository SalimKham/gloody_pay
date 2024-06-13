package com.zsoft.gloody.gloodypayroll.rest.dto;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LabelSettingDto {
    private Long id;
    private String key;
    private LabelSettingMinimalDto parentLabel;
    private String label;
    private String color;
    private String description;
}
