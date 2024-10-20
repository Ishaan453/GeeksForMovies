package com.ishaanbhela.geeksformovies.productionCompany;

public class productionCompanyModel {
    String name;
    String logo_path;

    public productionCompanyModel(String logo_path, String name) {
        this.logo_path = logo_path;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo_path() {
        return logo_path;
    }

    public void setLogo_path(String logo_path) {
        this.logo_path = logo_path;
    }
}
