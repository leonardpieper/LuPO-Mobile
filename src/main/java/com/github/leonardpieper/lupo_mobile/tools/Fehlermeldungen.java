package com.github.leonardpieper.lupo_mobile.tools;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.CursorBuilder;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Row;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.util.CaseInsensitiveColumnMatcher;
import com.healthmarketscience.jackcess.util.ColumnMatcher;
import com.healthmarketscience.jackcess.util.SimpleColumnMatcher;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Fehlermeldungen {
    private Database database;
    private Table tableSchuelerFaecher;

    public Fehlermeldungen(Database database) throws IOException {
        this.database = database;
        tableSchuelerFaecher = database.getTable("ABP_SchuelerFaecher");
    }

    public String checkForFehler() throws IOException {
        if (!fachVonEF1BisQ4("D")) {
            return "Deutsch muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        } else if (!fachVonEF1BisQ4("SP")) {
            return "Sport muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        } else if (!fachVonEF1BisQ4("M")) {
            return "Mathematik muss von der EF.1 bis zur Q2.2 durchgängig belegt werden.";
        } else if (!fachVonEF1BisQ4("GE") &&
                !fachVonEF1BisQ4("SW") &&
                !fachVonEF1BisQ4("RK") &&
                !fachVonEF1BisQ4("PS") &&
                !fachVonEF1BisQ4("Pa") &&
                !fachVonEF1BisQ4("SL") &&
                !fachVonEF1BisQ4("EK")) {
            return "Mindestens eine Gesellschaftswissenschaft muss von der Ef.1 bis zur Q2.2 durchgängig belegt werden.";
        } else if (!geschichteSowiePflicht("GE")) {
            return "Geschichte muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann ein Zusatzkurs von Q2.1 bis Q2.2 belegt werden";
        } else if (!geschichteSowiePflicht("SW")) {
            return "Sozialwissenschaften muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann ein Zusatzkurs von Q2.1 bis Q2.2 belegt werden";
        } else if (!reliPhiloPflicht()) {
            return "Religion muss von Ef.1 bis zur Q1.2 belegt werden, alternativ kann Philosophie gewählt werden.";
        } else if (!klassischeNaturwissenschaft()) {
            return "Mindestens eine klassische Naturwissenschaft (Physik, Biologie, Chemie) muss durchgehend von Q1.1 bis Q2.2 belegt werden.";
        } else if (!kunstelerischesFach()) {
            return "Kunst oder Musik muss von der EF bis zur Q1.2 durchgängig belegt werden. In der Q1 kann Literatur, Instrumental- oder Vokalpraktikum das künstlerische Fach ersetzen.";
        } else if (!zweiLKs()) {
            return "Es müssen zwei Fächer als Leistungskurse gewählt werden";
        } else if (!siebenGKs()) {
            return "In der Qualifikationsphase sind pro Halbjahr mindestens 7 Fächer in Grundkursen zu wählen.";
        } else if (!fremdsprache()) {
            return "Mindestens eine Fremdsprache muss von EF.1 bis Q2.2 durchgehend belegt werden. Handelt es sich hierbei um eine neu einsetzende Fremdsprache, so muss zusätzlich mindestens eine aus der SI fortgeführte Fremdsprache von EF.1 bis EF.2 belegt werden.";
        } else if (!schwerpunktVorhanden()) {
            return "Von EF.1 bis Q2.2 müssen entweder zwei Naturwissenschaften oder zwei Fremdsprachen durchgehend belegt werden. Hierbei ist eine Naturwissenschaft oder sind zwei Fremsprachen schriftlich zu belegen.Zu den Fremdsprachen zählen auch in einer weiteren Fremdsprache unterrichtete Sachfächer.";
        } else if (!fachHinzugewählt()) {
            return "Bis auf Literatur, vokal- und instrumentalpraktische Kurse, Zusatzkurse, Vertiefungsfächer und Projektkurse können keine Fächer hinzugewählt werden, die nicht schon ab EF.1 belegt wurden.";
        }

        //Klausurverpflichtungen:
        else if (!schriftlichVonEF1BisQ3("D")) {
            return "Deutsch muss von EF.1 bis wenigstens Q2.1 schriftlich belegt werden.";
        } else if (!schriftlichVonEF1BisQ3("M")) {
            return "Mathematik muss von EF.1 bis wenigstens Q2.1 schriftlich belegt werden.";
        } else if (!schriftlichFSEF1BisQ3()) {
            return "Mindestens eine durchgehend belegte Fremdsprache muss von EF.1 bis Q2.1 schriftlich sein.";
        } else if (!schriftlichGSEF1BisQ3()) {
            return "Mindestens eine Gesellschaftswissenschaft oder Religionslehre muss von EF.1 bis wenigstens Q2.1 schriftlich belegt werden.";
        } else if (!schriftlichNWEF1bisEF2()) {
            return "In EF.1 und EF.2 muss mindestens eine klassische Naturwissenschaft schriftlich belegt sein.";
        }
        return null;
    }

    /**
     * Überprüft, ob ein Fach von EF.1 bis Q2.2 durchgängig belegt ist
     *
     * @return Liefert true, wenn ein Fach durchgängig belegt ist.
     * @throws IOException
     */
    private boolean fachVonEF1BisQ4(String fachAbk) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if (found) {
            Row row = cursor.getCurrentRow();
            if (row.getString("Kursart_E1") != null
                    && row.getString("Kursart_E2") != null
                    && row.getString("Kursart_Q1") != null
                    && row.getString("Kursart_Q2") != null
                    && row.getString("Kursart_Q3") != null
                    && row.getString("Kursart_Q4") != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob ein Fach von EF.1 bis Q1.2 durchgängig belegt ist
     *
     * @return Liefert true, wenn ein Fach durchgängig belegt ist.
     * @throws IOException
     */
    private boolean fachVonEF1BisEF2(String fachAbk) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if (found) {
            Row row = cursor.getCurrentRow();
            if (row.getString("Kursart_E1") != null
                    && row.getString("Kursart_E2") != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob ein künstlerisches Fach von EF.1 bis Q1.2 belegt ist.
     * Künstlerische Fächer sind:
     * - Musik
     * - Kunst
     * In der Q1 außerdem:
     * - Literatur
     * - Instrumentalpraktikum
     * - Vokalpraktikum
     *
     * @return Liefert true, wenn ein künstlerisches Fach belegt ist.
     * @throws IOException
     */
    private boolean kunstelerischesFach() throws IOException {
        String kursart = null;
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    kursart = "E1";
                    break;
                case 1:
                    kursart = "E2";
                    break;
                case 2:
                    kursart = "Q1";
                    break;
                case 3:
                    kursart = "Q2";
                    break;
            }
            if (fachInJahrgangBelegt("MU", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("KU", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("LI", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("IP", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("VP", kursart)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft, ob Geschichte oder Sowie von EF.1-Q1.2 belegt sind, oder ein Zusatzkurs gewählt wurde
     *
     * @param fachAbk Geschichte oder Sowie ("GE", "SW")
     * @return Liefert true, wenn Geschichte oder Sowie richtig gewählt wurden.
     * @throws IOException
     */
    private boolean geschichteSowiePflicht(String fachAbk) throws IOException {
        String kursart = null;
        boolean inE1Q2belegt = true;
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    kursart = "E1";
                    break;
                case 1:
                    kursart = "E2";
                    break;
                case 2:
                    kursart = "Q1";
                    break;
                case 3:
                    kursart = "Q2";
                    break;
            }
            if (fachInJahrgangBelegt(fachAbk, kursart)) {
                continue;
            } else {
                inE1Q2belegt = false;
                break;
            }
        }
        if (!inE1Q2belegt) {
            if (fachInJahrgangBelegt(fachAbk, "Q3") && fachInJahrgangBelegt(fachAbk, "Q4")) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean reliPhiloPflicht() throws IOException {
        String kursart = null;
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    kursart = "E1";
                    break;
                case 1:
                    kursart = "E2";
                    break;
                case 2:
                    kursart = "Q1";
                    break;
                case 3:
                    kursart = "Q2";
                    break;
            }
            if (fachInJahrgangBelegt("ER", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("HR", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("YR", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("OR", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("KR", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("PL", kursart)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean klassischeNaturwissenschaft() throws IOException {
        String kursart = null;
        for (int i = 0; i < 6; i++) {
            switch (i) {
                case 0:
                    kursart = "E1";
                    break;
                case 1:
                    kursart = "E2";
                    break;
                case 2:
                    kursart = "Q1";
                    break;
                case 3:
                    kursart = "Q2";
                    break;
                case 4:
                    kursart = "Q3";
                    break;
                case 5:
                    kursart = "Q4";
                    break;
            }
            if (fachInJahrgangBelegt("PH", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("BI", kursart)) {
                continue;
            } else if (fachInJahrgangBelegt("CH", kursart)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean zweiLKs() throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("Kursart_Q1", "LK"));
        if (found) {
            if (cursor.findNextRow(Collections.singletonMap("Kursart_Q1", "LK"))) {
                return true;
            }
        }
        return false;
    }

    private boolean siebenGKs() throws IOException {
        int gks[] = new int[4];
        String jahrgang = null;
        for (Row row : tableSchuelerFaecher) {
            for (int i = 0; i < 4; i++) {
                switch (i) {
                    case 0:
                        jahrgang = "Q1";
                        break;
                    case 1:
                        jahrgang = "Q2";
                        break;
                    case 2:
                        jahrgang = "Q3";
                        break;
                    case 3:
                        jahrgang = "Q4";
                        break;
                }
                String kursart = Objects.toString(row.getString("Kursart_" + jahrgang), "");
                if (kursart.equalsIgnoreCase("s") || kursart.equalsIgnoreCase("m")) {
                    gks[i]++;
                }
            }
        }
        if (gks[0] >= 7 && gks[1] >= 7 && gks[2] >= 3) {
            return true;
        }
        return false;
    }

    private boolean fremdsprache() throws IOException {
        String kursart = null;
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    kursart = "E";
                    break;
                case 1:
                    kursart = "F";
                    break;
                case 2:
                    kursart = "L";
                    break;
                case 3:
                    kursart = "S";
                    break;
            }
            if (fachVonEF1BisQ4(kursart)) {
                return true;
            }
        }

        for (int i = 0; i < 9; i++) {
            switch (i) {
                case 0:
                    kursart = "S1";
                    break;
                case 1:
                    kursart = "L1";
                    break;
                case 2:
                    kursart = "H1";
                    break;
                case 3:
                    kursart = "T1";
                    break;
                case 4:
                    kursart = "I1";
                    break;
                case 5:
                    kursart = "R1";
                    break;
                case 6:
                    kursart = "G1";
                    break;
                case 7:
                    kursart = "K1";
                    break;
                case 8:
                    kursart = "C1";
                    break;
            }
            if (fachVonEF1BisQ4(kursart)) {
                for (int j = 0; j < 4; j++) {
                    switch (i) {
                        case 0:
                            kursart = "E";
                            break;
                        case 1:
                            kursart = "F";
                            break;
                        case 2:
                            kursart = "L";
                            break;
                        case 3:
                            kursart = "S";
                            break;
                    }
                    if (fachVonEF1BisEF2(kursart)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean schwerpunktVorhanden() throws IOException {
        String kursart = null;
        boolean eineNWoderFS = false;

        //2 Naturwissenschaften müssen belegt werden
        for (int i = 0; i < 7; i++) {
            switch (i) {
                case 0:
                    kursart = "BI";
                    break;
                case 1:
                    kursart = "PH";
                    break;
                case 2:
                    kursart = "CH";
                    break;
                case 3:
                    kursart = "NW";
                    break;
                case 4:
                    kursart = "EL";
                    break;
                case 5:
                    kursart = "IF";
                    break;
                case 6:
                    kursart = "TC";
                    break;
            }
            if (fachVonEF1BisQ4(kursart)) {
                if (eineNWoderFS) {
                    return true;
                }
                eineNWoderFS = true;
            }
        }

        //Alternativ: 2 Fremdsprachen müssen belegt werden#
        eineNWoderFS = false;

        for (int i = 0; i < 13; i++) {
            switch (i) {
                case 0:
                    kursart = "E";
                    break;
                case 1:
                    kursart = "F";
                    break;
                case 2:
                    kursart = "L";
                    break;
                case 3:
                    kursart = "S";
                    break;
                case 4:
                    kursart = "S1";
                    break;
                case 5:
                    kursart = "L1";
                    break;
                case 6:
                    kursart = "H1";
                    break;
                case 7:
                    kursart = "T1";
                    break;
                case 8:
                    kursart = "I1";
                    break;
                case 9:
                    kursart = "R1";
                    break;
                case 10:
                    kursart = "G1";
                    break;
                case 11:
                    kursart = "K1";
                    break;
                case 12:
                    kursart = "C1";
                    break;
            }
            if (fachVonEF1BisQ4(kursart)) {
                if (eineNWoderFS) {
                    return true;
                }
                eineNWoderFS = true;
            }
        }

        //Extra der Alternative: Auch Bilinguale Fächer zählen:
        for (Row row : tableSchuelerFaecher) {
            String unterichtssprache = Objects.toString(row.get("Unterichtssprache"), "");
            if (!unterichtssprache.equalsIgnoreCase("d") && !unterichtssprache.isEmpty()) {
                if (eineNWoderFS) {
                    return true;
                }
                eineNWoderFS = true;
            }
        }


        return false;

    }

    /*
        |
        | Klausurverpflichtungen
        V
     */

    private boolean schriftlichVonEF1BisQ3(String fachAbk) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if (found) {
            Row row = cursor.getCurrentRow();
            if (row.getString("Kursart_E1") != null
                    && row.getString("Kursart_E1").equals("S")
                    && row.getString("Kursart_E2") != null
                    && row.getString("Kursart_E2").equals("S")
                    && row.getString("Kursart_Q1") != null
                    && (row.getString("Kursart_Q1").equals("S")
                    || row.getString("Kursart_Q1").equals("LK"))
                    && row.getString("Kursart_Q2") != null
                    && (row.getString("Kursart_Q2").equals("S")
                    || row.getString("Kursart_Q1").equals("LK"))
                    && row.getString("Kursart_Q3") != null
                    && (row.getString("Kursart_Q3").equals("S")
                    || row.getString("Kursart_Q1").equals("LK"))) {
                return true;
            }
        }
        return false;
    }

    private boolean schriftlichVonEF1BisEF2(String fachAbk) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if (found) {
            Row row = cursor.getCurrentRow();
            if (row.getString("Kursart_E1") != null
                    && row.getString("Kursart_E1").equals("S")
                    && row.getString("Kursart_E2") != null
                    && row.getString("Kursart_E2").equals("S")) {
                return true;
            }
        }
        return false;
    }

    private boolean schriftlichFSEF1BisQ3() throws IOException {
        String kursart = null;
        for (int i = 0; i < 13; i++) {
            switch (i) {
                case 0:
                    kursart = "E";
                    break;
                case 1:
                    kursart = "F";
                    break;
                case 2:
                    kursart = "L";
                    break;
                case 3:
                    kursart = "S";
                    break;
                case 4:
                    kursart = "S1";
                    break;
                case 5:
                    kursart = "L1";
                    break;
                case 6:
                    kursart = "H1";
                    break;
                case 7:
                    kursart = "T1";
                    break;
                case 8:
                    kursart = "I1";
                    break;
                case 9:
                    kursart = "R1";
                    break;
                case 10:
                    kursart = "G1";
                    break;
                case 11:
                    kursart = "K1";
                    break;
                case 12:
                    kursart = "C1";
                    break;
            }
            if (schriftlichVonEF1BisQ3(kursart)) {
                return true;
            }
        }
        return false;
    }

    private boolean schriftlichGSEF1BisQ3() throws IOException {
        String kursart = null;
        for (int i = 0; i < 15; i++) {
            switch (i) {
                case 0:
                    kursart = "PK";
                    break;
                case 1:
                    kursart = "PA";
                    break;
                case 2:
                    kursart = "EK";
                    break;
                case 3:
                    kursart = "RK";
                    break;
                case 4:
                    kursart = "SW";
                    break;
                case 5:
                    kursart = "SL";
                    break;
                case 6:
                    kursart = "GE";
                    break;
                case 7:
                    kursart = "PS";
                    break;
                case 8:
                    kursart = "GP";
                    break;
                case 9:
                    kursart = "GW";
                    break;
                case 10:
                    kursart = "YR";
                    break;
                case 11:
                    kursart = "KR";
                    break;
                case 12:
                    kursart = "OR";
                    break;
                case 13:
                    kursart = "ER";
                    break;
                case 14:
                    kursart = "HR";
                    break;
            }
            if (schriftlichVonEF1BisQ3(kursart)) {
                return true;
            }
        }
        return false;
    }

    private boolean schriftlichNWEF1bisEF2() throws IOException {
        String kursart = null;
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    kursart = "BI";
                    break;
                case 1:
                    kursart = "PH";
                    break;
                case 2:
                    kursart = "CH";
                    break;
            }
            if (schriftlichVonEF1BisEF2(kursart)) {
                return true;
            }
        }
        return false;
    }

    private boolean fachHinzugewählt() {
        for (Row row : tableSchuelerFaecher) {
            if ((row.get("Kursart_Q4") != null
                    && (row.get("Kursart_Q3") == null
                    || row.get("Kursart_Q2") == null
                    || row.get("Kursart_Q1") == null
                    || row.get("Kursart_E2") == null
                    || row.get("Kursart_E1") == null)
            )
                    || (row.get("Kursart_Q3") != null
                    && (row.get("Kursart_Q2") == null
                    || row.get("Kursart_Q1") == null
                    || row.get("Kursart_E2") == null
                    || row.get("Kursart_E1") == null)
            )
                    || (row.get("Kursart_Q2") != null
                    && (row.get("Kursart_Q1") == null
                    || row.get("Kursart_E2") == null
                    || row.get("Kursart_E1") == null)
            )
                    || (row.get("Kursart_Q1") != null
                    && (row.get("Kursart_E2") == null
                    || row.get("Kursart_E1") == null)
            )
                    || (row.get("Kursart_E2") != null
                    && row.get("Kursart_E1") == null)) {
                System.out.println("Hallo");
                return false;
            }         }
        System.out.println("Hallo");
        return true;
    }

    private boolean fachInJahrgangBelegt(String fachAbk, String jahrgang) throws IOException {
        Cursor cursor = CursorBuilder.createCursor(tableSchuelerFaecher);
        boolean found = cursor.findFirstRow(Collections.singletonMap("FachKrz", fachAbk));
        if (found) {
            if (cursor.getCurrentRow().getString("Kursart_" + jahrgang) != null) {
                return true;
            }
        }
        return false;
    }
}
