package com.nykaa.loyalty.exception;

import com.nykaa.loyalty.enums.ErrorCodes;
import lombok.Getter;

@Getter
public class LoyaltyException extends RuntimeException{

    private static final long serialVersionUID = -5745391214569233826L;

    private ErrorCodes errorCode;

    public LoyaltyException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public LoyaltyException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
