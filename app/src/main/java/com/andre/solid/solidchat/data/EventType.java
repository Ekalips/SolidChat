package com.andre.solid.solidchat.data;

import android.support.annotation.IntDef;

/**
 * Created by lantain on 08.04.17.
 */

public class EventType {

    public static final int TYPE_USER_DATA = 0;
    public static final int TYPE_MESSAGE = 1;
    public static final int TYPE_DESTROY = 2;
    public static final int TYPE_ATTACHMENT = 3;

    @IntDef({TYPE_USER_DATA,TYPE_MESSAGE,TYPE_DESTROY,TYPE_ATTACHMENT})
    @interface EventTypeDef {
    }
}