package model;

import java.sql.Timestamp;

public class Auction {
    public enum Status {ALL,CREATED,OPEN,CLOSEDBYBUY,CLOSEDBYDROP,CLOSEDBYTIME}
    private int userId;
    private int itemId;
    private Timestamp startDate;
    private Timestamp endDate;
    private Status status;
    private int startingPrice;

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

    public Status getStatus() {
        return status;
    }

    public int getStartingPrice() {
        return startingPrice;
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

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStartingPrice(int startingPrice) {
        this.startingPrice = startingPrice;
    }
}
