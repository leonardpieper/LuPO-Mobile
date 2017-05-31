package com.github.leonardpieper.lupo_mobile.tools;

import android.app.Activity;
import android.graphics.Color;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by Leonard on 14.05.2017.
 */

public class StundenRechener {
    private Table tableSchuelerFaecher;
    private Table tableFaecher;

    private Activity mActivity;

    public StundenRechener(Activity activity, Database database) throws IOException {
        mActivity = activity;
        tableSchuelerFaecher = database.getTable("ABP_SchuelerFaecher");
        tableFaecher = database.getTable("ABP_Faecher");
    }

    /**
     * Gibt die Wochenstunden für einen Jahrgang zurück
     * @param jahrgang Der Jahrgang um den es sich handelt im Format "Kursart_XX"
     * @return Die Anzahl der Wochenstunden
     * @throws IOException Wenn der Cursor nicht richtig ausgeführt werden kann
     * wird eine IOExpection zurückgegeben.
     */
    public int getWochenstunden(String jahrgang) throws IOException {
        int wochenstunden = 0;
        for(Row row : tableSchuelerFaecher){
            wochenstunden = wochenstunden + stundenFuerFach(jahrgang, row);
        }
        return wochenstunden;
    }

    /**
     * Gibt die durchschnittliche Stundenanzahl, entweder für die E-Phase(0) oder die Q-Phase(1) an.
     * @param phase Die Phase um die es sich handelt
     * @return Die durchschnittliche Stundenanzahl
     * @throws IOException Wenn die Spalte nicht gefunden wird,
     * wird eine IOExpection zurückgegeben.
     */
    public int getPhasenstunden(int phase) throws IOException {
        int phasenstunden = 0;
        for(Row row : tableSchuelerFaecher){
            if(phase == 0){
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_E1", row);
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_E2", row);
            }else if (phase == 1){
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_Q1", row);
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_Q2", row);
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_Q3", row);
                phasenstunden = phasenstunden + stundenFuerFach("Kursart_Q4", row);
            }
        }
        if(phase==0){
            return phasenstunden/2;
        }else if(phase == 1){
            return phasenstunden/4;
        }
        return 0;

    }

    /**
     * Gibt die Wochenstundenanzahl für ein bestimmtes Fach aus.
     * @param jahrgang Der Jahrgang für den die Wochenstundenzahl bestimmt werden soll
     * @param row Die Reihe in der sich die for-Schleife befindet
     * @return Die Anzahl der Wochenstunden
     * @throws IOException Wenn die Spalte nicht gefunden wird,
     * wird eine IOExpection zurückgegeben.
     */
    private int stundenFuerFach(String jahrgang, Row row) throws IOException {
        switch(Objects.toString(row.get(jahrgang), "")){
            case "S":
            case "M":
                if(isNeueSprache(Objects.toString(row.get("FachKrz"), ""))){
                    return 4;
                }else {
                    return 3;
                }
            case "ZK":
                return 3;
            case "LK":
                return 5;
            default:
                return 0;
        }
    }

    /**
     * Prüft, ob eine Sprache eine neu einsetzende Framdsprache ist
     * @param fachKrz Das Fachkürzel des zu prüfenden Faches
     * @return Liefert true zurück, wenn fachKrz eine neu einsetzende Fremdsprache ist
     * @throws IOException Wenn der Cursor nicht richtig ausgeführt werden kann
     * wird eine IOExpection zurückgegeben.
     */
    private boolean isNeueSprache(String fachKrz) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachKrz));
        if(found){
            Row row = cursor.getCurrentRow();
            if(row.get("AlsNeueFSInSII").equals("J")){
                return true;
            }
        }
        return false;
    }

    public int wochenstundenanzahl(int anzahl, boolean phase, boolean qPhase){
        int minWochenstunden;
        if(phase){
            minWochenstunden = 34;
        }else {
            if(qPhase) {
                minWochenstunden = 31;
            }else {
                minWochenstunden = 32;
            }
        }
        if(anzahl<minWochenstunden){
            return mActivity.getResources().getColor(android.R.color.holo_red_dark);
        }else {
            return mActivity.getResources().getColor(android.R.color.tertiary_text_light);
        }
    }
}
