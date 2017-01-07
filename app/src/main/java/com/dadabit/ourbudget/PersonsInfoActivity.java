package com.dadabit.ourbudget;

import android.content.SharedPreferences;
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
import android.widget.ImageButton;

import com.dadabit.ourbudget.fragments.info.InfoMeFragment;
import com.dadabit.ourbudget.fragments.info.InfoPerson2Fragment;

public class PersonsInfoActivity extends AppCompatActivity {

    public static final String ARGUMENT_USERNAME = "username";

    private String userName = "Me";
    private String person2Name = "Person 2";
    private int tabsQuantity = 2;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (getIntent().hasExtra(ARGUMENT_USERNAME))
            userName = getIntent().getStringExtra(ARGUMENT_USERNAME);

        SharedPreferences sharedPreferences =
                getSharedPreferences(
                        MainActivity.APP_PREFERENCES,
                        MODE_PRIVATE);
        if (sharedPreferences.contains(MainActivity.APP_PREFERENCES_PERSON2_NAME))
            person2Name = sharedPreferences.getString(
                    MainActivity.APP_PREFERENCES_PERSON2_NAME, "");
        else
            tabsQuantity=1;


        TabLayout tabLayout = (TabLayout) findViewById(R.id.info_tabs);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.info_viewpager);
        mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(mViewPager);

        ImageButton btnBack = (ImageButton) findViewById(R.id.info_btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_right_500, R.anim.slide_out_left_200);
    }


    /**
     * ____________________________________________________________________________
     * ViewPager Fragment Adapter
     * ____________________________________________________________________________
     */

    class TabsAdapter extends FragmentStatePagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return tabsQuantity;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case 0: return InfoMeFragment.newInstance();
                case 1: return InfoPerson2Fragment.newInstance();
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
                case 0: return userName;
                case 1: return person2Name;
            }
            return "";
        }


    }
}
