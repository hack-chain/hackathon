package com.example.dmitry.ftm;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetAndSetBalanceAsync extends AsyncTask<String, String, String> {

    private HttpURLConnection _urlConnection;
    WeakReference<TextView> _view;

    @Override
    protected String doInBackground(String... args) {

        StringBuilder result = new StringBuilder();

        try {
            String publicKey = args[0];

            URL url = new URL("http://18.221.128.6:8080/account/" + publicKey);
            _urlConnection = (HttpURLConnection) url.openConnection();

            InputStream in = new BufferedInputStream(_urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _urlConnection.disconnect();
        }


        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject mainObject = new JSONObject(result);
            String balance = mainObject.getString("balance");

            if (_view.get() != null) {
                _view.get().setText("Balance: " + balance + " FTM");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}