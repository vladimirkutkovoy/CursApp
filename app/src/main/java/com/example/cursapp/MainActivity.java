package com.example.cursapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DecimalFormat;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {
    private Document doc;
    private Thread secThread;
    private Runnable runnable;
    private TextView textView;
    private Elements usd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();//убрать верхний бар
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidThreeTen.init(this);
        init();
    }

    private void init() {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeb();
            }
        };
        secThread = new Thread(runnable);
        secThread.start();
    }

    private void getWeb() {

        try {

            String in = Jsoup.connect("https://www.cbr-xml-daily.ru/daily_json.js").ignoreContentType(true).execute().body();
            JSONObject reader = new JSONObject(in);

            String usd = reader.getJSONObject("Valute").getJSONObject("USD").getString("Value");
            String usd_prev = reader.getJSONObject("Valute").getJSONObject("USD").getString("Previous");
            String trend_usd = "";
            String trend_eur = "";

            if (Float.parseFloat(usd) > Float.parseFloat(usd_prev))
                trend_usd = "↑";
            else if (Float.parseFloat(usd) < Float.parseFloat(usd_prev))
                trend_usd = "↓";

            String eur = reader.getJSONObject("Valute").getJSONObject("EUR").getString("Value");
            String eur_prev = reader.getJSONObject("Valute").getJSONObject("EUR").getString("Previous");

            if (Float.parseFloat(eur) > Float.parseFloat(eur_prev))
                trend_eur = "↑";
            else if (Float.parseFloat(eur) < Float.parseFloat(eur_prev))
                trend_eur = "↓";
            //*************************************************************************************************************************************************

            String cur_date = reader.getString("Date").split("T")[0];//получаем дату из строки 2021-08-14T11:30:00+03:00

            LocalDate tmp_date = LocalDate.parse(cur_date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));//дата последнего обновления курска
            LocalDate date_now = LocalDate.now(); // текущая дата

            if (date_now.isEqual(tmp_date))
                cur_date = "сегодня";
            else if (date_now.plusDays(1).isEqual(tmp_date))
                cur_date = "завтра";
            else
                cur_date = tmp_date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));//yyyy-MM-dd -> dd.MM.yyyy


            Log.d("Log1", usd.toString() + " " + cur_date + " " + date_now);
            //USD
            /*doc = Jsoup.connect("https://cbr.ru/scripts/XML_daily.asp?").get();
            String date = doc.childNodes().get(1).attributes().get("Date");

            Elements data1 = doc.getElementsByAttributeValue("ID", "R01235");
            usd = data1.get(0).getElementsByTag("Value");*/

            //USD
            /*data1 = doc.getElementsByAttributeValue("ID", "R01239");
            Elements eur = data1.get(0).getElementsByTag("Value");*/

            /*Elements tables = doc.getElementsByTag("Valute");
            Element table = tables.get(10);
            Elements val = table.getElementsByTag("Value");*/

            //Log.d("Log1", usd.text() + " " + eur.text() + " " + date);

            EditText text1 = (EditText) findViewById(R.id.input);
            EditText text2 = (EditText) findViewById(R.id.input2);

            int my_val = 0;
            int rub = 0;
            if (!text1.getText().toString().equals(""))
                my_val = Integer.parseInt(text1.getText().toString());

            if (!text2.getText().toString().equals(""))
                rub = Integer.parseInt(text2.getText().toString());

            float usd_val = my_val * Float.parseFloat(usd.replace(',', '.')) + rub;


            text2.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    boolean consumed = false;
                    if (keyCode == 66) {
                        //Делаем то, что нам нужно...
                        init();
                        consumed = true; //это если не хотим, чтобы нажатая кнопка обрабатывалась дальше видом, иначе нужно оставить false
                    }
                    return consumed;
                }
            });

            String tmp = cur_date;
            String tr_usd = trend_usd;
            String tr_eur = trend_eur;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Stuff that updates the UI
                    textView = (TextView) findViewById(R.id.t);
                    //textView.setText("Курс валют: на" + date + "\nUSD:" + usd.text() + " р." + "\n" + new DecimalFormat("###,###").format((int) usd_val) + " р.");

                    textView.setText(String.format("Курс валют на %s:\nUSD: %s р.%s EUR: %s р.%s\n%s р.", tmp, usd, tr_usd, eur, tr_eur, new DecimalFormat("###,###").format((int) usd_val)));
                }
            });
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Stuff that updates the UI
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Интернет соединение отсутствует",
                            Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            });
        }
    }
}