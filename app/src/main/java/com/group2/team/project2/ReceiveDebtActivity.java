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

    ReceiveDebt debt;

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
        boolean allPayed = getIntent().getBooleanExtra("allpayed", false);

        debt = new ReceiveDebt(name, account, amount, time, emails, names, payed, allPayed);

        detailReceiveAdapter adapter = new detailReceiveAdapter(this, debt);
        list.setAdapter(adapter);

        Button receive_ok_button = (Button) findViewById(R.id.receive_ok_button);
        receive_ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean allpayed = true;
                for (int i=0; i < debt.getPayed().length; i++){
                    if (!debt.getPayed()[0])
                        allpayed = false;
                }
                if (allpayed)
                    debt.setAllpayed();
                Log.d("Activity TEST", debt.getName());
                Log.d("Activity TEST", debt.getAccount());
                Log.d("Activity TEST", debt.getTime());
                Log.d("Activity TEST", debt.getAmount());
                Log.d("Activity TEST", debt.getNames().toString());
                Log.d("Activity TEST", debt.getEmails().toString());
                Log.d("Activity TEST", String.valueOf(debt.getPayed()[0]));
                Log.d("Activity TEST", String.valueOf(debt.getAllPayed()));
                //receiveAdapter.update();
                Bundle bundle = new Bundle();
                bundle.putString("name", debt.getName());
                bundle.putString("account", debt.getAccount());
                bundle.putString("amount", debt.getAmount());
                bundle.putString("time", debt.getTime());
                bundle.putStringArrayList("emails", debt.getEmails());
                bundle.putStringArrayList("names", debt.getNames());
                bundle.putBooleanArray("getpayed", debt.getPayed());
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

}
