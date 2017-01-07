package com.dadabit.ourbudget.customClasses;


import android.os.Parcel;
import android.os.Parcelable;

public class Budget implements Parcelable {

    public static final int TYPE_BY_WEEK = 1;
    public static final int TYPE_BY_MONTH = 2;
    public static final int TYPE_BY_YEAR = 3;

    private long id;
    private long categID;
    private String name;
    private int amount;
    private int type;
    private int spend;
    private int overBalance;
    private int dayBalance;

    public Budget(long id, long categID, String name, int amount, int type, int spend, int overBalance, int dayBalance) {
        this.id = id;
        this.categID = categID;
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.spend = spend;
        this.overBalance = overBalance;
        this.dayBalance = dayBalance;
    }

    protected Budget(Parcel in) {
        id = in.readLong();
        categID = in.readLong();
        name = in.readString();
        amount = in.readInt();
        type = in.readInt();
        spend = in.readInt();
        overBalance = in.readInt();
        dayBalance = in.readInt();
    }

    public static final Creator<Budget> CREATOR = new Creator<Budget>() {
        @Override
        public Budget createFromParcel(Parcel in) {
            return new Budget(in);
        }

        @Override
        public Budget[] newArray(int size) {
            return new Budget[size];
        }
    };


    public int getDayBalance() {
        return dayBalance;
    }

    public int getOverBalance() {
        return overBalance;
    }

    public int getSpend() {
        return spend;
    }

    public int getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public long getCategID() {
        return categID;
    }

    public long getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(categID);
        dest.writeString(name);
        dest.writeInt(amount);
        dest.writeInt(type);
        dest.writeInt(spend);
        dest.writeInt(overBalance);
        dest.writeInt(dayBalance);
    }
}
