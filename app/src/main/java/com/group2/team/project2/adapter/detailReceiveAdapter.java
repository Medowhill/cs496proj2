package com.group2.team.project2.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.group2.team.project2.R;
import com.group2.team.project2.object.ReceiveDebt;

/**
 * Created by q on 2017-01-03.
 */

public class detailReceiveAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final ReceiveDebt debt;


    public detailReceiveAdapter(Activity activity, ReceiveDebt debt) {
        assert activity != null;

        this.activity = activity;
        this.debt = debt;
    }

    @Override
    public int getCount() {
        return debt.getEmails().size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.activity_receive_debt, null);

        TextView receive_name = (TextView) convertView.findViewById(R.id.receive_textView_name);
        TextView receive_amount = (TextView) convertView.findViewById(R.id.receive_textView_amount);
        final CheckBox receive_check = (CheckBox) convertView.findViewById(R.id.payed_checkbox);

        String name = debt.getNames().get(position);
        String amount = debt.getAmount();
        boolean check = debt.getPayed()[position];

        receive_name.setText(name);
        receive_amount.setText(amount);
        receive_check.setTag(position);
        receive_check.setChecked(check);
        receive_check.setEnabled(!check);

        receive_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                final boolean isChecked = receive_check.isChecked();
                if (isChecked){
                    debt.setPayed((int) receive_check.getTag(), true);
                    receive_check.setChecked(true);
                    receive_check.setEnabled(false);
                }
            }
        });

        return convertView;
    }
}
