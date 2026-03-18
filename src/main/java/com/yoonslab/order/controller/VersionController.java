package com.yoonslab.order.controller;

import com.yoonslab.common.dto.ApiResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {

    private final BuildProperties buildProperties;

    public VersionController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/api/version")
    public ApiResponse<Map<String, String>> version() {
        return ApiResponse.ok(Map.of(
                "version", buildProperties.getVersion(),
                "name", buildProperties.getName(),
                "group", buildProperties.getGroup()
        ));
    }
}
