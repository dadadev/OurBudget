package com.dadabit.ourbudget.interfaces;


import com.dadabit.ourbudget.customClasses.RegularPayment;

public interface PaymentsListAdapterCallback {

    void deleteItem(int position, RegularPayment payment);
}
