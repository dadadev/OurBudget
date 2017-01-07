package com.dadabit.ourbudget.customClasses;


public class RegularPayment {

    private long id;
    private String name;
    private int amount;
    private int amountType;
    private int type;
    private long category;
    private long date;

    public RegularPayment(long id, String name, int amount, int amountType, int type, long category, long date) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.amountType = amountType;
        this.type = type;
        this.category = category;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }

    public int getAmountType() {
        return amountType;
    }

    public int getType() {
        return type;
    }

    public long getCategory() {
        return category;
    }

    public long getDate() {
        return date;
    }
}
