package com.dadabit.ourbudget.fragments.payments;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.CategoriesRecyclerAdapter;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.fragments.main.InputFragment;
import com.dadabit.ourbudget.interfaces.CategoryClickListener;
import com.dadabit.ourbudget.interfaces.PaymentsFragmentsCallback;

import java.util.Calendar;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class PaymentsInputFragment  extends Fragment implements CategoryClickListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    public static final int PAYMENT_TYPE_DAY = 1;
    public static final int PAYMENT_TYPE_WEEK = 2;
    public static final int PAYMENT_TYPE_MONTH = 3;

    private PaymentsFragmentsCallback callback;

    private RecyclerView mRecyclerView;
    private CategoriesRecyclerAdapter mAdapter;

    private CardView cardDataInput, cardSingleCategory, cardDayInMonth;
    private TextView tv_category;
    private ImageView iv_singleCategory;
    private Switch mSwitch;
    private NumberPicker dayInMonthPicker;
    private EditText etName, etAmount;
    private RadioGroup radioGroup;
    private Button btnSave;


    private long chosenCategoryID;
    private int chosenType;


    public static Fragment newInstance(){
        PaymentsInputFragment fragment = new PaymentsInputFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payments_input, container, false);

        callback = (PaymentsFragmentsCallback) getContext();

        cardDataInput = (CardView) view.findViewById(R.id.payments_card_name);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.payments_recycler_view);
        cardSingleCategory = (CardView) view.findViewById(R.id.payments_card_category);
        tv_category = (TextView) view.findViewById(R.id.payments_category_tv);
        iv_singleCategory = (ImageView) view.findViewById(R.id.payments_card_category_iv);
        cardDayInMonth = (CardView) view.findViewById(R.id.payments_card_choose_dayInMonth);
        mSwitch = (Switch) view.findViewById(R.id.payments_switch);

        radioGroup = (RadioGroup) view.findViewById(R.id.payments_radioGroup);
        radioGroup.setOnCheckedChangeListener(this);

        cardSingleCategory.setOnClickListener(this);

        dayInMonthPicker = (NumberPicker) view.findViewById(R.id.payments_numberPicker);
        etName = (EditText) view.findViewById(R.id.payments_name);
        etAmount = (EditText) view.findViewById(R.id.payments_amount);
        btnSave = (Button) view.findViewById(R.id.payments_input_ok_btn);
        btnSave.setOnClickListener(this);


        etAmount.setHint("0,00 "+currency);



        new GetCategoriesAsync().execute();

        return view;
    }


    @Override
    public void onClick(View v) {

        if (v==cardSingleCategory){
            chosenCategoryID=0;
            anim_categoryCard_out();
            anim_categoriesList_in();

        }

        if (v==btnSave){
            if(isChecked()){

                int chosenAmountType;
                if (mSwitch.isChecked())
                    chosenAmountType = InputFragment.TRANSACTION_INCOME;
                else
                    chosenAmountType = InputFragment.TRANSACTION_EXPENSE;

                Calendar calendar = Calendar.getInstance();

                if (chosenType == PAYMENT_TYPE_WEEK){
                    int dayOfWeek = dayInMonthPicker.getValue()+1;
                    if (dayOfWeek==8) {
                        dayOfWeek=1;
                        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                    } else {
                        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        calendar.set(Calendar.WEEK_OF_YEAR, calendar.get(Calendar.WEEK_OF_YEAR)-1);
                    }


                }

                if (chosenType == PAYMENT_TYPE_MONTH){
                    int dayOfMonth = dayInMonthPicker.getValue();
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)-1);
                }

                if (chosenType == PAYMENT_TYPE_DAY){
                    calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)-1);
                }

                new DB(getContext()).addPayment(etName.getText().toString(),
                        getAmount(),
                        chosenAmountType,
                        chosenType,
                        chosenCategoryID,
                        calendar.getTimeInMillis());

                anim_save_data();

            }
        }

    }

    private int getAmount(){
        String stAmount = etAmount.getText().toString();
        int amount;
        if (stAmount.contains(".")){
            String[] amountArray = stAmount.split("\\.");
            if (amountArray[1].length()>1){
                amount = Integer.valueOf(
                        amountArray[0]
                                + amountArray[1].charAt(0)
                                + amountArray[1].charAt(1));
            } else {
                amount = Integer.valueOf(
                        amountArray[0]
                                + amountArray[1]
                                + "0");
            }
        } else {
            amount = Integer.valueOf(stAmount+"00");
        }

        return amount;
    }

    private boolean isChecked(){
        if (chosenType==0) {
            Toast.makeText(getContext(), R.string.payments_input_no_type, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (chosenCategoryID==0) {
            Toast.makeText(getContext(), R.string.payments_input_no_category, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etName.getText())) {
            Toast.makeText(getContext(), R.string.payments_input_no_name, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etAmount.getText())) {
            Toast.makeText(getContext(), R.string.payments_input_no_amount, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

        if (checkedId==R.id.payments_day){
            chosenType = PAYMENT_TYPE_DAY;
            if (cardDayInMonth.getVisibility()==View.VISIBLE){
                cardDayInMonth.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200));
                cardDayInMonth.setVisibility(View.GONE);
            }

        }
        if (checkedId==R.id.payments_week){
            chosenType = PAYMENT_TYPE_WEEK;
            callback.hideKeyboard();
            if (cardDayInMonth.getVisibility()==View.VISIBLE){
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dayInMonthPicker.setMinValue(1);
                        dayInMonthPicker.setMaxValue(7);
                        dayInMonthPicker.setWrapSelectorWheel(false);
                        dayInMonthPicker.setValue(1);
                        cardDayInMonth.setVisibility(View.VISIBLE);
                        cardDayInMonth.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cardDayInMonth.startAnimation(animation);
                cardDayInMonth.setVisibility(View.GONE);
            } else {
                dayInMonthPicker.setMinValue(1);
                dayInMonthPicker.setMaxValue(7);
                dayInMonthPicker.setWrapSelectorWheel(false);
                dayInMonthPicker.setValue(1);
                cardDayInMonth.setVisibility(View.VISIBLE);
                cardDayInMonth.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500));
            }

        }
        if (checkedId==R.id.payments_month){
            chosenType = PAYMENT_TYPE_MONTH;
            callback.hideKeyboard();
            if (cardDayInMonth.getVisibility()==View.VISIBLE){
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dayInMonthPicker.setMinValue(1);
                        dayInMonthPicker.setMaxValue(28);
                        dayInMonthPicker.setWrapSelectorWheel(false);
                        dayInMonthPicker.setValue(1);
                        cardDayInMonth.setVisibility(View.VISIBLE);
                        cardDayInMonth.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cardDayInMonth.startAnimation(animation);
                cardDayInMonth.setVisibility(View.GONE);
            } else {
                dayInMonthPicker.setMinValue(1);
                dayInMonthPicker.setMaxValue(28);
                dayInMonthPicker.setWrapSelectorWheel(false);
                dayInMonthPicker.setValue(1);
                cardDayInMonth.setVisibility(View.VISIBLE);
                cardDayInMonth.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500));
            }
        }

    }

    @Override
    public void categoryClickListener(int position, long id) {
        chosenCategoryID = id;

        anim_categoriesList_out(position);
        tv_category.setText((CharSequence) categories.getCategoryNames().get((int)id));
        iv_singleCategory.setImageBitmap((Bitmap) categories.getCategoryImages().get((int)id));
        anim_categoryCard_in();
    }

    /**
     * ____________
     *  Animations
     * ------------
     */

    private void anim_start_fragment(){
        Animation slideRecyclerView = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_500);
        Animation slideRightCardInput = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_500);
        slideRecyclerView.setStartOffset(300);
        slideRightCardInput.setStartOffset(500);
        slideRecyclerView.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideRightCardInput.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardDataInput.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mRecyclerView.startAnimation(slideRecyclerView);
        mRecyclerView.setVisibility(View.VISIBLE);
        cardDataInput.startAnimation(slideRightCardInput);
        cardDataInput.setVisibility(View.VISIBLE);


    }

    private void anim_save_data(){

        Animation slideOutCategoryCard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right_200);
        Animation slideOutInputCard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right_200);
        Animation slideOutDayCard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_right_200);

        slideOutCategoryCard.setDuration(300);
        slideOutInputCard.setDuration(300);
        slideOutInputCard.setStartOffset(200);
        slideOutDayCard.setDuration(300);
        slideOutDayCard.setStartOffset(400);


        slideOutCategoryCard.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardSingleCategory.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slideOutInputCard.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardDataInput.setVisibility(View.INVISIBLE);
                etName.getText().clear();
                etAmount.getText().clear();
                radioGroup.clearCheck();
                callback.updateViewPager();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        cardSingleCategory.startAnimation(slideOutCategoryCard);
        cardDataInput.startAnimation(slideOutInputCard);
        if (chosenType == PAYMENT_TYPE_MONTH || chosenType == PAYMENT_TYPE_WEEK){
            cardDayInMonth.startAnimation(slideOutDayCard);
        }

    }

    //----------Categories list
    public void anim_categoriesList_in(){
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setTranslationX(-mRecyclerView.getWidth());
        mRecyclerView.animate()
                .translationXBy(mRecyclerView.getWidth())
                .setDuration(300)
                .setStartDelay(300)
                .alpha(1.0f);

    }

    public void anim_categoriesList_out(int position){
        mAdapter.notifyItemMoved(position, 0);
        mRecyclerView.animate()
                .translationX(-mRecyclerView.getWidth())
                .setDuration(300)
                .setStartDelay(300)
                .alpha(0.0f);
    }

    //----------Single Category card
    public void anim_categoryCard_in(){

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_500);
        animation.setStartOffset(300);
        cardSingleCategory.setVisibility(View.VISIBLE);
        cardSingleCategory.startAnimation(animation);
    }

    public void anim_categoryCard_out(){
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200);
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardSingleCategory.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        cardSingleCategory.startAnimation(animation);
    }






    /**
     * ___________________________
     *      AsyncTask`s
     *  __________________________
     */

    private class GetCategoriesAsync extends AsyncTask<Void, Void, int[]> {

        @Override
        protected int[] doInBackground(Void... params) {

            return new DB(getContext()).getCategoriesIds();
        }

        @Override
        protected void onPostExecute(int[] categoryIDs) {
            super.onPostExecute(categoryIDs);

            if (categoryIDs!=null){
                mAdapter = new CategoriesRecyclerAdapter(getContext(), categoryIDs);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL,false));
                mAdapter.setClickListener(PaymentsInputFragment.this);
                anim_start_fragment();
            }
        }
    }
}
