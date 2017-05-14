package com.github.leonardpieper.lupo_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.impl.ByteUtil;
import com.healthmarketscience.jackcess.impl.DatabaseImpl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private Database lupoDatabase;

    private Uri lupoDatabasePath;

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
                openAddKursDialog();
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

        try {
            openLupoDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                lupoDatabasePath = uri;
                Log.i(TAG, "Uri: " + uri.toString());



                if(EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    try {
                        //Diese Konvertierung ist nötig, da man von dem Storage Access Framework
                        //nur eine Content-uri zurück bekommt.
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        byte[] buffer = new byte[inputStream.available()];
                        inputStream.read(buffer);
                        File file = new File(this.getFilesDir(), new File(uri.getPath()).getName());
                        OutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(buffer);
                        outputStream.flush();

//                        lupoDatabase = DatabaseBuilder.open(file);
                        DatabaseBuilder dbb = new DatabaseBuilder();
                        dbb.setAutoSync(true);
                        lupoDatabase = dbb.open(file);
                        if(lupoDatabase!=null){
                            SharedPreferences sharedPreferences = getSharedPreferences(
                                    getResources().getString(R.string.prefs_key),
                                    Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("dbFileName", file.getName());
                            editor.commit();
                        }


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

    /**
     * Öffnet den Android-"Explorer".
     * Es sind nur Dateien des Typs "application" erlaubt auszuwählen.
     * @param view View ist der Android-Explorer
     */
    public void openFilePicker(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");

        startActivityForResult(intent, 42);
    }


    /**
     * Fügt eine Reihe in das tableLayout ein.
     * @param fachKrz Fachkrz ist die Kurzform des Faches
     * @param kursart_E1 Kursart_E1 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param kursart_E2 Kursart_E2 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param kursart_Q1 Kursart_Q1 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param kursart_Q2 Kursart_Q2 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param kursart_Q3 Kursart_Q3 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param kursart_Q4 Kursart_Q4 beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     */
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

        tvFachKrz.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_E1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_E2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_Q1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_Q2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_Q3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvKursart_Q4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);


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

    /**
     * Öffnet einen Dialog, indem man ein neues Fach in seine Laufbahn eintragen kann.
     * Unterschieden wird hier zwischen mündlich, schriftlich und LK.
     */
    private void openAddKursDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.dialog_add_kurs);
        builder.setTitle("Fach hinzufügen");
        final AlertDialog dialog = builder.create();
        dialog.show();



        List<String> faecher = new ArrayList<>();

        try {
            Table table = lupoDatabase.getTable("ABP_Faecher");
            for(Row row : table){
                Column colFachKrzl = table.getColumn("FachKrz");
                Column colBezeichnung = table.getColumn("Bezeichnung");

                Object fachKrzl = row.get(colFachKrzl.getName());
                Object bezeichnung = row.get(colBezeichnung.getName());

                String sBezeichnung = Objects.toString(bezeichnung, "");
                faecher.add(sBezeichnung);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Spinner kursSpinner = (Spinner) ((AlertDialog)dialog).findViewById(R.id.spinner_Kurse);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, faecher);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kursSpinner.setAdapter(spinnerArrayAdapter);

        final RadioGroup grpe1 = (RadioGroup)dialog.findViewById(R.id.radioGrpEF1);
        final RadioGroup grpe2 = (RadioGroup)dialog.findViewById(R.id.radioGrpEF2);
        final RadioGroup grpq1 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ1);
        final RadioGroup grpq2 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ2);
        final RadioGroup grpq3 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ3);
        final RadioGroup grpq4 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ4);

        CheckBox isLK = (CheckBox)dialog.findViewById(R.id.checkBoxIsLK);
        isLK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){

                for(int i = 0; i< grpq1.getChildCount(); i++){
                    grpq1.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq2.getChildCount(); i++){
                    grpq2.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq3.getChildCount(); i++){
                    grpq3.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq4.getChildCount(); i++){
                    grpq4.getChildAt(i).setEnabled(!isChecked);
                }
//                }else {

//                }
            }
        });



        Button finishBtn = (Button) dialog.findViewById(R.id.btn_add_kurs_finish);
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selE1Id = grpe1.getCheckedRadioButtonId();
                int selE2Id = grpe2.getCheckedRadioButtonId();
                int selQ1Id = grpq1.getCheckedRadioButtonId();
                int selQ2Id = grpq2.getCheckedRadioButtonId();
                int selQ3Id = grpq3.getCheckedRadioButtonId();
                int selQ4Id = grpq4.getCheckedRadioButtonId();

                RadioButton rbE1 = (RadioButton)dialog.findViewById(selE1Id);
                RadioButton rbE2 = (RadioButton)dialog.findViewById(selE2Id);
                RadioButton rbQ1 = (RadioButton)dialog.findViewById(selQ1Id);
                RadioButton rbQ2 = (RadioButton)dialog.findViewById(selQ2Id);
                RadioButton rbQ3 = (RadioButton)dialog.findViewById(selQ3Id);
                RadioButton rbQ4 = (RadioButton)dialog.findViewById(selQ4Id);

                String e1Type = "";
                String e2Type = "";
                String q1Type = "";
                String q2Type = "";
                String q3Type = "";
                String q4Type = "";

                if(rbE1!=null){
                    e1Type = (String)rbE1.getTag();
                }
                if(rbE2!=null){
                    e2Type = (String)rbE2.getTag();
                }
                if(rbQ1!=null){
                    q1Type = (String)rbQ1.getTag();
                }
                if(rbQ2!=null){
                    q2Type = (String)rbQ2.getTag();
                }
                if(rbQ3!=null){
                    q3Type = (String)rbQ3.getTag();
                }if(rbQ4!=null){
                    q4Type = (String)rbQ4.getTag();
                }


                String fach = kursSpinner.getItemAtPosition(kursSpinner.getSelectedItemPosition()).toString();
                addKurs(fach, e1Type, e2Type, q1Type, q2Type, q3Type, q4Type);
            }
        });
    }

    /**
     * Fügt einen Kurs in die Datenbank ein.
     * @param fach Fach ist der Fachname
     * @param e1Type E1Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param e2Type E2Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param q1Type Q1Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param q2Type Q2Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param q3Type Q3Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     * @param q4Type Q4Type beschreibt, ob das Fach mündl.-, schriftl.-, als LK oder ZK gewählt wurde.
     */
    private void addKurs(String fach, String e1Type, String e2Type,
                         String q1Type, String q2Type,
                         String q3Type, String q4Type){

        Log.d(TAG, "Fach: " + fach + "EF.1: " + e1Type + "EF.2: " + e2Type
                + "Q1.1: " + q1Type + "Q1.2: " + q2Type
                + "Q2.1: " + q3Type + "Q2.2: " + q4Type);

        try {
            String fachKrz = "";
            Table fachTable = lupoDatabase.getTable("ABP_Faecher");
            Cursor fachCursor = CursorBuilder.createCursor(fachTable);
            boolean fachFound = fachCursor.findFirstRow(Collections.singletonMap("Bezeichnung", fach));
            if(fachFound){
                fachKrz = Objects.toString(fachCursor.getCurrentRowValue(fachTable.getColumn("FachKrz")), "");
            }

            Table table = lupoDatabase.getTable("ABP_SchuelerFaecher");
            Cursor cursor = CursorBuilder.createCursor(table);
            boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachKrz));
            if(found){
                Row row = cursor.getCurrentRow();
                row.put("Kursart_E1", e1Type);
                row.put("Kursart_E2", e2Type);
                row.put("Kursart_Q1", q1Type);
                row.put("Kursart_Q2", q2Type);
                row.put("Kursart_Q3", q3Type);
                row.put("Kursart_Q4", q4Type);

                table.updateRow(row);
                refreshUI();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Öffnet die Datenbank, wenn eine über den Button ausgewählt wurde.
     * Der Pfad zu der Datenank wird in "dbFileName" gespeichert.
     * Setzt die Datenbank auf AutoSync --> Daten werden bei jedem Schreibvorgang gespeichert.
     * @throws IOException Wenn keine Datenbankdatei gefunden wurde wird eine IOException zurückgegeben.
     */
    private void openLupoDatabase() throws IOException {
        SharedPreferences sharedPreferences = getSharedPreferences(
                getResources().getString(R.string.prefs_key),
                Context.MODE_PRIVATE);
        String fileName = sharedPreferences.getString("dbFileName", null);
        if(fileName!=null){
            File file = new File(this.getFilesDir(), fileName);

            DatabaseBuilder dbb = new DatabaseBuilder();
            dbb.setAutoSync(true);
            lupoDatabase = dbb.open(file);

            refreshUI();
        }
    }


    /**
     * Synchronisiert die lupoDatabase mit dem TableLayout
     * @throws IOException Wenn die Tabelle "ABP_SchuelerFaecher" nicht
     * in der Datenbank "lupoDatabase" gefunden wurde wird eine IOExpection zurückgegeben.
     */
    private void refreshUI() throws IOException {
        tableLayout.removeAllViews();
        Table table = lupoDatabase.getTable("ABP_SchuelerFaecher");
        for (Row row : table) {
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
        }
    }
}
