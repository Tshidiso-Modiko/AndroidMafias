package com.example.travelgalore.ui.home;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Distance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.travelgalore.R.color;
import static com.example.travelgalore.R.color.grey;
import static com.example.travelgalore.ui.home.HomeFragment.My_Location;

public class GetNearbyPlaces extends AsyncTask<Object, String, String> {
    private static final String TAG = "GetNearbyPlaces";
    private String googlePlaceData, url;
    private GoogleMap mMap;
    private GeoApiContext mGeoApiContext = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Polyline currentPolyline;
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();
    private static final String ApiKey = "AIzaSyDhozPnG6H8AXN1sFPOzv5OIvBYSmPzV8M";

    @Override
    protected String doInBackground(Object... objects) {

        mMap = (GoogleMap) objects[0];
        Log.d(TAG, "mMap: initialized");
        url = (String)  objects[1];
        Log.d(TAG, "url: initialized");

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlaceData = downloadUrl.ReadUrl(url);
            Log.d(TAG, "googlePlaceData : initialized");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "googlePlaceData : failed");
        }
        Log.d(TAG, "googlePlaceData : returned");
        return googlePlaceData;
    }
    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: started");
        List<HashMap<String, String>> nearByPlacesList = null;
        Log.d(TAG, "onPostExecute: nearByPlacesList initialized");
        DataParser dataParser = new DataParser();
        nearByPlacesList = dataParser.parse(s);
        Log.d(TAG, "onPostExecute: dataParser initialized");
        DisplayNearByPlaces(nearByPlacesList);
        Log.d(TAG, "onPostExecute: DisplayNearByPlaces called");
    }

    private void DisplayNearByPlaces (List<HashMap<String, String>> nearByPlacesList){
        Log.d(TAG, "DisplayNearByPlaces: DisplayNearByPlaces Started");
        Log.d(TAG, "DisplayNearByPlaces: DisplayNearByPlacesList = "+ nearByPlacesList.size());
        for (int i = 0; i<nearByPlacesList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            Log.d(TAG, "DisplayNearByPlaces: MarkerOptions init");
            HashMap<String, String>  googleNearbyPlace = nearByPlacesList.get(i);
            Log.d(TAG, "DisplayNearByPlaces: MarkerOptions place added");
            String nameOfPlace = googleNearbyPlace.get("place_name");
            Log.d(TAG, "DisplayNearByPlaces: place_name = " + googleNearbyPlace.get("place_name"));
            String vicinity = googleNearbyPlace.get("vicinity");
            Log.d(TAG, "DisplayNearByPlaces: vicinity = " + googleNearbyPlace.get("vicinity"));
            double lat = Double.parseDouble(googleNearbyPlace.get("lat"));
            Log.d(TAG, "DisplayNearByPlaces: lat = " + googleNearbyPlace.get("lat"));
            double lng= Double.parseDouble(googleNearbyPlace.get("lng"));
            Log.d(TAG, "DisplayNearByPlaces: lng = " + googleNearbyPlace.get("lng"));


            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(nameOfPlace + " : " + vicinity);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        }
        if (mGeoApiContext == null) {
            Log.d(TAG, "mGeoApiContext: initializing GoeApiContext");
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(ApiKey)
                    .build();
            Log.d(TAG, "mGeoApiContext: initialized GoeApiContext successfully");
        }
        mMap.setOnPolylineClickListener(this::onPolylineClick);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                calculateDirections( marker.getPosition());
                return false;
            }
        });

    }

    public void calculateDirections(LatLng latLng) {
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                latLng.latitude,
                latLng.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);

        directions.alternatives(true);
        directions.origin(
                new com.google.maps.model.LatLng(
                        My_Location.latitude,
                        My_Location.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage());

            }
        });
    }
    private void addPolylinesToMap(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                if (mPolylinesData.size() > 0) {
                    for (PolylineData polylineData : mPolylinesData) {
                        polylineData.getPolyline().remove();
                    }
                    mPolylinesData.clear();
                    mPolylinesData = new ArrayList<>();
                }
                double duration = 99999999;
                for (DirectionsRoute route : result.routes) {
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for (com.google.maps.model.LatLng latLng : decodedPath) {

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(grey);
                    polyline.setClickable(true);
                    mPolylinesData.add(new PolylineData(polyline, route.legs[0]));

                    double tempDuration = route.legs[0].duration.inSeconds;
                    if (tempDuration < duration) {
                        duration = tempDuration;
                        onPolylineClick(polyline);
                    }
                }
            }
        });
    }


    public void onPolylineClick(Polyline polyline) {
        int index = 0;
        Distance Distance = null;
        String endAddress = null;
        for (PolylineData polylineData : mPolylinesData) {
            index++;
            Log.d(TAG, "onPolylineClick: toString: " + polylineData.toString());
            if (polyline.getId().equals(polylineData.getPolyline().getId())) {
                polylineData.getPolyline().setColor(color.teal_200);
                polylineData.getPolyline().setZIndex(1);

                LatLng endLocation = new LatLng(
                        polylineData.getLeg().endLocation.lat,
                        polylineData.getLeg().endLocation.lng
                );
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(endLocation)
                        .title("Trip: #" + index)
                        .snippet("Duration: " + polylineData.getLeg().duration + '/' + " Distance: " + polylineData.getLeg().distance)

                );
                Log.d(TAG, "onPolylineClick: to int :Distance");
                Distance = polylineData.getLeg().distance;
                endAddress = polylineData.getLeg().endAddress;

                marker.showInfoWindow();
            } else {
                polylineData.getPolyline().setColor(color.teal_200);
                polylineData.getPolyline().setZIndex(0);
            }
        }

    };



}
