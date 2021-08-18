package com.example.albaayo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import lombok.SneakyThrows;

public class NoticeImageFull extends AppCompatActivity {

    @SneakyThrows
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_image_full);

        ImageView imageView = findViewById(R.id.notice_full_imageView);
        Intent intent = getIntent();
        String path = intent.getStringExtra("image");

        ExifInterface exif = new ExifInterface(path);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap image = EmployerMainPage.rotateBitmap(bitmap, orientation);
        imageView.setImageBitmap(image);
    }
}
