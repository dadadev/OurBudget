package com.dadabit.ourbudget.fragments.info;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.fragments.main.InputFragment;

import java.util.Calendar;

import static com.dadabit.ourbudget.MainActivity.currency;


public class InfoMeFragment  extends Fragment {

    private CardView
            cardSpendToday,
            cardSpendMonth,
            cardSpendTotal,
            cardIncomeMonth,
            cardIncomeTotal,
            cardTransactionsMonth,
            cardTransactionsTotal;

    private double
            spendToday=0,
            spendMonth=0,
            spendTotal=0,
            incomeMonth=0,
            incomeTotal=0;

    private long
            transactionMonth=0,
            transactionTotal=0;

    private TextView
            tvSpendToday,
            tvSpendMonth,
            tvSpendTotal,
            tvIncomeMonth,
            tvIncomeTotal,
            tvTransactionsMonth,
            tvTransactionsTotal;



    public static Fragment newInstance(){
        InfoMeFragment fragment = new InfoMeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info_me, container, false);

        cardIncomeMonth = (CardView) view.findViewById(R.id.info_me_card_month_earnings);
        cardIncomeTotal = (CardView) view.findViewById(R.id.info_me_card_total_earnings);
        cardSpendMonth = (CardView) view.findViewById(R.id.info_me_card_month);
        cardSpendToday = (CardView) view.findViewById(R.id.info_me_card_today);
        cardSpendTotal = (CardView) view.findViewById(R.id.info_me_card_total);
        cardTransactionsMonth = (CardView) view.findViewById(R.id.info_me_card_month_transactions);
        cardTransactionsTotal = (CardView) view.findViewById(R.id.info_me_card_total_transactions);

        tvIncomeMonth = (TextView) view.findViewById(R.id.info_me_month_earnings_value);
        tvIncomeTotal = (TextView) view.findViewById(R.id.info_me_total_earnings_value);
        tvSpendMonth = (TextView) view.findViewById(R.id.info_me_month_value);
        tvSpendToday = (TextView) view.findViewById(R.id.info_me_today_value);
        tvSpendTotal = (TextView) view.findViewById(R.id.info_me_total_value);
        tvTransactionsMonth = (TextView) view.findViewById(R.id.info_me_month_transactions_value);
        tvTransactionsTotal = (TextView) view.findViewById(R.id.info_me_total_transactions_value);


        new GetDataAsync().execute();

        return view;
    }

    private void startInAnimation() {
        Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim2 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim3 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim4 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim5 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim6 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
        final Animation anim7 = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);

        anim1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardSpendMonth.startAnimation(anim2);
                cardSpendMonth.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardSpendTotal.startAnimation(anim3);
                cardSpendTotal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardIncomeMonth.startAnimation(anim4);
                cardIncomeMonth.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim4.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardIncomeTotal.startAnimation(anim5);
                cardIncomeTotal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim5.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardTransactionsMonth.startAnimation(anim6);
                cardTransactionsMonth.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim6.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardTransactionsTotal.startAnimation(anim7);
                cardTransactionsTotal.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        anim1.setStartOffset(300);
        cardSpendToday.startAnimation(anim1);
        cardSpendToday.setVisibility(View.VISIBLE);
    }

    /**
     * Get data Async Task
     */

    private class GetDataAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            DB db = new DB(getContext());
            Calendar todayCalendar = Calendar.getInstance();

            Cursor cursor = db.getMyTransactions();
            if (cursor.getCount()!=0){
                cursor.moveToFirst();

                while (!cursor.isAfterLast()){
                    Calendar currentCalendar =
                            getDate(cursor.getLong(cursor.getColumnIndex(DB.COLUMN_DATE)));
                    double currentAmount =
                            cursor.getDouble(cursor.getColumnIndex(DB.COLUMN_AMOUNT));
                    //if transaction type expense
                    if (cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TYPE)) ==
                            InputFragment.TRANSACTION_EXPENSE){
                        //if this day cursor
                        if (currentCalendar.get(Calendar.DAY_OF_YEAR) ==
                                todayCalendar.get(Calendar.DAY_OF_YEAR)){
                            spendToday+=currentAmount;
                        }// if this month cursor
                        if (currentCalendar.get(Calendar.MONTH) ==
                                todayCalendar.get(Calendar.MONTH)){
                            spendMonth+=currentAmount;
                            transactionMonth++;
                        }
                        spendTotal+=currentAmount;
                        transactionTotal++;
                    } //if transaction type income
                    else {
                        // if this month cursor
                        if (currentCalendar.get(Calendar.MONTH) ==
                                todayCalendar.get(Calendar.MONTH)){
                            incomeMonth+=currentAmount;
                            transactionMonth++;
                        }
                        incomeTotal+=currentAmount;
                        transactionTotal++;
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();
            db.close();
            return null;
        }

        private Calendar getDate(long timestamp){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            return calendar;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);


            tvSpendToday.setText(String.format("%s%s",
                    String.valueOf(spendToday/100),
                    currency));
            tvSpendMonth.setText(String.format("%s %s",
                    String.valueOf(spendMonth/100),
                    currency));
            tvSpendTotal.setText(String.format("%s %s",
                    String.valueOf(spendTotal/100),
                    currency));
            tvIncomeMonth.setText(String.format("%s %s",
                    String.valueOf(incomeMonth/100),
                    currency));
            tvIncomeTotal.setText(String.format("%s %s",
                    String.valueOf(incomeTotal/100),
                    currency));
            tvTransactionsMonth.setText(String.valueOf(transactionMonth));
            tvTransactionsTotal.setText(String.valueOf(transactionTotal));


            startInAnimation();
        }
    }


}
