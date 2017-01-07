package com.dadabit.ourbudget.interfaces;


import com.dadabit.ourbudget.customClasses.PostTransaction;
import com.dadabit.ourbudget.customClasses.Transaction;

public interface FragmentCallback {
    void onCallback(int fragment, Transaction transaction, PostTransaction postTransaction, int groupNum, long deletedTransactionID);
}
