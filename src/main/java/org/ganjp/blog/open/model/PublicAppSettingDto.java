package org.ganjp.blog.open.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.blog.bm.model.entity.AppSetting;

/**
 * Open DTO for App Settings - only exposes name, value, and language
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicAppSettingDto {

    private String name;
    private String value;
    private String lang;

    /**
     * Convert from AppSetting entity to open DTO
     */
    public static PublicAppSettingDto fromEntity(AppSetting setting) {
        return PublicAppSettingDto.builder()
                .name(setting.getName())
                .value(setting.getValue())
                .lang(setting.getLang().name())
                .build();
    }
}
