package ru.turbopro.didouble.chatcontract;

import android.provider.BaseColumns;

public final class ChatContract {
    private ChatContract(){};

    public static class ChatEntry  implements BaseColumns{
        public final static String TABLE_NAME = "chattable";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_SENDER = "sender";
        public final static String COLUMN_TEXT = "text";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_TIME = "time";
    }
}
