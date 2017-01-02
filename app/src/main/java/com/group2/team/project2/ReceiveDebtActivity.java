package com.group2.team.project2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class ReceiveDebtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_debt);

        String account = getIntent().getStringExtra("account");
        String time = getIntent().getStringExtra("time");
        String amount = getIntent().getStringExtra("amount");
        ArrayList<String> names = getIntent().getStringArrayListExtra("names");
        ArrayList<String> emails = getIntent().getStringArrayListExtra("emails");


    }
}
