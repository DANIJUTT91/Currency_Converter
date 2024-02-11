package com.example.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public String result;
    Button convert_btn;
    Spinner amount_sp,converted_amount_sp;
    EditText amount_ed,converted_amount_ed;
    ProgressBar progressBar;

    @SuppressLint("StaticFieldLeak")
    public class BG extends AsyncTask<String, Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            if(!internetConnected()){
                Toast.makeText(MainActivity.this, "Internet Not Connected", Toast.LENGTH_SHORT).show();
                cancel(true);
            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            result = s;
            convert_btn.setEnabled(true);
            String[] currencies = jsonKeysToArray();
            load_spinner(amount_sp,currencies);
            amount_sp.setSelection(149); //Select USD as default
            load_spinner(converted_amount_sp,currencies);
            converted_amount_sp.setSelection(114); //Select PKR as default
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {

            Log.d("BG","DoInBackGround");

            URL url;
            HttpURLConnection urlConnection;
            String result = "";
            try {
                url = new URL(strings[0]);
                urlConnection= (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }


            } catch (IOException e) {
                e.printStackTrace();
                return "Something went wrong";

            }
            return result;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        convert_btn = findViewById(R.id.buttonConvert);
        amount_ed = findViewById(R.id.editTextTextPersonName);
        converted_amount_ed = findViewById(R.id.editTextTextPersonName2);
        amount_sp = findViewById(R.id.spinner);
        converted_amount_sp = findViewById(R.id.spinner2);
        progressBar = findViewById(R.id.progressBar);

        convert_btn.setEnabled(false);

        String url = "https://openexchangerates.org/api/latest.json?app_id=66566ce2ac4a4623afe6b23ba922e6d2";
        BG myTask = new BG();
        myTask.execute(url);
        convert_btn.setOnClickListener(view -> {
            if(!amount_ed.getText().toString().isEmpty()) {
                String amountCur = amount_sp.getSelectedItem().toString();
                double amount = Double.parseDouble(amount_ed.getText().toString());
                String reqCur = converted_amount_sp.getSelectedItem().toString();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONObject ratesObject = jsonObject.getJSONObject("rates");
                    double from = ratesObject.getDouble(amountCur);
                    double to = ratesObject.getDouble(reqCur);
                    converted_amount_ed.setText((""+conversion(amount, from, to)));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage()+"", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, "Enter Amount", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private String[] jsonKeysToArray(){
        JSONObject jsonObject;
        String[] currencyArray;
        try {
            jsonObject = new JSONObject(result);
            JSONObject ratesObject = jsonObject.getJSONObject("rates");
            Iterator<String> keys = ratesObject.keys();
            List<String> currencyCodes = new ArrayList<>();
            while (keys.hasNext()) {
                String currencyCode = keys.next();
                currencyCodes.add(currencyCode);
            }
            currencyArray = currencyCodes.toArray(new String[0]);

        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return currencyArray;
    }
    private static double conversion(double amount,double from,double to){
        double factor = to/from;
        return amount*factor;
    }
    private void load_spinner(Spinner sp,String[] arr){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
    }
    private boolean internetConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        return connected;
    }
}