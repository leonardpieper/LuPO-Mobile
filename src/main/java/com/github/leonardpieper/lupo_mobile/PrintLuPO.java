package com.github.leonardpieper.lupo_mobile;

import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PrintLuPO{
    private final String TAG = "PrinLuPO";

    private Context mContext;
    private String lupoContent;

    public PrintLuPO(Context context){
        mContext = context;
    }

    private void doWebViewPrint(){
        // Create a WebView object specifically for printing
        WebView webView = new WebView(mContext);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "page finished loading " + url);
                createWebPrintJob(view);
            }
        });

        // Generate an HTML document on the fly:
        String htmlDocument = getPrintHTML();
        webView.loadDataWithBaseURL(null, htmlDocument, "text/HTML", "ISO-8859-1", null);
    }

    private void createWebPrintJob(WebView webView){
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

        String jobName = mContext.getString(R.string.app_name) + " Document";

        PrintDocumentAdapter printAdapter;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            printAdapter = webView.createPrintDocumentAdapter(jobName);
        }else{
            printAdapter = webView.createPrintDocumentAdapter();
        }

        PrintJob printJob = printManager.print(jobName, printAdapter,
                new PrintAttributes.Builder().build());

    }

    private String getPrintHTML(){
        BufferedReader reader = null;
        StringBuilder text = new StringBuilder();
        try {
            reader = new BufferedReader(
                    new InputStreamReader(mContext.getAssets().open("lupo_print.htm"), Charset.forName("ISO-8859-1")));

            String mLine;
            while ((mLine = reader.readLine()) != null){
                text.append(mLine);
                text.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        String htmlPage = text.toString();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.GERMANY);
        Date date = new Date();;

        htmlPage = htmlPage.replace("$LuPOContent", lupoContent);
        htmlPage = htmlPage.replace("$DateAndTime", dateFormat.format(date));
        htmlPage = htmlPage.replace("$Beratungsfehler", "");




        return htmlPage;
    }

    public void print(){
        doWebViewPrint();
    }

    public void addLineToHTML(String fachname, String fsJahrgang, String reihenfolge,
                              String ef1, String ef2,
                              String q1, String q2,
                              String q3, String q4, String af){

        StringBuilder builder = new StringBuilder();
        builder.append("<tr height=20 style='height:15.0pt'>")
                .append("<td height=20 class=xl1528744 style='height:15.0pt'>"+fachname+"</td>")
                .append("<td class=xl1528744>"+fsJahrgang+"</td>")
                .append("<td class=xl1528744>"+reihenfolge+"</td>")
                .append("<td class=xl1528744>"+ef1+"</td>")
                .append("<td class=xl1528744>"+ef2+"</td>")
                .append("<td class=xl1528744>"+q1+"</td>")
                .append("<td class=xl1528744>"+q2+"</td>")
                .append("<td class=xl1528744>"+q3+"</td>")
                .append("<td class=xl1528744>"+q4+"</td>")
                .append("<td class=xl1528744>"+af+"</td>")
                .append("</tr>");

        lupoContent = lupoContent + builder.toString();

    }
}
