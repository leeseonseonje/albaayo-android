package com.example.http.dto;

import android.graphics.Bitmap;
import android.net.Uri;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {

    private Bitmap image;
    private String text;
    private String path;
}
