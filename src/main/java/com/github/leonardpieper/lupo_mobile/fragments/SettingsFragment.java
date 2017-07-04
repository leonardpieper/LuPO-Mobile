package com.github.leonardpieper.lupo_mobile.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.leonardpieper.lupo_mobile.R;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private View view;

    private Database lupoDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.content_settings, container, false);

        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        return view;


    }

    /**
     * Öffnet den Android-"Explorer".
     * Es sind nur Dateien des Typs "application" erlaubt auszuwähjava.lang.Stringlen.
     */
    public void openFilePicker() {
        Intent intent;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/*");
        }else{
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/*");
        }


        if(EasyPermissions.hasPermissions(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startActivityForResult(intent, 42);
        }else {
        EasyPermissions.requestPermissions(this, "Diese App benötigt Zugriff auf deine Dateien", 50 ,Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 42 && resultCode == Activity.RESULT_OK){
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
//                lupoDatabasePath = uri;
                Log.i(TAG, "Uri: " + uri.toString());




                    try {
                        //Diese Konvertierung ist nötig, da man von dem Storage Access Framework
                        //nur eine Content-uri zurück bekommt.
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                        byte[] buffer = new byte[inputStream.available()];
                        inputStream.read(buffer);
                        File file = new File(getActivity().getFilesDir(), new File(uri.getPath()).getName());
                        OutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(buffer);
                        outputStream.flush();

//                        lupoDatabase = DatabaseBuilder.open(file);
                        DatabaseBuilder dbb = new DatabaseBuilder();
                        dbb.setAutoSync(true);
                        lupoDatabase = dbb.open(file);
                        if (lupoDatabase != null) {
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                                    getResources().getString(R.string.prefs_key),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("dbFileName", file.getName());
                            editor.commit();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==50){
            openFilePicker();
        }

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}
