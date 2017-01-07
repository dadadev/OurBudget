package com.dadabit.ourbudget.fragments.main;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dadabit.ourbudget.MainActivity;
import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.ExpListAdapter;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.customClasses.Transaction;
import com.dadabit.ourbudget.interfaces.FragmentCallback;
import com.dadabit.ourbudget.interfaces.SingleTransactionOnClick;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;


public class TransactionsFragment  extends Fragment implements SingleTransactionOnClick, RadioGroup.OnCheckedChangeListener{

    public static final String GROUP_NUMBER = "groupNum";

    private ExpandableListView listView;
    private ExpListAdapter adapter;
    private TextView tvTotalBalance;
    private FragmentCallback fragmentCallback;

    private BottomSheetBehavior mBottomSheetBehavior;
    private Button btnCancelDelete;

    private RadioButton radioMonth, radioWeek, radioDay;

    private ArrayList<ArrayList<Transaction>> groups;
    private Transaction deletedTransaction;

    private ProgressBar progressBar;


    public static Fragment newInstance(int groupNum){
        TransactionsFragment fragment = new TransactionsFragment();
        Bundle args = new Bundle();
        args.putInt(GROUP_NUMBER, groupNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        fragmentCallback = (FragmentCallback) getContext();

        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        listView = (ExpandableListView) view.findViewById(R.id.transaction_lv);

        progressBar = (ProgressBar) view.findViewById(R.id.transaction_progressBar);

        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.transaction_radioGroup);
        radioGroup.setOnCheckedChangeListener(this);

        radioMonth = (RadioButton) view.findViewById(R.id.transaction_radio_month);
        radioWeek = (RadioButton) view.findViewById(R.id.transaction_radio_week);
        radioDay = (RadioButton) view.findViewById(R.id.transaction_radio_day);
        tvTotalBalance = (TextView) view.findViewById(R.id.transaction_total_balance_tv);

        View bottomSheet = view.findViewById( R.id.transaction_bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        btnCancelDelete = (Button) view.findViewById(R.id.transaction_btnDelete_cancel);


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!radioMonth.isChecked() && !radioWeek.isChecked() && !radioDay.isChecked())
            radioMonth.setChecked(true);
    }

