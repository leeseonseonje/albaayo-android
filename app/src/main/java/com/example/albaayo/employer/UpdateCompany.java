package com.example.albaayo.employer;

import android.content.ContentResolver;
import android.content.Context;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.example.albaayo.EmployerMainPage.rotateBitmap;

public class UpdateCompany extends AppCompatActivity {

    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;

    private Long companyId;
    private SharedPreferences sf;
    private SharedPreferences.Editor editor;

    private EditText companyName, address, companyAddress, companyNumber;
    private Button addressSearch, imageButton, createButton;
    private ImageView companyImage;

    private String absolutePath;
    private byte[] imageBytes;

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
    }

    private void initData() {
        sf = getSharedPreferences("sFile", MODE_PRIVATE);
        editor = sf.edit();

        Intent intent = getIntent();
        companyId = intent.getLongExtra("companyId", 0);

        companyName = findViewById(R.id.input_company_name);
        address = findViewById(R.id.input_address);
        companyAddress = findViewById(R.id.input_company_address);
        companyNumber = findViewById(R.id.input_company_number);

        addressSearch = findViewById(R.id.address_search);
        imageButton = findViewById(R.id.company_image_button);
        createButton = findViewById(R.id.create_button);

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
