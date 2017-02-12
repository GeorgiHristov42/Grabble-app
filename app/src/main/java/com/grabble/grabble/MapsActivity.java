package com.grabble.grabble;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener ,
        LocationListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private boolean audioEnabled;
    private boolean vibrationEnabled;
    private boolean hardModeEnabled;

    private Vibrator vib;
    private MediaPlayer mp;

    public static final String TAG = "MapsActivity";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private ArrayList<String> inventory = new ArrayList<String>();
    public final int PERMISSION_LOCATION_REQUEST_CODE = 101;


    public boolean getNewMapFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Get settings
        SharedPreferences settings = getSharedPreferences(getResources().getString(R.string.MY_SETTINGS_PREF), MODE_PRIVATE);
        audioEnabled = settings.getBoolean("isAudioEnabled", true);
        vibrationEnabled = settings.getBoolean("isVibrationEnabled", true);
        hardModeEnabled = settings.getBoolean("isHardModeEnabled", false);
        vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mp = MediaPlayer.create(this, R.raw.marker_success);

        //Retrieve collected letters
        SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
        Gson gson = new Gson();
        String json = inventoryPref.getString("inventoryJson", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        if (json != null) {
            inventory = gson.fromJson(json, type);
        }
        //Log.i(TAG, "OnCreate Inventory array list " + inventory.toString());

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener( this )
            .addApi(LocationServices.API)
            . build () ;
            }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        LocationSettingsRequest. Builder builder = new LocationSettingsRequest.Builder ()
                    .addLocationRequest(mLocationRequest);


        String stringUrl = getStringUrl();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i(TAG, "Connected - downloading markers");
            new DownloadMarkersTask().execute(stringUrl);
        } else {
            Toast.makeText(MapsActivity.this, "Please enable internet",
                    Toast.LENGTH_LONG).show();
                Log.i(TAG, "Not connected to internet");
            }

    }

    // Check when game was last played and
    // decide whether to download new markers map
    // or continue previous session
    private String getStringUrl() {
        Calendar calendar = Calendar.getInstance();

        getNewMapFlag = true;
        String stringUrl = null ;

        SharedPreferences calendarPref = getSharedPreferences(getResources().getString(R.string.MY_CALENDAR_PREF), MODE_PRIVATE);
        SharedPreferences.Editor editorCalendar = calendarPref.edit();

        int lastDayPlayed = calendarPref.getInt("Day", -1);
        int lastMonthPlayed = calendarPref.getInt("Month",-1);
        int lastYearPlayed = calendarPref.getInt("Year",-1);

        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        Log.i(TAG, Integer.toString(currentDay) + " " + Integer.toString(currentMonth) + " " + Integer.toString(currentYear));
        Log.i(TAG, Integer.toString(lastDayPlayed) + " " + Integer.toString(lastMonthPlayed) + " " + Integer.toString(lastYearPlayed));

        if(lastDayPlayed != -1 || lastMonthPlayed != -1 || lastYearPlayed != -1){
            if(currentDay == lastDayPlayed && currentMonth == lastMonthPlayed && currentYear == lastYearPlayed){
                getNewMapFlag = false;
                Log.i(TAG, "Same date, continue previous session");
            }
            else {
                editorCalendar.putInt("Day", currentDay );
                editorCalendar.putInt("Month",currentMonth);
                editorCalendar.putInt("Year",currentYear);
                editorCalendar.commit();
                Log.i(TAG, "New date, new session: download map");
            }
        }
        else {
            editorCalendar.putInt("Day", currentDay );
            editorCalendar.putInt("Month",currentMonth);
            editorCalendar.putInt("Year",currentYear);
            editorCalendar.commit();
            Log.i(TAG, "New date, new session: download map");
        }

        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Log.i(TAG, "Current date " + String.valueOf(currentDayOfWeek));

        switch(currentDayOfWeek){
            case Calendar.SUNDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/sunday.kml";
                Log.i(TAG, "Sunday map to download");
                break;
            case Calendar.MONDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/monday.kml";
                Log.i(TAG, "Monday map to download");
                break;
            case Calendar.TUESDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/tuesday.kml";
                Log.i(TAG, "Tuesday map to download");
                break;
            case Calendar.WEDNESDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/wednesday.kml";
                Log.i(TAG, "Wednesday map to download");
                break;
            case Calendar.THURSDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/thursday.kml";
                Log.i(TAG, "Thursday map to download");
                break;
            case Calendar.FRIDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/friday.kml";
                Log.i(TAG, "Friday map to download");
                break;
            case Calendar.SATURDAY:
                stringUrl = "http://www.inf.ed.ac.uk/teaching/courses/selp/coursework/saturday.kml";
                Log.i(TAG, "Saturday map to download");
                break;
        }
        return stringUrl;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.i(TAG, "Resume maps activity");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Pause maps activity");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        Log.i(TAG, "Stop maps activity");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        Log.i(TAG, "Map ready");

        mMap.setMaxZoomPreference(21);
        mMap.setMinZoomPreference(20);

        mMap.getUiSettings().setScrollGesturesEnabled(false);
    }


    @Override
    public boolean onMarkerClick(final Marker marker) {

        Log.i(TAG, "Marker clicked");

        if(vibrationEnabled) {
            vib.vibrate(100);
        }
        if(audioEnabled) {
            mp.start();
        }

        if(hardModeEnabled && inventory.size() >= 25) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            alertDialogBuilder.setMessage("Inventory full. Please delete a letter from your inventory " +
                    "or switch to regular mode");
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
        else {
            SharedPreferences markersPref = getSharedPreferences(getResources().getString(R.string.MY_MARKERS_PREF), MODE_PRIVATE);
            final SharedPreferences.Editor editorMarkers = markersPref.edit();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            alertDialogBuilder.setMessage("You collected the letter " + marker.getTitle());
            alertDialogBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            editorMarkers.putBoolean(String.valueOf(marker.getSnippet()), true);
                            editorMarkers.commit();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            inventory.add(marker.getTitle());

            // Update letter inventory with new letter
            SharedPreferences inventoryPref = getSharedPreferences(getResources().getString(R.string.MY_INVENTORY_PREF), MODE_PRIVATE);
            SharedPreferences.Editor editorInventory = inventoryPref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(inventory);
            editorInventory.putString("inventoryJson", json);
            editorInventory.commit();

            marker.remove();
        }

        return true;
    }

    //==============================================================================================
    // Current Location
    //==============================================================================================

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (!checkPermission(this)) {
            Log.i(TAG, "Getting location permission.");

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
            }
        else {
            Log.i(TAG,"Have location permission");
//            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//            if (location == null) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//            }
//            else {
//                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//                //handleNewLocation(location);
//            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE: {
                Log.i(TAG,"Permission granted, getting location");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG,"Not all permission granted");

                    if (!checkPermission(this)) {
                        Log.i(TAG, "Have location permission");
//                        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//                        if (location == null) {
//                            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//                        } else {
//                            handleNewLocation(location);
//                        }

                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Please give us a permission",
                            Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng currentlatLng = new LatLng(currentLatitude, currentLongitude);



        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
        else {

            Log.i(TAG,"Cannot handleNewLocation, no permission");
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatLng));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatLng));
//        mMap.setMaxZoomPreference(21);
//        mMap.setMinZoomPreference(20);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
            Toast.makeText(MapsActivity.this, "Connection failed. Please enable internet.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        handleNewLocation(location);
    }

    //==============================================================================================
    // Download and parse markers
    //==============================================================================================

    private class DownloadMarkersTask extends AsyncTask<String, Void, List<Placemark>> {
        @Override
        protected List<Placemark> doInBackground(String... urls) {
            List<Placemark> fail = null;
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                Log.i(TAG, "IOException at loadingXmlFromNetwork");
                return fail;   // TO DO handle exception
            } catch (XmlPullParserException e) {
                Log.i(TAG, "XMLPullParserException at loadingXml");
                return fail;   // TO DO handle exception
            }
        }

        private List<Placemark> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            MarkersXmlParser xmlParser = new MarkersXmlParser();
            List<Placemark> placemarks = null;
            String name = null;
            String point = null;
            String description = null;
            Log.i(TAG, "Begin loadXmlFromNetwork");

            try {
                stream = downloadUrl(urlString);
                placemarks = xmlParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            Log.i(TAG, "Markers downloaded and parsed");
            return placemarks;
        }

        // Given a string representation of a URL, sets up a connection and gets
