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
public class OpenAppSettingDto {

    private String name;
    private String value;
    private String lang;

    /**
     * Convert from AppSetting entity to open DTO
     */
    public static OpenAppSettingDto fromEntity(AppSetting setting) {
        return OpenAppSettingDto.builder()
                .name(setting.getName())
                .value(setting.getValue())
                .lang(setting.getLang().name())
                .build();
    }
}
