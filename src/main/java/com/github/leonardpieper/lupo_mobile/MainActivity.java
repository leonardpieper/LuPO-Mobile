package com.github.leonardpieper.lupo_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tableLayout = (TableLayout)findViewById(R.id.tableLayoutMain);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 42 && resultCode == Activity.RESULT_OK){
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());



                if(EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    try {
                        //Diese Konvertierung ist nötig, da man von dem Storage Access Framework
                        //nur eine Content-uri zurück bekommt.
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] buffer = new byte[inputStream.available()];
                        inputStream.read(buffer);
                        File file = new File(this.getCacheDir(), new File(uri.getPath()).getName());
                        OutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(buffer);
                        outputStream.flush();


                        Database db = DatabaseBuilder.open(file);
                        Table table = db.getTable("ABP_SchuelerFaecher");
                        for (Row row : table) {
//                            Log.d(TAG, "Look ma, a row: " + row.toString());
//                            for(Column column : table.getColumns()){
                            Column fachKrz = table.getColumn("FachKrz");
                            Column kursartE1 = table.getColumn("Kursart_E1");
                            Column kursartE2 = table.getColumn("Kursart_E2");
                            Column kursartQ1 = table.getColumn("Kursart_Q1");
                            Column kursartQ2 = table.getColumn("Kursart_Q2");
                            Column kursartQ3 = table.getColumn("Kursart_Q3");
                            Column kursartQ4 = table.getColumn("Kursart_Q4");

                            Object valueFackKrz = row.get(fachKrz.getName());
                            Object valueKursartE1 = row.get(kursartE1.getName());
                            Object valueKursartE2 = row.get(kursartE2.getName());
                            Object valueKursartQ1 = row.get(kursartQ1.getName());
                            Object valueKursartQ2 = row.get(kursartQ2.getName());
                            Object valueKursartQ3 = row.get(kursartQ3.getName());
                            Object valueKursartQ4 = row.get(kursartQ4.getName());

                            String sValueFackKrz = Objects.toString(valueFackKrz, "");
                            String sValueKursartE1 = Objects.toString(valueKursartE1, "");
                            String sValueKursartE2 = Objects.toString(valueKursartE2, "");
                            String sValueKursartQ1 = Objects.toString(valueKursartQ1, "");
                            String sValueKursartQ2 = Objects.toString(valueKursartQ2, "");
                            String sValueKursartQ3 = Objects.toString(valueKursartQ3, "");
                            String sValueKursartQ4 = Objects.toString(valueKursartQ4, "");

                            addRow(sValueFackKrz, sValueKursartE1, sValueKursartE2,
                                    sValueKursartQ1, sValueKursartQ2, sValueKursartQ3,
                                    sValueKursartQ4);
//                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    EasyPermissions.requestPermissions(this, "Diese App benötigt Zugriff auf deine Dateien", 41 ,Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        }
    }

    public void openFilePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");

        startActivityForResult(intent, 42);
    }


    private void addRow(String fachKrz, String kursart_E1, String kursart_E2, String kursart_Q1,
                        String kursart_Q2, String kursart_Q3, String kursart_Q4){

        TableRow tableRow = new TableRow(this);
        TextView tvFachKrz = new TextView(this);
        TextView tvKursart_E1 = new TextView(this);
        TextView tvKursart_E2 = new TextView(this);
        TextView tvKursart_Q1 = new TextView(this);
        TextView tvKursart_Q2 = new TextView(this);
        TextView tvKursart_Q3 = new TextView(this);
        TextView tvKursart_Q4 = new TextView(this);

        TableRow.LayoutParams trParams = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        tvFachKrz.setLayoutParams(trParams);
        tvKursart_E1.setLayoutParams(trParams);
        tvKursart_E2.setLayoutParams(trParams);
        tvKursart_Q1.setLayoutParams(trParams);
        tvKursart_Q2.setLayoutParams(trParams);
        tvKursart_Q3.setLayoutParams(trParams);
        tvKursart_Q4.setLayoutParams(trParams);

        tvFachKrz.setText(fachKrz);
        tvKursart_E1.setText(kursart_E1);
        tvKursart_E2.setText(kursart_E2);
        tvKursart_Q1.setText(kursart_Q1);
        tvKursart_Q2.setText(kursart_Q2);
        tvKursart_Q3.setText(kursart_Q3);
        tvKursart_Q4.setText(kursart_Q4);

        tvFachKrz.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_E1.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_E2.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_Q1.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_Q2.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_Q3.setBackgroundResource(R.drawable.cell_shape);
        tvKursart_Q4.setBackgroundResource(R.drawable.cell_shape);

        tableRow.addView(tvFachKrz);
        tableRow.addView(tvKursart_E1);
        tableRow.addView(tvKursart_E2);
        tableRow.addView(tvKursart_Q1);
        tableRow.addView(tvKursart_Q2);
        tableRow.addView(tvKursart_Q3);
        tableRow.addView(tvKursart_Q4);

        tableLayout.addView(tableRow);

    }
}
