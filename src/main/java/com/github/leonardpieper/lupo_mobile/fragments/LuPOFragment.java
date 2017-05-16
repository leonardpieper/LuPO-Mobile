package com.github.leonardpieper.lupo_mobile.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.leonardpieper.lupo_mobile.R;
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
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Leonard on 16.05.2017.
 */

public class LuPOFragment extends Fragment {
    private Activity activity;

    private Database lupoDatabase;

    private View view;
    private TableLayout tableLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.content_main, container, false);

        tableLayout = (TableLayout)view.findViewById(R.id.tableLayoutMain);
        System.out.println(tableLayout.getId());

        try {
            openLupoDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
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

            refreshUI();
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

    /**
     * Synchronisiert die lupoDatabase mit dem TableLayout
     * @throws IOException Wenn die Tabelle "ABP_SchuelerFaecher" nicht
     * in der Datenbank "lupoDatabase" gefunden wurde wird eine IOExpection zurückgegeben.
     */
    private void refreshUI() throws IOException {
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

            addRow(sValueFackKrz, sValueKursartE1, sValueKursartE2,
                    sValueKursartQ1, sValueKursartQ2, sValueKursartQ3,
                    sValueKursartQ4);
        }
        refreshWochenStunden();
    }

    /**
     * Berechnet die Wochenstunden und zeigt sie unten in der BottomBar an.
     * Das ganze erfolgt asynchron, damit keine lange Ladezeit beim
     * starten der App und updaten der UI erfolgt.
     */
    private void refreshWochenStunden(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StundenRechener stundenRechener = new StundenRechener(lupoDatabase);
                    final String wochenStundenE1 = String.valueOf(stundenRechener.getWochenstunden("Kursart_E1"));
                    final String wochenStundenE2 = String.valueOf(stundenRechener.getWochenstunden("Kursart_E2"));
                    final String wochenStundenQ1 = String.valueOf(stundenRechener.getWochenstunden("Kursart_Q1"));
                    final String wochenStundenQ2 = String.valueOf(stundenRechener.getWochenstunden("Kursart_Q2"));
                    final String wochenStundenQ3 = String.valueOf(stundenRechener.getWochenstunden("Kursart_Q3"));
                    final String wochenStundenQ4 = String.valueOf(stundenRechener.getWochenstunden("Kursart_Q4"));

                    final String durchscnittE = String.valueOf(stundenRechener.getPhasenstunden(0));
                    final String durchscnittQ = String.valueOf(stundenRechener.getPhasenstunden(1));
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdE1)).setText(wochenStundenE1);
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdE2)).setText(wochenStundenE2);
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdQ1)).setText(wochenStundenQ1);
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdQ2)).setText(wochenStundenQ2);
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdQ3)).setText(wochenStundenQ3);
                            ((TextView)view.findViewById(R.id.tvInfoWochenStdQ4)).setText(wochenStundenQ4);

                            ((TextView)view.findViewById(R.id.tvInfoDurchStdE)).setText("E-Phase: " + durchscnittE);
                            ((TextView)view.findViewById(R.id.tvInfoDurchStdQ)).setText("Q-Phase: " + durchscnittQ);

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
