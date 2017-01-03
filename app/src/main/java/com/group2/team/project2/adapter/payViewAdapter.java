package com.group2.team.project2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.group2.team.project2.R;
import com.group2.team.project2.object.PayDebt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class payViewAdapter extends BaseAdapter implements ListAdapter {

    private final ArrayList<PayDebt> debts = new ArrayList<>();

    public payViewAdapter(JSONArray jsonArray, String email) {
        assert jsonArray != null;

        int payedStart = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject pay_json = jsonArray.getJSONObject(i);
                int index = -1;
                JSONArray array = pay_json.getJSONArray("people");
                for (int j = 0; j < array.length(); j++)
                    if (array.getJSONObject(j).getString("email").equals(email))
                        index = j;
                PayDebt debt = new PayDebt(pay_json.getString("email"), pay_json.getString("name"), pay_json.getString("account"), pay_json.getString("amount"), pay_json.getString("time"), array.getJSONObject(index).getBoolean("payed"));
                if (debt.isNew()) {
                    debts.add(payedStart, debt);
                } else {
                    debts.add(0, debt);
                    payedStart++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(int position, PayDebt debt) {
        debts.add(position, debt);
        notifyDataSetChanged();
    }

    public void update(PayDebt debt) {
        int j;
        PayDebt debtUpdated = null;
        for (int i = 0; i < debts.size(); i++) {
            PayDebt debt1 = debts.get(i);
            if (debt1.getEmail().equals(debt.getEmail()) && debt1.getAccount().equals(debt.getAccount())
                    && debt1.getAmount().equals(debt.getAmount())) {
                debt1.setNew(true);
                debtUpdated = debts.remove(i);
                break;
            }
        }
        for (j = 0; j < debts.size(); j++)
            if (debts.get(j).isNew())
                break;
        debts.add(j, debtUpdated);
        Log.i("ds496test", debtUpdated + "");

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return debts.size();
    }

    @Override
    public PayDebt getItem(int position) {
        return debts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.pay_item, null);
        }

        TextView pay_name = (TextView) convertView.findViewById(R.id.pay_name);
        TextView pay_amount = (TextView) convertView.findViewById(R.id.pay_amount);
        TextView pay_account = (TextView) convertView.findViewById(R.id.pay_account);
        TextView pay_time = (TextView) convertView.findViewById(R.id.pay_time);
        ImageView pay_background = (ImageView) convertView.findViewById(R.id.pay_background);

        PayDebt debt = debts.get(position);
        pay_name.setText(debt.getName());
        pay_amount.setText(debt.getAmount() + "원");
        pay_account.setText(debt.getAccount());
        pay_time.setText(debt.getTime());
        if (debt.isNew())
            pay_background.setVisibility(View.VISIBLE);
        else
            pay_background.setVisibility(View.INVISIBLE);

        return convertView;
    }
}
