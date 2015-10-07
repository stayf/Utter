package com.stayfprod.utter.model;


import android.support.annotation.NonNull;

import org.drinkless.td.libcore.telegram.TdApi;

public class Contact implements Comparable<Contact> {

    public CachedUser cachedUser;
    public String lastSeen;

    public TdApi.ChatParticipant chatParticipant;
    public CharSequence[] drawLastSeen = new CharSequence[2];
    public CharSequence[] drawTitle = new CharSequence[2];
    public boolean isOnline;
    public boolean sortByJoinDate;

    public Contact(CachedUser cachedUser, String lastSeen) {
        this.cachedUser = cachedUser;
        this.lastSeen = lastSeen;
    }

    @Override
    public int compareTo(@NonNull Contact another) {
        if (sortByJoinDate && chatParticipant != null) {
           return compare(chatParticipant.joinDate, another.chatParticipant.joinDate);
        }
        return this.cachedUser.fullName.compareTo(another.cachedUser.fullName);
    }


    public static int compare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }
}
