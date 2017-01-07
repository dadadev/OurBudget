package com.dadabit.ourbudget.adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.interfaces.CategoryClickListener;

import static com.dadabit.ourbudget.MainActivity.categories;

public class CategoriesRecyclerAdapter extends RecyclerView.Adapter<CategoriesRecyclerAdapter.ViewHolder>{

    private CategoryClickListener clickListener=null;

    private int[] categoryIDs;
    private Context mContext;

    public CategoriesRecyclerAdapter(Context context, int[] categoryIDs) {
        this.categoryIDs = categoryIDs;
        mContext = context;
    }

    public void setClickListener(CategoryClickListener clickListener) {
        this.clickListener = clickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public long id;

        CardView mCardView;
        ImageView mImageView;

        ViewHolder(View v){
            super(v);
            // Get the widget reference from the custom layout
            mCardView = (CardView) v.findViewById(R.id.card_category);
            mImageView = (ImageView) v.findViewById(R.id.card_category_iv);

            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.categoryClickListener(getAdapterPosition(), id);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_card_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mImageView.setImageBitmap((Bitmap) categories.getCategoryImages().get(categoryIDs[position]));
        holder.id = categoryIDs[position];

    }

    @Override
    public int getItemCount() {
        return categoryIDs.length;
    }

}
