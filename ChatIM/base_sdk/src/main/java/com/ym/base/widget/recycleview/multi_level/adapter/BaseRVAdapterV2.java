package com.ym.base.widget.recycleview.multi_level.adapter;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.ym.base.widget.recycleview.multi_level.vo.Tree;


public abstract class BaseRVAdapterV2<T extends RecyclerView.ViewHolder, M> extends RecyclerView.Adapter<T> {


    private OnRVDataChangeListener onRVDataChangeListener;

    List<M> mDatas = new ArrayList<>();


    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    public void addMoreDatas(List<M> info) {
        if (info != null && info.size() > 0) {
            mDatas.addAll(mDatas.size(), info);
            notifyItemRangeInserted(mDatas.size(), info.size());
        }
    }

    public List<M> getDatas() {
        return mDatas;
    }

    public <X extends Tree> void setDatas(List<X> info) {
        if (info != null) {
            mDatas = (List<M>) info;
        } else {
            mDatas.clear();
        }
        notifyDataSetChanged();
        if (onRVDataChangeListener != null)
            onRVDataChangeListener.isEmpty(mDatas == null || mDatas.size() == 0);
    }

    public void setOnRVDataChangeListener(OnRVDataChangeListener onRVDataChangeListener) {
        this.onRVDataChangeListener = onRVDataChangeListener;
    }

    public interface OnRVDataChangeListener {
        void isEmpty(boolean isEmpty);
    }
}