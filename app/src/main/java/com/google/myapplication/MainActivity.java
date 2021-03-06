package com.google.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.IDNA;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mWaterRef, mCheckInRef, myStudentRef, myDeviceRef;
    private FirebaseDatabase mFirebase;
    private Button btnUpdateData, btnInfo;
    private CardAutoCompleteTextView inputCardID;
    private MaterialCalendarView cldView;
    private RecyclerView rcvCheckInOut, rcvWater;
    private CardAdapter mCheckInOutAdapter;
    private WaterAdapter mWaterAdapter;
    private Query query, studentQuery, dateDataQuery;
    private Query queryCheckIn, dateDataQueryCheckIn;
    private ArrayList<String> arrAllCardTime = new ArrayList<>();
    private ArrayList<String> arrChooseCardTime = new ArrayList<>();
    private ArrayList<WaterCard> arrChooseWaterTime = new ArrayList<>();
    private TextView tvNotFound, tvNotInputCardID, tvCardNotExist, tvNumberOfCup, tvNoWaterData;
    private SharedPreferences mSharedPreferences;
    private ArrayList<String> arrSuggestCardID = new ArrayList<>();
    private ArrayAdapter suggestAdapter;
    private HashSet<CalendarDay> mCldDays = new HashSet<>();
    private RadioGroup optionRdg;
    private TextView tvName;
    private LinearLayout lnlNumberOfCup;

    private TelephonyManager telephonyManager;
    private int numberOfUsage = 0;
    private int currentYear, currentMonth, currentDay = 0;
    private int mCurrentOptionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //load sharedPreference to get ID card nearest
        if(true) {
            mSharedPreferences = getSharedPreferences("RFIDPreference", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            Gson gson = new Gson();
            String json = mSharedPreferences.getString("suggest", "");
            if (!json.isEmpty()) {
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                arrSuggestCardID = gson.fromJson(json, type);
            }
            initViewer();
            if (arrSuggestCardID != null && arrSuggestCardID.size() > 0) {
                inputCardID.setText(arrSuggestCardID.get(arrSuggestCardID.size() - 1));
            }
            suggestAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, arrSuggestCardID);
            inputCardID.setThreshold(0);
            inputCardID.setAdapter(suggestAdapter);
            inputCardID.setImeOptions(EditorInfo.IME_ACTION_DONE);
            ((RadioButton) optionRdg.getChildAt(0)).setChecked(true);
            cldView.setSelectedDate(CalendarDay.today());
        }

        //check device using app how many time and update to firebase
        if(true) {
            String imei = getImei();
            myDeviceRef = mFirebase.getReference("devices");
            if (imei != null && !imei.equals("")) {
                myDeviceRef.child(imei).setValue(numberOfUsage + 1);
            }
            saveNumberOfCount();
            showCurrentNumberOfUsage(imei);
        }

        //auto load data for nearest card
        if(!inputCardID.getText().toString().equals("")) {
            getCheckInData();
            getCheckInOfCurrentDate(currentDay);
            getStudentName();
        }

        //set event for view
        setEvent();
    }

    private void setEvent(){
        // button update event
        btnUpdateData.setOnClickListener(view -> {
            cldView.setSelectedDate(CalendarDay.today());
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            cldView.removeDecorators();
            getStudentName();
            if (mCurrentOptionIndex == 0) {
                getCheckInData();
                getCheckInOfCurrentDate(CalendarDay.today().getDay());
            } else if (mCurrentOptionIndex == 1) {
                getWaterData();
                getWaterOfCurrentDate(CalendarDay.today().getDay());
            }
        });

        //button infor event
        btnInfo.setOnClickListener(view -> {
            Intent mIntent = new Intent(MainActivity.this, InfoActivity.class);
            mIntent.putExtra("cardID", inputCardID.getText().toString());
            startActivity(mIntent);
        });

        // event for calendarView
        cldView.setOnMonthChangedListener((widget, date) -> {
            if (date.getYear() != currentYear || date.getMonth() != currentMonth) {
                currentYear = date.getYear();
                currentMonth = date.getMonth();
                if (getCurrentSelectedRadioButtonIndex() == 1) {
                    getWaterData();
                }
            }
        });
        cldView.setOnDateChangedListener((widget, date, selected) -> {
            currentYear = date.getYear();
            currentMonth = date.getMonth();
            currentDay = date.getDay();

            if (mCurrentOptionIndex == 1) {
                getWaterOfCurrentDate(date.getDay());
            } else if (mCurrentOptionIndex == 0) {
                getCheckInOfCurrentDate(date.getDay());
            }
        });

        //event for radio button
        optionRdg.setOnCheckedChangeListener((group, checkedId) -> {
            if(inputCardID.getText().toString().length() < 2) // check tren inputCard ma chua co gi thi thoat luon
                return;
            mCurrentOptionIndex = getCurrentSelectedRadioButtonIndex();

            if (mCurrentOptionIndex == 0) { // check in, check out for student
                getCheckInData();
                getCheckInOfCurrentDate(currentDay);
            } else if (mCurrentOptionIndex == 1) { // check water for student
                getWaterData();
                getWaterOfCurrentDate(currentDay);
            }
            updateList();
        });
    }

    private void initViewer(){
        // Write a message to the database
        mFirebase = FirebaseDatabase.getInstance();
        rcvCheckInOut = findViewById(R.id.rcv_check_in_out);
        rcvWater = findViewById(R.id.rcv_water);
        mCheckInOutAdapter = new CardAdapter(arrChooseCardTime);
        mWaterAdapter = new WaterAdapter(arrChooseWaterTime);
        tvNotFound = findViewById(R.id.tv_not_found);
        tvNotInputCardID = findViewById(R.id.tv_not_input_card);
        tvCardNotExist = findViewById(R.id.tv_card_not_exist);
        tvNoWaterData = findViewById(R.id.tv_water_not_data);
        optionRdg = findViewById(R.id.option_rg);
        tvNumberOfCup = findViewById(R.id.tv_number_of_cups);

        numberOfUsage = mSharedPreferences.getInt("number_count", 0);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);

        rcvCheckInOut.setLayoutManager(mLayoutManager);
        rcvCheckInOut.setAdapter(mCheckInOutAdapter);

        LinearLayoutManager mWaterLayoutManager = new LinearLayoutManager(this);
        mWaterLayoutManager.setOrientation(RecyclerView.VERTICAL);

        rcvWater.setLayoutManager(mWaterLayoutManager);
        rcvWater.setAdapter(mWaterAdapter);

        tvNotFound.setVisibility(GONE);
        rcvCheckInOut.setVisibility(GONE);
        rcvWater.setVisibility(GONE);
        tvNotInputCardID.setVisibility(GONE);
        tvCardNotExist.setVisibility(GONE);

        //myRef.setValue();
        btnUpdateData = findViewById(R.id.btn_get_data);
        btnInfo = findViewById(R.id.btn_get_info);
        inputCardID = findViewById(R.id.input_card);
        cldView = findViewById(R.id.card_calendar);
        cldView.setSelectedDate(CalendarDay.today());

        currentYear = CalendarDay.today().getYear();
        currentMonth = CalendarDay.today().getMonth();
        currentDay = CalendarDay.today().getDay();

        lnlNumberOfCup = findViewById(R.id.lnl_number_of_cup);

        tvName = findViewById(R.id.tv_student_name);
        tvName.setText("");


    }

    private void saveNumberOfCount() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("number_count", numberOfUsage + 1);
        editor.commit();
    }

    private String getImei() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (telephonyManager != null) {
                    try {
                        return telephonyManager.getImei();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
                    }
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1010);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                if (telephonyManager != null) {
                    return telephonyManager.getDeviceId();
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1010);
            }
        }
        return "";
    }

    private void showCurrentNumberOfUsage(String mCurrentImei) {
        if (!mCurrentImei.equals("")) {
            myDeviceRef.child(mCurrentImei).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                   numberOfUsage = snapshot.getValue(Integer.class);
                   Toast.makeText(MainActivity.this, "You use this app in this device in " + numberOfUsage + " times", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getStudentName() {
        if (!inputCardID.getText().toString().equals("")) {
            myStudentRef = mFirebase.getReference("students");
            studentQuery = myStudentRef.child(inputCardID.getText().toString());
            studentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    if (dataSnapshot.getChildrenCount() == 0) {
                        tvName.setText("Card doesn't match to any student");
                    } else {
                        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                        while(iterator.hasNext()) {
                            DataSnapshot next = iterator.next();
                            if (next.getKey().equals("name")) {
                                tvName.setText("Học sinh: " + next.getValue().toString());
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            tvName.setText("");
        }
    }

    private void getCheckInData() {
        if (!inputCardID.getText().toString().equals("")) {
            mCheckInRef = mFirebase.getReference("CHECK IN").child("RFID");
            queryCheckIn = mCheckInRef.child(inputCardID.getText().toString()).child(cldView.getCurrentDate().getYear() + "")
                    .child(cldView.getCurrentDate().getMonth() < 10? "0" + cldView.getCurrentDate().getMonth() : cldView.getCurrentDate().getMonth() + "");
            queryCheckIn.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    if (dataSnapshot.getChildrenCount() == 0) {
                        tvNotFound.setVisibility(GONE);
                        rcvCheckInOut.setVisibility(GONE);
                        rcvWater.setVisibility(GONE);
                        tvNotInputCardID.setVisibility(GONE);
                        tvCardNotExist.setVisibility(View.VISIBLE);
                        lnlNumberOfCup.setVisibility(GONE);
                        tvNoWaterData.setVisibility(View.GONE);
                    } else {
                        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                        if (!arrSuggestCardID.contains(inputCardID.getText().toString())) {
                            arrSuggestCardID.add(inputCardID.getText().toString());
                            suggestAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrSuggestCardID);
                            inputCardID.setAdapter(suggestAdapter);
                            Gson gson = new Gson();
                            String json = gson.toJson(arrSuggestCardID);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("suggest", json);
                            editor.commit();
                        }
                        mCldDays.clear();
                        arrAllCardTime.clear();
                        while (iterator.hasNext()) {
                            tvNoWaterData.setVisibility(View.GONE);
                            DataSnapshot next = iterator.next();
                            int cardDate = Integer.parseInt(next.getKey());
                            mCldDays.add(CalendarDay.from(cldView.getCurrentDate().getYear(), cldView.getCurrentDate().getMonth(), cardDate));
                        }
                        cldView.addDecorator(new EventDecorator(MainActivity.this, mCldDays));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void getWaterData() {
        if (!inputCardID.getText().toString().equals("")) {
            mWaterRef = mFirebase.getReference("QUAY ROT").child("RFID");
            query = mWaterRef.child(inputCardID.getText().toString()).child(cldView.getCurrentDate().getYear() + "")
                    .child(cldView.getCurrentDate().getMonth() < 10? "0" + cldView.getCurrentDate().getMonth() : cldView.getCurrentDate().getMonth() + "");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    if (dataSnapshot.getChildrenCount() == 0) {
                        tvNotFound.setVisibility(GONE);
                        rcvCheckInOut.setVisibility(GONE);
                        rcvWater.setVisibility(GONE);
                        tvNotInputCardID.setVisibility(GONE);
                        tvCardNotExist.setVisibility(View.GONE);
                        lnlNumberOfCup.setVisibility(GONE);
                        tvNoWaterData.setVisibility(View.VISIBLE);
                    } else {
                        Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                        if (!arrSuggestCardID.contains(inputCardID.getText().toString())) {
                            arrSuggestCardID.add(inputCardID.getText().toString());
                            suggestAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, arrSuggestCardID);
                            inputCardID.setAdapter(suggestAdapter);
                            Gson gson = new Gson();
                            String json = gson.toJson(arrSuggestCardID);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putString("suggest", json);
                            editor.commit();
                        }
                        mCldDays.clear();

                        while (iterator.hasNext()) {
                            tvNoWaterData.setVisibility(View.GONE);
                            DataSnapshot next = iterator.next();
                            int cardDate = Integer.parseInt(next.getKey());
                            mCldDays.add(CalendarDay.from(cldView.getCurrentDate().getYear(), cldView.getCurrentDate().getMonth(), cardDate));
                        }
                        cldView.addDecorator(new EventDecorator(MainActivity.this, mCldDays));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            tvNotFound.setVisibility(GONE);
            rcvCheckInOut.setVisibility(GONE);
            tvNotInputCardID.setVisibility(View.VISIBLE);
            tvCardNotExist.setVisibility(GONE);
            rcvWater.setVisibility(GONE);
            lnlNumberOfCup.setVisibility(GONE);
            tvNoWaterData.setVisibility(GONE);
        }
    }

    void getCheckInOfCurrentDate(int currentDate) {
        String currentSelectedCheckInDate = currentDate < 10? "0" + currentDate : currentDate + "";
        dateDataQueryCheckIn = mCheckInRef.child(inputCardID.getText().toString()).child(cldView.getCurrentDate().getYear() + "")
                .child(cldView.getCurrentDate().getMonth() < 10? "0" + cldView.getCurrentDate().getMonth() : cldView.getCurrentDate().getMonth() + "")
                .child(currentSelectedCheckInDate);
        arrChooseCardTime.clear();
        dateDataQueryCheckIn.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                while(iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    arrChooseCardTime.add(next.getKey().toString());
                }
                Collections.reverse(arrChooseCardTime);
                mCheckInOutAdapter.notifyDataSetChanged();
                updateList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getWaterOfCurrentDate(int currentDate) {
        String currentSelectedWaterDate = currentDate < 10? "0" + currentDate : currentDate + "";
        dateDataQuery = mWaterRef.child(inputCardID.getText().toString()).child(cldView.getCurrentDate().getYear() + "")
                .child(cldView.getCurrentDate().getMonth() < 10? "0" + cldView.getCurrentDate().getMonth() : cldView.getCurrentDate().getMonth() + "")
                .child(currentSelectedWaterDate);
        arrChooseWaterTime.clear();
        dateDataQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                while(iterator.hasNext()) {
                    DataSnapshot next = iterator.next();
                    WaterFirebase mWaterFirebase = next.getValue(WaterFirebase.class);
                    arrChooseWaterTime.add(new WaterCard(next.getKey(), mWaterFirebase.getT_Nuoc()));
                }
                Collections.reverse(arrChooseWaterTime);

                mWaterAdapter.notifyDataSetChanged();
                updateList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void updateList() {
        switch (mCurrentOptionIndex) {
            case 0:
                if (arrChooseCardTime.size() > 0) {
                    tvNotFound.setVisibility(GONE);
                    rcvCheckInOut.setVisibility(View.VISIBLE);
                    rcvWater.setVisibility(GONE);
                    lnlNumberOfCup.setVisibility(GONE);
                    tvNotInputCardID.setVisibility(GONE);
                    tvCardNotExist.setVisibility(GONE);
                    tvNoWaterData.setVisibility(GONE);
                } else {
                    tvNotFound.setVisibility(View.VISIBLE);
                    rcvCheckInOut.setVisibility(GONE);
                    rcvWater.setVisibility(GONE);
                    lnlNumberOfCup.setVisibility(GONE);
                    tvNotInputCardID.setVisibility(GONE);
                    tvCardNotExist.setVisibility(GONE);
                    tvNoWaterData.setVisibility(GONE);
                }
                break;
            case 1:
                if (arrChooseWaterTime.size() > 0) {
                    tvNotFound.setVisibility(GONE);
                    tvNumberOfCup.setText(arrChooseWaterTime.size() + "");
                    rcvCheckInOut.setVisibility(GONE);
                    rcvWater.setVisibility(View.VISIBLE);
                    lnlNumberOfCup.setVisibility(View.VISIBLE);
                    tvNotInputCardID.setVisibility(GONE);
                    tvCardNotExist.setVisibility(GONE);
                    tvNoWaterData.setVisibility(GONE);
                } else {
                    tvNotFound.setVisibility(View.VISIBLE);
                    rcvCheckInOut.setVisibility(GONE);
                    rcvWater.setVisibility(GONE);
                    lnlNumberOfCup.setVisibility(GONE);
                    tvNotInputCardID.setVisibility(GONE);
                    tvCardNotExist.setVisibility(GONE);
                    tvNoWaterData.setVisibility(GONE);
                }
                break;
            default:
                tvNotFound.setVisibility(GONE);
                rcvWater.setVisibility(GONE);
                rcvCheckInOut.setVisibility(GONE);
                lnlNumberOfCup.setVisibility(GONE);
                tvNotInputCardID.setVisibility(GONE);
                tvCardNotExist.setVisibility(GONE);
                tvNoWaterData.setVisibility(GONE);
                break;
        }
    }

    private int getCurrentSelectedRadioButtonIndex() {
        int radioButtonID = optionRdg.getCheckedRadioButtonId();
        View radioButton = optionRdg.findViewById(radioButtonID);
        return optionRdg.indexOfChild(radioButton);
    }
}