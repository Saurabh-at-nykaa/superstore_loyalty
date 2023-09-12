package com.nykaa.loyalty.enums;

import lombok.Getter;

@Getter
public enum CatalogBrandParams {

    BRAND_ID("Brand ID"),
    FASHION_BRAND_ID("Fashion Brand ID"),
    BRAND_NAME("Brand Name");
    
    String name;

    CatalogBrandParams(String name) {
        this.name = name;
    }

}
