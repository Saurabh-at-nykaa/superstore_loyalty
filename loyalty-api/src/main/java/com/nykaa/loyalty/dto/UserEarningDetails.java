package com.nykaa.loyalty.dto;

import lombok.Data;

@Data
public class UserEarningDetails {

    private String month;

    private double earning = 0;

    private String status;

}
