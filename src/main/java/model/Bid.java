package model;

import java.sql.Timestamp;

public class Bid {
    private Timestamp date;
    private int userId;
    private int itemId;
    private Timestamp startDate;
    private Timestamp endDate;
    private double bidAmount;

    public Timestamp getDate() {
        return date;
    }

    public int getUserId() {
        return userId;
    }

    public int getItemId() {
        return itemId;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }
}
