package com.example.travelgalore.ui.home;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    private static final String TAG = "DataParse";
    private HashMap<String,String> getSinglePlace(JSONObject googlePlaceJSON){
        Log.d(TAG, "getSinglePlace: started");
        HashMap<String, String> googlePLaceMap = new HashMap<>();
        String NameOfPlace = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";


        try {
           if(!googlePlaceJSON.isNull("name")){
               Log.d(TAG, "getSinglePlace: JSON name is " + googlePlaceJSON.getString("name"));
               NameOfPlace = googlePlaceJSON.getString("name");
           }
           else{
               Log.d(TAG, "getSinglePlace: JSON name is Null");
           }
           if(!googlePlaceJSON.isNull("vicinity ")){
                Log.d(TAG, "getSinglePlace: JSON vicinity is  " + googlePlaceJSON.getString("vicinity"));
                vicinity  = googlePlaceJSON.getString("vicinity");
           }
           else{
                Log.d(TAG, "getSinglePlace: JSON vicinity is Null");
           }
            latitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference  = googlePlaceJSON.getString("reference");
            googlePLaceMap.put("place_name", NameOfPlace);
            googlePLaceMap.put("vicinity", vicinity);
            googlePLaceMap.put("lat",  latitude);
            googlePLaceMap.put("lng",  longitude);
            googlePLaceMap.put("reference",  reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePLaceMap;
    }

    private List<HashMap<String, String>> getAllNearbyPlaces(JSONArray jsonArray){
        int counter = jsonArray.length();
        Log.d(TAG, "jsonArray Length: " + jsonArray.length());
        List<HashMap<String, String>> nearbyPlacesList = new ArrayList<>();
        HashMap<String, String> NearbyPlacesMap;
        for(int i = 0; i<counter; i++){
            try {
                NearbyPlacesMap =  getSinglePlace((JSONObject) jsonArray.get(i));
                nearbyPlacesList.add(NearbyPlacesMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "NearByPlaceList: " + nearbyPlacesList);
        return nearbyPlacesList;
    }

    public List<HashMap<String, String>> parse(String jsonData){
        Log.d(TAG, "Parse: Pares Method stated ");
        JSONArray jsonArray = null;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
            Log.d(TAG, "Parse: jsonArray and Object assigned");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Parse: ALl near by places returned");
        return getAllNearbyPlaces(jsonArray);
    }
}
