package com.stayfprod.utter.model.chat;


import android.graphics.Rect;
import android.text.Spannable;

import com.stayfprod.utter.model.AbstractChatMsg;
import com.stayfprod.utter.model.CachedUser;
import com.stayfprod.utter.model.OutputMsgIconType;

public class AbstractMainMsg extends AbstractChatMsg {

    //обычное сообщение
    public String date;
    public OutputMsgIconType msgIcon;
    public CachedUser cachedUser;

    //переадресованное
    public boolean isForward;
    public Spannable forwardText;   //Сообщение, которое находится под пересланным сообщением(todo в апи пока нет)
    public String forwardDate;
    public CachedUser cachedForwardUser;

    final public int[] layoutHeight = new int[2];

    final public Rect[] cycleRect = {new Rect(), new Rect()};
    final public Rect[] clockRect = {new Rect(), new Rect()};

    final public CharSequence[] drawName = new CharSequence[2];
    final public CharSequence[] forwardDrawName = new CharSequence[2];

    final public float[] dateStartX = new float[2];
    final public float[] forwardDateStartX = new float[2];

}
