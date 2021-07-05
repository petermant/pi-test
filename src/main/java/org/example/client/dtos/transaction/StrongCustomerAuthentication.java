package org.example.client.dtos.transaction;

/** TODO the online documentation for this object is hard to find ... copying hard-coded values for now, for sake of getting broader process working */
public class StrongCustomerAuthentication {
    private final String website = "https://mydomain.com";
    private final String notificationURL = "https://notification.url";
    private final String browserIP = "10.68.21.21";
    private final String browserAcceptHeader = "text/html, application/json";
    private final boolean browserJavascriptEnabled = true;
    private final boolean browserJavaEnabled = false;
    private final String browserLanguage = "en-GB";
    private final String browserColorDepth = "16";
    private final String browserScreenHeight = "768";
    private final String browserScreenWidth = "1200";
    private final String browserTZ = "+300";
    private final String browserUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:67.0) Gecko/20100101 Firefox/67.0";
    private final String challengeWindowSize = "Small";
    private final String threeDSRequestorChallengeInd = "02";
    private final boolean requestSCAExemption = false;
    private final String transType = "GoodsAndServicePurchase";
    private final String threeDSRequestorDecReqInd = "N";

    public String getWebsite() {
        return website;
    }

    public String getNotificationURL() {
        return notificationURL;
    }

    public String getBrowserIP() {
        return browserIP;
    }

    public String getBrowserAcceptHeader() {
        return browserAcceptHeader;
    }

    public boolean isBrowserJavascriptEnabled() {
        return browserJavascriptEnabled;
    }

    public boolean isBrowserJavaEnabled() {
        return browserJavaEnabled;
    }

    public String getBrowserLanguage() {
        return browserLanguage;
    }

    public String getBrowserColorDepth() {
        return browserColorDepth;
    }

    public String getBrowserScreenHeight() {
        return browserScreenHeight;
    }

    public String getBrowserScreenWidth() {
        return browserScreenWidth;
    }

    public String getBrowserTZ() {
        return browserTZ;
    }

    public String getBrowserUserAgent() {
        return browserUserAgent;
    }

    public String getChallengeWindowSize() {
        return challengeWindowSize;
    }

    public String getThreeDSRequestorChallengeInd() {
        return threeDSRequestorChallengeInd;
    }

    public boolean isRequestSCAExemption() {
        return requestSCAExemption;
    }

    public String getTransType() {
        return transType;
    }

    public String getThreeDSRequestorDecReqInd() {
        return threeDSRequestorDecReqInd;
    }
}