// an input stream.
        private InputStream downloadUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            Log.i(TAG, "Markers downloaded");
            return conn.getInputStream();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<Placemark> placemarks) {
            Log.i(TAG, "Placing markers on map");

            SharedPreferences markersPref = getSharedPreferences(getResources().getString(R.string.MY_MARKERS_PREF), MODE_PRIVATE);
            SharedPreferences.Editor editorMarkers = markersPref.edit();


            if(placemarks != null) {
                if(getNewMapFlag == true) {
                    Log.i(TAG, "Placing markers for first time");
                    for (Placemark placemark : placemarks) {
                        editorMarkers.putBoolean(String.valueOf(placemark.name), false);
                        editorMarkers.commit();
                        mMap.addMarker(new MarkerOptions().position(placemark.point).title(placemark.description).snippet(String.valueOf(placemark.name)));
                    }
                }
                else
                {
                    Log.i(TAG, "Continue previous session, put previous markers");
                    for (Placemark placemark : placemarks) {
                        boolean hasMarkerBeenClicked = markersPref.getBoolean(String.valueOf(placemark.name), false);
                        if (hasMarkerBeenClicked == false) {
                            mMap.addMarker(new MarkerOptions().position(placemark.point).title(placemark.description).snippet(String.valueOf(placemark.name)));
                        }
                    }
                }
            }
            else {
                Toast.makeText(MapsActivity.this, "Letter markers download error",
                        Toast.LENGTH_LONG).show();
            }
            Log.i(TAG, "Markers placed on map");
            //Log.i(TAG, "Example placemark" + placemarks.get(1).point);
        }
    }
}
