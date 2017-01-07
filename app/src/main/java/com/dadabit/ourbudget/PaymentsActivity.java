package com.dadabit.ourbudget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;

import com.dadabit.ourbudget.fragments.payments.PaymentsInputFragment;
import com.dadabit.ourbudget.fragments.payments.PaymentsListFragment;
import com.dadabit.ourbudget.interfaces.PaymentsFragmentsCallback;

public class PaymentsActivity extends AppCompatActivity implements PaymentsFragmentsCallback {

    public static final int PAYMENTS_REQUEST_CODE = 3421;

    private ViewPager mViewPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.payments_tabs);
        mViewPager = (ViewPager) findViewById(R.id.payments_viewpager);
        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(viewPagerChangeListener);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(mViewPager);

        ImageButton btnBack = (ImageButton) findViewById(R.id.payments_btn_back);
        assert btnBack != null;
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private ViewPager.OnPageChangeListener viewPagerChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position==1)
                hideKeyboard();
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(PaymentsActivity.this, MainActivity.class);
        setResult(Activity.RESULT_OK, intent);
        finish();
        overridePendingTransition(R.anim.slide_from_right_500, R.anim.slide_out_left_200);
    }

    @Override
    public void updateViewPager() {
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) PaymentsActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View v = PaymentsActivity.this.getCurrentFocus();
        if (v == null)
            return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    /**
     * ____________________________________________________________________________
     * ViewPager Fragment Adapter
     * ____________________________________________________________________________
     */

    class TabsAdapter extends FragmentStatePagerAdapter {

        TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case 0: return PaymentsInputFragment.newInstance();
                case 1: return PaymentsListFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return getResources().getString(R.string.payments_tab1);
                case 1: return getResources().getString(R.string.payments_tab2);
            }
            return "";
        }


    }
}
