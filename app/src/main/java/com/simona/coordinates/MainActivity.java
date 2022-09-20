package com.simona.coordinates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText inputEditText;
    Button convertBtn, exportBtn, pinOnTheMapBtn;
    TextView showDMStextView;

    String decimalCoordinates, decimalLatiString, decimalLongiString, dmsCoordinates, latitudeEmisphere, longitugeEmisphere;
    double deciLatiDouble, deciLongiDouble;
    int id;

    public static final int WRITING_PERMISSION_CODE = 100;
    public static final String LATI = "latitude";
    public static final String LONGI = "longitude";
    public static final String SHARED_PREF = "sp";
    public static final String ID_VALUE = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    void convertDecimalCoordinatesFromStringToDouble() {
        // check whether the user input the coordinates, using a correct format
        decimalCoordinates = inputEditText.getText().toString().trim();

        decimalLatiString = decimalCoordinates.substring(0, decimalCoordinates.indexOf(" ")).trim().toUpperCase();
        if (decimalLatiString.startsWith("N") || decimalLatiString.startsWith("S")
                || decimalLatiString.startsWith("n") || decimalLatiString.startsWith("s")) {
            decimalLatiString = decimalLatiString.substring(1) + decimalLatiString.substring(0, 1).toUpperCase();
        }

        decimalLongiString = decimalCoordinates.substring(decimalCoordinates.indexOf(" ") + 1).trim().toUpperCase();
        if (decimalLongiString.startsWith("E") || decimalLongiString.startsWith("W") ||
                decimalLongiString.startsWith("e") || decimalLongiString.startsWith("w")) {
            decimalLongiString = decimalLongiString.substring(1) + decimalLongiString.substring(0, 1).toUpperCase();
        }

        String latitude = decimalLatiString.substring(0, decimalLatiString.length() - 1);
        if (decimalLatiString.endsWith("N")) {
            deciLatiDouble = Double.parseDouble(latitude);
            latitudeEmisphere = "N";
        } else {
            deciLatiDouble = -Double.parseDouble(latitude);
            latitudeEmisphere = "S";
        }

        String longitude = decimalLongiString.substring(0, decimalLongiString.length() - 1);
        if (decimalLongiString.endsWith("E")) {
            deciLongiDouble = Double.parseDouble(longitude);
            longitugeEmisphere = "E";
        } else {
            deciLongiDouble = -Double.parseDouble(longitude);
            longitugeEmisphere = "W";
        }

    }

    void convertCoordinatesFromDecimalToDMS() {
        StringBuilder stringBuilderLati = new StringBuilder();

        String latiDegree = Location.convert(Math.abs(deciLatiDouble), Location.FORMAT_SECONDS);
        String[] latiSplit = latiDegree.split(":");
        stringBuilderLati.append(latiSplit[0]);
        stringBuilderLati.append("°");
        stringBuilderLati.append(latiSplit[1]);
        stringBuilderLati.append("'");
        stringBuilderLati.append(latiSplit[2]);
        stringBuilderLati.append("\"");
        if (deciLatiDouble < 0) {
            stringBuilderLati.append("S");
        } else if (deciLongiDouble > 0) {
            stringBuilderLati.append("N");
        } else {
            stringBuilderLati.append(latitudeEmisphere);
        }

        stringBuilderLati.append(" ");

        String longiDegree = Location.convert(Math.abs(deciLongiDouble), Location.FORMAT_SECONDS);
        String[] longiSplit = longiDegree.split(":");
        stringBuilderLati.append(longiSplit[0]);
        stringBuilderLati.append("°");
        stringBuilderLati.append(longiSplit[1]);
        stringBuilderLati.append("'");
        stringBuilderLati.append(longiSplit[2]);
        stringBuilderLati.append("\"");
        if (deciLongiDouble < 0) {
            stringBuilderLati.append("W");
        } else if (deciLongiDouble > 0) {
            stringBuilderLati.append("E");
        } else {
            stringBuilderLati.append(longitugeEmisphere);
        }
        dmsCoordinates = stringBuilderLati.toString();

        showDMStextView.setText(dmsCoordinates);
    }

    void exportToCsvFile() {
        rememberIdCurrentValue();
        String stringToBeSaved = id + "," + decimalLatiString + " " + decimalLongiString + "," + dmsCoordinates + "\n";
        String FILENAME = "allCoordinates.csv";

        try {
            File pathToFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath(), FILENAME);
            pathToFile.mkdirs();

            FileOutputStream exportToFile = openFileOutput(FILENAME,
                    Context.MODE_APPEND);
            exportToFile.write(stringToBeSaved.getBytes());
            exportToFile.close();

            saveIdNewValue();
            inputEditText.setText(null);
            inputEditText.requestFocus();
            showDMStextView.setText(null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(MainActivity.this, "Export successfully completed", Toast.LENGTH_SHORT).show();
    }

    void shareCsvFileToOtherApps() {
        String csvFilePath = "/data/data/com.simona.coordinates/allCoordinates.csv";
        File file = new File(csvFilePath);
        Uri uri = Uri.parse(file.getPath());

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Please chose whom you'd like to share the file allCoordinates.csv to:"));
    }

    void initViews() {
        inputEditText = findViewById(R.id.inputCoordinatesEditText);
        convertBtn = findViewById(R.id.convertBtn);
        exportBtn = findViewById(R.id.exportBtn);
        pinOnTheMapBtn = findViewById(R.id.toTheMapBtn);
        showDMStextView = findViewById(R.id.showDMStextView);

        convertBtn.setOnClickListener(this::onClick);
        exportBtn.setOnClickListener(this::onClick);
        pinOnTheMapBtn.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.convertBtn:
                convertDecimalCoordinatesFromStringToDouble();
                convertCoordinatesFromDecimalToDMS();
                exportBtn.setEnabled(true);
                pinOnTheMapBtn.setEnabled(true);
                hideKb();
                break;

            case R.id.exportBtn:
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    exportToCsvFile();
                    shareCsvFileToOtherApps();
                    exportBtn.setEnabled(false);
                    pinOnTheMapBtn.setEnabled(false);
                } else {
                    requestPermissionToWrite();
                }
                break;

            case R.id.toTheMapBtn:
                Intent toTheMap = new Intent(MainActivity.this, MapsActivity.class);
                toTheMap.putExtra(LATI, deciLatiDouble);
                toTheMap.putExtra(LONGI, deciLongiDouble);
                startActivity(toTheMap);
                break;

            default:
                return;
        }
    }

    void requestPermissionToWrite() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Write permission explanation")
                    .setMessage("You have to allow access to write, in order to be able to save data!")
                    .setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    WRITING_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.create();
            builder.show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITING_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITING_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToCsvFile();
                exportBtn.setEnabled(false);
                pinOnTheMapBtn.setEnabled(false);
            } else {
                Toast.makeText(MainActivity.this, "You denied access to write", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void saveIdNewValue() {
        id++;
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ID_VALUE, id);
        editor.commit();
    }

    void rememberIdCurrentValue() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        id = sharedPreferences.getInt(ID_VALUE, 0);
    }

    void hideKb() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
    }

}