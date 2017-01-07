package com.dadabit.ourbudget.fragments.payments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.PaymentsRecyclerAdapter;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.customClasses.RegularPayment;
import com.dadabit.ourbudget.interfaces.PaymentsListAdapterCallback;

import java.util.ArrayList;


public class PaymentsListFragment extends Fragment implements PaymentsListAdapterCallback, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private PaymentsRecyclerAdapter mAdapter;
    private BottomSheetBehavior mBottomSheetBehavior;

    private int deletedPaymentPosition;
    private boolean isCanceled;
    private RegularPayment deletedPayment;


    public static Fragment newInstance(){
        PaymentsListFragment fragment = new PaymentsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payments_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.payments_list_recycler_view);

        View bottomSheet = view.findViewById(R.id.payments_list_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        Button cancelDeleteBtn = (Button) view.findViewById(R.id.payments_list_delete_cancel);
        cancelDeleteBtn.setOnClickListener(this);

        new GetPayments().execute();

        return view;
    }

    @Override
    public void deleteItem(int position, RegularPayment payment) {

        deletedPaymentPosition = position;
        deletedPayment = payment;

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (!isCanceled && isAdded())
                            new DB(getContext()).deletePayment(deletedPayment.getId());

                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }, 2000);

    }

    @Override
    public void onClick(View v) {
        isCanceled = true;
        mAdapter.getDeletedBack(deletedPaymentPosition, deletedPayment);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    private class GetPayments extends AsyncTask<Void, Void, ArrayList<RegularPayment>> {
        @Override
        protected ArrayList<RegularPayment> doInBackground(Void... params) {

            return new DB(getContext()).getPayments();
        }

        @Override
        protected void onPostExecute(ArrayList<RegularPayment> paymentArrayList) {
            super.onPostExecute(paymentArrayList);

            mAdapter = new PaymentsRecyclerAdapter(
                    getContext(),
                    paymentArrayList,
                    PaymentsListFragment.this);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setLayoutManager(
                    new LinearLayoutManager(
                            getContext(),
                            LinearLayoutManager.VERTICAL, false));
        }
    }
}
