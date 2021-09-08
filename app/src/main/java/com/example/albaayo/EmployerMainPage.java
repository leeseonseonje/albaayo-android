package com.example.albaayo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.albaayo.mypage.UserMyPage;
import com.example.http.dto.CompanyDto;
import com.example.http.dto.Id;
import com.example.http.dto.ResponseLoginDto;
import com.example.list.accept_company.CompanyListAdapter;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EmployerMainPage extends AppCompatActivity {

    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private TextView headerName;
    private RecyclerView recyclerView;
    private ConstraintLayout companyCreateLayout;
    private ConstraintLayout employerMainLayout;
    private ImageView companyPicture;
    private EditText address;
    private byte[] imageBytes;
    private ProgressDialog progressDialog;
    private TextView emptyText;
    private Button myPageButton;
    private String absolutePath;
    private ResponseLoginDto data;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employer_main_page);

        initData();

        companiesApi(Id.getInstance().getId());

        footerButton();

        createGroup();

        addressSearch();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initData() {
        progressDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        progressDialog.setMessage("로딩중!");

        emptyText = findViewById(R.id.empty_list);
        address = findViewById(R.id.input_address);

        headerName = findViewById(R.id.header_name_text);

        headerName.setText(Id.getInstance().getName());

        companyPicture = findViewById(R.id.company_image_view);
        employerMainLayout = findViewById(R.id.employer_main_layout);
        companyCreateLayout = findViewById(R.id.company_create_layout);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        sharedPreferencesPut();
        getFirebaseToken();
    }

    private void getFirebaseToken() {
        System.out.println("파이어베이스 토큰: " + FirebaseMessaging.getInstance().getToken());
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("FirebaseSettingEx", "getInstanceId failed", task.getException());
                return;
            }

            // 토큰을 읽고, 텍스트 뷰에 보여주기
            String token = task.getResult();
            System.out.println("token = " + token);
        });
    }

    private void sharedPreferencesPut() {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        data = intent.getParcelableExtra("login");
        if (data != null) {
            editor.putString("accessToken", data.getAccessToken());
            editor.putLong("id", data.getId());
            editor.putString("userId", data.getUserId());
            editor.putString("role", data.getRole());
            editor.putString("name", data.getName());
            editor.commit();
        }
        Id.getInstance().setAccessToken(sf.getString("accessToken", ""));
        Id.getInstance().setId(sf.getLong("id", 0L));
        Id.getInstance().setUserId(sf.getString("userId", ""));
        Id.getInstance().setName(sf.getString("name", ""));
        Id.getInstance().setRole(sf.getString("role", ""));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createGroup() {
        Button pictureButton = findViewById(R.id.company_image_button);
        pictureButton.setOnClickListener(v -> {
            Uri uri = Uri.parse("content://media/external/images/media");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 0);
        });

        Button createGroupButton = findViewById(R.id.create_button);
        EditText nameText = findViewById(R.id.input_company_name);
        EditText locationText = findViewById(R.id.input_company_address);
        EditText numberText = findViewById(R.id.input_company_number);

        createGroupButton.setOnClickListener(v -> {
            if (!nameText.getText().toString().replace(" ", "").equals("")) {
                if (!address.getText().toString().replace(" ", "").equals("")) {
                    if (!numberText.getText().toString().replace(" ", "").equals("")) {
                        if (imageBytes != null) {
                            progressDialog.show();
                            File file = new File(absolutePath);

                            ArrayList<MultipartBody.Part> list = new ArrayList<>();
                            list.add(MultipartBody.Part.createFormData("name", nameText.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("location", address.getText().toString()
                                    + " " + locationText.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("businessRegistrationNumber", numberText.getText().toString()));
                            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
                            list.add(MultipartBody.Part.createFormData("image", file.getName(), fileBody));


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
                                                    System.out.println("response = " + response.body().getPicture());
                                                    Intent intent = getIntent();
                                                    finish();
                                                    startActivity(intent);
                                                    progressDialog.dismiss();
                                                } else {
                                                    progressDialog.dismiss();
                                                    new AlertDialog.Builder(EmployerMainPage.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                                                Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if (response.code() != 500) {
                                        System.out.println("response = " + response.body().getPicture());
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                    } else {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(EmployerMainPage.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                                    Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            ArrayList<MultipartBody.Part> list = new ArrayList<>();
                            list.add(MultipartBody.Part.createFormData("name", nameText.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("location", address.getText().toString()
                                    + " " + locationText.getText().toString()));
                            list.add(MultipartBody.Part.createFormData("businessRegistrationNumber", numberText.getText().toString()));
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
                                                    new AlertDialog.Builder(EmployerMainPage.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                                                Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else if (response.code() != 500) {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                        progressDialog.dismiss();
                                    } else {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(EmployerMainPage.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                                    Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(EmployerMainPage.this, "사업자번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployerMainPage.this, "주소를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EmployerMainPage.this, "이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void footerButton() {
        Button createButton = findViewById(R.id.create);
        createButton.setOnClickListener(v -> {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            companyCreateLayout.setVisibility(View.VISIBLE);
        });

        Button homeButton = findViewById(R.id.main);
        homeButton.setOnClickListener(v -> {
            companyCreateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            companiesApi(Id.getInstance().getId());
        });

        myPageButton = findViewById(R.id.my_page);
        myPageButton.setOnClickListener(v -> {
            Intent intent = new Intent(EmployerMainPage.this, UserMyPage.class);
            startActivity(intent);
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
                companyPicture.setImageBitmap(bitmap);

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

    private void recyclerViewSetting(List<CompanyDto> result) {

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(EmployerMainPage.this, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(new CompanyListAdapter(result, this));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void companiesApi(Long employerId) {
        progressDialog.show();
        Call<List<CompanyDto>> call = Http.getInstance().getApiService()
                .companies(Id.getInstance().getAccessToken(), employerId);
        call.enqueue(new Callback<List<CompanyDto>>() {
            @Override
            public void onResponse(Call<List<CompanyDto>> call, Response<List<CompanyDto>> response) {
                if (response.code() == 401) {
                    Id.getInstance().setAccessToken(response.headers().get("Authorization"));
                    editor.putString("accessToken", response.headers().get("Authorization"));
                    editor.commit();

                    Call<List<CompanyDto>> reCall = Http.getInstance().getApiService()
                            .companies(Id.getInstance().getAccessToken(), employerId);
                    reCall.enqueue(new Callback<List<CompanyDto>>() {
                        @Override
                        public void onResponse(Call<List<CompanyDto>> call, Response<List<CompanyDto>> response) {
                            if (!response.body().isEmpty()) {
                                recyclerViewSetting(response.body());
                                emptyText.setVisibility(View.GONE);
                                progressDialog.dismiss();
                            } else {
                                recyclerViewSetting(response.body());
                                emptyText.setVisibility(View.VISIBLE);
                                progressDialog.dismiss();
                            }

                        }

                        @Override
                        public void onFailure(Call<List<CompanyDto>> call, Throwable t) {
                            Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (!response.body().isEmpty()) {
                    recyclerViewSetting(response.body());
                    emptyText.setVisibility(View.GONE);
                    progressDialog.dismiss();
                } else {
                    recyclerViewSetting(response.body());
                    emptyText.setVisibility(View.VISIBLE);
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<List<CompanyDto>> call, Throwable t) {
                System.out.println("t.getMessage() = " + t.getMessage());
                t.getStackTrace();
                Toast.makeText(EmployerMainPage.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addressSearch() {

        Button addressSearch = findViewById(R.id.address_search);
        addressSearch.setOnClickListener(v -> {
            progressDialog.show();
            Intent i = new Intent(EmployerMainPage.this, AddressSearchActivity.class);
            startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
            progressDialog.dismiss();
        });

    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
