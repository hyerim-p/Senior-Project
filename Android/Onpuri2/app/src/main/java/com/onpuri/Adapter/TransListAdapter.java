package com.onpuri.Adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.onpuri.R;

import java.util.ArrayList;

public class TransListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private final String TAG = "TransListAdapter";
    private ArrayList<String> senList;

    public TransListAdapter(ArrayList<String> list_trans, RecyclerView recyclerView) {
        this.senList=list_trans;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mSenItem;
        public ItemViewHolder(View v) {
            super(v);
            mSenItem = (TextView) v.findViewById(R.id.tv_trans_item);
        }
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.trans_list, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "trans list " + position + " set.");
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        itemViewHolder.mSenItem.setText(senList.get(position));

    }

    @Override
    public int getItemCount() {
        return senList.size();
    }

}