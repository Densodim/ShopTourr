package com.shoptourr.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "voyage.client")
public record ClientConfigProperties(
        int minAndroidBuild,
        int minIosBuild,
        Integer softMinAndroidBuild,
        Integer softMinIosBuild,
        String storeUrlAndroid,
        String storeUrlIos,
        Flags flags
) {
    public ClientConfigProperties {
        if (minAndroidBuild <= 0) {
            minAndroidBuild = 1;
        }
        if (minIosBuild <= 0) {
            minIosBuild = 1;
        }
        if (flags == null) {
            flags = new Flags(true, true, false);
        }
    }

    public record Flags(boolean exportPdf, boolean ocrAssist, boolean nativeMaps) {}
}
