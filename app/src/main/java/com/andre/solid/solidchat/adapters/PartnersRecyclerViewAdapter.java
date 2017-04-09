package com.andre.solid.solidchat.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.PartnerUserData;
import com.andre.solid.solidchat.databinding.RvItemPartnersBinding;
import com.andre.solid.solidchat.events.FetchOfflineChatEvent;
import com.andre.solid.solidchat.events.TryToConnectEvent;
import com.andre.solid.solidchat.stuff.BindingViewHolder;
import com.andre.solid.solidchat.stuff.ClickAdapter;
import com.andre.solid.solidchat.stuff.DataSetInterface;
import com.andre.solid.solidchat.stuff.PartnersDiffUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 08.04.17.
 */

public class PartnersRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder<RvItemPartnersBinding>> implements DataSetInterface<PartnerUserData> {
    public static final Class clazz = PartnersRecyclerViewAdapter.class;
    private static final String TAG = PartnersRecyclerViewAdapter.class.getSimpleName();

    private List<PartnerUserData> data = new ArrayList<>();

    @Override
    public BindingViewHolder<RvItemPartnersBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingViewHolder<>((RvItemPartnersBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rv_item_partners, parent, false));
    }

    @Override
    public void onBindViewHolder(final BindingViewHolder<RvItemPartnersBinding> holder, int position) {
        holder.getBinding().setData(data.get(holder.getAdapterPosition()));
        holder.getBinding().setOnClick(new ClickAdapter() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new TryToConnectEvent(data.get(holder.getAdapterPosition())));
            }
        });
        holder.getBinding().setOnOfflineClick(new ClickAdapter() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new FetchOfflineChatEvent(data.get(holder.getAdapterPosition())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void setData(List<PartnerUserData> newData) {

//        DiffUtil.DiffResult diffUtilCallback =  DiffUtil.calculateDiff(new PartnersDiffUtil(data,newData),false);
//        diffUtilCallback.dispatchUpdatesTo(this);

        this.data = new ArrayList<>(newData);

        if (newData.size()>0)
            notifyItemRangeChanged(0,newData.size());
        else
            notifyDataSetChanged();
    }


}
