package com.dadabit.ourbudget.adapters;


import android.content.Context;
import android.graphics.Bitmap;
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
import com.dadabit.ourbudget.customClasses.RegularPayment;
import com.dadabit.ourbudget.fragments.main.InputFragment;
import com.dadabit.ourbudget.fragments.payments.PaymentsInputFragment;
import com.dadabit.ourbudget.interfaces.PaymentsListAdapterCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class PaymentsRecyclerAdapter extends RecyclerView.Adapter<PaymentsRecyclerAdapter.ViewHolder>{


    private Context mContext;
    private ArrayList<RegularPayment> paymentsArray;

    private PaymentsListAdapterCallback callback;
    

    public PaymentsRecyclerAdapter(Context context, ArrayList<RegularPayment> payments, PaymentsListAdapterCallback callback) {
        mContext = context;
        paymentsArray = payments;
        this.callback = callback;
    }



    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener{

        CardView mCardView;
        ImageView mImageView;
        TextView mTextViewName, mTextViewType, mTextViewAmount;
        ImageButton mButtonDelete;
        long id;

        ViewHolder(View v){
            super(v);
            // Get the widget reference from the custom layout
            mCardView = (CardView) v.findViewById(R.id.payments_list_card);
            mImageView = (ImageView) v.findViewById(R.id.payments_list_iv);
            mTextViewName = (TextView) v.findViewById(R.id.payments_list_name);
            mTextViewAmount = (TextView) v.findViewById(R.id.payments_list_amount);
            mTextViewType = (TextView) v.findViewById(R.id.payments_list_tv_description);
            mButtonDelete = (ImageButton) v.findViewById(R.id.payments_list_btn_delete);

            mCardView.setOnLongClickListener(this);
            mButtonDelete.setOnClickListener(this);

        }


        @Override
        public boolean onLongClick(View v) {


            if (mButtonDelete.getVisibility()== View.GONE){
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right_200);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mTextViewAmount.setVisibility(View.GONE);
                        mTextViewType.setVisibility(View.GONE);
                        mButtonDelete.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_from_right_500));
                        mButtonDelete.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mTextViewAmount.startAnimation(animation);
                mTextViewType.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right_200));
            } else {
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right_200);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mButtonDelete.setVisibility(View.GONE);
                        mTextViewAmount.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_from_right_500));
                        mTextViewType.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_from_right_500));
                        mTextViewAmount.setVisibility(View.VISIBLE);
                        mTextViewType.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mButtonDelete.startAnimation(animation);
            }

            return true;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            callback.deleteItem(position, paymentsArray.get(position));
            paymentsArray.remove(position);
            notifyItemRemoved(position);
        }
    }

    
    public void getDeletedBack(int position, RegularPayment payment){
        paymentsArray.add(position, payment);
        notifyItemInserted(position);

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_payments_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTextViewName.setText(paymentsArray.get(position).getName());

        if (paymentsArray.get(position).getAmountType()== InputFragment.TRANSACTION_EXPENSE) {
            holder.mTextViewAmount.setText(String.format("-%s", getAmountString(paymentsArray.get(position).getAmount())));
        }
        else {
            holder.mTextViewAmount.setText(String.format("+%s", getAmountString(paymentsArray.get(position).getAmount())));
        }

        holder.mImageView.setImageBitmap((Bitmap) categories.getCategoryImages()
                .get((int) paymentsArray.get(position).getCategory()));
        holder.mTextViewType.setText(getType(paymentsArray.get(position).getType()));
        holder.id = paymentsArray.get(position).getId();

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
        switch (type){
            case PaymentsInputFragment.PAYMENT_TYPE_DAY:
                st = mContext.getString(R.string.every_day);
                break;
            case PaymentsInputFragment.PAYMENT_TYPE_WEEK:
                st = mContext.getString(R.string.every_week);
                break;
            case PaymentsInputFragment.PAYMENT_TYPE_MONTH:
                st = mContext.getString(R.string.every_month);
                break;
        }
        return st;
    }

    @Override
    public int getItemCount() {
        return paymentsArray.size();
    }



}
