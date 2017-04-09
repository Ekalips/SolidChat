package com.andre.solid.solidchat.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andre.solid.solidchat.BR;
import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.Message;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.stuff.BindingViewHolder;
import com.andre.solid.solidchat.stuff.DataSetInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 09.04.17.
 */

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder> implements DataSetInterface {

    public static final Class clazz = MessagesRecyclerViewAdapter.class;

    private List<Object> data = new ArrayList<>();


    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rv_tem_message, parent, false));
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        holder.getBinding().setVariable(BR.message, data.get(holder.getAdapterPosition()));

        boolean isSame = false;
        if (holder.getAdapterPosition() == (data.size() - 1))
            isSame = false;
        else if (data.size() > 1) {
            if (data.get(holder.getAdapterPosition()) instanceof Message && data.get(holder.getAdapterPosition() + 1) instanceof Message) {
                Message item1 = (Message) data.get(holder.getAdapterPosition());
                Message item2 = (Message) data.get(holder.getAdapterPosition() + 1);
                isSame = (item1.isMine() == item2.isMine());
            }
        }

        holder.getBinding().setVariable(BR.same, isSame);
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void setData(List data) {
        this.data.clear();
        this.data.addAll(data);

        if (this.data.size() > 0) {
            notifyItemChanged(0, data.size() - 1);
        } else notifyDataSetChanged();
    }
}
