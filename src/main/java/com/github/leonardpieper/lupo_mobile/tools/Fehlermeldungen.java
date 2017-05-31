package com.github.leonardpieper.lupo_mobile.tools;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Fehlermeldungen {
    private Table tableSchuelerFaecher;

    public Fehlermeldungen(Database database) throws IOException {
        tableSchuelerFaecher = database.getTable("ABP_SchuelerFaecher");
    }

    public String checkForFehler() throws IOException {
        if(!fachVonEF1BisQ4("D")){
            return "Deutsch muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        }
        else if(!fachVonEF1BisQ4("SP")){
            return "Sport muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        }
        else if(!fachVonEF1BisQ4("M")){
            return "Mathematik muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        }
        else if(!fachVonEF1BisQ4("GE") &&
                !fachVonEF1BisQ4("SW") &&
                !fachVonEF1BisQ4("RK") &&
                !fachVonEF1BisQ4("PS") &&
                !fachVonEF1BisQ4("Pa") &&
                !fachVonEF1BisQ4("SL") &&
                !fachVonEF1BisQ4("EK")){
            return "Mindestens eine Gesellschaftswissenschaft muss von der Ef.1 bis zur Q2.2 durchgängig belegt werden.";
        }
        else if(!geschichteSowiePflicht("GE")){
            return "Geschichte muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann ein Zusatzkurs von Q2.1 bis Q2.2 belegt werden";
        }
        else if(!geschichteSowiePflicht("SW")){
            return "Sozialwissenschaften muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann ein Zusatzkurs von Q2.1 bis Q2.2 belegt werden";
        }
        else if(!reliPhiloPflicht()){
            return "Religion muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann Philosophie gewählt werden.";
        }
        else if(!klassischeNaturwissenschaft()){
            return "Mindestens eine klassische Naturwissenschaft (Physik, Biologie, Chemie) muss durchgehend von Q1.1 bis Q2.2 belegt werden.";
        }
        else if(!kunstelerischesFach()){
            return "Kunst oder Musik muss von der EF bis zur Q1.2 durchgängig belegt werden. In der Q1 kann Literatur, Instrumental- oder Vokalpraktikum das künstlerische Fach ersetzen.";
        }
        else if(!zweiLKs()){
            return "Es müssen zwei Fächer als Leistungskurse gewählt werden";
        }
        return null;
    }

    /**
     * Überprüft, ob ein Fach von EF.1 bis Q2.2 durchgängig belegt ist
     * @return Liefert true, wenn ein Fach durchgängig belegt ist.
     * @throws IOException
     */
    private boolean fachVonEF1BisQ4(String fachAbk) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if(found){
            Row row = cursor.getCurrentRow();
            if(row.getString("Kursart_E1")!=null
                    &&row.getString("Kursart_E2")!=null
                    &&row.getString("Kursart_Q1")!=null
                    &&row.getString("Kursart_Q2")!=null
                    &&row.getString("Kursart_Q3")!=null
                    &&row.getString("Kursart_Q4")!=null){
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob ein künstlerisches Fach von EF.1 bis Q1.2 belegt ist.
     * Künstlerische Fächer sind:
     *  - Musik
     *  - Kunst
     *  In der Q1 außerdem:
     *  - Literatur
     *  - Instrumentalpraktikum
     *  - Vokalpraktikum
     * @return Liefert true, wenn ein künstlerisches Fach belegt ist.
     * @throws IOException
     */
    private boolean kunstelerischesFach() throws IOException {
        String kursart = null;
        for(int i = 0; i<4; i++){
            switch (i){
                case 0: kursart = "E1"; break;
                case 1: kursart = "E2"; break;
                case 2: kursart = "Q1"; break;
                case 3: kursart = "Q2"; break;
            }
            if(fachInJahrgangBelegt("MU", kursart)){continue;}
            else if(fachInJahrgangBelegt("KU", kursart)){continue;}
            else if(fachInJahrgangBelegt("LI", kursart)){continue;}
            else if(fachInJahrgangBelegt("IP", kursart)){continue;}
            else if(fachInJahrgangBelegt("VP", kursart)){continue;}
            else {return false;}
        }
        return true;
    }

    /**
     * Prüft, ob Geschichte oder Sowie von EF.1-Q1.2 belegt sind, oder ein Zusatzkurs gewählt wurde
     * @param fachAbk Geschichte oder Sowie ("GE", "SW")
     * @return Liefert true, wenn Geschichte oder Sowie richtig gewählt wurden.
     * @throws IOException
     */
    private boolean geschichteSowiePflicht(String fachAbk) throws IOException {
        String kursart = null;
        boolean inE1Q2belegt = true;
        for(int i = 0; i<4; i++){
            switch (i){
                case 0: kursart = "E1"; break;
                case 1: kursart = "E2"; break;
                case 2: kursart = "Q1"; break;
                case 3: kursart = "Q2"; break;
            }
            if(fachInJahrgangBelegt(fachAbk, kursart)){continue;}
            else {
                inE1Q2belegt=false;
                break;
            }
        }
        if(!inE1Q2belegt){
            if(fachInJahrgangBelegt(fachAbk, "Q3") && fachInJahrgangBelegt(fachAbk, "Q4")){
                return true;
            }else {
                return false;
            }
        }
        return true;
    }

    private boolean reliPhiloPflicht() throws IOException {
        String kursart = null;
        for(int i = 0; i<4; i++){
            switch (i){
                case 0: kursart = "E1"; break;
                case 1: kursart = "E2"; break;
                case 2: kursart = "Q1"; break;
                case 3: kursart = "Q2"; break;
            }
            if(fachInJahrgangBelegt("ER", kursart)){continue;}
            else if(fachInJahrgangBelegt("HR", kursart)){continue;}
            else if(fachInJahrgangBelegt("YR", kursart)){continue;}
            else if(fachInJahrgangBelegt("OR", kursart)){continue;}
            else if(fachInJahrgangBelegt("KR", kursart)){continue;}
            else if(fachInJahrgangBelegt("PL", kursart)){continue;}
            else {return false;}
        }
        return true;
    }
    private boolean klassischeNaturwissenschaft() throws IOException {
        String kursart = null;
        for(int i = 0; i<6; i++){
            switch (i){
                case 0: kursart = "E1"; break;
                case 1: kursart = "E2"; break;
                case 2: kursart = "Q1"; break;
                case 3: kursart = "Q2"; break;
                case 4: kursart = "Q3"; break;
                case 5: kursart = "Q4"; break;
            }
            if(fachInJahrgangBelegt("PH", kursart)){continue;}
            else if(fachInJahrgangBelegt("BI", kursart)){continue;}
            else if(fachInJahrgangBelegt("CH", kursart)){continue;}
            else {return false;}
        }
        return true;
    }

    private boolean zweiLKs() throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("Kursart_Q1", "LK"));
        if(found){
            if(cursor.findNextRow(Collections.singletonMap("Kursart_Q1", "LK"))){
                return true;
            }
        }
        return false;
    }

    private boolean fachInJahrgangBelegt(String fachAbk, String jahrgang) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if(found){
            if(cursor.getCurrentRow().getString("Kursart_" + jahrgang)!=null){
                return true;
            }
        }
        return false;
    }
}
