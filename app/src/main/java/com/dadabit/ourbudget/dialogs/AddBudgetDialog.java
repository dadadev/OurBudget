package com.dadabit.ourbudget.dialogs;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.dadabit.ourbudget.MainActivity;
import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.adapters.CategoriesRecyclerAdapter;
import com.dadabit.ourbudget.customClasses.Budget;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.interfaces.CategoryClickListener;
import com.dadabit.ourbudget.interfaces.FragmentCallback;

import static com.dadabit.ourbudget.MainActivity.categories;

public class AddBudgetDialog extends DialogFragment implements
        View.OnClickListener,
        CategoryClickListener,
        RadioGroup.OnCheckedChangeListener {

    private EditText etAmount;
    private RecyclerView recyclerView;
    private Button btnCreate;
    private CardView singleCategory;
    private ImageView ivSingleCategory;

    private int chosenType;
    private long chosenCategory;

    private FragmentCallback fragmentCallback;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        fragmentCallback = (FragmentCallback) getContext();

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_budget);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogBudgets;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        etAmount = (EditText) dialog.findViewById(R.id.dialog_budget_amount);
        recyclerView = (RecyclerView) dialog.findViewById(R.id.dialog_budget_recycler_view);
        btnCreate = (Button) dialog.findViewById(R.id.dialog_budget_create);
        singleCategory = (CardView) dialog.findViewById(R.id.dialog_budget_card_category);
        ivSingleCategory = (ImageView) dialog.findViewById(R.id.dialog_budget_card_category_iv);
        RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.dialog_budget_radioGroup);

        btnCreate.setOnClickListener(this);
        singleCategory.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);

        new GetCategories().execute();

        return dialog;
    }


    @Override
    public void onClick(View v) {



        if(v==singleCategory){
            chosenCategory=0;
            animate(v);
        }

        if (v==btnCreate){

            if (isCheckedData()){

                new DB(getContext()).addBudget((String) categories.getCategoryNames().get((int) chosenCategory), getAmount(), chosenType, chosenCategory);
                fragmentCallback.onCallback(MainActivity.CALLBACK_RESTART, null, null, 0, 0);

                dismiss();
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

    @Override
    public void categoryClickListener(int position, long id) {
        chosenCategory=id;
        ivSingleCategory.setImageBitmap((Bitmap) categories.getCategoryImages().get((int)id));


        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                recyclerView.setVisibility(View.INVISIBLE);
                singleCategory.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_right_500));
                singleCategory.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        recyclerView.startAnimation(animation);

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId==R.id.dialog_budget_year){
            chosenType= Budget.TYPE_BY_YEAR;
        }
        if(checkedId==R.id.dialog_budget_month){
            chosenType=Budget.TYPE_BY_MONTH;
        }
        if(checkedId==R.id.dialog_budget_week){
            chosenType=Budget.TYPE_BY_WEEK;
        }
    }

    private boolean isCheckedData() {

        if (TextUtils.isEmpty(etAmount.getText())){
            return false;
        }
        if (chosenType==0){
            return false;
        }
        if (chosenCategory==0){
            return false;
        }

        return true;
    }

    private void animate(View v){


        if(v==singleCategory){
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_left_200);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    singleCategory.setVisibility(View.GONE);
                    recyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_from_left_300));
                    recyclerView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            singleCategory.startAnimation(animation);
        }

    }

    /**
     * ______________________________________________________
     *  Get Categories & Create adapter AsyncTask
     *  _____________________________________________________
     */

    private class GetCategories extends AsyncTask<Void, Void, int[]> {

        @Override
        protected int[] doInBackground(Void... params) {

            return new DB(getContext()).getSortedCategoriesIds();
        }

        @Override
        protected void onPostExecute(int[] categoryIDs) {
            super.onPostExecute(categoryIDs);

            if (categoryIDs!=null){
                CategoriesRecyclerAdapter mAdapter = new CategoriesRecyclerAdapter(getContext(), categoryIDs);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                        LinearLayoutManager.HORIZONTAL,false));
                mAdapter.setClickListener(AddBudgetDialog.this);
            }
        }
    }
}
