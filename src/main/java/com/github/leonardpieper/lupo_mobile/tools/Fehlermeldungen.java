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

public class Fehlermeldungen {
    private Table tableSchuelerFaecher;

    public Fehlermeldungen(Database database) throws IOException {
        tableSchuelerFaecher = database.getTable("ABP_SchuelerFaecher");
    }

    public String checkForFehler() throws IOException {
        if(!deutschVonEF1BisQ4()){
            return "Deutsch muss von der EF bis zur Q2 durchgängig belegt werden.";
        }
        else if(!kunstelerischesFach()){
            return "Kunst oder Musik muss von der EF bis zur Q1.2 durchgängig belegt werden. In der Q1 kann Literatur, Instrumental- oder Vokalpraktikum das künstlerische Fach ersetzen.";
        }
        return null;
    }

    /**
     * Überprüft, ob Deutsch von EF.1 bis Q2.2 durchgängig belegt ist
     * @return Liefert true, wenn Deuscth durchgängig belegt ist.
     * @throws IOException
     */
    private boolean deutschVonEF1BisQ4() throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", "D"));
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
