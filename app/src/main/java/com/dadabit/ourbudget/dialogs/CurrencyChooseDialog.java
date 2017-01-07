package com.dadabit.ourbudget.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.dadabit.ourbudget.MainActivity;
import com.dadabit.ourbudget.R;
import com.dadabit.ourbudget.interfaces.FragmentCallback;

import static com.dadabit.ourbudget.MainActivity.APP_PREFERENCES;
import static com.dadabit.ourbudget.MainActivity.APP_PREFERENCES_CURRENCY;
import static com.dadabit.ourbudget.MainActivity.currency;


public class CurrencyChooseDialog extends DialogFragment {

    private String[] currencyArray;

    private RecyclerView mRecyclerView;

    private FragmentCallback fragmentCallback;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_currency);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.LTGRAY));
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogCurrency;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        currencyArray = getResources().getStringArray(R.array.currency_array);
        mRecyclerView = (RecyclerView) dialog.findViewById(R.id.dialog_currency_recyclerView);
        mRecyclerView.setAdapter(new CardsAdapter());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        fragmentCallback = (FragmentCallback) getContext();

        return dialog;
    }

    private void saveNewCurrency(String newCurrency){


        SharedPreferences.Editor editor =
                getActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(APP_PREFERENCES_CURRENCY, newCurrency);
        editor.apply();
        currency = newCurrency;
        fragmentCallback.onCallback(MainActivity.CALLBACK_RESTART, null, null,0,0);
        dismiss();
    }



    private class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.ViewHolder>{


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_card_currency, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (currencyArray!=null){
                holder.textView.setText(currencyArray[position]);
            }

        }

        @Override
        public int getItemCount() {
            return currencyArray.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            CardView cardView;
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);

                cardView = (CardView) itemView.findViewById(R.id.card_currency);
                textView = (TextView) itemView.findViewById(R.id.card_currency_tv);

                cardView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                saveNewCurrency(currencyArray[getAdapterPosition()]);
            }
        }

    }
}
