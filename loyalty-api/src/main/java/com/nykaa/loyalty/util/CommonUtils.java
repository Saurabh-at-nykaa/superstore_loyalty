package com.nykaa.loyalty.util;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class CommonUtils {

    public static long roundToNearest100thCeiling(double input) {
        long i = (long) Math.ceil(input);
        return ((i + 99) / 100) * 100;
    };

    public static Long roundToNearestLong(double input) {
        return (long) Math.ceil(input);
    }

    public static int getNoOfConfiguredTiers(List<String> targetValues) {
        int size = 0;
        for (int i = 0; i < targetValues.size(); i++) {
            if (StringUtils.isBlank(targetValues.get(i))) {
                break;
            }
            size++;
        }
        return size;
    }

    public static double roundOff(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("##.00");
        String formattedValue = decimalFormat.format(value);
        return Double.valueOf(formattedValue).doubleValue();
    }

    public static String formatDouble(Double value) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(value);
    }

    public static List<String> maskEmailList(List<String> emails) {
        emails = emails.parallelStream().map(email -> maskEmail(email)).collect(Collectors.toList());
        return emails;
    }

    public static String maskEmail(String email) {
        return email.replaceAll(Constants.EMAIL_REGEX, Constants.MASK_REPLACEMENT);
    }

}
