package com.group2.team.project2.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.group2.team.project2.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by q on 2017-01-02.
 */

public class payViewAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public payViewAdapter(Activity activity, JSONArray jsonArray) {
        assert activity != null;
        assert jsonArray != null;

        this.activity = activity;
        this.jsonArray = jsonArray;
    }

    @Override
    public int getCount() {
        if (null == jsonArray)
            return 0;
        else
            return jsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        if (null == jsonArray) return null;
        else
            return jsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        JSONObject jsonObject = getItem(position);
        return jsonObject.optLong("id");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = activity.getLayoutInflater().inflate(R.layout.pay_item, null);

        TextView pay_name = (TextView) convertView.findViewById(R.id.pay_name);
        TextView pay_amount = (TextView) convertView.findViewById(R.id.pay_amount);
        TextView pay_account = (TextView) convertView.findViewById(R.id.pay_account);
        TextView pay_time = (TextView) convertView.findViewById(R.id.pay_time);
        ImageView pay_background = (ImageView) convertView.findViewById(R.id.pay_background);

        JSONObject pay_json = getItem(position);

        if (pay_json != null) {
            try {
                pay_name.setText(pay_json.getString("name"));
                pay_amount.setText(pay_json.getString("amount"));
                pay_account.setText(pay_json.getString("account"));
                pay_time.setText(pay_json.getString("time"));
                if (pay_json.getBoolean("payed"))
                    pay_background.setVisibility(View.VISIBLE);
                else
                    pay_background.setVisibility(View.INVISIBLE);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }
}
