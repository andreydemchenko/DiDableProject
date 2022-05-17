package ru.turbopro.didouble;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import ru.turbopro.didouble.chatcontract.ChatContract;

public class ChatActivity extends AppCompatActivity implements BotReply{

    RecyclerView chatView;
    ChatAdapter chatAdapter;
    List<Message> messageList = new ArrayList<>();
    EditText editMessage;
    ImageButton btnSend;
    ConstraintLayout layoutSend;
    ConstraintLayout layoutRecieve;
    LinearLayout welcomeLayout;

    //dialogFlow
    private SessionsClient sessionsClient;
    private SessionName sessionName;
    private String uuid = UUID.randomUUID().toString();
    private String TAG = "chatactivity";

    private ChatDbHelper mDbHelper;

    private SearchView mSearchView;
    private ViewPager mVpContent;
    private Toolbar mToolbar;

    Date currentDate = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mDbHelper = new ChatDbHelper(this);
        displayDatabaseInfo();

        mToolbar = (Toolbar) findViewById(R.id.toolbarChat);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        chatView = findViewById(R.id.chatView);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);

        layoutSend = findViewById(R.id.constraintLayoutSend);
        layoutRecieve = findViewById(R.id.constraintLayoutReceive);
        welcomeLayout = findViewById(R.id.linearLayoutWelcomeMessage);

        if(messageList.isEmpty()){
            chatView.setVisibility(View.GONE);
            welcomeLayout.setVisibility(View.VISIBLE);
        }
        chatAdapter = new ChatAdapter(messageList, this);
        chatView.setAdapter(chatAdapter);

        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                String message = editMessage.getText().toString();
                if (!message.isEmpty()) {
                    chatView.setVisibility(View.VISIBLE);
                    welcomeLayout.setVisibility(View.GONE);
                    messageList.add(new Message(message, false, timeText, dateText));
                    // Gets the database in write mode
                    SQLiteDatabase db = mDbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(ChatContract.ChatEntry.COLUMN_SENDER, "user");
                    values.put(ChatContract.ChatEntry.COLUMN_TEXT, message);
                    values.put(ChatContract.ChatEntry.COLUMN_DATE, dateText);
                    values.put(ChatContract.ChatEntry.COLUMN_TIME, timeText);

                    editMessage.setText("");
                    sendMessageToBot(message);
                    Objects.requireNonNull(chatView.getAdapter()).notifyDataSetChanged();
                    Objects.requireNonNull(chatView.getLayoutManager())
                            .scrollToPosition(messageList.size() - 1);
                } else {
                    Toast.makeText(ChatActivity.this, "Please enter text!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setUpBot();
    }

    private void displayDatabaseInfo(){
        // Создадим и откроем для чтения базу данных
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Зададим условие для выборки - список столбцов
        String[] projection = {
                ChatContract.ChatEntry._ID,
                ChatContract.ChatEntry.COLUMN_SENDER,
                ChatContract.ChatEntry.COLUMN_TEXT,
                ChatContract.ChatEntry.COLUMN_DATE,
                ChatContract.ChatEntry.COLUMN_TIME };

        // Делаем запрос
        Cursor cursor = db.query(
                ChatContract.ChatEntry.TABLE_NAME,   // таблица
                projection,            // столбцы
                null,                  // столбцы для условия WHERE
                null,                  // значения для условия WHERE
                null,                  // Don't group the rows
                null,                  // Don't filter by row groups
                null);                   // порядок сортировки

        try {
            // Узнаем индекс каждого столбца
            int idColumnIndex = cursor.getColumnIndex(ChatContract.ChatEntry._ID);
            int senderColumnIndex = cursor.getColumnIndex(ChatContract.ChatEntry.COLUMN_SENDER);
            int textColumnIndex = cursor.getColumnIndex(ChatContract.ChatEntry.COLUMN_TEXT);
            int dateColumnIndex = cursor.getColumnIndex(ChatContract.ChatEntry.COLUMN_DATE);
            int timeColumnIndex = cursor.getColumnIndex(ChatContract.ChatEntry.COLUMN_TIME);

            // Проходим через все ряды
            while (cursor.moveToNext()) {
                // Используем индекс для получения строки или числа
                int currentID = cursor.getInt(idColumnIndex);
                String currentSender = cursor.getString(senderColumnIndex);
                String currentText = cursor.getString(textColumnIndex);
                String currentDate = cursor.getString(dateColumnIndex);
                String currentTime = cursor.getString(timeColumnIndex);
                // Выводим значения каждого столбца
                if (currentSender.equals("user")) {
                    messageList.add(new Message(currentText, false, currentTime, currentDate));
                } else if (currentSender.equals("bot")) {
                    messageList.add(new Message(currentText, true, currentTime, currentDate));
                }
            }
        } finally {
            // Всегда закрываем курсор после чтения
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.itemSearch);
        mSearchView = (SearchView) searchItem.getActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //deleteItem.setVisible(true);
        //chatAdapter.finalUpdateList();

        switch (item.getItemId()) {
            case android.R.id.home: {
                this.finish();
                return true;
            }
            case R.id.itemDeleteChat: {
                AlertDialog.Builder ad = new AlertDialog.Builder(this);
                ad.setTitle("Удалить чат");  // заголовок
                ad.setMessage("Вы действительно хотите удалить чат с ботом?"); // сообщение
                ad.setCancelable(true);
                ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                });
                ad.setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        mDbHelper.deleteAll();
                        finish();
                    }
                });
                AlertDialog alert = ad.create();
                alert.show();
            }
            case R.id.itemSearch:{
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        mSearchView.clearFocus();
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        chatAdapter.filter(newText);
                        return false;
                    }
                });
                mSearchView.setQueryHint("поиск");
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpBot() {
        try (InputStream stream = this.getResources().openRawResource(R.raw.poperechnii_xugi_c587ad3dd1a0)){
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream)
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(
                    FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, uuid);

            Log.d(TAG, "projectId : " + projectId);
        } catch (Exception e) {
            Log.d(TAG, "setUpBot: " + e.getMessage());
        }
    }

    private void sendMessageToBot(String message) {
        QueryInput input = QueryInput.newBuilder()
                .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
        new SendMessageInBg(this, sessionName, sessionsClient, input).execute();
    }

    @Override
    public void callback(DetectIntentResponse returnResponse) {
        if(returnResponse != null) {
            String botReply = returnResponse.getQueryResult().getFulfillmentText();
            DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeText = timeFormat.format(currentDate);
            DateFormat dateFormat = new SimpleDateFormat("dd MMMM", Locale.getDefault());
            String dateText = dateFormat.format(currentDate);
            if(!botReply.isEmpty()){
                messageList.add(new Message(botReply, true, timeText, dateText));
                // Gets the database in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(ChatContract.ChatEntry.COLUMN_SENDER, "bot");
                values.put(ChatContract.ChatEntry.COLUMN_TEXT, botReply);
                values.put(ChatContract.ChatEntry.COLUMN_DATE, dateText);
                values.put(ChatContract.ChatEntry.COLUMN_TIME, timeText);

                long newRowId = db.insert(ChatContract.ChatEntry.TABLE_NAME, null, values);

                if (newRowId == -1)
                    // Если ID  -1, значит произошла ошибка
                    Toast.makeText(this, "Ошибка при создании сообщения", Toast.LENGTH_SHORT).show();

                chatAdapter.notifyDataSetChanged();
                Objects.requireNonNull(chatView.getLayoutManager()).scrollToPosition(messageList.size() - 1);
            }else {
                Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "failed to connect!", Toast.LENGTH_SHORT).show();
        }
    }
}