package com.dadabit.ourbudget;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dadabit.ourbudget.customClasses.Categories;
import com.dadabit.ourbudget.customClasses.DB;
import com.dadabit.ourbudget.customClasses.PostTransaction;
import com.dadabit.ourbudget.customClasses.Transaction;
import com.dadabit.ourbudget.customClasses.UserMetadata;
import com.dadabit.ourbudget.dialogs.CurrencyChooseDialog;
import com.dadabit.ourbudget.eventbus.ShowCalculator;
import com.dadabit.ourbudget.fragments.main.BudgetsFragment;
import com.dadabit.ourbudget.fragments.main.InputFragment;
import com.dadabit.ourbudget.fragments.main.StatisticFragment;
import com.dadabit.ourbudget.fragments.main.TransactionsFragment;
import com.dadabit.ourbudget.interfaces.FragmentCallback;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements
        FragmentCallback,
        GoogleApiClient.OnConnectionFailedListener,
        NavigationView.OnNavigationItemSelectedListener{

    public static final int CALLBACK_INPUT = 1;
    public static final int CALLBACK_UPDATE_TRANSACTION = 2;
    public static final int CALLBACK_RESTART = 3;
    public static final int CALLBACK_INSERT_TO_FIREBASE = 4;
    public static final int CALLBACK_DELETE_FROM_FIREBASE = 5;

    public static final int DEFAULT_GROUP_POSITION = -1;

    public static final String APP_PREFERENCES = "shared_preferences";
    public static final String APP_PREFERENCES_PERSON2_UID = "person2_uid";
    public static final String APP_PREFERENCES_PERSON2_NAME = "person2_name";
    public static final String APP_PREFERENCES_CURRENCY = "currency";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private Transaction transaction;
    private int groupPosition = DEFAULT_GROUP_POSITION;

    private DatabaseReference mDatabaseReference;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private String useremail;
    private String userID;
    private String userName;
    private String person2ID;
    private String person2Name;

    public static Categories categories;
    public static String currency;

    private SharedPreferences sharedPreferences;

    private EventBus eventBus;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setup FireBase database
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .addApi(AppIndex.API).build();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        //Start authentication activity if need
        if (mFirebaseUser == null) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        //setup tabs ViewPager
        tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        //get categories with images from db & start viewpager
        new LoadCategoriesAsync().execute();


        //setup side navigation menu
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        //hide native keyboard if open navigation view
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                hideKeyboard(MainActivity.this);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        //set person name & pic

        View navHeader = navigationView.getHeaderView(0);
        ImageView ivUserImg = (ImageView) navHeader.findViewById(R.id.nav_header_iv);
        TextView tvUserName = (TextView) navHeader.findViewById(R.id.nav_header_tv);


        //get userID
        userID = mFirebaseUser.getUid();
        userName = mFirebaseUser.getDisplayName();
        useremail = mFirebaseUser.getEmail();
        tvUserName.setText(userName);
        if(mFirebaseUser.getPhotoUrl()!=null)
            Glide.with(MainActivity.this).load(mFirebaseUser.getPhotoUrl()).into(ivUserImg);

        //Get person 2 userID if exist
        sharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (sharedPreferences.contains(APP_PREFERENCES_PERSON2_UID)){
            person2ID = sharedPreferences.getString(APP_PREFERENCES_PERSON2_UID,"");
            person2Name = sharedPreferences.getString(APP_PREFERENCES_PERSON2_NAME, "");
            navigationView.getMenu().findItem(R.id.nav_add_user).setVisible(false);
        }


        //set application currency
        if (sharedPreferences.contains(APP_PREFERENCES_CURRENCY))
            currency = sharedPreferences.getString(APP_PREFERENCES_CURRENCY, "");
        else
            currency = getResources().getString(R.string.default_currency);


        eventBus = EventBus.getDefault();

        ImageButton btnMenu = (ImageButton) findViewById(R.id.main_icon_menu);
        assert btnMenu != null;
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        setFirebaseChangeListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, //  an action type.
                "Main Page", // a title for the content shown.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.dadabit.ourbudget/http/host/path")
        );
        AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // an action type.
                "Main Page", // a title for the content shown.
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.dadabit.ourbudget/http/host/path")
        );
        AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
        mGoogleApiClient.disconnect();
    }

    private void setFirebaseChangeListener(){

        if (person2ID != null) {

            mDatabaseReference.child(person2ID).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            new UpdatePerson2DatabaseAsync().execute(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

        } else {

            mDatabaseReference
                    .child("uids")
                    .child(getPathFromEmail(useremail))
                    .addListenerForSingleValueEvent(
                            new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if ( !dataSnapshot.exists() ){
                                        //Write user id to FireBase
                                        mDatabaseReference
                                                .child("uids")
                                                .child(getPathFromEmail(useremail))
                                                .push()
                                                .setValue(
                                                        new UserMetadata(userID, userName, ""));
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
        }
    }


    private Boolean exit = false;
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (mViewPager.getCurrentItem() != 0){
            mViewPager.setCurrentItem(0);
            return;
        }
        if (exit) {
            super.onBackPressed(); // finish activity
        } else {
            Toast.makeText(this, R.string.toast_exit,
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }
    }

    // hide keyboard on tab change
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (position!=0){
                navigationView.getMenu().getItem(1).setChecked(false);
                hideKeyboard(MainActivity.this);
            }
//            if (position==1){
//
//            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            switch (requestCode){

                case PaymentsActivity.PAYMENTS_REQUEST_CODE:
                    new CheckRegularPayments().execute();
                    break;

            }
        }
    }


    @Override
    public void onCallback(int callback,
                           Transaction transaction,
                           PostTransaction postTransaction,
                           int groupNum,
                           long deletedTransactionID) {

        switch (callback){
            case CALLBACK_INPUT:
                if (transaction!=null){
                    //set Transaction to InputFragment and reset ViewPager
                    this.transaction = transaction;
                    this.groupPosition = groupNum;
                    mViewPager.getAdapter().notifyDataSetChanged();
                    mViewPager.setCurrentItem(0);
                }
                break;
            case CALLBACK_UPDATE_TRANSACTION:
                //update Transaction in FireBase & reset ViewPager
                this.transaction=null;
                if (postTransaction !=null)
                    new UpdateFireBaseAsync().execute(postTransaction);
                mViewPager.getAdapter().notifyDataSetChanged();
                mViewPager.setCurrentItem(1);
                break;
            case CALLBACK_RESTART:
                this.transaction=null;
                this.groupPosition=DEFAULT_GROUP_POSITION;
                mViewPager.getAdapter().notifyDataSetChanged();
                navigationView.getMenu().getItem(1).setChecked(false);
                break;
            case CALLBACK_INSERT_TO_FIREBASE:
                this.transaction=null;
                this.groupPosition=DEFAULT_GROUP_POSITION;
                navigationView.getMenu().getItem(1).setChecked(false);
                new InsertToFireBaseAsync().execute();
                break;
            case CALLBACK_DELETE_FROM_FIREBASE:
                if (deletedTransactionID!=0)
                    new DeleteInFireBaseAsync().execute(deletedTransactionID);
                break;
        }

    }


    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * ____________________________________________________________________________
     * Options menu
     * ____________________________________________________________________________
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_settings) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ____________________________________________________________________________
     * Side navigation bar
     * ____________________________________________________________________________
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==R.id.nav_persons){
            item.setChecked(false);
            Intent personsInfoIntent = new Intent(MainActivity.this, PersonsInfoActivity.class);
            if (userName!=null)
                personsInfoIntent.putExtra(PersonsInfoActivity.ARGUMENT_USERNAME,userName);
            startActivity(personsInfoIntent);
        }

        if (item.getItemId()==R.id.nav_sync){
            mDatabaseReference.child(userID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            new SyncDatabasesAsync().execute(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }

        if (item.getItemId()==R.id.nav_calculator){
            if (item.isChecked()){
                item.setChecked(false);
                eventBus.post(new ShowCalculator(false));
            }
            else  {
                mViewPager.setCurrentItem(0);
                item.setChecked(true);
                eventBus.post(new ShowCalculator(true));
            }
        }

        if (item.getItemId()==R.id.nav_payments){
            item.setChecked(false);
            Intent paymentsIntent = new Intent(MainActivity.this, PaymentsActivity.class);
            startActivityForResult(paymentsIntent, PaymentsActivity.PAYMENTS_REQUEST_CODE);
        }

        if (item.getItemId()==R.id.nav_add_user){

            showAddPerson2Dialog();

        }

        if (item.getItemId()==R.id.nav_currency){
            new CurrencyChooseDialog().show(getSupportFragmentManager(), "dialog_choose_currency");
        }

        if (item.getItemId()==R.id.nav_sign_out){
            mFirebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            startActivity(new Intent(this, AuthActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * ____________________________________________________________________________
     * Second user connect
     * ____________________________________________________________________________
     */


    private void showAddPerson2Dialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_add_user, null);
        final EditText etInput = (EditText)dialogView.findViewById(R.id.dialog_add_person_et);
        final ProgressBar mProgressBar = (ProgressBar) dialogView.findViewById(R.id.dialog_add_person_progressBar);
        final Button btnCancel = (Button) dialogView.findViewById(R.id.dialog_add_person_btn_cancel);
        final Button btnOk = (Button) dialogView.findViewById(R.id.dialog_add_person_btn_ok);


        builder.setCancelable(false);
        builder.setView(dialogView);

        final AlertDialog addPersonDialog = builder.create();
        addPersonDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAddUser2;

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPersonDialog.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = String.valueOf(etInput.getText()).toLowerCase().replaceAll("\\s","");

                if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mProgressBar.setVisibility(View.VISIBLE);
                    writeP2idToFireBase(email);

                    addPersonDialog.dismiss();

                    final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle(getString(R.string.dialog_add_user_progress_title)+ email);
                    progressDialog.setMessage(getString(R.string.dialog_add_user_progress_message));
                    progressDialog.setCancelable(false);
                    progressDialog.setIndeterminate(true);
                    progressDialog.getWindow().getAttributes().windowAnimations = R.style.DialogProgress;
                    progressDialog.setButton(
                            Dialog.BUTTON_NEGATIVE,
                            getString(R.string.dialog_add_user_progress_btn_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    progressDialog.dismiss();
                                }
                            });
                    progressDialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectUsers(email, 5, progressDialog);
                        }
                    }, 5000);

                    connectUsers(email, 5, progressDialog);

                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_email_not_valid, Toast.LENGTH_SHORT).show();
                }
            }
        });

        addPersonDialog.show();

    }

    private void writeP2idToFireBase(final String email) {

        mDatabaseReference.child("uids").child(getPathFromEmail(useremail)).addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot snapshot: dataSnapshot.getChildren()){

                            String key = snapshot.getKey();
                            String path = "/"+dataSnapshot.getKey()+"/"+key;

                            HashMap<String, Object> result = new HashMap<>();
                            result.put("user2id", email);
                            mDatabaseReference.child("uids").child(path).updateChildren(result);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

    }

    private void connectUsers(final String email, final int retryCount, final ProgressDialog progressDialog) {

        mDatabaseReference.child("uids").child(getPathFromEmail(email)).addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){

                            UserMetadata userMetadata;

                            for (DataSnapshot postSnapshot :
                                    dataSnapshot.getChildren()) {

                                userMetadata = postSnapshot.getValue(UserMetadata.class);

                                if (userMetadata.getUser2id().equalsIgnoreCase(useremail)){

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(
                                            APP_PREFERENCES_PERSON2_UID,
                                            userMetadata.getUid());
                                    editor.putString(
                                            APP_PREFERENCES_PERSON2_NAME,
                                            userMetadata.getUserName());
                                    editor.apply();

                                    person2ID = userMetadata.getUid();
                                    person2Name = userMetadata.getUserName();

                                    progressDialog.dismiss();

                                    setFirebaseChangeListener();

                                    Toast.makeText(
                                            MainActivity.this,
                                            String.format("%s %s",
                                                    email,
                                                    getString(R.string.toast_new_user_added)),
                                            Toast.LENGTH_SHORT)
                                            .show();
                                } else {

                                    progressDialog.dismiss();

                                    if (retryCount != 0){
                                        connectUsers(email, retryCount - 1, progressDialog);
                                    } else {
                                        Toast.makeText(
                                                MainActivity.this,
                                                R.string.toast_not_correct_email,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            }


                        } else {
                            if (retryCount!=0){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        connectUsers(email, retryCount - 1, progressDialog);
                                    }
                                }, 5000);

                            } else {
                                progressDialog.dismiss();

                                askRetryConnect();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

    }

    private void askRetryConnect() {



        Snackbar.make(
                findViewById(R.id.main_coordinatorLayout),
                R.string.dialog_add_user_ask_retry,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.dialog_add_user_btn_retry,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                showAddPerson2Dialog();

                            }
                        })
                .setActionTextColor(Color.RED)
                .show();
    }


    private String getPathFromEmail(String eMail){

        String[] eMailParts = eMail.split("\\.");
        String result = "";
        //replace last dot with "+@+"
        for (int i = 0; i < eMailParts.length; i++) {
            if (i == eMailParts.length-1){
                result += "+@+"+eMailParts[i];
            } else {
                result += eMailParts[i];
            }
        }

        return result;
    }


    /**
     * ____________________________________________________________________________
     * Load Categories from DB AsyncTask
     * ____________________________________________________________________________
     */

    private class LoadCategoriesAsync extends AsyncTask<Void, Void, Categories> {

//        DB db = new DB(MainActivity.this);

        @Override
        protected Categories doInBackground(Void... params) {

            SparseArray<String> categoryNames=new SparseArray<>();
            SparseArray<Bitmap> categoryImages=new SparseArray<>();

            int[] ids = new DB(MainActivity.this).getCategoriesIds();

            if (ids != null){
                for (int i = 0; i < ids.length; i++) {

                    categoryImages.put(
                            ids[i],
                            getBitmapFromRes(ids[i]));

                    categoryNames.put(
                            ids[i],
                            getResources().getStringArray(R.array.category_names)[i]);
                }
            }

//            Cursor cursor = db.getCategories();
//            cursor.moveToFirst();
//            while (!cursor.isAfterLast()){
//                categoryImages.put(
//                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID)),
//                        byteToBitmap(cursor.getBlob(cursor.getColumnIndex(DB.COLUMN_CATEGORY_IMG)))
//                );
//                categoryNames.put(cursor.getInt(cursor.getColumnIndex(DB.COLUMN_ID)),
//                        cursor.getString(cursor.getColumnIndex(DB.COLUMN_CATEGORY_NAME)));
//                cursor.moveToNext();
//            }
//            cursor.close();
//            db.close();

            return new Categories(categoryNames, categoryImages);
        }

        Bitmap getBitmapFromRes(int id){
            switch (id){
                case 1:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_auto);
                case 2:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_transport);
                case 3:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_kids);
                case 4:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_clothing);
                case 5:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_footwear);
                case 6:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_purchases);
                case 7:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_utilites);
                case 8:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_cosmetics);
                case 9:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_entertainment);
                case 10:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_food);
                case 11:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_restaurant);
                case 12:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_chemicals);
                case 13:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_internet_buy);
                case 14:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_credit_card);
                case 15:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_jewelry);
                case 16:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_medicine);
                case 17:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_jobs);
                case 18:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_pets);
                case 19:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_gifts);
                case 20:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_salary);
                case 21:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_sport);
                case 22:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_studies);
                case 23:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_travels);
                case 24:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.categ_appliances);
                default:
                    return BitmapFactory.decodeResource(
                            getResources(),
                            R.drawable.ic_cloud_off_black_72dp);
            }
        }

