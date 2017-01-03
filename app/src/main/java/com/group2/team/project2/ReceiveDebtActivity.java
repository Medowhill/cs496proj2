package com.group2.team.project2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import com.group2.team.project2.adapter.detailReceiveAdapter;
import com.group2.team.project2.adapter.receiveViewAdapter;
import com.group2.team.project2.object.ReceiveDebt;

import java.util.ArrayList;

public class ReceiveDebtActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_receive);
        ListView list = (ListView) findViewById(R.id.receiveDetailView);

        String name = getIntent().getStringExtra("name");
        String account = getIntent().getStringExtra("account");
        String time = getIntent().getStringExtra("time");
        String amount = getIntent().getStringExtra("amount");
        ArrayList<String> names = getIntent().getStringArrayListExtra("names");
        ArrayList<String> emails = getIntent().getStringArrayListExtra("emails");
        boolean[] payed = getIntent().getBooleanArrayExtra("payed");
        final int position = getIntent().getIntExtra("position", -1);

        final detailReceiveAdapter adapter = new detailReceiveAdapter(this, amount, names, payed);
        list.setAdapter(adapter);

        Button receive_ok_button = (Button) findViewById(R.id.receive_ok_button);
        receive_ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //receiveAdapter.update();
                Bundle bundle = new Bundle();
                bundle.putBooleanArray("payed", adapter.getPayed());
                bundle.putInt("position", position);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        Button receive_cancel_button = (Button) findViewById(R.id.receive_cancel_button);
        receive_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
    }
}
