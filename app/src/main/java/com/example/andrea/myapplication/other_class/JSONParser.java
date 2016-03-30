package com.example.andrea.myapplication.other_class;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class JSONParser {

    private InputStream is;
    private JSONObject jObj;
    private String json;
    private URLConnection urlConnection;
    private String charset = "UTF-8";

    public JSONParser() {

    }

    public JSONObject getJSONFromUrl(String url) {

        try {

            urlConnection = new URL(url).openConnection();
            is = urlConnection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);

            StringBuilder sb = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();

            json = sb.toString();

        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jObj;
    }

    public JSONObject makeHttpRequest(String url, String method, String params) {

        try {

            if(method.equals("POST")){

                urlConnection = new URL(url).openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Accept-Charset", charset);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);

                OutputStream stream = urlConnection.getOutputStream();
                try {
                    stream.write(params.getBytes(charset));
                }finally {
                    if (stream !=null) stream.close();
                }

                is = urlConnection.getInputStream();

            }else if(method.equals("GET")){

                urlConnection = new URL(url + "?" + params).openConnection();
                urlConnection.setRequestProperty("Accept-Charset", charset);
                is = urlConnection.getInputStream();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jObj;
    }
}


