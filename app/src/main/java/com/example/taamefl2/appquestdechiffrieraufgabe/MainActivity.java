package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Variables
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private Context context = this;
    ImageView imageView;
    ImageView imageFiltered;
    Bitmap capturedImage;
    Button captureImageButton;
    Button logResultsButton;
    LogbookHandling logbookHandling = new LogbookHandling();
    ImageHandling imageHandling = new ImageHandling();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Add eventlisteners
        addListenerOnCaptureImageButton();
        addListenerOnLogResultsButton();
    }

    public void addListenerOnCaptureImageButton() {
        captureImageButton = findViewById(R.id.captureImageButton);
        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                captureAndProcessImage();
            }
        });
    }

    public void addListenerOnLogResultsButton() {
        logResultsButton = findViewById(R.id.logbookEntryButton);
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
                        boolean logbookInstalled = logbookHandling.checkIfLogbookInstalled(context);
                        if (logbookInstalled) {
                            logbookHandling.passDataToLogbook(context, solution);
                        }
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

    // Takes an image using the internal camera app
    private void captureAndProcessImage() {
        Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureImage.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(captureImage, REQUEST_IMAGE_CAPTURE);
            File image = null;
            try {
                image = imageHandling.createUniqueImageName(context);
            } catch (IOException ex) {
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

    // Gets the image from the camera and display it after being edited
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        imageView = findViewById(R.id.displayImageView);
        imageFiltered = findViewById(R.id.displayFilteredImageView);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            File tempImage = imageHandling.addImageToGallery(context);
            capturedImage = imageHandling.readSavedImage(Uri.fromFile(tempImage));
            imageView.setImageBitmap(capturedImage);

            Bitmap bitmapFiltered = imageHandling.editBitmap(capturedImage);
            imageFiltered.setImageBitmap(bitmapFiltered);
        }
    }

}
