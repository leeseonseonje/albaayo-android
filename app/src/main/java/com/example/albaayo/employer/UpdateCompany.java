package com.example.albaayo.employer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.albaayo.AddressSearchActivity;
import com.example.albaayo.R;
import com.example.http.Http;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.Id;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.albaayo.EmployerMainPage.rotateBitmap;

public class UpdateCompany extends AppCompatActivity {

    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;

    private Long companyId;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    private EditText companyName, address, companyAddress, companyNumber;
    private Button addressSearch, imageButton, updateButton;
    private ImageView companyImage;

    private String absolutePath;
    private byte[] imageBytes;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update);

        initData();

        addressSearch();

        imageButton.setOnClickListener(v -> {
            Uri uri = Uri.parse("content://media/external/images/media");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 0);
        });

        updateButton.setOnClickListener(v -> {
            if (!companyName.getText().toString().replace(" ", "").equals("")) {
                if (!address.getText().toString().replace(" ", "").equals("")) {
                    if (!companyNumber.getText().toString().replace(" ", "").equals("")) {
                        if (imageBytes != null) {
                            progressDialog.show();
                            File file = new File(absolutePath);

                            ArrayList<MultipartBody.Part> list = new ArrayList<>();
                            list.add(MultipartBody.Part.createFormData("name", companyName.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("location", address.getText().toString()
                                    + " " + companyAddress.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("businessRegistrationNumber", companyNumber.getText().toString()));
                            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                            list.add(MultipartBody.Part.createFormData("image", file.getName(), fileBody));


                            Call<Void> call = Http.getInstance().getApiService()
                                    .updateCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list);
                            call.enqueue(new Callback<Void>() {
                                @SneakyThrows
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (response.code() == 401) {
                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                        editor.commit();

                                        Call<Void> reCall = Http.getInstance().getApiService()
                                                .updateCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list);

                                        reCall.enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                if (response.code() != 500) {
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.dismiss();
                                                    new AlertDialog.Builder(UpdateCompany.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                            .setMessage("중복된 사업자번호 입니다.")
                                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which){
                                                                }
                                                            })
                                                            .show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Toast.makeText(UpdateCompany.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if (response.code() != 500) {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                    } else {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(UpdateCompany.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage("중복된 사업자번호 입니다.")
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which){
                                                    }
                                                })
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    Toast.makeText(UpdateCompany.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            ArrayList<MultipartBody.Part> list = new ArrayList<>();
                            list.add(MultipartBody.Part.createFormData("name", companyName.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("location", address.getText().toString()
                                    + " " + companyAddress.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("businessRegistrationNumber", companyNumber.getText().toString()));
                            Call<CompanyDto> call = Http.getInstance().getApiService()
                                    .createCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list);
                            call.enqueue(new Callback<CompanyDto>() {
                                @SneakyThrows
                                @Override
                                public void onResponse(Call<CompanyDto> call, Response<CompanyDto> response) {
                                    if (response.code() == 401) {
                                        Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                                        editor.putString("accessToken", response.headers().get("Authorization"));
                                        editor.commit();

                                        Call<CompanyDto> reCall = Http.getInstance().getApiService()
                                                .createCompany(Id.getInstance().getAccessToken(), Id.getInstance().getId(), list);

                                        reCall.enqueue(new Callback<CompanyDto>() {
                                            @Override
                                            public void onResponse(Call<CompanyDto> call, Response<CompanyDto> response) {
                                                if (response.code() != 500) {
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.dismiss();
                                                    new AlertDialog.Builder(UpdateCompany.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                            .setMessage("중복된 사업자번호 입니다.")
                                                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which){
                                                                }
                                                            })
                                                            .show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<CompanyDto> call, Throwable t) {
                                                System.out.println("t.getMessage() = " + t.getMessage());
                                                System.out.println("t.getMessage() = " + t.getStackTrace());
                                                Toast.makeText(UpdateCompany.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if (response.code() != 500) {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                    } else {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(UpdateCompany.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                                .setMessage("중복된 사업자번호 입니다.")
                                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which){
                                                    }
                                                })
                                                .show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<CompanyDto> call, Throwable t) {
                                    System.out.println("t.getMessage() = " + t.getMessage());
                                    System.out.println("t.getMessage() = " + t.getStackTrace());
                                    Toast.makeText(UpdateCompany.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(UpdateCompany.this, "사업자번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdateCompany.this, "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(UpdateCompany.this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);

        companyName = findViewById(R.id.input_company_name);
        address = findViewById(R.id.input_address);
        companyAddress = findViewById(R.id.input_company_address);
        companyNumber = findViewById(R.id.input_company_number);

        addressSearch = findViewById(R.id.address_search);
        imageButton = findViewById(R.id.company_image_button);
        updateButton = findViewById(R.id.update_button);

        companyImage = findViewById(R.id.company_image_view);
    }

    public void addressSearch() {

        Button addressSearch = findViewById(R.id.address_search);
        addressSearch.setOnClickListener(v -> {
            Intent i = new Intent(UpdateCompany.this, AddressSearchActivity.class);
            startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0)
        {
            if(resultCode == RESULT_OK)
            {
                ExifInterface exif = null;
                absolutePath = getRealPathFromURI(this, data.getData());
                try{
                    exif = new ExifInterface(absolutePath);
                }catch(Exception e) {
                    e.printStackTrace();
                }

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);
                Bitmap b = BitmapFactory.decodeFile(absolutePath);
                Bitmap bitmap = rotateBitmap(b, orientation);
                companyImage.setImageBitmap(bitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageBytes = stream.toByteArray();

            }
            else if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == SEARCH_ADDRESS_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                String add = data.getExtras().getString("add");
                if (add != null) {
                    address.setText(add.substring(7));
                }
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri uri) {

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
