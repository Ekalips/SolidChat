package com.andre.solid.solidchat.stuff;

import com.andre.solid.solidchat.data.Message;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class MessageUtils {
    public static String getDateFromMessage(Message item){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return format.format(item.getDate());
    }
}