//        Bitmap byteToBitmap(byte[] imgByte){
//            if (imgByte!=null)
//                return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
//            else return BitmapFactory.decodeResource(getResources(), R.drawable.ic_cloud_off_black_72dp);
//        }


        @Override
        protected void onPostExecute(Categories loadedCategories) {
            super.onPostExecute(loadedCategories);

            categories = loadedCategories;
            mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
            mViewPager.addOnPageChangeListener(pageChangeListener);
            tabLayout.setupWithViewPager(mViewPager);
            new CheckRegularPayments().execute();
        }
    }

    /**
     * ____________________________________________________________________________
     * Check Regular Payments AsyncTask
     * ____________________________________________________________________________
     */

    private class CheckRegularPayments extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {

            ArrayList<PostTransaction> newTransactions =
                    new DB(MainActivity.this)
                            .getNewTransactions();

            if (!newTransactions.isEmpty()){
                for (PostTransaction transaction :
                        newTransactions) {
                    mDatabaseReference.child(userID)
                            .push()
                            .setValue(transaction);
                }
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean needReboot) {
            super.onPostExecute(needReboot);

            if (needReboot){
                mViewPager.getAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * ____________________________________________________________________________
     * Insert to FireBase AsyncTask
     * ____________________________________________________________________________
     */

    private class InsertToFireBaseAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {

            mDatabaseReference.child(userID)
                    .push()
                    .setValue(new DB(MainActivity.this)
                            .getLastTransaction());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mViewPager.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * ____________________________________________________________________________
     * Change value in FireBase AsyncTask
     * ____________________________________________________________________________
     */

    private class UpdateFireBaseAsync extends AsyncTask<PostTransaction, Void, Void>{
        @Override
        protected Void doInBackground(final PostTransaction... params) {
            mDatabaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        PostTransaction postTransaction = snapshot.getValue(PostTransaction.class);

                        if (postTransaction.getId()==params[0].getId()){
                            String key = snapshot.getKey();
                            String path = "/"+dataSnapshot.getKey()+"/"+key;
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("amount", params[0].getAmount());
                            result.put("amountType", params[0].getAmountType());
                            result.put("categoryID", params[0].getCategoryID());
                            result.put("date", params[0].getDate());
                            result.put("description", params[0].getDescription());
                            result.put("status", params[0].getStatus());
                            mDatabaseReference.child(path).updateChildren(result);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

    /**
     * ____________________________________________________________________________
     * Delete value in FireBase AsyncTask
     * ____________________________________________________________________________
     */

    private class DeleteInFireBaseAsync extends AsyncTask<Long, Void, Void>{
        @Override
        protected Void doInBackground(final Long... params) {
            mDatabaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        PostTransaction postTransaction = snapshot.getValue(PostTransaction.class);

                        if (postTransaction.getId()==params[0]){
                            String key = snapshot.getKey();
                            String path = "/"+dataSnapshot.getKey()+"/"+key;
                            HashMap<String, Object> result = new HashMap<>();
                            result.put("status", PostTransaction.STATUS_NEED_DELETE);
                            mDatabaseReference.child(path).updateChildren(result);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            return null;
        }
    }

    /**
     * ____________________________________________________________________________
     * Update Person 2 database AsyncTask
     * ____________________________________________________________________________
     */


    private class UpdatePerson2DatabaseAsync extends AsyncTask<DataSnapshot, Void, PostTransaction>{

        DB db = new DB(MainActivity.this);
        ArrayList<Long> person2DbIds = new ArrayList<>();

        @Override
        protected PostTransaction doInBackground(DataSnapshot... params) {

            PostTransaction newTransaction = null;

            Cursor cursor = db.getAllPerson2Transactions();
            if (cursor.getCount()==0){//person2 database is empty
                for (DataSnapshot postSnapshot: params[0].getChildren()) {

                    PostTransaction postTransaction = postSnapshot.getValue(PostTransaction.class);

                    if (postTransaction.getId()!=0) {
                        db.addPerson2Transaction(
                                postTransaction.getId(),
                                postTransaction.getAmount(),
                                postTransaction.getAmountType(),
                                postTransaction.getDate(),
                                postTransaction.getCategoryID(),
                                postTransaction.getDescription(),
                                postTransaction.getStatus());
                    }

                }
            } else {//collect person2 transactionID`s
                cursor.moveToFirst();
                while (!cursor.isAfterLast()){
                    person2DbIds.add(cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)));
                    cursor.moveToNext();
                }//check`n`save
                for (DataSnapshot postSnapshot: params[0].getChildren()) {

                    PostTransaction postTransaction = postSnapshot.getValue(PostTransaction.class);

                    if (!person2DbIds.contains(postTransaction.getId())
                            && postTransaction.getId()!=0){

                        newTransaction = postTransaction;

                        db.addPerson2Transaction(
                                postTransaction.getId(),
                                postTransaction.getAmount(),
                                postTransaction.getAmountType(),
                                postTransaction.getDate(),
                                postTransaction.getCategoryID(),
                                postTransaction.getDescription(),
                                postTransaction.getStatus());

                    }

                    if (postTransaction.getStatus() == PostTransaction.STATUS_CHANGED){
                        //change in db
                        db.changePerson2Transaction(
                                postTransaction.getId(),
                                postTransaction.getAmount(),
                                postTransaction.getAmountType(),
                                postTransaction.getDate(),
                                postTransaction.getCategoryID(),
                                postTransaction.getDescription());
                        //change status in cloud
                        String key = postSnapshot.getKey();
                        String path = "/"+params[0].getKey()+"/"+key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", PostTransaction.STATUS_EXIST);
                        mDatabaseReference.child(path).updateChildren(result);
                    }

                    if (postTransaction.getStatus() == PostTransaction.STATUS_NEED_DELETE){
                        //change in db
                        db.deletePerson2Transaction(postTransaction.getId());
                        //change status in cloud
                        String key = postSnapshot.getKey();
                        String path = "/"+params[0].getKey()+"/"+key;
                        HashMap<String, Object> result = new HashMap<>();
                        result.put("status", PostTransaction.STATUS_DELETED);
                        mDatabaseReference.child(path).updateChildren(result);
                    }
                }
            }
            cursor.close();
            db.close();
            return newTransaction;
        }

        @Override
        protected void onPostExecute(PostTransaction newTransaction) {
            super.onPostExecute(newTransaction);

            if ( newTransaction != null ){

                String message = MessageFormat.format("{0} : -{1} {2}",
                        person2Name,
                        (double) newTransaction.getAmount() / 100,
                        currency);
                if (newTransaction.getAmountType() == InputFragment.TRANSACTION_INCOME){
                    message = MessageFormat.format("{0} : +{1} {2}",
                            person2Name,
                            (double) newTransaction.getAmount() / 100,
                            currency);
                }

                Snackbar.make(findViewById(R.id.main_coordinatorLayout), message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.main_snackbar_update,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mViewPager.getAdapter().notifyDataSetChanged();
                                    }
                        })
                        .setActionTextColor(Color.RED)
                        .show();

            }
        }
    }


    /**
     * ____________________________________________________________________________
     * Synchronize user database and FireBase AsyncTask
     * ____________________________________________________________________________
     */

    private class SyncDatabasesAsync extends AsyncTask<DataSnapshot, Void, Void>{

        DB db = new DB(MainActivity.this);
        HashMap<Long, PostTransaction> transactionMap = new HashMap<>();
        LongSparseArray<String> snapshotKeys = new LongSparseArray<>();

        @Override
        protected Void doInBackground(DataSnapshot... params) {


            for (DataSnapshot snapshot: params[0].getChildren()){

                PostTransaction postTransaction = snapshot.getValue(PostTransaction.class);

                transactionMap.put(postTransaction.getId(), postTransaction);
                snapshotKeys.put(postTransaction.getId(), snapshot.getKey());
            }

            Cursor cursor = db.getMyTransactions();

            if (cursor.getCount()>0){
                cursor.moveToFirst();
                while (cursor.moveToNext()){
                    if (!transactionMap.containsKey(
                            cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)))){

                        mDatabaseReference.child(userID).push().setValue(
                                new PostTransaction(
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_AMOUNT)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TYPE)),
                                        cursor.getLong(cursor.getColumnIndex(DB.COLUMN_DATE)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_CATEGORY_ID)),
                                        cursor.getString(cursor.getColumnIndex(DB.COLUMN_DESCRIPTION)),
                                        cursor.getInt(cursor.getColumnIndex(DB.COLUMN_IS_DELETED))));
                    } else {

                        if (transactionMap.get(
                                cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID))).getAmount()
                                != cursor.getInt(cursor.getColumnIndex(DB.COLUMN_AMOUNT))){

                            HashMap<String, Object> result = new HashMap<>();
                            result.put("amount", cursor.getInt(cursor.getColumnIndex(DB.COLUMN_AMOUNT)));
                            result.put("amountType", cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TYPE)));
                            result.put("date", cursor.getLong(cursor.getColumnIndex(DB.COLUMN_DATE)));
                            result.put("categoryID", cursor.getInt(cursor.getColumnIndex(DB.COLUMN_CATEGORY_ID)));
                            result.put("description", cursor.getString(cursor.getColumnIndex(DB.COLUMN_DESCRIPTION)));
                            result.put("status", PostTransaction.STATUS_CHANGED);

                            String path = String.format("/%s/%s",
                                    params[0].getKey(),
                                    snapshotKeys.get(cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID))));

                            mDatabaseReference.child(path).updateChildren(result);

                        }

                        transactionMap.remove(cursor.getLong(cursor.getColumnIndex(DB.COLUMN_ID)));
                    }
                    cursor.moveToNext();
                }
            }

            if (transactionMap.size()>0){
                for (Long id :
                        transactionMap.keySet()) {
                    db.addTransactionWithId(id,
                            transactionMap.get(id).getAmount(),
                            transactionMap.get(id).getAmountType(),
                            transactionMap.get(id).getDate(),
                            transactionMap.get(id).getCategoryID(),
                            transactionMap.get(id).getDescription(),
                            transactionMap.get(id).getStatus());
                }
            }

            cursor.close();
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mViewPager.getAdapter().notifyDataSetChanged();
            navigationView.getMenu().getItem(1).setChecked(false);

        }
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
            return 4;
        }

        @Override
        public Fragment getItem(int i) {
            switch(i) {
                case 0: return InputFragment.newInstance(transaction);
                case 1: return TransactionsFragment.newInstance(groupPosition);
                case 2: return BudgetsFragment.newInstance();
                case 3: return StatisticFragment.newInstance();
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
                case 0: return getResources().getString(R.string.main_tab1);
                case 1: return getResources().getString(R.string.main_tab2);
                case 2: return getResources().getString(R.string.main_tab3);
                case 3: return getResources().getString(R.string.main_tab4);
            }
            return "";
        }
    }

}
