package com.google.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class InfoActivity extends AppCompatActivity {
    private TextView tvName, tvBirthday, tvClass, tvSchool, tvPhoneNumber, tvEmail, tvLocation;
    private DatabaseReference mDatabaseReference;
    private String formatDate;
    private ImageView imgProfile;
    private String cardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Date dateObj = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        formatDate = df.format(dateObj);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("students");

        Intent infoIntent = getIntent();
        cardId = infoIntent.getStringExtra("cardID");

        tvName = findViewById(R.id.tv_name);
        tvBirthday = findViewById(R.id.tv_birthday);
        tvSchool = findViewById(R.id.tv_school);
        tvClass = findViewById(R.id.tv_class);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvEmail = findViewById(R.id.tv_email);
        tvLocation = findViewById(R.id.tv_location);
        imgProfile = findViewById(R.id.img_profile);

        resetData();
        getStudentData();

    }

    private void getStudentData() {
            mDatabaseReference.child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    resetData();
                    Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();
                    Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
                    while(iterator.hasNext()) {
                        DataSnapshot next = iterator.next();
                        String tempNext = next.getValue().toString();
                        setDataForView(next.getKey(), tempNext);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    private void resetData() {
        tvName.setText("No data");
        tvBirthday.setText("No data");
        tvSchool.setText("No data");
        tvClass.setText("No data");
        tvPhoneNumber.setText("No data");
        tvEmail.setText("No data");
        tvLocation.setText("No data");
        imgProfile.setImageResource(R.drawable.rfid);
    }

    private void setDataForView(String cardKey, String cardValue) {
        switch (cardKey) {
            case "name":
                tvName.setText(cardValue);
                break;
            case "birthday":
                tvBirthday.setText(cardValue);
                break;
            case "school":
                tvSchool.setText(cardValue);
                break;
            case "classRoom":
                tvClass.setText(cardValue);
                break;
            case "phoneNumber":
                tvPhoneNumber.setText(cardValue);
                break;
            case "email":
                tvEmail.setText(cardValue);
                break;
            case "location":
                tvLocation.setText(cardValue);
                break;
            case "avatar":
                Glide.with(InfoActivity.this)
                        .load(cardValue)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imgProfile);
                break;
        }
    }


}
