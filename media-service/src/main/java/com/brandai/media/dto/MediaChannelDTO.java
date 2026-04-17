package com.brandai.media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class MediaChannelDTO {

    @Data
    public static class MediaChannelVO {
        private Long id;
        private Long brandId;
        private String name;
        private String type;
        private String platform;
        private String accountName;
        private String authStatus;
        private String listPrice;
        private String followersCount;
        private Boolean inPlan;
    }

    @Data
    public static class AddToPlanRequest {
        private int articleCount = 1;
    }

    @Data
    public static class CreateSelfMediaRequest {
        @NotBlank(message = "媒体名称不能为空")
        private String name;
        @NotBlank(message = "所属平台不能为空")
        private String platform;
        @NotBlank(message = "账号名称不能为空")
        private String accountName;
    }
}
