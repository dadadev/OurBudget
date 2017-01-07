package com.dadabit.ourbudget.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.customClasses.Transaction;
import com.dadabit.ourbudget.fragments.main.InputFragment;
import com.dadabit.ourbudget.interfaces.SingleTransactionOnClick;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.dadabit.ourbudget.MainActivity.categories;
import static com.dadabit.ourbudget.MainActivity.currency;

public class ExpListAdapter extends BaseExpandableListAdapter {

    public static final int SORT_BY_MONTH = 0;
    public static final int SORT_BY_WEEK = 1;
    public static final int SORT_BY_DAY = 2;

    private ArrayList<ArrayList<Transaction>> mGroups;
    private Context mContext;
    private int sortBy;

    private SingleTransactionOnClick setToFragment = null;

    public ExpListAdapter(Context context, ArrayList<ArrayList<Transaction>> groups, int sortNum, SingleTransactionOnClick onClick){
        mContext = context;
        mGroups = groups;
        sortBy = sortNum;
        setToFragment = onClick;
    }

    public void setGroups(ArrayList<ArrayList<Transaction>> mGroups) {
        this.mGroups = mGroups;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_expandable_list_group, null);
        }

        if (isExpanded){
            //Group is open
        }
        else{
            //Group is closed
        }

        if (mGroups.get(groupPosition).size()!=0 && mGroups.get(groupPosition)!=null){
            try {
                TextView date = (TextView) convertView.findViewById(R.id.exList_group_date);
                int year = mGroups.get(groupPosition).get(0).getDate().get(Calendar.YEAR);
                if (sortBy==SORT_BY_MONTH){
//                    String month = new SimpleDateFormat("MMMM").format(mGroups.get(groupPosition).get(0).getDate().getTime());
                    String month = (String) mContext.getResources().getTextArray(
                            R.array.statistic_month_array)[mGroups.get(groupPosition).get(0).getDate().get(Calendar.MONTH)];
                    date.setText(year+" "+month);
                }
                if (sortBy==SORT_BY_WEEK){
                    String week = mContext.getString(R.string.week)+mGroups.get(groupPosition).get(0).getDate().get(Calendar.WEEK_OF_YEAR);
                    date.setText(year+week);
                }
                if (sortBy==SORT_BY_DAY){
                    String day = mGroups.get(groupPosition).get(0).getDate().get(Calendar.DAY_OF_MONTH)+"/"+
                            mGroups.get(groupPosition).get(0).getDate().get(Calendar.MONTH)+"/"+
                            mGroups.get(groupPosition).get(0).getDate().get(Calendar.YEAR);
                    date.setText(day);
                }

                TextView tvBalanceGroup = (TextView) convertView.findViewById(R.id.exList_group_balance);
                tvBalanceGroup.setText(getSummOfGroup(groupPosition, tvBalanceGroup)+" "+currency);
            } catch (NullPointerException e){
                
            }

        }
        return convertView;


    }

    public double getSummOfGroup(int position, TextView textView){
        double sum=0;
        for (int i = 0; i < mGroups.get(position).size(); i++) {
            if (mGroups.get(position).get(i).getAmountType() == InputFragment.TRANSACTION_EXPENSE)
                sum-=mGroups.get(position).get(i).getAmount();
            else sum+=mGroups.get(position).get(i).getAmount();
        }
        if (sum<0) textView.setTextColor(ContextCompat.getColor(mContext, R.color.amount_minus));
        else textView.setTextColor(ContextCompat.getColor(mContext, R.color.amount_plus));
//        return new BigDecimal(sum).setScale(2, RoundingMode.UP).doubleValue();
        return sum/100;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.view_expandable_list_child, null);
        }

        ImageView categoryImg = (ImageView) convertView.findViewById(R.id.exList_child_iv);
        categoryImg.setImageBitmap((Bitmap)categories.getCategoryImages().get(mGroups.get(groupPosition).get(childPosition).getCategoryID()));
//        if (mCategories!=null){
//            categoryImg.setImageBitmap((Bitmap) mCategories.get(mGroups.get(groupPosition).get(childPosition).getCategoryID()));
//        } else categoryImg.setImageResource(R.mipmap.ic_categ_default);



        TextView date = (TextView) convertView.findViewById(R.id.exList_child_date);

        int day = (mGroups.get(groupPosition).get(childPosition).getDate().get(Calendar.DAY_OF_MONTH));
        int month = (mGroups.get(groupPosition).get(childPosition).getDate().get(Calendar.MONTH));
        int year = (mGroups.get(groupPosition).get(childPosition).getDate().get(Calendar.YEAR));
        date.setText(day+"/"+month+"/"+year);

        TextView day_tv = (TextView) convertView.findViewById(R.id.exList_child_day);

        String dayOfWeek = new SimpleDateFormat("EEEE").format((mGroups.get(groupPosition).get(childPosition).getDate().getTime()));
        day_tv.setText(dayOfWeek);

        TextView amount = (TextView) convertView.findViewById(R.id.exList_child_amount);

        if (mGroups.get(groupPosition).get(childPosition).getAmountType()==InputFragment.TRANSACTION_EXPENSE){
            amount.setText(getAmountString("-", mGroups.get(groupPosition).get(childPosition).getAmount()));
        } else {
            amount.setText(getAmountString("+", mGroups.get(groupPosition).get(childPosition).getAmount()));
        }

        ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.trList_btn_delete);
        btnDelete.setVisibility(View.GONE);

        ImageButton btnEdit = (ImageButton) convertView.findViewById(R.id.trList_btn_edit);
        btnEdit.setVisibility(View.GONE);

        TextView tvDescription = (TextView) convertView.findViewById(R.id.trList_tv_description);
        tvDescription.setVisibility(View.GONE);

        TextView tvCategoryName = (TextView) convertView.findViewById(R.id.trList_tv_category_name);
        tvCategoryName.setVisibility(View.GONE);

        TextView tvPerson = (TextView) convertView.findViewById(R.id.trList_tv_person);
        tvPerson.setVisibility(View.GONE);



        CardView cardView = (CardView) convertView.findViewById(R.id.trList_card);
        cardView.setTag(R.id.trList_btn_delete, btnDelete);
        cardView.setTag(R.id.trList_btn_edit, btnEdit);
        cardView.setTag(R.id.trList_tv_description, tvDescription);
        cardView.setTag(R.id.trList_tv_category_name, tvCategoryName);
        cardView.setTag(R.id.trList_tv_person, tvPerson);


        setToFragment.transactionClickListener(cardView, groupPosition, childPosition);



        return convertView;
    }

    private String getAmountString(String type, int amount){
        String result = String.valueOf(amount);
        if (result.endsWith("00")){
            result = String.valueOf(amount/100);
        } else {
            DecimalFormat formatter = new DecimalFormat("#0.00");
            result = formatter.format((double)amount/100);
        }
        return type+result+" "+ currency;
    }





    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

}