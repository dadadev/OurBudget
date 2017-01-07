package com.dadabit.ourbudget.fragments.main;


import android.animation.Animator;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.customClasses.DB;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.communication.IOnItemFocusChangedListener;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class StatisticFragment extends Fragment implements
        View.OnClickListener,
        TextSwitcher.ViewFactory,
        Switch.OnCheckedChangeListener{

    private PieChart mPieChart;
    private int numberPieSlices;
    private TextSwitcher tvMonth, tvBalance;
    private ImageView ivLeft, ivRight, pieChartLeft, pieChartRight;
    private Switch switchDate, switchAmount;
    private FloatingActionButton fab;
    private CardView cardPieChart;

    private BottomSheetBehavior mBottomSheetBehavior;
    private boolean showFAB=true;
    private int chartType = InputFragment.TRANSACTION_EXPENSE;

    private Calendar currentDate;
    private String currentType="-";
    private boolean isCreated;
    private boolean goRight;
    private boolean byYear;

    private ArrayList<HashMap<Integer, Integer>> monthAmounts;


    public static Fragment newInstance(){
        StatisticFragment fragment = new StatisticFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistic, container, false);

        currentDate = Calendar.getInstance();

        mPieChart = (PieChart) view.findViewById(R.id.statistic_piechart);
        cardPieChart = (CardView) view.findViewById(R.id.statistic_piechart_card);
        fab = (FloatingActionButton) view.findViewById(R.id.statistic_fab);
        fab.setOnClickListener(this);

        tvMonth = (TextSwitcher) view.findViewById(R.id.statistic_tv_month);
        tvMonth.setFactory(this);
        tvBalance = (TextSwitcher) view.findViewById(R.id.statistic_total_balance_tv);
        tvBalance.setFactory(this);
        ivLeft = (ImageView) view.findViewById(R.id.statistic_iv_left);
        ivLeft.setOnClickListener(this);
        ivRight = (ImageView) view.findViewById(R.id.statistic_iv_right);
        ivRight.setOnClickListener(this);
        pieChartLeft = (ImageView) view.findViewById(R.id.statistic_piechart_iv_left);
        pieChartLeft.setOnClickListener(this);
        pieChartRight = (ImageView) view.findViewById(R.id.statistic_piechart_iv_right);
        pieChartRight.setOnClickListener(this);

        switchDate = (Switch) view.findViewById(R.id.statistic_switch_date);
        switchDate.setOnCheckedChangeListener(this);
        switchAmount = (Switch) view.findViewById(R.id.statistic_switch_amount);
        switchAmount.setOnCheckedChangeListener(this);

        tvMonth.setText(getResources().getTextArray(
                R.array.statistic_month_array)[currentDate.get(Calendar.MONTH)]);

        View bottomSheet = view.findViewById( R.id.statistic_bottom_sheet );
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(BottomSheetListener);

        cardPieChart.setVisibility(View.GONE);

        new LoadAmounts().execute();

        return view;
    }

    @Override
    public void onClick(View v) {

        if (v==fab){
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            fab.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shrink));
        }

        if (v==ivLeft){
            goRight = false;
            if (byYear){
                currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR)-1);
                new LoadAmounts().execute();
            } else {
                int year = currentDate.get(Calendar.YEAR);
                currentDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH)-1);
                if (year!=currentDate.get(Calendar.YEAR)){
                    new LoadAmounts().execute();
                    Toast.makeText(getContext(), getString(R.string.toast_year_changed)
                            + currentDate.get(Calendar.YEAR), Toast.LENGTH_SHORT).show();
                }
            }
            updatePieChart();
            changeMonth();
        }
        if (v==ivRight){
            goRight = true;
            if (byYear){
                currentDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR)+1);
                new LoadAmounts().execute();
            } else {
                int year = currentDate.get(Calendar.YEAR);
                currentDate.set(Calendar.MONTH, currentDate.get(Calendar.MONTH)+1);
                if (year!=currentDate.get(Calendar.YEAR)){
                    new LoadAmounts().execute();
                    Toast.makeText(getContext(), getString(R.string.toast_year_changed)
                            + currentDate.get(Calendar.YEAR), Toast.LENGTH_SHORT).show();
                }
            }
            updatePieChart();
            changeMonth();
        }

        if (v==pieChartLeft){
            pieChartLeft.animate().translationX(-10).setDuration(300).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {}

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pieChartLeft.animate().translationX(10).setDuration(300);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {}

                        @Override
                        public void onAnimationRepeat(Animator animation) {}
                    });

            int nextPosition = mPieChart.getCurrentItem() + 1;
            if (nextPosition<numberPieSlices) {
                mPieChart.setCurrentItem(nextPosition);
            }
            else {
                mPieChart.setCurrentItem(0);
            }
        }

        if (v==pieChartRight){
            pieChartRight.animate().translationX(10).setDuration(300).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            pieChartRight.animate().translationX(-10).setDuration(300);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });


            int nextPosition = mPieChart.getCurrentItem() - 1;
            if (nextPosition>=0) {
                mPieChart.setCurrentItem(nextPosition);

            }
            else {
                mPieChart.setCurrentItem(numberPieSlices-1);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton==switchDate){
            if(isChecked){
                byYear=true;
                updatePieChart();
                changeMonth();
            } else {
                byYear=false;
                updatePieChart();
                changeMonth();
            }
            return;
        }
        if (compoundButton==switchAmount){
            if(isChecked){
                chartType=InputFragment.TRANSACTION_INCOME;
                currentType="+";
                new LoadAmounts().execute();
            } else {
                chartType=InputFragment.TRANSACTION_EXPENSE;
                currentType="-";
                new LoadAmounts().execute();
            }
        }
    }

    private void updatePieChart(){

        mPieChart.clearChart();

        if (monthAmounts==null){
            new LoadAmounts().execute();
        } else {

            double currentBalance = 0;

            if (byYear){

                HashMap<Integer, Integer> yearAmounts = new HashMap<>();

                for (HashMap<Integer, Integer> month :
                        monthAmounts) {
                    for (Integer id :
                            month.keySet()) {

                        if (yearAmounts.containsKey(id)){
                            yearAmounts.put(id,
                                    yearAmounts.get(id) + month.get(id));
                        } else {
                            yearAmounts.put(id, month.get(id));
                        }
                    }
                }

                if (yearAmounts.size()!=0){

                    pieChartLeft.setVisibility(View.VISIBLE);
                    pieChartRight.setVisibility(View.VISIBLE);

                    mPieChart.setUseCustomInnerValue(true);
                    mPieChart.setShowDecimal(true);

                    numberPieSlices = yearAmounts.size();

                    for (Integer id :
                            yearAmounts.keySet()) {

                        mPieChart.addPieSlice(
                                new PieModel(
                                        String.valueOf(categories.getCategoryNames().get(id)),
                                        (float) yearAmounts.get(id)/100,
                                        getColor(id)));

                        currentBalance += yearAmounts.get(id);

                    }

                    mPieChart.setInnerValueString(
                            mPieChart.getData().get(mPieChart.getCurrentItem()).getValue() + currency);
                    mPieChart.setOnItemFocusChangedListener(
                            new IOnItemFocusChangedListener() {
                                @Override
                                public void onItemFocusChanged(int _Position) {
                                    mPieChart.setInnerValueString(
                                            mPieChart.getData().get(_Position).getValue()
                                                    +currency);
                                }
                            });

                }



            } else {//byMonth

                int currentMonth = currentDate.get(Calendar.MONTH);

                if (monthAmounts !=null && monthAmounts.get(currentMonth).size()!=0){



                    pieChartLeft.setVisibility(View.VISIBLE);
                    pieChartRight.setVisibility(View.VISIBLE);

                    mPieChart.setUseCustomInnerValue(true);
                    mPieChart.setShowDecimal(true);

                    numberPieSlices = monthAmounts.get(currentMonth).size();

                    for (Integer id :
                            monthAmounts.get(currentMonth).keySet()) {

                        mPieChart.addPieSlice(
                                new PieModel(
                                        String.valueOf(categories.getCategoryNames().get(id)),
                                        (float) monthAmounts.get(currentMonth).get(id)/100,
                                        getColor(id)));

                        currentBalance += monthAmounts.get(currentMonth).get(id);

                    }

                    mPieChart.setInnerValueString(
                            mPieChart.getData().get(
                                    mPieChart.getCurrentItem()).getValue() + currency);
                    mPieChart.setOnItemFocusChangedListener(
                            new IOnItemFocusChangedListener() {
                                @Override
                                public void onItemFocusChanged(int _Position) {
                                    mPieChart.setInnerValueString(
                                            mPieChart.getData().get(_Position).getValue()
                                                    +currency);
                                }
                            });
                }
            }

            if (goRight){

                tvBalance.setInAnimation(AnimationUtils.loadAnimation(getContext(),
                        R.anim.slide_from_right_500));
                tvBalance.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                        R.anim.slide_out_left_200));
            } else {
                tvBalance.setInAnimation(AnimationUtils.loadAnimation(getContext(),
                        android.R.anim.slide_in_left));
                tvBalance.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                        android.R.anim.slide_out_right));
            }

            tvBalance.setText(String.format("%s%s %s",
                    currentType,
                    currentBalance /100,
                    currency));

            if(currentBalance==0) {
                tvBalance.setText("");
                pieChartLeft.setVisibility(View.INVISIBLE);
                pieChartRight.setVisibility(View.INVISIBLE);
                mPieChart.setUseCustomInnerValue(true);
                mPieChart.addPieSlice(
                        new PieModel(0, Color.parseColor("#FFFFFF")));
                mPieChart.setInnerValueString("No data");
            }
        }


        mPieChart.startAnimation();
    }

    private void changeMonth(){

        if (goRight){
            ivRight.animate().translationX(10).setDuration(300).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ivRight.animate().translationX(-10).setDuration(300);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

            tvMonth.setInAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_from_left_300));
            tvMonth.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_out_right_200));
            cardPieChart.startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_from_right_500));
        } else {
            ivLeft.animate().translationX(-10).setDuration(300).setListener(
                    new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            ivLeft.animate().translationX(10).setDuration(300);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

            tvMonth.setInAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_from_right_500));
            tvMonth.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_out_left_200));
            cardPieChart.startAnimation(AnimationUtils.loadAnimation(getContext(),
                    R.anim.slide_from_left_300));
        }

        if (byYear){
            tvMonth.setText(String.valueOf(currentDate.get(Calendar.YEAR)));
        } else {
            tvMonth.setText(getResources().getTextArray(
                    R.array.statistic_month_array)[currentDate.get(Calendar.MONTH)]);
        }


    }

    private int getColor(int id){
        switch (id){
            case 1:
                return Color.parseColor("#EF5350");
            case 2:
                return Color.parseColor("#E91E63");
            case 3:
                return Color.parseColor("#9C27B0");
            case 4:
                return Color.parseColor("#673AB7");
            case 5:
                return Color.parseColor("#3F51B5");
            case 6:
                return Color.parseColor("#2196F3");
            case 7:
                return Color.parseColor("#03A9F4");
            case 8:
                return Color.parseColor("#00BCD4");
            case 9:
                return Color.parseColor("#009688");
            case 10:
                return Color.parseColor("#4CAF50");
            case 11:
                return Color.parseColor("#8BC34A");
            case 12:
                return Color.parseColor("#CDDC39");
            case 13:
                return Color.parseColor("#FFEB3B");
            case 14:
                return Color.parseColor("#FFC107");
            case 15:
                return Color.parseColor("#FF9800");
            case 16:
                return Color.parseColor("#FF5722");
            case 17:
                return Color.parseColor("#795548");
            case 18:
                return Color.parseColor("#9E9E9E");
            case 19:
                return Color.parseColor("#607D8B");
            case 20:
                return Color.parseColor("#FFAB00");
            default:
                return Color.parseColor("#BDBDBD");
        }
    }


    private BottomSheetBehavior.BottomSheetCallback BottomSheetListener =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    final Animation growAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.grow);
                    final Animation shrinkAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shrink);


                    switch (newState) {
                        case BottomSheetBehavior.STATE_DRAGGING:
                            if (showFAB)
                                fab.startAnimation(shrinkAnimation);
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            showFAB = true;
                            fab.setVisibility(View.VISIBLE);
                            fab.startAnimation(growAnimation);
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            showFAB = false;
                            fab.setVisibility(View.INVISIBLE);
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
            };

    @Override
    public View makeView() {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL);
        textView.setTextSize(20);
        return textView;
    }


    /**
     * ____________________________________________________________________________
     * Load Data AsyncTask
     * ____________________________________________________________________________
     */

    private class LoadAmounts extends AsyncTask<Void, Void, ArrayList<HashMap<Integer, Integer>>>{
        @Override
        protected ArrayList<HashMap<Integer, Integer>> doInBackground(Void... params) {
            return new DB(getContext()).getStatisticData(currentDate, chartType);
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<Integer, Integer>> hashMaps) {
            super.onPostExecute(hashMaps);

            if (isAdded()){
                monthAmounts = hashMaps;
                if (!isCreated){
                    cardPieChart.startAnimation(AnimationUtils.loadAnimation(getContext(),
                            R.anim.slide_down_500));
                    cardPieChart.setVisibility(View.VISIBLE);
                    isCreated = true;
                }
                updatePieChart();

            }
        }
    }

}
