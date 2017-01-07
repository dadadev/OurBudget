package com.dadabit.ourbudget.customClasses;


import java.util.Calendar;

public class Transaction {

    public static final int PERSON_THIS = 1;
    public static final int PERSON_2 = 2;

    private long id;
    private int amount;
    private int amountType;
    private Calendar date;
    private int categoryID;
    private String description;
    private int person;


    public Transaction(long id, int amount, int amountType, Calendar date, int categoryID, String description, int person) {
        this.id = id;
        this.amount = amount;
        this.amountType = amountType;
        this.date = date;
        this.categoryID = categoryID;
        this.description = description;
        this.person = person;
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

    public Calendar getDate() {
        return date;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public String getDescription() {
        return description;
    }

    public int getPerson() {
        return person;
    }
}
