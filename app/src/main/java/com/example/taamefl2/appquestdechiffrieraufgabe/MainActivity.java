package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

//    Variabeln für Foto aufnehmen
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

//    Variabeln für anzeige (alles was View betrifft)
    ImageView gefiltertesFoto;
    ImageView meineFotoView;
    Button button;

//    Das Foto wird in Variable meinFoto gespeichert
    Bitmap meinFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addListenerOnButton();
    }

    // Button zum Foto schiessen (nicht zwingend nötig, aber nice für den Flow)
    public void addListenerOnButton() {
        button = findViewById(R.id.fotoButton);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                fotoAufnehmenIntent();
            }
        });
    }

    // Foto aufnehmen (mit Camera-App):
    private void fotoAufnehmenIntent() {
        Intent nimmFotoAuf = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (nimmFotoAuf.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(nimmFotoAuf, REQUEST_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = uniqueFotoNamen();
            } catch (IOException ex) {
                // Error handling
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

    //    Foto von Kamera zurückbekommen und anzeigen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        meineFotoView = findViewById(R.id.meineFotoView);
        gefiltertesFoto = findViewById(R.id.gefiltertesFotoView);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File aufgenommenesFoto = fotoZuGallerieHinzufuegen();
            meinFoto = fotoEinlesen(Uri.fromFile(aufgenommenesFoto));
            meineFotoView.setImageBitmap(meinFoto);

            // Filter anwenden und Foto in ImageView unter Foto anzeigen
            Bitmap meinGefiltertesFoto = bildBearbeiten(meinFoto);
            gefiltertesFoto.setImageBitmap(meinGefiltertesFoto);
        }
    }

    // Foto in Gallerie speichern (Gallerie der App)
    private File fotoZuGallerieHinzufuegen() {
        Intent meinFoto = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        meinFoto.setData(contentUri);
        this.sendBroadcast(meinFoto);
        return f;
    }

    // Hilfsfunktion zum Einlesen des Bildes: Merci an AppQuest!
    private Bitmap fotoEinlesen(Uri imageUri) {
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

//    Foto zum Bearbeiten bereitstellen (damit Pixel bearbeitet weren können) Merci an AppQuest!
    private Bitmap bildBearbeiten(Bitmap bitmap) {
        // Macht bitmap veränderbar; sonst Error bei SetPixels()
        bitmap = bitmap.copy( Bitmap.Config.ARGB_8888 , true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];

        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        // ToDO: Hier können die Pixel im data-array bearbeitet und
        // ToDo: anschliessend damit ein neues Bitmap erstellt werden

        for (int i = 0; i < data.length; i++){
            int alpha = (data[i]>>24) & 0xff0000;
            int rot = (data[i]>>16) & 0xff0000;
            int gruen = (data[i]>>8) & 0xff0000;
            int blau = data[i] & 0xff0000;
            
            data[i] = (alpha<<24) | (rot<<16) | (gruen<<8) | blau;
        }
        bitmap.setPixels(data, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
        return bitmap;
    }
}
