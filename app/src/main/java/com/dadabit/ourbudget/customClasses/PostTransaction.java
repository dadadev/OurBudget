package com.dadabit.ourbudget.customClasses;



public class PostTransaction {

    public static final int STATUS_EXIST = 0;
    public static final int STATUS_DELETED = 1;
    public static final int STATUS_CHANGED = 2;
    public static final int STATUS_NEED_DELETE = 3;

    private long id;
    private int amount;
    private int amountType;
    private long date;
    private int categoryID;
    private String description;
    private int status;

    public PostTransaction() {
    }

    public PostTransaction(long id, int amount, int amountType, long date, int categoryID, String description, int status) {
        this.id = id;
        this.amount = amount;
        this.amountType = amountType;
        this.date = date;
        this.categoryID = categoryID;
        this.description = description;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public int getAmountType() {
        return amountType;
    }

    public long getDate() {
        return date;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public String getDescription() {
        return description;
    }

    public int getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setAmountType(int amountType) {
        this.amountType = amountType;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
