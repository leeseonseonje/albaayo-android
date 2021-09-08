package com.example.albaayo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.company_notice.CompanyNoticeRegisterAdapter;
import com.example.http.dto.Id;
import com.example.http.dto.ImageDto;
import com.example.http.dto.NoticeImageDto;
import com.example.http.dto.RequestNoticeUpdateDto;
import com.example.http.dto.ResponseNoticeDto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeUpdate extends AppCompatActivity {

    private TextView headerName;
    private EditText title, content;
    private Button update;
    private ImageView imageButton;
    private CompanyNoticeRegisterAdapter companyNoticeRegisterAdapter;
    private List<ImageDto> list = new ArrayList<>();
    List<NoticeImageDto> noticeImageDto =new ArrayList<>();
    RequestNoticeUpdateDto requestNoticeUpdateDto;
    private ProgressDialog progressDialog;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;
    private long noticeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_notice_register);

        initData();
        gallery();

        updateForm();

        update();
    }

    private void update() {
        update.setOnClickListener(v -> {
            progressDialog.show();
            List<ImageDto> list = companyNoticeRegisterAdapter.getList();
            List<NoticeImageDto> imageList = new ArrayList<>();
            for (ImageDto imageDto : list) {
                Bitmap bitmap = imageDto.getImage();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();
                String image = Base64.encodeToString(imageBytes, 0);
                imageList.add(NoticeImageDto.builder().image(image).imageContent(imageDto.getText()).build());
            }

            requestNoticeUpdateDto = RequestNoticeUpdateDto.builder().noticeId(noticeId).title(title.getText().toString())
                    .contents(content.getText().toString()).imageList(imageList).build();

            Call<Void> call1 = Http.getInstance().getApiService()
                    .noticeUpdate(Id.getInstance().getAccessToken(), requestNoticeUpdateDto);
            call1.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.code() == 401) {
                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                        editor.putString("accessToken", response.headers().get("Authorization"));
                        editor.commit();

                        Call<Void> reCall1 = Http.getInstance().getApiService()
                                .noticeUpdate(Id.getInstance().getAccessToken(), requestNoticeUpdateDto);
                        reCall1.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                finish();
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(NoticeUpdate.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        finish();
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(NoticeUpdate.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateForm() {
        progressDialog.show();
        Call<ResponseNoticeDto> call = Http.getInstance().getApiService()
                .noticeContent(Id.getInstance().getAccessToken(), noticeId);
        call.enqueue(new Callback<ResponseNoticeDto>() {
            @Override
            public void onResponse(Call<ResponseNoticeDto> call, Response<ResponseNoticeDto> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<ResponseNoticeDto> reCall = Http.getInstance().getApiService()
                            .noticeContent(Id.getInstance().getAccessToken(), noticeId);
                    reCall.enqueue(new Callback<ResponseNoticeDto>() {
                        @Override
                        public void onResponse(Call<ResponseNoticeDto> call, Response<ResponseNoticeDto> response) {
                            title.setText(response.body().getTitle());
                            content.setText(response.body().getContents());

                            for (NoticeImageDto noticeImageDto : response.body().getImageList()) {
                                byte[] b = Base64.decode(noticeImageDto.getImage(), 0);
                                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                                list.add(ImageDto.builder().image(bitmap).text(noticeImageDto.getImageContent()).build());
                            }

                            companyNoticeRegisterAdapter = new CompanyNoticeRegisterAdapter(list);
                            RecyclerView recyclerView = findViewById(R.id.recycler_view);
                            LinearLayoutManager manager = new LinearLayoutManager(NoticeUpdate.this, LinearLayoutManager.VERTICAL,false);
                            recyclerView.setLayoutManager(manager);
                            recyclerView.setAdapter(companyNoticeRegisterAdapter);
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<ResponseNoticeDto> call, Throwable t) {
                            Toast.makeText(NoticeUpdate.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    title.setText(response.body().getTitle());
                    content.setText(response.body().getContents());

                    for (NoticeImageDto noticeImageDto : response.body().getImageList()) {
                        byte[] b = Base64.decode(noticeImageDto.getImage(), 0);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                        list.add(ImageDto.builder().image(bitmap).text(noticeImageDto.getImageContent()).build());
                    }

                    companyNoticeRegisterAdapter = new CompanyNoticeRegisterAdapter(list);
                    RecyclerView recyclerView = findViewById(R.id.recycler_view);
                    LinearLayoutManager manager = new LinearLayoutManager(NoticeUpdate.this, LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(companyNoticeRegisterAdapter);
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<ResponseNoticeDto> call, Throwable t) {
                Toast.makeText(NoticeUpdate.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void gallery() {
        imageButton.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, 0);
        });
    }

    private void initData() {
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        String companyName = intent.getStringExtra("companyName");
        noticeId = intent.getLongExtra("noticeId", 0);
        headerName = findViewById(R.id.header_name_text);
        headerName.setText(companyName);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        title = findViewById(R.id.input_notice_title);
        content = findViewById(R.id.input_notice_content);
        imageButton = findViewById(R.id.notice_image_button);
        update = findViewById(R.id.notice_register_button);

        update.setText("수정");
    }

    @SneakyThrows
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                ExifInterface exif = null;
                String absolutePath = getRealPathFromURI(this, data.getData());
                exif = new ExifInterface(absolutePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Bitmap b = BitmapFactory.decodeFile(absolutePath);
                Bitmap bitmap = EmployerMainPage.rotateBitmap(b, orientation);
                ImageDto build = ImageDto.builder().image(bitmap).path(absolutePath).build();
                companyNoticeRegisterAdapter.addItem(build);
            } else if(resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getRealPathFromURI(Context context, Uri uri) {

        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null) {
            return null;
        }

        String filePath = context.getApplicationInfo().dataDir + File.separator + System.currentTimeMillis();

        File file = new File(filePath);
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream == null) {
                return null;
            }
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0)
                outputStream.write(buf, 0, len);
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            return null;
        }
        return file.getAbsolutePath();
    }
}
