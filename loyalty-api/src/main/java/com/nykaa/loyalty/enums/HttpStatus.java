package com.nykaa.loyalty.enums;

import lombok.Getter;

import javax.servlet.http.HttpServletResponse;

@Getter
public enum HttpStatus {
    OK(HttpServletResponse.SC_OK),
    BAD_REQUEST(HttpServletResponse.SC_BAD_REQUEST),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    private final String code;

    HttpStatus(Integer code) {
        this.code = String.valueOf(code);
    }
}