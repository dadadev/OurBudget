package com.dadabit.ourbudget.fragments.main;


import android.animation.Animator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dadabit.ourbudget.MainActivity;
import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.CategoriesRecyclerAdapter;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.customClasses.PostTransaction;
import com.dadabit.ourbudget.customClasses.Transaction;
import com.dadabit.ourbudget.eventbus.ShowCalculator;
import com.dadabit.ourbudget.interfaces.CategoryClickListener;
import com.dadabit.ourbudget.interfaces.FragmentCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class InputFragment  extends Fragment implements
        CategoryClickListener,
        View.OnClickListener{

    public static final int TRANSACTION_EXPENSE = 0;
    public static final int TRANSACTION_INCOME = 1;


    public static final String ARGUMENT_ID = "id";
    public static final String ARGUMENT_DATE = "date";
    public static final String ARGUMENT_DESCRIPTION = "description";
    public static final String ARGUMENT_AMOUNT = "amount";
    public static final String ARGUMENT_AMOUNT_TYPE = "amount_type";
    public static final String ARGUMENT_CATEGORY = "category";

    public static final int CALCULATE_TYPE_PLUS = 1;
    public static final int CALCULATE_TYPE_MINUS = 2;
    public static final int CALCULATE_TYPE_DIVIDE = 3;
    public static final int CALCULATE_TYPE_MULTIPLY = 4;

    private FragmentCallback fragmentCallback;
    private EventBus eventBus = EventBus.getDefault();

    private RecyclerView mRecyclerView;
    private CategoriesRecyclerAdapter mAdapter;

    private CardView cardDate, cardDescriptionBtn, cardDescriptionText, cardSingleCategory, cardAmount, cardKeyboard, cardCalculator;
    private ImageView iv_singleCategory;
    private Button btn_date, btn_clear, btn_save, btn_description_ok;
    private Button btn_1, btn_2, btn_3, btn_4, btn_5, btn_6, btn_7, btn_8, btn_9, btn_0, btn_comma;
    private ImageButton btn_del;
    private Button btn_calc_plus, btn_calc_minus, btn_calc_divide, btn_calc_multiply, btn_calc_equally;
    private ImageButton btn_description;
    private TextView tv_category, tv_amount;
    private EditText et_description;
    private Switch mSwitch;

    private DecimalFormat decimalFormat = new DecimalFormat("##0.00");

    private Calendar currentDate;
    private long chosenDate;
    private long chosenCategoryID;
    private String description;
    private int transactionAmount;
    private int transactionType;
    private long transactionID;

    private String amountString="";
    private int countAfterComma=0;
    private int calculationType;
    private double calculationFirstNumber;


    public static Fragment newInstance(Transaction transaction){
        InputFragment fragment = new InputFragment();
        Bundle args = new Bundle();
        if (transaction!=null){
            args.putLong(ARGUMENT_ID, transaction.getId());
            args.putLong(ARGUMENT_DATE, transaction.getDate().getTimeInMillis());
            args.putString(ARGUMENT_DESCRIPTION, transaction.getDescription());
            args.putInt(ARGUMENT_AMOUNT, transaction.getAmount());
            args.putInt(ARGUMENT_AMOUNT_TYPE, transaction.getAmountType());
            args.putInt(ARGUMENT_CATEGORY, transaction.getCategoryID());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(ShowCalculator event){
        if (event.isVisible()) {
            cardCalculator.setVisibility(View.VISIBLE);
            cardCalculator.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.slide_from_right_500));
        }
        else {
            cardCalculator.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_input, container, false);

        fragmentCallback = (FragmentCallback) getContext();
        if (!eventBus.isRegistered(this))
            eventBus.register(this);

        cardDate = (CardView) view.findViewById(R.id.input_date_card);
        cardSingleCategory = (CardView) view.findViewById(R.id.input_category_card);
        cardDescriptionBtn = (CardView) view.findViewById(R.id.input_description_card);
        cardDescriptionText = (CardView) view.findViewById(R.id.input_description_text_card);
        cardAmount = (CardView) view.findViewById(R.id.input_amount_card);
        cardKeyboard = (CardView) view.findViewById(R.id.input_keyboard);
        cardCalculator = (CardView) view.findViewById(R.id.input_calculate);

        btn_date = (Button) view.findViewById(R.id.input_date);
        btn_description = (ImageButton) view.findViewById(R.id.input_description);
        btn_description_ok = (Button) view.findViewById(R.id.input_description_ok);
        btn_clear = (Button) view.findViewById(R.id.input_clear);
        btn_save = (Button) view.findViewById(R.id.input_save);

        tv_category = (TextView) view.findViewById(R.id.input_category);
        iv_singleCategory = (ImageView) view.findViewById(R.id.input_card_category_iv);
        et_description = (EditText) view.findViewById(R.id.input_description_et);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.input_recycler_view_category);

        tv_amount = (TextView) view.findViewById(R.id.input_amount);
        mSwitch = (Switch) view.findViewById(R.id.input_switch);

        //Keyboard Card
        btn_0 = (Button) view.findViewById(R.id.input_btn_0);
        btn_1 = (Button) view.findViewById(R.id.input_btn_1);
        btn_2 = (Button) view.findViewById(R.id.input_btn_2);
        btn_3 = (Button) view.findViewById(R.id.input_btn_3);
        btn_4 = (Button) view.findViewById(R.id.input_btn_4);
        btn_5 = (Button) view.findViewById(R.id.input_btn_5);
        btn_6 = (Button) view.findViewById(R.id.input_btn_6);
        btn_7 = (Button) view.findViewById(R.id.input_btn_7);
        btn_8 = (Button) view.findViewById(R.id.input_btn_8);
        btn_9 = (Button) view.findViewById(R.id.input_btn_9);
        btn_comma = (Button) view.findViewById(R.id.input_btn_comma);
        btn_del = (ImageButton) view.findViewById(R.id.input_btn_del);
        //Calculator
        btn_calc_plus = (Button) view.findViewById(R.id.input_calc_plus);
        btn_calc_minus = (Button) view.findViewById(R.id.input_calc_minus);
        btn_calc_divide = (Button) view.findViewById(R.id.input_calc_divide);
        btn_calc_equally = (Button) view.findViewById(R.id.input_calc_equally);
        btn_calc_multiply = (Button) view.findViewById(R.id.input_calc_multiply);
        //set ClickListener
        btn_date.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        btn_description.setOnClickListener(this);
        cardSingleCategory.setOnClickListener(this);
        btn_description_ok.setOnClickListener(this);
        btn_0.setOnClickListener(this);
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        btn_4.setOnClickListener(this);
        btn_5.setOnClickListener(this);
        btn_6.setOnClickListener(this);
        btn_7.setOnClickListener(this);
        btn_8.setOnClickListener(this);
        btn_9.setOnClickListener(this);
        btn_del.setOnClickListener(this);
        btn_comma.setOnClickListener(this);
        btn_calc_plus.setOnClickListener(this);
        btn_calc_minus.setOnClickListener(this);
        btn_calc_divide.setOnClickListener(this);
        btn_calc_equally.setOnClickListener(this);
        btn_calc_multiply.setOnClickListener(this);



        if (getArguments().getLong(ARGUMENT_ID)!=0){
            currentDate = Calendar.getInstance();
            transactionID = getArguments().getLong(ARGUMENT_ID);
            chosenCategoryID = getArguments().getInt(ARGUMENT_CATEGORY);
            description = getArguments().getString(ARGUMENT_DESCRIPTION);
            transactionAmount = getArguments().getInt(ARGUMENT_AMOUNT);
            transactionType = getArguments().getInt(ARGUMENT_AMOUNT_TYPE);
            currentDate.setTimeInMillis(getArguments().getLong(ARGUMENT_DATE));
        }

        setCurrentDate();

        //load Categories cards
        new LoadCategoriesIDs().execute();

        if (transactionID!=0)
        {
            //set category
            tv_category.setText((CharSequence) categories.getCategoryNames().get((int)chosenCategoryID));
            iv_singleCategory.setImageBitmap((Bitmap) categories.getCategoryImages().get((int)chosenCategoryID));

            anim_categoryCard_in();

            //set description
            et_description.setText(description);
            if (!TextUtils.isEmpty(et_description.getText()))
                btn_description.setImageResource(R.drawable.ic_description_added_24dp);

            //set amount
            changeAmount(transactionAmount);



            setStartAnimation(false);
        }
        else {
            tv_amount.setText(String.format("0,00 %s", currency));
            chosenCategoryID = 0;

            setStartAnimation(true);
        }

        return view;

    }




    @Override
    public void onStart() {
        super.onStart();

    }

    private void setCurrentDate(){
        if(currentDate==null)
            currentDate = Calendar.getInstance();
        btn_date.setText(String.format("%d/%d",
                currentDate.get(Calendar.DAY_OF_MONTH),
                currentDate.get(Calendar.MONTH)+1));
        chosenDate=currentDate.getTimeInMillis();
    }

    private void changeAmount(int amount){
        String num = String.valueOf(amount);
        if (num.endsWith("00")){
            num = String.valueOf(amount/100);
            for (char ch: num.toCharArray()) {
                setAmount(String.valueOf(ch));
            }
        } else {
            num = decimalFormat.format((double)amount/100);
            for (char ch: num.toCharArray()) {
                if (String.valueOf(ch).equals(".")){
                    setAmount(",");
                    countAfterComma++;
                } else {
                    setAmount(String.valueOf(ch));
                }
            }
        }
    }

    private void setStartAnimation(boolean isNew){

        if (!isNew){
            Animation animSlideFromRight = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500);
            mRecyclerView.setAlpha(0.0f);
            cardDate.startAnimation(animSlideFromRight);
            cardDescriptionBtn.startAnimation(animSlideFromRight);
            cardAmount.startAnimation(animSlideFromRight);
            cardSingleCategory.startAnimation(animSlideFromRight);
            cardKeyboard.setVisibility(View.VISIBLE);
            cardKeyboard.startAnimation(animSlideFromRight);

        } else {
            final Animation animDate = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_300);
            final Animation animDescription = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down_300);
            final Animation animCategories = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);
            final Animation animAmount = AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300);

            animDate.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    cardDescriptionBtn.setVisibility(View.VISIBLE);
                    cardDescriptionBtn.startAnimation(animDescription);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            animDescription.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.startAnimation(animCategories);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            animCategories.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cardAmount.setVisibility(View.VISIBLE);
                    cardAmount.startAnimation(animAmount);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            animAmount.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    cardKeyboard.setVisibility(View.VISIBLE);
                    cardKeyboard.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_up_500));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mRecyclerView.setVisibility(View.INVISIBLE);
            cardAmount.setVisibility(View.INVISIBLE);
            cardDescriptionBtn.setVisibility(View.INVISIBLE);
            cardKeyboard.setVisibility(View.INVISIBLE);

            cardDate.startAnimation(animDate);
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

    @Override
    public void onClick(View v) {

        if (v==btn_date){

            anim_dateCard_out();

            // Launch Date Picker Dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view,
                                              int year,
                                              int monthOfYear,
                                              int dayOfMonth) {

                            currentDate.set(year,monthOfYear,dayOfMonth);
                            setCurrentDate();
//                            Calendar newDate = Calendar.getInstance();
//                            newDate.set(year,monthOfYear,dayOfMonth);
//                            chosenDate = newDate.getTimeInMillis();
//                            btn_date.setText(dayOfMonth+"/"+monthOfYear);

                            anim_dateCard_in();
                        }
                    },
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.setOnDismissListener(
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {

                            anim_dateCard_in();}
                    });

            datePickerDialog.getWindow()
                    .getAttributes()
                    .windowAnimations = R.style.DialogCalendar;
            datePickerDialog.show();

            return;
        }

        if (v==btn_description){

            anim_descriptionCard_in();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //set focus to editText & show keyboard
                    et_description.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }, 600);
            return;
         }

        if (v==btn_clear){

            anim_clear();
            return;
        }

        if (v==btn_save){

            if (isCheckedData()){
                anim_save();
            }
            return;
        }

        if (v==cardSingleCategory){

            chosenCategoryID=0;
            anim_categoryCard_out();
            anim_categoriesList_in();
            return;
        }

        if (v==btn_description_ok){

            description = String.valueOf(et_description.getText());
            if (!TextUtils.isEmpty(et_description.getText()))
                btn_description.setImageResource(R.drawable.ic_description_added_24dp);
            else btn_description.setImageResource(R.drawable.ic_assignment_black_24dp);

            anim_descriptionCard_out();

            //hide keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_description.getWindowToken(), 0);
            return;
        }

        if (v==btn_1) {
            setAmount("1");
            return;
        }
        if (v==btn_2) {
            setAmount("2");
            return;
        }
        if (v==btn_3) {
            setAmount("3");
            return;
        }
        if (v==btn_4) {
            setAmount("4");
            return;
        }
        if (v==btn_5) {
            setAmount("5");
            return;
        }
        if (v==btn_6) {
            setAmount("6");
            return;
        }
        if (v==btn_7) {
            setAmount("7");
            return;
        }
        if (v==btn_8) {
            setAmount("8");
            return;
        }
        if (v==btn_9) {
            setAmount("9");
            return;
        }
        if (v==btn_0) {
            setAmount("0");
            return;
        }

        if (v==btn_comma){
            if (amountString != null && amountString.length() > 0 && countAfterComma==0) {
                setAmount(",");
                countAfterComma++;
            }
            return;
        }

        if (v==btn_del){
            if (amountString != null && amountString.length() > 0) {
                removeLastNumber();
            }
            return;
        }

        if (v==btn_calc_plus){
            calculationType=CALCULATE_TYPE_PLUS;
            getFirstCalcNumber(CALCULATE_TYPE_PLUS);
            return;
        }

        if (v==btn_calc_minus){
            calculationType=CALCULATE_TYPE_MINUS;
            getFirstCalcNumber(CALCULATE_TYPE_MINUS);
            return;
        }

        if (v==btn_calc_divide){
            calculationType=CALCULATE_TYPE_DIVIDE;
            getFirstCalcNumber(CALCULATE_TYPE_DIVIDE);
            return;
        }

        if (v==btn_calc_multiply){
            calculationType=CALCULATE_TYPE_MULTIPLY;
            getFirstCalcNumber(CALCULATE_TYPE_MULTIPLY);
            return;
        }

        if (v==btn_calc_equally){
            calculate();
        }

    }

    private void setAmount(String num){

        if (num.equals("0") && amountString.equals("0")){
            return;
        } else {
            if (countAfterComma==0 && amountString.length()<9){
                if (amountString.length()==3 && !num.equals(",")){
                    amountString = amountString.substring(0,1) + " " + amountString.substring(1, amountString.length());
                }
                if (amountString.length()==5 && !num.equals(",")){
                    StringBuilder stringBuilder = new StringBuilder(amountString);
                    stringBuilder.deleteCharAt(1);
                    amountString = stringBuilder.toString();
                    amountString = amountString.substring(0,2) + " " + amountString.substring(2, amountString.length());
                }
                if (amountString.length()==6 && !num.equals(",")){
                    StringBuilder stringBuilder = new StringBuilder(amountString);
                    stringBuilder.deleteCharAt(2);
                    amountString = stringBuilder.toString();
                    amountString = amountString.substring(0,3) + " " + amountString.substring(3, amountString.length());
                }
                if (amountString.length()==7 && !num.equals(",")){
                    StringBuilder stringBuilder = new StringBuilder(amountString);
                    stringBuilder.deleteCharAt(3);
                    amountString = stringBuilder.toString();
                    amountString = amountString.substring(0,1) + " " + amountString.substring(1, amountString.length());
                    amountString = amountString.substring(0,5) + " " + amountString.substring(5, amountString.length());
                }
            }
            if(countAfterComma<3 && amountString.length()<9){
                amountString+=num;
                tv_amount.setText(amountString+" "+currency);
            }
            if (countAfterComma>0 && countAfterComma<3)
                countAfterComma++;
        }
    }

    private void removeLastNumber(){
        if (amountString.endsWith(" ")){
            amountString = amountString.substring(0, amountString.length()-1);
        }

        if (amountString.length()==5 && !amountString.contains(",")){
            StringBuilder stringBuilder = new StringBuilder(amountString);
            stringBuilder.deleteCharAt(1);
            amountString = stringBuilder.toString();
        }
        if (amountString.length()==6 && !amountString.contains(",")){
            StringBuilder stringBuilder = new StringBuilder(amountString);
            stringBuilder.deleteCharAt(2);
            amountString = stringBuilder.toString();
            amountString = amountString.substring(0,1) + " " + amountString.substring(1, amountString.length());
        }
        if (amountString.length()==7 && !amountString.contains(",")){
            StringBuilder stringBuilder = new StringBuilder(amountString);
            stringBuilder.deleteCharAt(3);
            amountString = stringBuilder.toString();
            amountString = amountString.substring(0,2) + " " + amountString.substring(2, amountString.length());
        }
        if (amountString.length()==9 && !amountString.contains(",")){
            StringBuilder stringBuilder = new StringBuilder(amountString);
            stringBuilder.deleteCharAt(1);
            stringBuilder.deleteCharAt(4);
            amountString = stringBuilder.toString();
            amountString = amountString.substring(0,3) + " " + amountString.substring(3, amountString.length());
        }

        amountString = amountString.substring(0, amountString.length()-1);

        if (amountString.length()==0)
            tv_amount.setText(String.format("%s %s",
                    getResources().getString(R.string.default_amount),
                    currency));
        else
            tv_amount.setText(amountString+" "+currency);

        if (countAfterComma>0)
            countAfterComma--;
    }

    private void getFirstCalcNumber(int calculationType){
        if (amountString != null && amountString.length() > 0){

            amountString = amountString.replaceAll("\\s+","");
            amountString = amountString.replaceAll(",","\\.");


            if (calculationFirstNumber==0){

                calculationFirstNumber = Double.valueOf(amountString);

                amountString="";
                countAfterComma=0;
                switch (calculationType){

                    case CALCULATE_TYPE_PLUS:
                        tv_amount.setText("+");
                        break;
                    case CALCULATE_TYPE_MINUS:
                        tv_amount.setText("-");
                        break;
                    case CALCULATE_TYPE_DIVIDE:
                        tv_amount.setText("/");
                        break;
                    case CALCULATE_TYPE_MULTIPLY:
                        tv_amount.setText("x");
                        break;
                }
            } else {
                double secondNumber;

                amountString = amountString.replaceAll("\\s+","");
                amountString = amountString.replaceAll(",","\\.");

                secondNumber = Double.valueOf(amountString);

                switch (calculationType){

                    case CALCULATE_TYPE_PLUS:
                        calculationFirstNumber = calculationFirstNumber + secondNumber;
                        tv_amount.setText("+");
                        break;
                    case CALCULATE_TYPE_MINUS:
                        calculationFirstNumber = calculationFirstNumber - secondNumber;
                        tv_amount.setText("-");
                        break;
                    case CALCULATE_TYPE_DIVIDE:
                        calculationFirstNumber = calculationFirstNumber / secondNumber;
                        tv_amount.setText("/");
                        break;
                    case CALCULATE_TYPE_MULTIPLY:
                        calculationFirstNumber = calculationFirstNumber * secondNumber;
                        tv_amount.setText("x");
                        break;
                }
                amountString="";
                countAfterComma=0;
            }
        }
    }

    private void calculate(){

        if (amountString!=null && calculationType!=0 && calculationFirstNumber!=0){

            amountString = amountString.replaceAll("\\s+","");
            amountString = amountString.replaceAll(",","\\.");

            double secondNumber = Double.valueOf(amountString);
            double result=0;

            switch (calculationType){

                case CALCULATE_TYPE_PLUS:
                    result = new BigDecimal(calculationFirstNumber + secondNumber)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    break;
                case CALCULATE_TYPE_MINUS:
                    result = new BigDecimal(calculationFirstNumber - secondNumber)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    break;
                case CALCULATE_TYPE_DIVIDE:
                    result = new BigDecimal(calculationFirstNumber / secondNumber)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    break;
                case CALCULATE_TYPE_MULTIPLY:
                    result = new BigDecimal(calculationFirstNumber * secondNumber)
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    break;
            }

            calculationFirstNumber=0;
            amountString=String.valueOf(result);
            amountString = amountString.replaceAll("\\.","\\,");

            if (amountString.contains(",")){
                String[] amountArray = amountString.split(",");
                if (amountArray[1].length()==1){
                    if (amountArray[1].contains("0")){
                        amountString = amountArray[0];
                    }
                }
            }
            tv_amount.setText(amountString+" "+currency);
        }
    }

    private boolean isCheckedData() {
        if (chosenCategoryID==0){
            Toast.makeText(getContext(),
                    R.string.toast_choose_category,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (amountString.equals("")){
            Toast.makeText(getContext(),
                    R.string.toast_choose_amount,
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            transactionAmount = getIntAmount();
            if (transactionAmount==0){
                Toast.makeText(getContext(),
                        getString(R.string.toast_zero_amount)+currency,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            if (mSwitch.isChecked())
                transactionType=TRANSACTION_INCOME;
            else transactionType=TRANSACTION_EXPENSE;

            return true;
        }
    }

    private int getIntAmount(){
        amountString = amountString.replaceAll("\\s+", "");

        if (amountString.contains(",")){
            String[] splitAmount = amountString.split(",");
            if (splitAmount[1].length()>1)
                return Integer.valueOf(splitAmount[0]+splitAmount[1]);
            else
                return Integer.valueOf(splitAmount[0]+splitAmount[1]+"0");
        } else
            return Integer.valueOf(amountString+"00");
    }




    /**
     * ___________________________________________
     * Animations
     * -------------------------------------------
     */

    public void anim_clear(){

        cardDate.animate()
                .translationX(-2000)
                .setDuration(300)
                .setStartDelay(100);

        cardDescriptionBtn.animate()
                .translationX(-2000)
                .setDuration(300)
                .setStartDelay(300);

        cardKeyboard.animate()
                .translationX(-2000)
                .setDuration(300)
                .setStartDelay(200);

        if (chosenCategoryID!=0){
            cardSingleCategory.animate()
                    .translationX(-2000)
                    .setDuration(300)
                    .setStartDelay(200);
        } else {
            mRecyclerView.animate()
                    .translationX(-2000)
                    .setDuration(300)
                    .setStartDelay(350);
        }

        if (cardCalculator.getVisibility()==View.VISIBLE)
            cardCalculator.animate()
                    .translationX(-2000)
                    .setDuration(300)
                    .setStartDelay(200);

        cardAmount.animate()
                .translationX(-2000)
                .setDuration(300)
                .setStartDelay(400).setListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        et_description.getText().clear();
                        mSwitch.setChecked(false);
                        fragmentCallback.onCallback(MainActivity.CALLBACK_RESTART, null, null, -1, 0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    public void anim_save(){
        cardDescriptionBtn.animate()
                .translationX(2000)
                .setDuration(300);

        cardSingleCategory.animate()
                .translationX(2000)
                .setDuration(300)
                .setStartDelay(100);

        cardDate.animate()
                .translationX(2000)
                .setDuration(300)
                .setStartDelay(200);

        cardKeyboard.animate()
                .translationX(2000)
                .setDuration(300)
                .setStartDelay(200);

        if (cardCalculator.getVisibility()==View.VISIBLE)
            cardCalculator.animate()
                    .translationX(2000)
                    .setDuration(300)
                    .setStartDelay(200);

        cardAmount.animate()
                .translationX(2000)
                .setDuration(300)
                .setStartDelay(300).setListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {}

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        new AddToDBAsync().execute();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
    }

    //----------Date card
    public void anim_dateCard_in(){
        cardDate.animate()
                .translationXBy(cardDate.getWidth())
                .setDuration(300)
                .setStartDelay(300)
                .alpha(1.0f);
    }

    public void anim_dateCard_out(){
        cardDate.animate()
                .translationX(-cardDate.getWidth())
                .setDuration(300)
                .alpha(0.0f);
    }

    //----------Categories list
    public void anim_categoriesList_in(){
        mAdapter.notifyDataSetChanged();
        mRecyclerView.setTranslationX(-mRecyclerView.getWidth());
        mRecyclerView.animate()
                .translationXBy(mRecyclerView.getWidth())
                .setDuration(300)
                .setStartDelay(400)
                .alpha(1.0f);

    }

    public void anim_categoriesList_out(int position){
        if (position==0){
            mAdapter.notifyItemRemoved(position);
        } else {
            mAdapter.notifyItemMoved(position, 0);
        }
        mRecyclerView.animate()
                .translationX(-mRecyclerView.getWidth())
                .setDuration(300)
                .setStartDelay(400)
                .alpha(0.0f);
    }

    //----------Single Category card
    public void anim_categoryCard_in(){

        cardSingleCategory.setVisibility(View.VISIBLE);
        cardSingleCategory.setTranslationY(-cardSingleCategory.getWidth()*2);
        cardSingleCategory.animate()
                .translationYBy(cardSingleCategory.getWidth()*2)
                .setDuration(300)
                .setStartDelay(600)
                .alpha(1.0f);
    }

    public void anim_categoryCard_out(){
        cardSingleCategory.animate()
                .translationY(-cardSingleCategory.getHeight())
                .setDuration(300)
                .setStartDelay(50)
                .alpha(0.0f);
    }

    //----------Description card
    public void anim_descriptionCard_in(){
        cardSingleCategory.animate()
                .translationY(-cardSingleCategory.getHeight())
                .setDuration(300)
                .alpha(0.0f)
                .setStartDelay(100);

        cardDescriptionBtn.animate()
                .translationY(-cardDescriptionBtn.getHeight())
                .setDuration(300)
                .alpha(0.0f)
                .setStartDelay(200);

        cardDate.animate()
                .translationY(-cardDate.getHeight())
                .setDuration(300)
                .alpha(0.0f)
                .setStartDelay(300);


        if(chosenCategoryID==0){
            mRecyclerView.animate()
                    .translationX(-mRecyclerView.getWidth())
                    .setDuration(300)
                    .alpha(0.0f)
                    .setStartDelay(400);
        }


        cardAmount.animate()
                .translationX(-cardAmount.getWidth())
                .setDuration(300)
                .alpha(0.0f)
                .setStartDelay(500);

        cardKeyboard.animate()
                .translationX(-cardKeyboard.getWidth())
                .setDuration(300)
                .alpha(0.0f);

        cardDescriptionText.setVisibility(View.VISIBLE);
        cardDescriptionText.setTranslationY(-cardDescriptionText.getHeight());
        cardDescriptionText.animate()
                .translationYBy(cardDescriptionText.getHeight())
                .setDuration(300)
                .setStartDelay(400)
                .alpha(1.0f);

        if (cardCalculator.getVisibility()==View.VISIBLE){
            cardCalculator.setVisibility(View.INVISIBLE);
        }
    }

    public void anim_descriptionCard_out(){

        cardDescriptionText.animate()
                .translationY(-cardDescriptionText.getHeight())
                .setDuration(300)
                .alpha(0.0f);

        if (chosenCategoryID!=0){
            cardSingleCategory.setTranslationY(-cardSingleCategory.getHeight());
            cardSingleCategory.animate()
                    .translationYBy(cardSingleCategory.getHeight())
                    .setDuration(300)
                    .alpha(1.0f)
                    .setStartDelay(400);
        } else {
            mRecyclerView.setTranslationX(-mRecyclerView.getWidth());
            mRecyclerView.animate()
                    .translationXBy(mRecyclerView.getWidth())
                    .setDuration(300)
                    .alpha(1.0f)
                    .setStartDelay(400);
        }

        cardDescriptionBtn.setTranslationY(-cardDescriptionBtn.getHeight());
        cardDescriptionBtn.animate()
                .translationYBy(cardDescriptionBtn.getHeight())
                .setDuration(300)
                .alpha(1.0f)
                .setStartDelay(500);

        cardDate.setTranslationY(-cardDate.getHeight());
        cardDate.animate()
                .translationYBy(cardDate.getHeight())
                .setDuration(300)
                .alpha(1.0f)
                .setStartDelay(600);

        cardAmount.setTranslationX(-cardAmount.getWidth());
        cardAmount.animate()
                .translationXBy(cardAmount.getWidth())
                .setDuration(500)
                .alpha(1.0f)
                .setStartDelay(500);

        cardKeyboard.setTranslationX(-cardKeyboard.getWidth());
        cardKeyboard.animate()
                .translationXBy(cardKeyboard.getWidth())
                .setDuration(500)
                .alpha(1.0f)
                .setStartDelay(500);
        if (cardCalculator.getVisibility()==View.INVISIBLE){
            cardCalculator.setVisibility(View.VISIBLE);
            cardCalculator.startAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.slide_from_right_500));
        }
    }


    /**
     * ____________________________________
     *      Load Categories IDs AsyncTask
     *  ___________________________________
     */

    private class LoadCategoriesIDs extends AsyncTask<Void, Void, int[]>{
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
                mRecyclerView.setLayoutManager(
                        new LinearLayoutManager(
                                getContext(),
                                LinearLayoutManager.HORIZONTAL,false));
                mAdapter.setClickListener(InputFragment.this);
            }
        }
    }



    /**
     * ___________________________
     *      Add to DB AsyncTask
     *  __________________________
     */

    private class AddToDBAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            DB db = new DB(getContext());
            if (transactionID==0){
                db.addTransaction(
                        transactionAmount,
                        transactionType,
                        chosenDate,
                        chosenCategoryID,
                        description,
                        0);
            } else {
                db.changeTransaction(transactionID,
                        transactionAmount,
                        transactionType,
                        chosenDate,
                        chosenCategoryID,
                        description);
            }
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            tv_amount.setText(String.format("%s %s",
                    getResources().getString(R.string.default_amount),
                    currency));

            et_description.getText().clear();
            mSwitch.setChecked(false);

            if (transactionID==0)
                fragmentCallback.onCallback(MainActivity.CALLBACK_INSERT_TO_FIREBASE,
                        null,
                        null,
                        MainActivity.DEFAULT_GROUP_POSITION,
                        0);
            else fragmentCallback.onCallback(MainActivity.CALLBACK_UPDATE_TRANSACTION,
                    null,
                    new PostTransaction(
                            transactionID,
                            transactionAmount,
                            transactionType,
                            chosenDate,
                            (int)chosenCategoryID,
                            description,
                            PostTransaction.STATUS_CHANGED),
                    MainActivity.DEFAULT_GROUP_POSITION,
                    0);
        }
    }
}
