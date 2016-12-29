package com.group2.team.project2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group2.team.project2.R;

public class BTabFragment extends Fragment {

    public BTabFragment() {
    }

    public static BTabFragment newInstance() {
        return new BTabFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_b, container, false);
        return rootView;
    }
}
