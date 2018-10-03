package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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

//    Variabeln für Anzeige (alles was View betrifft)
    ImageView imageFiltered;
    ImageView imageView;
    Button capturePictureButton;
    Button logResultsButton;
    Bitmap capturedImage;
    private Context context = this;

//    ToDo: weitere Lifecycle Hooks ansprechen
//    ToDo: Problem: wenn Bildschirmgedreht, App wird zurückgesetzt
//    ToDo: Klassen machen --> CleanCode allgemein
//    ToDo: Testing!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addListenerOnCapturePictureButton();
        addListenerOnLogResultsButton();
    }

    // Button zum Foto schiessen (nicht zwingend nötig, aber nice für den Flow)
    public void addListenerOnCapturePictureButton() {
        capturePictureButton = findViewById(R.id.fotoButton);
        capturePictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                captureImage();
            }
        });
    }

    // Foto aufnehmen (mit Camera-App):
    private void captureImage() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImage.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(captureImage, REQUEST_IMAGE_CAPTURE);
            File image = null;
            try {
                image = createUniqueImageName();
            } catch (IOException ex) {
                // Error handling
                System.out.print("Error: Kein Foto gefunden!");
            }

            if (image != null) {
                Uri imageURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        image);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                startActivityForResult(captureImage, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //  Foto mit unique Namen abspeichern
    private File createUniqueImageName() throws IOException {
//      Foto-Namen generieren (mit Timestamp)
        String time = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        String imageName = "Appquest_" + time + "_";
        File directory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageName,".jpg", directory);

//      Abspeichern des Fotos
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    //    Foto von Kamera zurückbekommen und anzeigen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageView = findViewById(R.id.meineFotoView);
        imageFiltered = findViewById(R.id.gefiltertesFotoView);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File tempImage = addImageToGallery();
            capturedImage = readSavedImage(Uri.fromFile(tempImage));
            imageView.setImageBitmap(capturedImage);

            // Filter anwenden und Foto in ImageView unter Foto anzeigen
            Bitmap bitmapFiltered = editBitmap(capturedImage);
            imageFiltered.setImageBitmap(bitmapFiltered);
        }
    }

    // Foto in Gallerie speichern (Gallerie der App)
    private File addImageToGallery() {
        Intent scannedTempImage = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File tempImageFromPath = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(tempImageFromPath);
        scannedTempImage.setData(contentUri);
        this.sendBroadcast(scannedTempImage);
        return tempImageFromPath;
    }

    // Hilfsfunktion zum Einlesen des Bildes: Merci an AppQuest!
    private Bitmap readSavedImage(Uri imageUri) {
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
    private Bitmap editBitmap(Bitmap bitmap) {
        // Macht bitmap veränderbar; sonst Error bei SetPixels()
        bitmap = bitmap.copy( Bitmap.Config.ARGB_8888 , true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];

        bitmap.getPixels(data, 0, width, 0, 0, width, height);

        // ToDO: Filter verbessern

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

//  Button um Lösung ins Logbuch eintragen
    public void addListenerOnLogResultsButton() {
        logResultsButton = findViewById(R.id.logBuchEintrag);
        logResultsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final AlertDialog.Builder inputAlert = new AlertDialog.Builder(context);
                inputAlert.setTitle("Lösungswort eintragen");
                inputAlert.setMessage("Bitte Lösung eintragen:");
                final EditText userInput = new EditText(context);
                inputAlert.setView(userInput);
                inputAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String solution = userInput.getText().toString();
                        log(solution);
                    }
                });
                inputAlert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = inputAlert.create();
                alertDialog.show();
            }
        });
    }

//    Logbuch Eintrag erfassen:
    private void log(String solution) {
        Intent intent = new Intent("ch.appquest.intent.LOG");

        if (getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return;
        }

        // ToDo: als Funktion abkapseln
        // Lösungsword eintragen
        JSONObject solutionJSON = new JSONObject();
        try {
            solutionJSON.put("task", "Dechiffrierer");
            solutionJSON.put("solution", solution);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("ch.appquest.logmessage", solutionJSON.toString());

        startActivity(intent);
    }

}
