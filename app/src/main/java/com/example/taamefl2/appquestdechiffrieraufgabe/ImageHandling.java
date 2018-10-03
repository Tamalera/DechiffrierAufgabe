package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class ImageHandling {

    private String mCurrentPhotoPath;

    // Saves the image using an unique name (timestamp)
    public File createUniqueImageName(Context context) throws IOException {
        String time = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageName = "Appquest_" + time + "_";
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageName,".jpg", directory);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Saves the image to the internal gallery
    public File addImageToGallery(Context context) {
        Intent scannedTempImage = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File tempImageFromPath = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(tempImageFromPath);
        scannedTempImage.setData(contentUri);
        context.sendBroadcast(scannedTempImage);
        return tempImageFromPath;
    }

    // Reads the previously saved image - Merci @ AppQuest!
    public Bitmap readSavedImage(Uri imageUri) {
        File file = new File(imageUri.getPath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e("DECODER", "Error: Bild nicht gefunden", e);
            return null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("ERROR","Error: Kein Foto eingelesen");
                }
            }
        }
    }

    // Takes the captured image and edits it to get it readable - Merci @ AppQuest!
    public Bitmap editBitmap(Bitmap bitmap) {
        // Copies original bitmap in order to make it mutable - else error in setPixels()
        bitmap = bitmap.copy( Bitmap.Config.ARGB_8888 , true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];

        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        for (int i = 0; i < data.length; i++){
            int alpha = (data[i]>>24) & 0x000000;
            int red = (data[i]>>16) & 0x00ffff;
            int green = (data[i]>>8) & 0xff0000;
            int blue = data[i] & 0xff0000;

            data[i] = (alpha<<24) | (red<<16) | (green<<8) | blue;
        }
        bitmap.setPixels(data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }
}
