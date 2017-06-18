package com.github.leonardpieper.lupo_mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.leonardpieper.lupo_mobile.PrintLuPO;
import com.github.leonardpieper.lupo_mobile.R;
import com.github.leonardpieper.lupo_mobile.tools.Fehlermeldungen;
import com.github.leonardpieper.lupo_mobile.tools.StundenRechener;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class LuPOFragment extends Fragment {
    private static final String TAG = "LuPOFragment";

    private Database lupoDatabase;

//    private View view;
    private TableLayout tableLayout;

    private boolean onlySelected = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.app_bar_main, container, false);

        tableLayout = (TableLayout) view.findViewById(R.id.tableLayoutMain);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddKursDialog();
            }
        });

        setHasOptionsMenu(true);

        final ScrollView svFacher = (ScrollView)view.findViewById(R.id.main_sv_TableScroll);
        //Wenn man runterscrollt soll der FAB verschwinden
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            svFacher.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    View view = svFacher.getChildAt(svFacher.getChildCount() - 1);
                    int diff = (view.getBottom() - (svFacher.getHeight() + svFacher.getScrollY()));
                    if(scrollY > oldScrollY && scrollY > 0){
                        fab.hide();
                    }else if(diff <= 10){
                        fab.hide();
                    }else {
                        fab.show();
                    }
                }
            });
        }

        //FIXME
        onlySelected = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.prefs_showOnlySelected), false);

        try {
            openLupoDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.action_print){
                    try {
                        printLupo();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_showChecked) {
            item.setChecked(!item.isChecked());
            try {
                refreshUI(item.isChecked());
                onlySelected = item.isChecked();
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(
                        getString(R.string.prefs_key), Context.MODE_PRIVATE).edit();
                editor.putBoolean(getString(R.string.prefs_showOnlySelected), item.isChecked());
                editor.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }else if(id==R.id.action_print){

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Öffnet einen Dialog, indem man ein neues Fach in seine Laufbahn eintragen kann.
     * Unterschieden wird hier zwischen mündlich, schriftlich und LK.
     */
    private void openAddKursDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.dialog_add_kurs);
        builder.setTitle("Fach hinzufügen");
        final AlertDialog dialog = builder.create();
        dialog.show();



        final List<String> faecher = new ArrayList<>();

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
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, faecher);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kursSpinner.setAdapter(spinnerArrayAdapter);

        final TextView tvTitleExtra = (TextView)dialog.findViewById(R.id.tvDialogAddKursExtra);
        final RadioGroup grpe1 = (RadioGroup)dialog.findViewById(R.id.radioGrpEF1);
        final RadioGroup grpe2 = (RadioGroup)dialog.findViewById(R.id.radioGrpEF2);
        final RadioGroup grpq1 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ1);
        final RadioGroup grpq2 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ2);
        final RadioGroup grpq3 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ3);
        final RadioGroup grpq4 = (RadioGroup)dialog.findViewById(R.id.radioGrpQ4);

        final CheckBox isLK = (CheckBox)dialog.findViewById(R.id.checkBoxIsLK);
        final CheckBox isZK = (CheckBox)dialog.findViewById(R.id.checkBoxIsZK);

        final LinearLayout llLK = (LinearLayout)dialog.findViewById(R.id.llIsLK);
        final LinearLayout llZK = (LinearLayout)dialog.findViewById(R.id.llIsZK);



        kursSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(spinnerArrayAdapter.getItem(position));

                //Das Layout wird zurückgesetzt
                llZK.setVisibility(View.GONE);
                isLK.setEnabled(true);
                isLK.setChecked(false);

                tvTitleExtra.setVisibility(View.INVISIBLE);

                for(int i = 0; i< grpq1.getChildCount() -1; i++){
                    grpq1.getChildAt(i).setEnabled(true);
                }
                for(int i = 0; i< grpq2.getChildCount() -1; i++){
                    grpq2.getChildAt(i).setEnabled(true);
                }
                for(int i = 0; i< grpq3.getChildCount() -1; i++){
                    grpq3.getChildAt(i).setEnabled(true);
                }
                for(int i = 0; i< grpq4.getChildCount() -1; i++){
                    grpq4.getChildAt(i).setEnabled(true);
                }
                grpq1.getChildAt(grpq1.getChildCount()-1).setVisibility(View.INVISIBLE);
                grpq2.getChildAt(grpq2.getChildCount()-1).setVisibility(View.INVISIBLE);
                grpq3.getChildAt(grpq3.getChildCount()-1).setVisibility(View.INVISIBLE);
                grpq4.getChildAt(grpq4.getChildCount()-1).setVisibility(View.INVISIBLE);

                if(spinnerArrayAdapter.getItem(position).equals("Geschichte") ||
                        spinnerArrayAdapter.getItem(position).equals("Sozialwissenschaften")){

                    llZK.setVisibility(View.VISIBLE);
                    isZK.setChecked(false);
                }


                try {
                    if(!isLKAvailable(spinnerArrayAdapter.getItem(position))){
                        llLK.setVisibility(View.GONE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        isLK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //GetChildCount()-1 damit der LK-RadioButton nicht ausgegraut wird.
                for(int i = 0; i< grpq1.getChildCount() -1; i++){
                    grpq1.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq2.getChildCount() -1; i++){
                    grpq2.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq3.getChildCount() -1; i++){
                    grpq3.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq4.getChildCount() -1; i++){
                    grpq4.getChildAt(i).setEnabled(!isChecked);
                }

                if(isChecked){
                    grpq1.getChildAt(grpq1.getChildCount()-1).setVisibility(View.VISIBLE);
                    grpq2.getChildAt(grpq2.getChildCount()-1).setVisibility(View.VISIBLE);
                    grpq3.getChildAt(grpq3.getChildCount()-1).setVisibility(View.VISIBLE);
                    grpq4.getChildAt(grpq4.getChildCount()-1).setVisibility(View.VISIBLE);
                    tvTitleExtra.setVisibility(View.VISIBLE);

                    ((RadioButton)grpq1.getChildAt(grpq1.getChildCount()-1)).setChecked(true);
                    ((RadioButton)grpq2.getChildAt(grpq2.getChildCount()-1)).setChecked(true);
                    ((RadioButton)grpq3.getChildAt(grpq3.getChildCount()-1)).setChecked(true);
                    ((RadioButton)grpq4.getChildAt(grpq4.getChildCount()-1)).setChecked(true);

                    tvTitleExtra.setText("LK");

                    isZK.setEnabled(false);
                }else {
                    grpq1.getChildAt(grpq1.getChildCount()-1).setVisibility(View.INVISIBLE);
                    grpq2.getChildAt(grpq2.getChildCount()-1).setVisibility(View.INVISIBLE);
                    grpq3.getChildAt(grpq3.getChildCount()-1).setVisibility(View.INVISIBLE);
                    grpq4.getChildAt(grpq4.getChildCount()-1).setVisibility(View.INVISIBLE);
                    tvTitleExtra.setVisibility(View.INVISIBLE);

                    isZK.setEnabled(true);
                }
            }
        });

        isZK.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //GetChildCount()-1 damit der LK-RadioButton nicht ausgegraut wird.
                for(int i = 0; i< grpq3.getChildCount() -1; i++){
                    grpq3.getChildAt(i).setEnabled(!isChecked);
                }
                for(int i = 0; i< grpq4.getChildCount() -1; i++){
                    grpq4.getChildAt(i).setEnabled(!isChecked);
                }

                if(isChecked) {
                    grpq3.getChildAt(grpq3.getChildCount() - 1).setVisibility(View.VISIBLE);
                    grpq4.getChildAt(grpq4.getChildCount() - 1).setVisibility(View.VISIBLE);
                    tvTitleExtra.setVisibility(View.VISIBLE);

                    ((RadioButton)grpq3.getChildAt(grpq3.getChildCount()-1)).setChecked(true);
                    ((RadioButton)grpq4.getChildAt(grpq4.getChildCount()-1)).setChecked(true);

                    tvTitleExtra.setText("ZK");
                    isLK.setEnabled(false);
                }else {
                    grpq3.getChildAt(grpq3.getChildCount()-1).setVisibility(View.INVISIBLE);
                    grpq4.getChildAt(grpq4.getChildCount()-1).setVisibility(View.INVISIBLE);
                    tvTitleExtra.setVisibility(View.INVISIBLE);
                    isLK.setEnabled(true);
                }
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

                String e1Type = null;
                String e2Type = null;
                String q1Type = null;
                String q2Type = null;
                String q3Type = null;
                String q4Type = null;

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

                //Wenn das Fach als ZK oder LK angegeben wurde werden hier alle anderen Werte ggf. überschrieben!
                //LK überschreibt ZK!
                if(isZK.isChecked()){
                    q3Type = "ZK";
                    q4Type = "ZK";
                }
                if(isLK.isChecked()){
                    q1Type = "LK";
                    q2Type = "LK";
                    q3Type = "LK";
                    q4Type = "LK";
                }


                String fach = kursSpinner.getItemAtPosition(kursSpinner.getSelectedItemPosition()).toString();
                addKurs(fach, e1Type, e2Type, q1Type, q2Type, q3Type, q4Type);

                dialog.hide();

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
                refreshUI(onlySelected);
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
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences(
                getResources().getString(R.string.prefs_key),
                Context.MODE_PRIVATE);
        String fileName = sharedPreferences.getString("dbFileName", null);
        if(fileName!=null){
            File file = new File(this.getActivity().getFilesDir(), fileName);

            DatabaseBuilder dbb = new DatabaseBuilder();
            dbb.setAutoSync(true);
            lupoDatabase = dbb.open(file);

            refreshUI(onlySelected);
        }
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

        TableRow tableRow = new TableRow(this.getActivity());
        TextView tvFachKrz = new TextView(this.getActivity());
        TextView tvKursart_E1 = new TextView(this.getActivity());
        TextView tvKursart_E2 = new TextView(this.getActivity());
        TextView tvKursart_Q1 = new TextView(this.getActivity());
        TextView tvKursart_Q2 = new TextView(this.getActivity());
        TextView tvKursart_Q3 = new TextView(this.getActivity());
        TextView tvKursart_Q4 = new TextView(this.getActivity());

        TableRow.LayoutParams trNameParams = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .28f);
        trNameParams.setMargins(0, 0, 0, 2);

        TableRow.LayoutParams trParams = new TableRow.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, .12f);
        trParams.setMargins(0, 0, 0, 2);

        tvFachKrz.setLayoutParams(trNameParams);
        tvKursart_E1.setLayoutParams(trParams);
        tvKursart_E2.setLayoutParams(trParams);
        tvKursart_Q1.setLayoutParams(trParams);
        tvKursart_Q2.setLayoutParams(trParams);
        tvKursart_Q3.setLayoutParams(trParams);
        tvKursart_Q4.setLayoutParams(trParams);

        tvFachKrz.setPadding(0, 16, 0, 16);
        tvKursart_E1.setPadding(0, 16, 0, 16);
        tvKursart_E2.setPadding(0, 16, 0, 16);
        tvKursart_Q1.setPadding(0, 16, 0, 16);
        tvKursart_Q2.setPadding(0, 16, 0, 16);
        tvKursart_Q3.setPadding(0, 16, 0, 16);
        tvKursart_Q4.setPadding(0, 16, 0, 16);

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

    private void printLupo() throws IOException {
        Table table = lupoDatabase.getTable("ABP_SchuelerFaecher");
        PrintLuPO printLuPO = new PrintLuPO(getActivity());

        Column fachKrz = table.getColumn("FachKrz");
        Column fsJahrgang = table.getColumn("FS_BeginnJg");
        Column reihenfolge = table.getColumn("Sprachenfolge");
        Column kursartE1 = table.getColumn("Kursart_E1");
        Column kursartE2 = table.getColumn("Kursart_E2");
        Column kursartQ1 = table.getColumn("Kursart_Q1");
        Column kursartQ2 = table.getColumn("Kursart_Q2");
        Column kursartQ3 = table.getColumn("Kursart_Q3");
        Column kursartQ4 = table.getColumn("Kursart_Q4");
        Column abifach = table.getColumn("AbiturFach");

        for(Row row : table){
            String sValueFackKrz = Objects.toString(row.get(fachKrz.getName()), "");
            String sValueFsJahgang = Objects.toString(row.get(fachKrz.getName()), "");
            String sValueReihenfolge = Objects.toString(row.get(reihenfolge.getName()), "");
            String sValueKursartE1 = Objects.toString(row.get(kursartE1.getName()), "");
            String sValueKursartE2 = Objects.toString(row.get(kursartE2.getName()), "");
            String sValueKursartQ1 = Objects.toString(row.get(kursartQ1.getName()), "");
            String sValueKursartQ2 = Objects.toString(row.get(kursartQ2.getName()), "");
            String sValueKursartQ3 = Objects.toString(row.get(kursartQ3.getName()), "");
            String sValueKursartQ4 = Objects.toString(row.get(kursartQ4.getName()), "");
            String sValueAbifach = Objects.toString(row.get(abifach.getName()), "");

            printLuPO.addLineToHTML(sValueFackKrz,
                    sValueFsJahgang,
                    sValueReihenfolge,
                    sValueKursartE1,
                    sValueKursartE2,
                    sValueKursartQ1,
                    sValueKursartQ2,
                    sValueKursartQ3,
                    sValueKursartQ4,
                    sValueAbifach);
        }
        printLuPO.print();
    }

    /**
     * Synchronisiert die lupoDatabase mit dem TableLayout
     * @throws IOException Wenn die Tabelle "ABP_SchuelerFaecher" nicht
     * in der Datenbank "lupoDatabase" gefunden wurde wird eine IOExpection zurückgegeben.
     */
    private void refreshUI(boolean onlyChecked) throws IOException {
        tableLayout.removeAllViews();
        Table table = lupoDatabase.getTable("ABP_SchuelerFaecher");

        //Die UI-Table wird nach der vorgegebenen Sortierung der Lupo-Datenbank sortiert.
//        for(Row row : CursorBuilder.createCursor(table.getIndex("Sortierung"))){

        addRow("Fachkrz", "EF.1", "EF.2", "Q1.1", "Q1.2", "Q2.1", "Q2.2");
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

            if(onlyChecked){
                if(!sValueKursartE1.isEmpty()
                        || !sValueKursartE2.isEmpty()
                        || !sValueKursartQ1.isEmpty()
                        || !sValueKursartQ2.isEmpty()
                        || !sValueKursartQ3.isEmpty()
                        || !sValueKursartQ4.isEmpty()){
                    addRow(sValueFackKrz, sValueKursartE1, sValueKursartE2,
                            sValueKursartQ1, sValueKursartQ2, sValueKursartQ3,
                            sValueKursartQ4);
                }
            }else {
                addRow(sValueFackKrz, sValueKursartE1, sValueKursartE2,
                        sValueKursartQ1, sValueKursartQ2, sValueKursartQ3,
                        sValueKursartQ4);
            }
        }
        refreshWochenStunden();
        getFehler();
    }

    /**
     * Berechnet die Wochenstunden und zeigt sie unten in der BottomBar an.
     * Das ganze erfolgt asynchron, damit keine lange Ladezeit beim
     * starten der App und updaten der UI erfolgt.
     */
    private void refreshWochenStunden(){
//        new asyncCalcWochenstunden().execute();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final StundenRechener stundenRechener = new StundenRechener(getActivity(), lupoDatabase);
                    final int wochenStundenE1 = stundenRechener.getWochenstunden("Kursart_E1");
                    final int wochenStundenE2 = stundenRechener.getWochenstunden("Kursart_E2");
                    final int wochenStundenQ1 = stundenRechener.getWochenstunden("Kursart_Q1");
                    final int wochenStundenQ2 = stundenRechener.getWochenstunden("Kursart_Q2");
                    final int wochenStundenQ3 = stundenRechener.getWochenstunden("Kursart_Q3");
                    final int wochenStundenQ4 = stundenRechener.getWochenstunden("Kursart_Q4");

                    final int durchscnittE = stundenRechener.getPhasenstunden(0);
                    final int durchscnittQ = stundenRechener.getPhasenstunden(1);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tvWE1 =  (TextView) getActivity().findViewById(R.id.tvInfoWochenStdE1);
                            TextView tvWE2 =  (TextView) getActivity().findViewById(R.id.tvInfoWochenStdE2);
                            TextView tvWQ1 = (TextView) getActivity().findViewById(R.id.tvInfoWochenStdQ1);
                            TextView tvWQ2 = (TextView) getActivity().findViewById(R.id.tvInfoWochenStdQ2);
                            TextView tvWQ3 = (TextView) getActivity().findViewById(R.id.tvInfoWochenStdQ3);
                            TextView tvWQ4 = (TextView) getActivity().findViewById(R.id.tvInfoWochenStdQ4);

                            TextView tvDE = (TextView) getActivity().findViewById(R.id.tvInfoDurchStdE);
                            TextView tvDQ = (TextView) getActivity().findViewById(R.id.tvInfoDurchStdQ);

                            tvWE1.setText(String.valueOf(wochenStundenE1));
                            tvWE2.setText(String.valueOf(wochenStundenE2));
                            tvWQ1.setText(String.valueOf(wochenStundenQ1));
                            tvWQ2.setText(String.valueOf(wochenStundenQ2));
                            tvWQ3.setText(String.valueOf(wochenStundenQ3));
                            tvWQ4.setText(String.valueOf(wochenStundenQ4));

                            tvDE.setText("E-Phase: " + String.valueOf(durchscnittE));
                            tvDQ.setText("Q-Phase: " + String.valueOf(durchscnittQ));

                            tvWE1.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenE1, false, false));
                            tvWE2.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenE2, false, false));
                            tvWQ1.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenQ1, false, true));
                            tvWQ2.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenQ2, false, true));
                            tvWQ3.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenQ3, false, true));
                            tvWQ4.setTextColor(stundenRechener.wochenstundenanzahl(wochenStundenQ4, false, true));

                            tvDE.setTextColor(stundenRechener.wochenstundenanzahl(durchscnittE, true, false));
                            tvDQ.setTextColor(stundenRechener.wochenstundenanzahl(durchscnittE, true, true));

                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getFehler(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Fehlermeldungen fehlermeldungen = new Fehlermeldungen(lupoDatabase);
                    final String s = fehlermeldungen.checkForFehler();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(s!=null) {
                                ((TextView) getActivity().findViewById(R.id.tvFehlermeldungen)).setVisibility(View.VISIBLE);
                                ((TextView) getActivity().findViewById(R.id.tvFehlermeldungen)).setText(s);
                            }else {
                            ((TextView) getActivity().findViewById(R.id.tvFehlermeldungen)).setVisibility(View.GONE);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Prüft ob ein LK in einem Fach gewählt werden kann
     * @param fach Der zu prüfende Fachname
     * @return Ob das Fach als LK gewählt werden kann
     * @throws IOException Wenn die Tabelle "ABP_Faecher" nicht
     * in der Datenbank "lupoDatabase" gefunden wurde wird eine IOExpection zurückgegeben.
     */
    private boolean isLKAvailable(String fach) throws IOException {
        Table table = lupoDatabase.getTable("ABP_Faecher");
        Cursor cursor = CursorBuilder.createCursor(table);
        boolean found = cursor.findFirstRow(Collections.singletonMap("Bezeichnung", fach));
        if(found){
            Row row = cursor.getCurrentRow();
            if(row.get("LK_Moegl").equals("J")){
                return true;
            }
        }
        return false;
    }

}
