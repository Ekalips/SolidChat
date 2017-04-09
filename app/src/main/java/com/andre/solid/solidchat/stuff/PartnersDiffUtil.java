package com.andre.solid.solidchat.stuff;

import android.support.v7.util.DiffUtil;
import android.text.TextUtils;

import com.andre.solid.solidchat.data.PartnerUserData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lantain on 09.04.17.
 */

public class PartnersDiffUtil extends DiffUtil.Callback {
    private List<PartnerUserData> oldList = new ArrayList<>();
    private List<PartnerUserData> newList = new ArrayList<>();

    public PartnersDiffUtil(List<PartnerUserData> oldList, List<PartnerUserData> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        PartnerUserData oldData = oldList.get(oldItemPosition);
        PartnerUserData newData = newList.get(newItemPosition);

        return TextUtils.equals(oldData.getDisplayName(), newData.getDisplayName())
                && (oldData.getConnectionStatus() == newData.getConnectionStatus())
                && (TextUtils.equals(oldData.getImage(), newData.getImage()));
    }
}
