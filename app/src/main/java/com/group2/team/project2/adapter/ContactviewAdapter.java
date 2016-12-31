package com.group2.team.project2.adapter;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.group2.team.project2.R;
import com.group2.team.project2.fragment.ATabFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by q on 2016-12-31.
 */

public class ContactviewAdapter extends BaseAdapter implements ListAdapter {

    private final Activity activity;
    private final JSONArray jsonArray;

    public ContactviewAdapter(Activity activity, JSONArray jsonArray) {
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
            convertView = activity.getLayoutInflater().inflate(R.layout.contact_item, null);

        TextView topText = (TextView) convertView.findViewById(R.id.toptext);
        TextView bottomText = (TextView) convertView.findViewById(R.id.bottomtext);


        JSONObject json_data = getItem(position);

        if (null != json_data) {
            String tt = null;
            String bt = null;
            try {
                tt = json_data.getString("name");
                topText.setText(tt);
                if (json_data.has("mobile_number"))
                    bt = json_data.getString("mobile_number");
                bottomText.setText(bt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return convertView;
    }
}
