package com.example.languagetranslatorapp;


import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdit;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translatedTV;

    private Button btn;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Toast.makeText(MainActivity.this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.share:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this application");
                intent.putExtra(Intent.EXTRA_TEXT, "App is Here");
                startActivity(Intent.createChooser(intent, "Share"));
                return super.onOptionsItemSelected(item);
            case R.id.exit:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urudu", "Korean", "Tamil", "Spanish", "Japanese", "French", "Kannada", "Telugu", "Polish", "Marathi", "Dutch", "Thai", "Filipino", "Georgian", "Italian", "Turkish", "Swedish"};

    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Hindi", "Urudu", "Korean", "Tamil", "Spanish", "Japanese", "French", "Kannada", "Telugu", "Polish", "Marathi", "Dutch", "Thai", "Filipino", "Georgian", "Italian", "Turkish", "Swedish"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode, tolanguageCode = 0;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdit = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);
        Button btn = findViewById(R.id.btn);


        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                fromLanguageCode = getLanguageCode(fromLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                tolanguageCode = getLanguageCode(toLanguages[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translatedTV.setText("");
                if (sourceEdit.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter your text to translate", Toast.LENGTH_SHORT).show();
                } else if (fromLanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select source language", Toast.LENGTH_SHORT).show();
                } else if (tolanguageCode == 0) {
                    Toast.makeText(MainActivity.this, "Please select the language to make translation", Toast.LENGTH_SHORT).show();
                } else {
                    translateText(fromLanguageCode, tolanguageCode, sourceEdit.getText().toString());
                }
            }
        });


        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra((RecognizerIntent.EXTRA_LANGUAGE_MODEL), RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "SPEAK TO CONVERT INTO TEXT");
                try {
                    startActivityForResult(i, REQUEST_PERMISSION_CODE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdit.setText(result.get(0));
            }
        }
    }

    private void translateText(int fromLanguageCode, int tolanguageCode, String source) {
        translatedTV.setText("Translating..");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {

            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to Translate:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to download language Model" + e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    public int getLanguageCode(String language) {

        int languageCode = 0;
        switch (language) {
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Urudu":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Polish":
                languageCode = FirebaseTranslateLanguage.PL;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            case "Dutch":
                languageCode = FirebaseTranslateLanguage.NL;
                break;
            case "Thai":
                languageCode = FirebaseTranslateLanguage.TH;
                break;
            case "Filipino":
                languageCode = FirebaseTranslateLanguage.FI;
                break;
            case "Georgian":
                languageCode = FirebaseTranslateLanguage.KA;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Swedish":
                languageCode = FirebaseTranslateLanguage.SV;
                break;

            default:
                languageCode = 0;
        }
        return languageCode;


    }
}