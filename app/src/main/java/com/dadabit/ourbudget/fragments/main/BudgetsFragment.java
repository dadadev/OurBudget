package com.dadabit.ourbudget.fragments.main;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dadabit.ourbudget.MainActivity;
import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.BudgetsRecyclerAdapter;
import com.dadabit.ourbudget.customClasses.Budget;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.dialogs.AddBudgetDialog;
import com.dadabit.ourbudget.interfaces.BudgetDeleteClickListener;

import java.util.ArrayList;

public class BudgetsFragment  extends Fragment implements View.OnClickListener, BudgetDeleteClickListener {


    private BudgetsRecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private FloatingActionButton fab;
    private Button cancelBtn;
    private int currentPosition;
    private boolean isCanceled;

    private BottomSheetBehavior mBottomSheetBehavior;



    public static Fragment newInstance(){
        BudgetsFragment fragment = new BudgetsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        fab = (FloatingActionButton) view.findViewById(R.id.budgets_fab);
        fab.setOnClickListener(this);

        View bottomSheet = view.findViewById( R.id.budgets_bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        cancelBtn = (Button) view.findViewById(R.id.budgets_delete_cancel);
        cancelBtn.setOnClickListener(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.budgets_recycler_view);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });

        new LoadBudgetsAsync().execute();

        return view;
    }


    @Override
    public void onClick(View v) {
        if (v==fab){

            AddBudgetDialog addBudgetDialog = new AddBudgetDialog();
            addBudgetDialog.show(getActivity().getSupportFragmentManager(), "AddBudgetDialog");
        }

        if(v==cancelBtn){
            isCanceled=true;
            mAdapter.getBudgetBack(currentPosition);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void deleteOnClickItem(int position, final long id) {

        mAdapter.hideBudget(position);
        currentPosition=position;

        fab.animate()
                .translationY(-150)
                .setDuration(100);
        fab.hide();

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:

                        fab.animate()
                                .translationYBy(150)
                                .setDuration(200)
                                .alpha(1.0f);
                        if (!isCanceled)
                            if(isAdded())
                                new DB(getContext()).deleteBudget(id);
                        isCanceled=false;
                        fab.show();
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

    /**
     * Load Budgets Async
     */

    private class LoadBudgetsAsync extends AsyncTask<Void, Void, ArrayList<Budget>>{
        @Override
        protected ArrayList<Budget> doInBackground(Void... params) {
            return new DB(getContext()).getBudgetArrayList();
        }

        @Override
        protected void onPostExecute(ArrayList<Budget> budgets) {
            super.onPostExecute(budgets);

            if (isAdded() && budgets.size() != 0){
                mAdapter = new BudgetsRecyclerAdapter(getContext(), budgets);
                mAdapter.setOnClickListener(BudgetsFragment.this);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            }
        }
    }
}
