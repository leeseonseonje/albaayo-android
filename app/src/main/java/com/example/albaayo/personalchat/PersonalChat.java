package com.example.albaayo.personalchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.R;
import com.example.albaayo.chat.ResponseChatMessage;
import com.example.http.Http;
import com.example.http.dto.Id;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PersonalChat extends AppCompatActivity {

    private static final String TAG = "CompanyChat";
    private static final String SERVER_PORT = "9000";

    private PersonalChatAdapter mAdapter;
    private List<ResponsePersonalChatMessage> mDataSet = new ArrayList<>();
    private StompClient mStompClient;
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private RecyclerView mRecyclerView;
    private Gson mGson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }).create();


    private CompositeDisposable compositeDisposable;

    private ImageView sendButton, back;
    private EditText chatContent;
    private TextView headerName;
    private Long myMemberId;
    private Long memberId;
    private String memberName;
    private ProgressDialog progressDialog;
    private String date = "";

    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_chat);

        initData();

        chatContents();

        webSocketConnection();

        sendMessage();

        back();
        progressDialog.dismiss();
    }

    private void back() {
        back = findViewById(R.id.chatting_back_image_button);
        back.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendMessage() {
        chatContent = findViewById(R.id.chatEdit);
        sendButton = findViewById(R.id.chatSend);
        sendButton.setOnClickListener(v -> {
            if (!chatContent.getText().toString().replace(" ", "").equals("")) {
                RequestPersonalChatMessage request = RequestPersonalChatMessage.builder()
                        .sendMemberId(myMemberId).recvMemberId(memberId)
                        .name(Id.getInstance().getName())
                        .message(chatContent.getText().toString()).build();

                compositeDisposable.add(mStompClient.send("/send/member", mGson.toJson(request))
                        .compose(applySchedulers())
                        .subscribe(() -> {
                            Log.d(TAG, "STOMP echo send successfully =>");
                        }, throwable -> {
                            Log.e(TAG, "Error send STOMP echo", throwable);
                            toast(throwable.getMessage());
                        }));
                chatContent.setText("");
            } else {
                toast("메시지를 입력해 주세요.");
            }
        });
    }

    private void webSocketConnection() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://" + Http.URL
                + ":" + SERVER_PORT + "/example-endpoint/websocket");

        resetSubscriptions();

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader(LOGIN, "guest"));
        headers.add(new StompHeader(PASSCODE, "guest"));

        mStompClient.withClientHeartbeat(1000).withServerHeartbeat(1000);

        resetSubscriptions();
        Disposable dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            break;
                        case CLOSED:
                            resetSubscriptions();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            break;
                    }
                });

        compositeDisposable.add(dispLifecycle);

        // Receive greetings
        Disposable dispTopic = mStompClient.topic("/recv/member/" + myMemberId + "/" + memberId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "ReceivedA " + topicMessage.getPayload());
                    ResponseChatMessage message = mGson.fromJson(topicMessage.getPayload(), ResponseChatMessage.class);
                    if (!date.equals(message.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
                        mAdapter.addItem(ResponsePersonalChatMessage.builder().time(message.getTime()).build());
                        date = message.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    }
                    mAdapter.addItem(mGson.fromJson(topicMessage.getPayload(), ResponsePersonalChatMessage.class));
                    mRecyclerView.smoothScrollToPosition(mDataSet.size()-1);
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                });
//
//        Disposable dispTopicB = mStompClient.topic("/recv/message/" + "2")
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(topicMessage -> {
//                    Log.d(TAG, "ReceivedB " + topicMessage.getPayload());
//                    addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
//                }, throwable -> {
//                    Log.e(TAG, "Error on subscribe topic", throwable);
//                });


        compositeDisposable.add(dispTopic);
//        compositeDisposable.add(dispTopicB);

        mStompClient.connect(headers);
    }

    private void chatContents() {
        Call<List<ResponsePersonalChatMessage>> call =
                Http.getInstance().getApiService().personalChatContents(Id.getInstance().getAccessToken(), myMemberId, memberId);

        call.enqueue(new Callback<List<ResponsePersonalChatMessage>>() {
            @Override
            public void onResponse(Call<List<ResponsePersonalChatMessage>> call, Response<List<ResponsePersonalChatMessage>> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<List<ResponsePersonalChatMessage>> reCall =
                            Http.getInstance().getApiService().personalChatContents(Id.getInstance().getAccessToken(), myMemberId, memberId);
                    reCall.enqueue(new Callback<List<ResponsePersonalChatMessage>>() {
                        @Override
                        public void onResponse(Call<List<ResponsePersonalChatMessage>> call, Response<List<ResponsePersonalChatMessage>> response) {
                            mDataSet = response.body();

                            for (int i = 0; i < mDataSet.size(); i++) {
                                if (!date.equals(mDataSet.get(i).getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko"))))) {
                                    mDataSet.add(i, ResponsePersonalChatMessage.builder().time(mDataSet.get(i).getTime()).build());
                                    date = mDataSet.get(i).getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")));
                                }
                            }

                            mAdapter = new PersonalChatAdapter(mDataSet);
                            mAdapter = new PersonalChatAdapter(mDataSet);
                            mRecyclerView.setAdapter(mAdapter);
                            mRecyclerView.scrollToPosition(mDataSet.size() - 1);
                        }

                        @Override
                        public void onFailure(Call<List<ResponsePersonalChatMessage>> call, Throwable t) {
                        }
                    });
                } else {

                    mDataSet = response.body();

                    for (int i = 0; i < mDataSet.size(); i++) {
                        if (!date.equals(mDataSet.get(i).getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko"))))) {
                            mDataSet.add(i, ResponsePersonalChatMessage.builder().time(mDataSet.get(i).getTime()).build());
                            date = mDataSet.get(i).getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withLocale(Locale.forLanguageTag("ko")));
                        }
                    }

                    mAdapter = new PersonalChatAdapter(mDataSet);
                    mAdapter = new PersonalChatAdapter(mDataSet);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.scrollToPosition(mDataSet.size() - 1);
                }
            }
            @Override
            public void onFailure(Call<List<ResponsePersonalChatMessage>> call, Throwable t) {
            }
        });
    }

    private void initData() {
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");
        progressDialog.show();

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        Intent intent = getIntent();
        myMemberId = intent.getLongExtra("myMemberId", 0);
        memberId = intent.getLongExtra("memberId", 0);
        memberName = intent.getStringExtra("memberName");
        headerName = findViewById(R.id.header_name_text);
        headerName.setText(memberName);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    public void disconnectStomp(View view) {
        mStompClient.disconnect();
    }

    public static final String LOGIN = "login";

    public static final String PASSCODE = "passcode";


    private void toast(String text) {
        Log.i(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void resetSubscriptions() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onDestroy() {
        mStompClient.disconnect();

        if (compositeDisposable != null) compositeDisposable.dispose();
        super.onDestroy();
    }

}
