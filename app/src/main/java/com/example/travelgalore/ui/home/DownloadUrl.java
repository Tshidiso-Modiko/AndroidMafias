package com.example.travelgalore.ui.home;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUrl {
    private static final String TAG = "Download Url";
    public String ReadUrl(String placeURL) throws IOException {
        Log.d(TAG, "Variables: initialised");
        String Data = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try
        {
            Log.d(TAG, "Read Url: started");
            URL url = new URL(placeURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "Read Url: connection opened");
            httpURLConnection.connect();
            Log.d(TAG, "Read Url: connected");
            inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();

            String line = "";

            while( (line = bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }
            Data = stringBuffer.toString();
            bufferedReader.close();
            Log.d(TAG, "Read Url: successful");
        }
        catch (MalformedURLException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            inputStream.close();
            httpURLConnection.disconnect();
        }
        Log.d(TAG, "Read Url: Data returned");
        return Data;

    }
}
