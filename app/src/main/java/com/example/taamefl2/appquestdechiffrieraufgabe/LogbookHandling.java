package com.example.taamefl2.appquestdechiffrieraufgabe;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LogbookHandling {

    private Intent logbookIntent = new Intent("ch.appquest.intent.LOG");

    // Checks if logbook app is installed on the phone
    public boolean checkIfLogbookInstalled(Context context) {
        if (context.getPackageManager().queryIntentActivities(logbookIntent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            Toast.makeText(context, "Logbook App not Installed", Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    // Passes JSON object with solution info to logbook app
    public void passDataToLogbook(Context context, String solution) {
        JSONObject solutionJSON = new JSONObject();
        try {
            solutionJSON.put("task", "Dechiffrierer");
            solutionJSON.put("solution", solution);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        logbookIntent.putExtra("ch.appquest.logmessage", solutionJSON.toString());
        context.startActivity(logbookIntent);
    }
}
