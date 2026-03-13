package com.ecomm.user_service.util;

public class AppConstants {

    public static final String PHONE_REGEX = "^[6-9]\\d{9}$";
    public static final String PINCODE_REGEX = "^[1-9][0-9]{5}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$";
    public static final String OTP_REGEX = "^[0-9]{6}$";
    public static final String GSTIN_REGEX = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
}
