package com.nykaa.loyalty.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserOfferResponseDto {

    private List<UserOfferDetails> activeOffers = new ArrayList<>();

    private double totalPotentialEarning = 0;

    private List<UserEarningDetails> pastEarning = new ArrayList<>();

    private String noActiveOffersMessage;

}
