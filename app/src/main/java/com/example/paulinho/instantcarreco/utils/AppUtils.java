package com.example.paulinho.instantcarreco.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class AppUtils {



    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 90, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0,      decodedByte.length);
    }

    public static String decodeModel(List<String> elements){
        StringBuilder model = new StringBuilder();
        for(int i=1;i<elements.size()-1;i++) {
            model.append(elements.get(i)).append(" ");
        }
        return String.valueOf(model);
    }
}
