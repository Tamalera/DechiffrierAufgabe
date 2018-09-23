package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    ImageView meineFotoView;

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addListenerOnButton();
    }
//  Button zum Foto schiessen
    public void addListenerOnButton() {

        button = findViewById(R.id.fotoButton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                fotoAufnehmenIntent();
            }
        });
    }

//    Foto aufnehmen:
    private void fotoAufnehmenIntent() {
        Intent nimmFotoAuf = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (nimmFotoAuf.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(nimmFotoAuf, REQUEST_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = uniqueFotoNamen();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.print("Error: Keine Datei!");
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                nimmFotoAuf.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(nimmFotoAuf, REQUEST_TAKE_PHOTO);
            }
        }
    }

//    Foto von Kamera zurückbekommen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        meineFotoView = findViewById(R.id.meineFotoView);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File aufgenommenesFoto = fotoZuGallerieHinzufuegen();
            Bitmap meinFoto = fotoEinlesen(Uri.fromFile(aufgenommenesFoto));
            meineFotoView.setImageBitmap(meinFoto);
        }
    }

//  Foto mit unique Namen abspeichern
    private File uniqueFotoNamen() throws IOException {
//      Foto-Namen generieren (mit Timestamp)
        String zeit = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String fotoNamen = "Appquest_" + zeit + "_";
        File speicherOrt = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fotoNamen,".jpg", speicherOrt);

//        Abspeichern des Fotos
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

//    Foto in Gallerie speichern
    private File fotoZuGallerieHinzufuegen() {
        Intent meinFoto = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        meinFoto.setData(contentUri);
        this.sendBroadcast(meinFoto);
        return f;
    }

//    Hilfsfunktion zum Einlesen des Bildes
    private Bitmap fotoEinlesen(Uri imageUri) {
        File file = new File(imageUri.getPath());
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e("DECODER", "Could not find image file", e);
            return null;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.print("Error: Kein Foto eingelesen");
                }
            }
        }
    }
}
