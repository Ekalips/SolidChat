package com.andre.solid.solidchat.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andre.solid.solidchat.BR;
import com.andre.solid.solidchat.R;
import com.andre.solid.solidchat.data.QuickQuestion;
import com.andre.solid.solidchat.stuff.BindingViewHolder;
import com.andre.solid.solidchat.stuff.DataSetInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 09.04.17.
 */

public class QuestionsRecyclerViewAdapter extends RecyclerView.Adapter<BindingViewHolder> implements DataSetInterface<QuickQuestion> {
    public static final Class clazz = QuestionsRecyclerViewAdapter.class;

    private List<QuickQuestion> quickQuestions = new ArrayList<>();

    @Override
    public BindingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingViewHolder<>(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.rv_item_quick_question,parent,false));
    }

    @Override
    public void onBindViewHolder(BindingViewHolder holder, int position) {
        holder.getBinding().setVariable(BR.question,quickQuestions.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return quickQuestions.size();
    }

    @Override
    public void setData(List<QuickQuestion> data) {
        this.quickQuestions.clear();
        this.quickQuestions.addAll(data);

        if (this.quickQuestions.size() > 0)
            notifyItemRangeChanged(0, this.quickQuestions.size() - 1);
        else
            notifyDataSetChanged();

    }
}
