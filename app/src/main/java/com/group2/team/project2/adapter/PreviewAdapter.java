package com.group2.team.project2.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group2.team.project2.R;
import com.group2.team.project2.fragment.BTabFragment;
import com.group2.team.project2.object.PhotoPreview;

import java.util.ArrayList;

/**
 * Created by q on 2016-12-31.
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {

    private ArrayList<PhotoPreview> previews;
    private BTabFragment fragment;

    public PreviewAdapter(BTabFragment fragment) {
        previews = new ArrayList<>();
        this.fragment = fragment;
    }

    public void add(PhotoPreview preview) {
        previews.add(preview);
        notifyDataSetChanged();
    }

    public PhotoPreview get(int position) {
        return previews.get(position);
    }

    public String remove(ViewHolder viewHolder) {
        String str = previews.remove(viewHolder.position).getTime();
        notifyDataSetChanged();
        return str;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.position = position;
        final PhotoPreview preview = previews.get(position);
        holder.imageView.setImageBitmap(preview.getBitmap());
        String t = preview.getTime();
        holder.textView.setText(t.substring(0, 4) + "-" + t.substring(4, 6) + "-" + t.substring(6, 8) + " " +
                t.substring(8, 10) + ":" + t.substring(10, 12) + ":" + t.substring(12, 14));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.clickItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return previews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private int position;
        private CardView cardView;
        private ImageView imageView;
        private TextView textView;

        private ViewHolder(View view) {
            super(view);
            cardView = (CardView) view.findViewById(R.id.b_preview_cardView);
            imageView = (ImageView) view.findViewById(R.id.b_preview_imageView);
            textView = (TextView) view.findViewById(R.id.b_preview_textView);
        }
    }

}