    //  onTransactionSelectedListener
    @Override
    public void transactionClickListener(final CardView cardView,
                                         final int groupPosition,
                                         final int childPosition) {

        cardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                TextView tvDescription = (TextView) cardView.getTag(R.id.trList_tv_description);
                final ImageButton btnDelete = (ImageButton) cardView.getTag(R.id.trList_btn_delete);
                ImageButton btnEdit = (ImageButton) cardView.getTag(R.id.trList_btn_edit);
                TextView tvCategoryName = (TextView) cardView.getTag(R.id.trList_tv_category_name);
                TextView tvPerson = (TextView) cardView.getTag(R.id.trList_tv_person);

                Animation CategoryNameAnimation =
                        AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_300);
                Animation personNameAnimation =
                        AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_400);
                Animation btnDeleteAnimation =
                        AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_500);
                Animation btnEditAnimation =
                        AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_300);
                Animation descriptionAnimation =
                        AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_500);
                //show card elements
                if (tvDescription.getVisibility() == View.GONE){

                    // My transaction
                    if (groups.get(groupPosition).get(childPosition).getPerson()
                            == Transaction.PERSON_THIS)
                    {

                        //Delete button
                        btnDelete.setOnClickListener(new View.OnClickListener() {
                            boolean isLastChild;
                            boolean isCanceled;
                            @Override
                            public void onClick(View v) {
                                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                mBottomSheetBehavior.setBottomSheetCallback(
                                        new BottomSheetBehavior.BottomSheetCallback() {
                                            @Override
                                            public void onStateChanged(@NonNull View bottomSheet,
                                                                       int newState) {
                                                switch (newState) {
                                                    case BottomSheetBehavior.STATE_COLLAPSED:
                                                        if (!isCanceled){
                                                            if(isAdded() && deletedTransaction!=null){
                                                                new DB(getContext()).deleteTransaction(
                                                                        deletedTransaction.getId());
                                                                new UpdateTotalBalance().execute(groups);
                                                                fragmentCallback.onCallback(
                                                                        MainActivity.CALLBACK_DELETE_FROM_FIREBASE,
                                                                        null,
                                                                        null,
                                                                        0,
                                                                        deletedTransaction.getId());
                                                            }
                                                        }

                                                        isCanceled=false;
                                                        deletedTransaction=null;
                                                        break;
                                                }
                                            }

                                            @Override
                                            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                                            }
                                        });
                                deletedTransaction = groups.get(groupPosition).get(childPosition);
                                groups.get(groupPosition).remove(childPosition);
                                if (groups.get(groupPosition).size()==0) {
                                    groups.remove(groupPosition);
                                    isLastChild = true;
                                }
                                adapter.setGroups(groups);
                                adapter.notifyDataSetChanged();
                                btnCancelDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        isCanceled=true;
                                        if(isLastChild){
                                            groups.add( groupPosition, new ArrayList<Transaction>());
                                        }

                                        groups.get(groupPosition)
                                                .add(childPosition, deletedTransaction);
                                        adapter.setGroups(groups);
                                        adapter.notifyDataSetChanged();
                                        mBottomSheetBehavior
                                                .setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                });

                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBottomSheetBehavior
                                                .setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                }, 2000);
                            }
                        });
                        btnDelete.setVisibility(View.VISIBLE);
                        btnDelete.startAnimation(btnDeleteAnimation);


                        //Edit button
                        btnEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fragmentCallback.onCallback(MainActivity.CALLBACK_INPUT,
                                        groups.get(groupPosition).get(childPosition),
                                        null,
                                        groupPosition, 0);

                            }
                        });
                        btnEdit.setVisibility(View.VISIBLE);
                        btnEdit.startAnimation(btnEditAnimation);

                        tvPerson.setVisibility(View.INVISIBLE);
                    }

                    //Person 2 transaction
                    else {
                        SharedPreferences sharedPreferences =
                                getActivity().getSharedPreferences(
                                        MainActivity.APP_PREFERENCES,
                                        MODE_PRIVATE);
                        if (sharedPreferences.contains(MainActivity.APP_PREFERENCES_PERSON2_NAME))
                            tvPerson.setText(sharedPreferences.getString(
                                    MainActivity.APP_PREFERENCES_PERSON2_NAME, ""));

                        tvPerson.setVisibility(View.VISIBLE);
                        tvPerson.startAnimation(personNameAnimation);
                    }

                    tvCategoryName.setText(String.valueOf(categories.getCategoryNames()
                            .get(groups.get(groupPosition).get(childPosition).getCategoryID())));
                    tvCategoryName.setVisibility(View.VISIBLE);
                    tvCategoryName.startAnimation(CategoryNameAnimation);

                    tvDescription.setText(
                            groups.get(groupPosition).get(childPosition).getDescription());

                    if (!TextUtils.isEmpty(tvDescription.getText())){
                        tvDescription.setVisibility(View.VISIBLE);
                        tvDescription.startAnimation(descriptionAnimation);
                    } else {
                        tvDescription.setVisibility(View.INVISIBLE);
                    }

                    //hide card elements
                } else {
                    btnDelete.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.GONE);
                    tvDescription.setVisibility(View.GONE);
                    tvPerson.setVisibility(View.GONE);
                    tvCategoryName.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId){
            case R.id.transaction_radio_month:
                new LoadTransactionsLists()
                        .execute(ExpListAdapter.SORT_BY_MONTH);
                break;
            case R.id.transaction_radio_week:
                new LoadTransactionsLists()
                        .execute(ExpListAdapter.SORT_BY_WEEK);
                break;
            case R.id.transaction_radio_day:
                new LoadTransactionsLists()
                        .execute(ExpListAdapter.SORT_BY_DAY);
                break;
        }
    }

    /**
     * ______________________________________________________
     *      Load Transactions Async
     *  _____________________________________________________
     */

    private class LoadTransactionsLists extends AsyncTask<Integer, Void, ArrayList<ArrayList<Transaction>>> {

        private int loaderType;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            listView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_up_300));
            listView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected ArrayList<ArrayList<Transaction>> doInBackground(Integer... params) {

            this.loaderType = params[0];

            switch (loaderType){
                case ExpListAdapter.SORT_BY_MONTH:
                    return new DB(getContext()).getMonthTransactionsArrays();
                case ExpListAdapter.SORT_BY_WEEK:
                    return new DB(getContext()).getWeekTransactionsArrays();
                case ExpListAdapter.SORT_BY_DAY:
                    return new DB(getContext()).getDayTransactionsArrays();
                default:
                    return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ArrayList<Transaction>> arrayLists) {
            super.onPostExecute(arrayLists);

            if (isAdded()){
                progressBar.setVisibility(View.GONE);

                if (arrayLists!=null){
                    if (adapter==null){

                        groups=arrayLists;

                        adapter = new ExpListAdapter(getContext(),
                                groups,
                                loaderType,
                                TransactionsFragment.this);

                        listView.setAdapter(adapter);
                        listView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_300));
                        listView.setVisibility(View.VISIBLE);

                        new UpdateTotalBalance().execute(groups);
                    } else {
                        groups = arrayLists;

                        listView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_300));
                        listView.setVisibility(View.VISIBLE);

                        adapter.setGroups(groups);
                        adapter.setSortBy(loaderType);
                        adapter.notifyDataSetChanged();

                        new UpdateTotalBalance().execute(groups);
                    }
                }

                if(getArguments().getInt(GROUP_NUMBER)>=0){
                    listView.expandGroup(getArguments().getInt(GROUP_NUMBER));
                    listView.smoothScrollToPositionFromTop(listView.getCount()*2, 0, 1000);
                }

            }
        }
    }

    /**
     * ______________________________________________________
     *      Get Total Balance Async
     *  _____________________________________________________
     */

    private class UpdateTotalBalance extends AsyncTask<ArrayList<ArrayList<Transaction>>, Void, Double>{
        @SafeVarargs
        @Override
        protected final Double doInBackground(ArrayList<ArrayList<Transaction>>... params) {

            double sum=0;
            for (int position = 0; position < params[0].size(); position++) {
                for (int i = 0; i < params[0].get(position).size(); i++) {
                    if (params[0].get(position).get(i).getAmountType()
                            == InputFragment.TRANSACTION_EXPENSE)
                        sum-=params[0].get(position).get(i).getAmount();
                    else sum+=params[0].get(position).get(i).getAmount();
                }
            }
            return sum/100;
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);

            if(isAdded()){
                tvTotalBalance.setText(String.format("%s %s %s",
                        getResources().getString(R.string.title_balance),
                        aDouble,
                        currency));
                tvTotalBalance.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300));
            }
        }
    }


}
