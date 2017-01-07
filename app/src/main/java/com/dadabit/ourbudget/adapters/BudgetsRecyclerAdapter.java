package com.dadabit.ourbudget.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.customClasses.Budget;
import com.dadabit.ourbudget.customClasses.TextProgressBar;
import com.dadabit.ourbudget.interfaces.BudgetDeleteClickListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class BudgetsRecyclerAdapter extends RecyclerView.Adapter<BudgetsRecyclerAdapter.ViewHolder>{


    private ArrayList<Budget> mBudgets;
    private Context mContext;

    private BudgetDeleteClickListener deleteClickListener = null;
    private Budget deletedBudget;


    public BudgetsRecyclerAdapter(Context context, ArrayList<Budget> budgets) {
        mBudgets = budgets;
        mContext = context;
    }

    public void setOnClickListener (BudgetDeleteClickListener deleteClickListener){
        this.deleteClickListener = deleteClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,  View.OnLongClickListener{

        public long id;

        CardView mCardView;
        ImageView mImageView;
        TextProgressBar mProgressBar;
        TextView mTextViewName, mTextViewDate, mTextViewAmount, mTextViewDaySpend;
        ImageButton mButtonDelete;

        ViewHolder(View v){
            super(v);
            // Get the widget reference from the custom layout
            mCardView = (CardView) v.findViewById(R.id.card_budget);
            mImageView = (ImageView) v.findViewById(R.id.card_budgets_iv);
            mProgressBar = (TextProgressBar) v.findViewById(R.id.card_budgets_progressBar);
            mTextViewName = (TextView) v.findViewById(R.id.card_budgets_name);
            mTextViewDate = (TextView) v.findViewById(R.id.card_budgets_by_date);
            mTextViewAmount = (TextView) v.findViewById(R.id.card_budgets_amount);

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            v.setTag(mCardView);

            mTextViewDaySpend = (TextView) v.findViewById(R.id.card_budgets_day_spend);
            mButtonDelete = (ImageButton) v.findViewById(R.id.card_budgets_delete);

            if (mButtonDelete.getVisibility()== View.VISIBLE){
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_up_300);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mButtonDelete.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                mButtonDelete.startAnimation(animation);
            }

            if (mTextViewDaySpend.getVisibility() == View.GONE){

                int dayBalance = mBudgets.get(getAdapterPosition()).getDayBalance();
                if (dayBalance>0){
                    mTextViewDaySpend.setText(
                            String.format("%s%s",
                                    getAmountString(dayBalance),
                                    mContext.getString(R.string.budget_card_every_day)));
                    mTextViewDaySpend.setVisibility(View.VISIBLE);
                    mTextViewDaySpend.startAnimation(AnimationUtils
                            .loadAnimation(mContext, R.anim.slide_down_300));
                }

            }
            else {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_up_300);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTextViewDaySpend.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                mTextViewDaySpend.startAnimation(animation);
            }
        }

        @Override
        public boolean onLongClick(View v) {

            mTextViewDaySpend = (TextView) v.findViewById(R.id.card_budgets_day_spend);
            mButtonDelete = (ImageButton) v.findViewById(R.id.card_budgets_delete);

            if (mTextViewDaySpend.getVisibility()== View.VISIBLE){
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_up_300);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTextViewDaySpend.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                mTextViewDaySpend.startAnimation(animation);
            }

            if (mButtonDelete.getVisibility()== View.GONE){
                mButtonDelete.setVisibility(View.VISIBLE);
                mButtonDelete.startAnimation(AnimationUtils
                        .loadAnimation(mContext, R.anim.slide_down_300));
            }

            mButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (deleteClickListener!=null)
                        deleteClickListener.deleteOnClickItem(getAdapterPosition(), mBudgets.get(getAdapterPosition()).getId());
                }
            });
            return true;
        }
    }

    public void hideBudget(int position){

        deletedBudget = mBudgets.get(position);
        mBudgets.remove(position);
        notifyItemRemoved(position);

    }

    public void getBudgetBack(int position){
        mBudgets.add(position, deletedBudget);
        notifyItemChanged(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_card_budget, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.id = mBudgets.get(position).getId();
        holder.mTextViewName.setText(mBudgets.get(position).getName());
        holder.mTextViewAmount.setText(getAmountString(mBudgets.get(position).getAmount()));
        holder.mTextViewDate.setText(getType(mBudgets.get(position).getType()));

        holder.mProgressBar.setMax(mBudgets.get(position).getAmount());
        holder.mProgressBar.setProgress(mBudgets.get(position).getSpend());
        holder.mProgressBar.setText(
                getAmountString(mBudgets.get(position).getSpend()),
                getAmountString(mBudgets.get(position).getOverBalance()));

        holder.mImageView.setImageBitmap((Bitmap) categories.getCategoryImages().get((int) mBudgets.get(position).getCategID()));
        if (mBudgets.get(position).getSpend()>mBudgets.get(position).getAmount())
            holder.mProgressBar.setProgressDrawable(ContextCompat.getDrawable(mContext, R.drawable.progress_bar_full));
    }

    private String getAmountString(int amount){
        DecimalFormat formatter = new DecimalFormat("#0.00");
        String result = formatter.format((double)amount/100);
        if (result.endsWith("00")){
            result = String.valueOf(amount/100);
        }
        return result+" "+currency;
    }

    private String getType(int type){
        String st="";
        if (type==Budget.TYPE_BY_WEEK)
            st = mContext.getString(R.string.budget_card_in_week);
        if (type==Budget.TYPE_BY_MONTH)
            st = mContext.getString(R.string.budget_card_in_month);
        if (type==Budget.TYPE_BY_YEAR)
            st = mContext.getString(R.string.budget_card_in_year);
        return st;
    }

    @Override
    public int getItemCount() {
        return mBudgets.size();
    }

}
