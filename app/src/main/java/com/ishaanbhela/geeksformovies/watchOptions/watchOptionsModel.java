package com.ishaanbhela.geeksformovies.watchOptions;

public class watchOptionsModel {
    private String logoPath;
    private String providerName;
    private String type;

    public watchOptionsModel(String logoPath, String providerName, String type) {
        this.logoPath = logoPath;
        this.providerName = providerName;
        this.type = type;
    }

    // Getters
    public String getLogoPath() {
        return logoPath;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getType() {
        return type;
    }
}

