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
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.company_notice.CompanyNoticeRegisterAdapter;
import com.example.http.dto.Id;
import com.example.http.dto.ImageDto;
import com.example.http.dto.NoticeImageDto;
import com.example.http.dto.RequestNoticeDto;

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

public class CompanyNoticeRegister extends AppCompatActivity {

    private Long companyId;
    private String companyName;
    private TextView headerTextView;
    private EditText title, contents;
    private ImageView image;
    private Button register;
    private RequestNoticeDto request;
    private CompanyNoticeRegisterAdapter companyNoticeRegisterAdapter;
    private List<ImageDto> list = new ArrayList<>();
    List<NoticeImageDto> noticeImageDto =new ArrayList<>();
    private byte[] imageBytes;
    private ProgressDialog progressDialog;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_notice_register);

        initData();

        noticeInput();
    }

    private void initData() {
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);
        companyName = intent.getStringExtra("companyName");
        headerTextView = findViewById(R.id.header_name_text);
        headerTextView.setText(companyName);
        title = findViewById(R.id.input_notice_title);
        contents = findViewById(R.id.input_notice_content);
        image = findViewById(R.id.notice_image_button);
        register = findViewById(R.id.notice_register_button);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        companyNoticeRegisterAdapter = new CompanyNoticeRegisterAdapter(list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(CompanyNoticeRegister.this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(companyNoticeRegisterAdapter);

        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void noticeInput() {

        image.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, 0);
        });
        register.setOnClickListener(v -> {
            if (!title.getText().toString().replace(" ", "").equals("")) {
                if (!contents.getText().toString().replace(" ", "").equals("")) {
                    noticeRegister();
                } else {
                Toast.makeText(CompanyNoticeRegister.this, "내용을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CompanyNoticeRegister.this, "제목을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void noticeRegister() {
        progressDialog.show();

        if (companyNoticeRegisterAdapter.getList().size() != 0) {

            for (ImageDto imageDto : list) {
                Bitmap b = imageDto.getImage();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageBytes = stream.toByteArray();
                String image = Base64.encodeToString(imageBytes, 0);
                noticeImageDto.add(NoticeImageDto.builder().image(image).imageContent(imageDto.getText()).build());
            }
        }

        request = RequestNoticeDto.builder().title(title.getText().toString()).contents(contents.getText().toString()).image(noticeImageDto).build();
        Call<Void> call = Http.getInstance().getApiService()
                .noticeRegister(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId, request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<Void> reCall = Http.getInstance().getApiService()
                            .noticeRegister(Id.getInstance().getAccessToken(), Id.getInstance().getId(), companyId, request);
                    reCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            finish();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(CompanyNoticeRegister.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    finish();
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CompanyNoticeRegister.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
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
}
