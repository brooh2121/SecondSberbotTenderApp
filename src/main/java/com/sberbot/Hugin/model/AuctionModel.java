package com.sberbot.Hugin.model;

import java.util.Objects;

public class AuctionModel {

    private String auctionNumber;
    private String orgName;
    private String tenderName;
    private String tenderType;
    private String publicDate;
    private String tenderBegDate;
    private String tenderEndDate;
    private String sum;
    private String tenderStatus;
    private String tenderPlaceUrl;
    private String tenderGovUrl;

    public AuctionModel() {
    }

    public AuctionModel(String auctionNumber, String orgName, String tenderName, String tenderType, String publicDate, String tenderBegDate, String tenderEndDate, String sum, String tenderStatus,String tenderPlaceUrl, String tenderGovUrl) {
        this.auctionNumber = auctionNumber;
        this.orgName = orgName;
        this.tenderName = tenderName;
        this.tenderType = tenderType;
        this.publicDate = publicDate;
        this.tenderBegDate = tenderBegDate;
        this.tenderEndDate = tenderEndDate;
        this.sum = sum;
        this.tenderStatus = tenderStatus;
        this.tenderPlaceUrl = tenderPlaceUrl;
        this.tenderGovUrl = tenderGovUrl;
    }

    public String getAuctionNumber() {
        return auctionNumber;
    }

    public void setAuctionNumber(String auctionNumber) {
        this.auctionNumber = auctionNumber;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getTenderName() {
        return tenderName;
    }

    public void setTenderName(String tenderName) {
        this.tenderName = tenderName;
    }

    public String getTenderType() {
        return tenderType;
    }

    public void setTenderType(String tenderType) {
        this.tenderType = tenderType;
    }

    public String getPublicDate() {
        return publicDate;
    }

    public void setPublicDate(String publicDate) {
        this.publicDate = publicDate;
    }

    public String getTenderBegDate() {
        return tenderBegDate;
    }

    public void setTenderBegDate(String tenderBegDate) {
        this.tenderBegDate = tenderBegDate;
    }

    public String getTenderEndDate() {
        return tenderEndDate;
    }

    public void setTenderEndDate(String tenderEndDate) {
        this.tenderEndDate = tenderEndDate;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getTenderStatus() {
        return tenderStatus;
    }

    public void setTenderStatus(String tenderStatus) {
        this.tenderStatus = tenderStatus;
    }

    public String getTenderPlaceUrl() {
        return tenderPlaceUrl;
    }

    public void setTenderPlaceUrl(String tenderPlaceUrl) {
        this.tenderPlaceUrl = tenderPlaceUrl;
    }

    public String getTenderGovUrl() {
        return tenderGovUrl;
    }

    public void setTenderGovUrl(String tenderGovUrl) {
        this.tenderGovUrl = tenderGovUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionModel that = (AuctionModel) o;
        return auctionNumber.equals(that.auctionNumber) &&
                orgName.equals(that.orgName) &&
                tenderName.equals(that.tenderName) &&
                publicDate.equals(that.publicDate) &&
                sum.equals(that.sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionNumber, orgName, tenderName, publicDate, sum);
    }


    @Override
    public String toString() {
        return "AuctionModel{" +
                "auctionNumber='" + auctionNumber + '\'' +
                ", orgName='" + orgName + '\'' +
                ", tenderName='" + tenderName + '\'' +
                ", tenderType='" + tenderType + '\'' +
                ", publicDate='" + publicDate + '\'' +
                ", tenderBegDate='" + tenderBegDate + '\'' +
                ", tenderEndDate='" + tenderEndDate + '\'' +
                ", sum='" + sum + '\'' +
                ", tenderStatus='" + tenderStatus + '\'' +
                '}';
    }
}
