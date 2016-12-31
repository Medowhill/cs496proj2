package com.group2.team.project2.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group2.team.project2.R;
import com.group2.team.project2.object.PhotoPreview;

import java.util.ArrayList;

/**
 * Created by q on 2016-12-31.
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {

    private ArrayList<PhotoPreview> previews;

    public PreviewAdapter() {
        previews = new ArrayList<>();
    }

    public void add(PhotoPreview preview) {
        previews.add(preview);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PhotoPreview preview = previews.get(position);
        holder.imageView.setImageBitmap(preview.getBitmap());
        holder.textView.setText(preview.getTime());
    }

    @Override
    public int getItemCount() {
        return previews.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView;

        ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.b_preview_imageView);
            textView = (TextView) view.findViewById(R.id.b_preview_textView);
        }
    }

}
