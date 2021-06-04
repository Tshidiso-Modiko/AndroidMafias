package com.example.travelgalore.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.travelgalore.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.Distance;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private static final String FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String ApiKey = "AIzaSyDhozPnG6H8AXN1sFPOzv5OIvBYSmPzV8M";
    public static LatLng My_Location;
    private GoogleMap mMap;
    private static final String TAG = "HomeFragment";
    private Boolean mLocationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;


    //widgets
    private ImageView mGps;
    private ImageView Places;
    private ImageView pointOfInterest;
    private ImageView Museum;
    private ImageView Restaurant;
    private TextView  tvPointOfInterest;
    private TextView  tvMuseum;
    private TextView  tvRestaurant;




    //vars
    private GeoApiContext mGeoApiContext = null;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private double latitude, longitude;
    private int ProximityRadius = 10000;
    private Polyline currentPolyline;
    private ArrayList<PolylineData> mPolylinesData = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                getLocationPermission();
            }
        });
        getLocationPermission();
        return root;
    }

    /*
---------------------------------------Requesting Permission for Gps---------------------------------------------------------------------
 */
    protected void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permission");
        String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(super.getContext(), ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(super.getContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(super.getContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "getLocationPermission: location permission granted");
                    mLocationPermissionsGranted = true;
                    initMap();
                } else {
                    ActivityCompat.requestPermissions(super.getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
                }
            } else {
                ActivityCompat.requestPermissions(super.getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(super.getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called. ");
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    /*
----------------------------------initialize Map-------------------------------------------------------------------------
 */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Log.d(TAG, "initMap: initializing fragment");
        // mSearchText = (EditText) super.getActivity().findViewById(R.id.input_search);
        Log.d(TAG, "initMap: initializing  input_search");
        mGps = (ImageView) super.getActivity().findViewById(R.id.ic_gps);
        Places = (ImageView) super.getActivity().findViewById(R.id.ic_explore);
        Museum = (ImageView) super.getActivity().findViewById(R.id.ic_museum);
        Restaurant =(ImageView) super.getActivity().findViewById(R.id.ic_restaurant);
        pointOfInterest = (ImageView) super.getActivity().findViewById(R.id.ic_interest);
        tvMuseum = (TextView) super.getActivity().findViewById(R.id.tv_museum);
        tvRestaurant =(TextView) super.getActivity().findViewById(R.id.tv_restaurant);
        tvPointOfInterest = (TextView) super.getActivity().findViewById(R.id.tv_interest);


        Log.d(TAG, "initMap: initializing mGps ");

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "initMap: initializing googleMap");
                Toast.makeText(getContext(), "Map is ready, Click map to Start", Toast.LENGTH_LONG).show();
                String stRestaurants = "restaurant";
                String stMuseum = "museum";
                String stInterestPoint = "point_of_interest";
               Object transferData[] = new Object[2];
               GetNearbyPlaces getNearbyPlaces = new GetNearbyPlaces();
                Restaurant.setVisibility(View.INVISIBLE);
                tvRestaurant.setVisibility(View.INVISIBLE);
                Museum.setVisibility(View.INVISIBLE);
                tvMuseum.setVisibility(View.INVISIBLE);
                pointOfInterest.setVisibility(View.INVISIBLE);
                tvPointOfInterest.setVisibility(View.INVISIBLE);

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Log.d(TAG, "initMap: initializing mMap too");
                        if (ActivityCompat.checkSelfPermission(HomeFragment.super.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeFragment.super.getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            getLocationPermission();
                        } else if (ActivityCompat.checkSelfPermission(HomeFragment.super.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(HomeFragment.super.getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(HomeFragment.super.getContext(),
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            mMap = googleMap;
                            mMap.setMyLocationEnabled(true);
                            getDeviceLocation();
                            //geoLocate();
                            //mMap.setOnPolylineClickListener(HomeFragment.this::onPolylineClick);
                            mMap.setOnPolylineClickListener(HomeFragment.this::onPolylineClick);
                            mGps.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.d(TAG, "onClick: clicked gps icon");
                                    getDeviceLocation();
                                }
                            });
                            Places.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.clear();
                                    Log.d(TAG, "onClick: clicked explore icon");
                                    Restaurant.setVisibility(View.VISIBLE);
                                    tvRestaurant.setVisibility(View.VISIBLE);
                                    Museum.setVisibility(View.VISIBLE);
                                    tvMuseum.setVisibility(View.VISIBLE);
                                    pointOfInterest.setVisibility(View.VISIBLE);
                                    tvPointOfInterest.setVisibility(View.VISIBLE);

                                }
                            });
                            Restaurant.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.clear();
                                    String url = getUrl(latitude,longitude, stRestaurants);
                                    transferData[0] = mMap;
                                    transferData [1] = url;
                                    getNearbyPlaces.execute(transferData);
                                    Toast.makeText(getContext(), "searching for restaurants...", Toast.LENGTH_LONG).show();
                                    Toast.makeText(getContext(), "showing  nearby restaurants", Toast.LENGTH_LONG).show();
                                    Restaurant.setVisibility(View.INVISIBLE);
                                    tvRestaurant.setVisibility(View.INVISIBLE);
                                    Museum.setVisibility(View.INVISIBLE);
                                    tvMuseum.setVisibility(View.INVISIBLE);
                                    pointOfInterest.setVisibility(View.INVISIBLE);
                                    tvPointOfInterest.setVisibility(View.INVISIBLE);
                                }
                            });
                            Museum.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.clear();
                                    String url = getUrl(latitude,longitude, stMuseum);
                                    transferData[0] = mMap;
                                    transferData [1] = url;
                                    getNearbyPlaces.execute(transferData);
                                    Toast.makeText(getContext(), "searching for museums...", Toast.LENGTH_LONG).show();
                                    Toast.makeText(getContext(), "showing  nearby museums", Toast.LENGTH_LONG).show();
                                    Restaurant.setVisibility(View.INVISIBLE);
                                    tvRestaurant.setVisibility(View.INVISIBLE);
                                    Museum.setVisibility(View.INVISIBLE);
                                    tvMuseum.setVisibility(View.INVISIBLE);
                                    pointOfInterest.setVisibility(View.INVISIBLE);
                                    tvPointOfInterest.setVisibility(View.INVISIBLE);
                                }
                            });
                            pointOfInterest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mMap.clear();
                                    String url = getUrl(latitude,longitude, stInterestPoint);
                                    transferData[0] = mMap;
                                    transferData [1] = url;
                                    getNearbyPlaces.execute(transferData);
                                    Toast.makeText(getContext(), "searching for points of interest...", Toast.LENGTH_LONG).show();
                                    Toast.makeText(getContext(), "showing  nearby points of interest", Toast.LENGTH_LONG).show();
                                    Restaurant.setVisibility(View.INVISIBLE);
                                    tvRestaurant.setVisibility(View.INVISIBLE);
                                    Museum.setVisibility(View.INVISIBLE);
                                    tvMuseum.setVisibility(View.INVISIBLE);
                                    pointOfInterest.setVisibility(View.INVISIBLE);
                                    tvPointOfInterest.setVisibility(View.INVISIBLE);
                                }
                            });

                            if (mGeoApiContext == null) {
                                Log.d(TAG, "mGeoApiContext: initializing GoeApiContext");
                                mGeoApiContext = new GeoApiContext.Builder()
                                        .apiKey(ApiKey)
                                        .build();
                                Log.d(TAG, "mGeoApiContext: initialized GoeApiContext successfully");
                            }

                        } else {
                            return;
                        }

                        //init();
                    }
                });
            }
        });
    }

    /*
----------------------------------get Device Location-------------------------------------------------------------------------
*/
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting device current location");
         mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(super.getContext());

        try {
            if (mLocationPermissionsGranted) {

                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            My_Location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            latitude = currentLocation.getLatitude();
                            longitude =  currentLocation.getLongitude();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(HomeFragment.super.getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }
/*
----------------------------------Move Camera-------------------------------------------------------------------------
 */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.clear();
            mMap.addMarker(options);
            mMap.setOnMarkerClickListener(marker -> {
                    calculateDirections(latLng);
                return false;
            });
            //calculateDirections(latLng);
        }
    }
/*
    --------------------------------------------get pLaces url------------------------------------------
*/
  private  String getUrl(double latitude, double longitude, String nearbyPlace){
     StringBuffer googleURL = new StringBuffer("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
     googleURL.append("location=" + latitude + "," +  longitude);
     googleURL.append("&radius=" +  ProximityRadius);
     googleURL.append("&type=" + nearbyPlace);
      googleURL.append("&sensor=false");
      googleURL.append("&key=" + ApiKey );
      Log.d(TAG, "googleUrl = " + googleURL.toString());
      return  googleURL.toString();
    }
/*
    ----------------------------------pollylines-----------------------------------------------------------
*/

    private void calculateDirections(LatLng latLng) {
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
                    polyline.setColor(ContextCompat.getColor(getActivity(), R.color.grey));
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
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.teal_200));
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
                polylineData.getPolyline().setColor(ContextCompat.getColor(getActivity(), R.color.grey));
                polylineData.getPolyline().setZIndex(0);
            }
        }

        };

    }


