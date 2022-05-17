package ru.turbopro.didouble;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ru.turbopro.didouble.chatcontract.ChatContract;

public class ChatDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ChatDbHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 1;

    public ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Строка для создания таблицы
        String SQL_CREATE_GUESTS_TABLE = "CREATE TABLE " + ChatContract.ChatEntry.TABLE_NAME + " ("
                + ChatContract.ChatEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ChatContract.ChatEntry.COLUMN_SENDER + " TEXT NOT NULL, "
                + ChatContract.ChatEntry.COLUMN_TEXT + " TEXT NOT NULL, "
                + ChatContract.ChatEntry.COLUMN_DATE + " TEXT NOT NULL, "
                + ChatContract.ChatEntry.COLUMN_TIME + " TEXT NOT NULL);";

        // Запускаем создание таблицы
        db.execSQL(SQL_CREATE_GUESTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Запишем в журнал
        Log.w("SQLite", "Обновляемся с версии " + oldVersion + " на версию " + newVersion);

        // Удаляем старую таблицу и создаём новую
        db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_NAME);
        // Создаём новую таблицу
        onCreate(db);
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + ChatContract.ChatEntry.TABLE_NAME);
        db.close();
    }
}