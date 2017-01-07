package com.dadabit.ourbudget.interfaces;


import android.support.v7.widget.CardView;

public interface SingleTransactionOnClick {
    void transactionClickListener(CardView cardView, int groupPosition, int childPosition);
}
