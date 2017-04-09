package com.andre.solid.solidchat.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.QuickQuestion;
import com.andre.solid.solidchat.databinding.RvItemQuickAnswerBinding;
import com.andre.solid.solidchat.events.OnQuickAnswerClick;
import com.andre.solid.solidchat.stuff.BindingViewHolder;
import com.andre.solid.solidchat.stuff.ClickAdapter;
import com.andre.solid.solidchat.stuff.DataSetInterface;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 09.04.17.
 */

public class QuickResponceRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder<RvItemQuickAnswerBinding>> implements DataSetInterface<String> {
    private List<String> quickQuestions = new ArrayList<>();
    public static final Class clazz = QuickResponceRecyclerViewAdapter.class;

    @Override
    public BindingViewHolder<RvItemQuickAnswerBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingViewHolder<RvItemQuickAnswerBinding>((RvItemQuickAnswerBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rv_item_quick_answer, parent, false));
    }

    @Override
    public void onBindViewHolder(final BindingViewHolder<RvItemQuickAnswerBinding> holder, int position) {
        holder.getBinding().setData(quickQuestions.get(holder.getAdapterPosition()));
        holder.getBinding().setOnClick(new ClickAdapter() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new OnQuickAnswerClick(quickQuestions.get(holder.getAdapterPosition())));
            }
        });
    }

    @Override
    public int getItemCount() {
        return quickQuestions.size();
    }

    @Override
    public void setData(List<String> data) {
        this.quickQuestions.clear();
        this.quickQuestions.addAll(data);

        if (this.quickQuestions.size() > 0)
            notifyItemRangeChanged(0, this.quickQuestions.size() - 1);
        else
            notifyDataSetChanged();
    }
}
