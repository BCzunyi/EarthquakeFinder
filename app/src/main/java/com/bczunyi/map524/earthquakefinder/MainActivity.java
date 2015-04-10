package com.bczunyi.map524.earthquakefinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    static ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
    static GoogleApiClient mGoogleApiClient;
    static boolean isResolvingError = false;

    JSONObject jObject;
    LatLng currentLocation;
    FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    LocationManager locationManager;
    NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_about: {
                showAbout();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAbout() {
        // Inflate the about message contents
        View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.app_icon);
        builder.setTitle(R.string.app_name);
        builder.setView(messageView);
        builder.create();
        builder.show();
    }

    public void pullXml(View view) throws XmlPullParserException, IOException, URISyntaxException {
        quakes.clear();
        XmlGrabber xmlGrabber = new XmlGrabber();
        RadioButton r1 = (RadioButton) findViewById(R.id.radioButton1);
        RadioButton r2 = (RadioButton) findViewById(R.id.radioButton2);
        RadioButton r3 = (RadioButton) findViewById(R.id.radioButton3);
        int days = r1.isChecked() ? 7 : r2.isChecked() ? 30 : 365;
        xmlGrabber.execute("http://www.earthquakescanada.nrcan.gc.ca/api/earthquakes/latest/" + days + "d.json");
    }

    public void getLoc(View view) {
        Toast.makeText(getBaseContext(), "Retrieving GPS location", Toast.LENGTH_LONG).show();
        updateLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        criteria.setAccuracy( Criteria.ACCURACY_FINE );
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, false);
        Log.i( "LOCTAION", provider); // logging
        Location location = locationManager.getLastKnownLocation( provider );*/
        EditText latField = (EditText) findViewById(R.id.latField);
        EditText lonField = (EditText) findViewById(R.id.lonField);
        latField.setText(Double.toString(currentLocation.latitude));
        lonField.setText(Double.toString(currentLocation.longitude));
    }

    boolean updateLocation(Location location){
        Location loc = location;
        if(loc != null){
            currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
            return true;
        }else{
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc == null) loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(loc == null) loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(loc != null){
                currentLocation = new LatLng(loc.getLatitude(),loc.getLongitude());
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnected(Bundle arg0) {
        ///////////////////////////////////////////////////
        // Following block of code is used to enable GPS //
        ///////////////////////////////////////////////////
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Check GPS location provider
        boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabledGPS) {
            // Build a dialog to ask a user to enable GPS
            AlertDialog.Builder gpsEnableRequest = new AlertDialog.Builder(this);

            gpsEnableRequest.setTitle("Enable GPS?").setMessage("Your precise location is required for EarthquakeFinder to function properly");
            gpsEnableRequest.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            gpsEnableRequest.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            gpsEnableRequest.create().show();
        }
        ///////////////////////////////////////////////////
        ///////////////////// END /////////////////////////
        ///////////////////////////////////////////////////
        updateLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // This code is taken straight from Accessing Google Play APIs tutorial at http://developer.android.com/google/auth/api-client.html //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (isResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                isResolvingError = true;
                result.startResolutionForResult(this, 3);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());

            isResolvingError = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // This code is taken straight from Accessing Google Play APIs tutorial at http://developer.android.com/google/auth/api-client.html //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt("google_play_services_error", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // This code is taken straight from Accessing Google Play APIs tutorial at http://developer.android.com/google/auth/api-client.html //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        isResolvingError = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // This code is taken straight from Accessing Google Play APIs tutorial at http://developer.android.com/google/auth/api-client.html //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt("google_play_services_error");
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), 3);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();

            // Close the application, since Play Services are essential for this application
            // And show a toast explaining why app is being closed
            Toast.makeText(getActivity(), "This application cannot function without Google Play Services", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    private class XmlGrabber extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... url) {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url[0]);
            InputStream content = null;
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    content = entity.getContent();

                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(content, "UTF-8"), 8);
                        StringBuilder sb = new StringBuilder();

                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        String result = sb.toString();
                        jObject = new JSONObject(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (content != null) content.close();
                        } catch (Exception squish) {
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to download earthquake data", Toast.LENGTH_LONG).show();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return content;
        }

        @Override
        protected void onPostExecute(InputStream result) {
            super.onPostExecute(result);
            Iterator<String> iter = jObject.keys();
            boolean test = true;
            while (iter.hasNext()) {
                String key = iter.next();

                if (!key.equals("metadata")) {
                    try {
                        JSONObject value = jObject.getJSONObject(key);
                        Earthquake temp = new Earthquake(value.getString("origin_time"), value.getJSONObject("location").getString("en"), value.getJSONObject("geoJSON").getJSONArray("coordinates").getDouble(0), value.getJSONObject("geoJSON").getJSONArray("coordinates").getDouble(1), value.getDouble("depth"), value.getDouble("magnitude"), value.getString("magnitude_type"));
                        quakes.add(temp);
                    } catch (JSONException e) {
                    }
                }
            }
            try {
                Intent intent = new Intent(MainActivity.this, QuakeListActivity.class);
                double lat = Double.parseDouble(((EditText) findViewById(R.id.latField)).getText().toString());
                double lon = Double.parseDouble(((EditText) findViewById(R.id.lonField)).getText().toString());
                float dist = Float.parseFloat(((EditText) findViewById(R.id.distField)).getText().toString());
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("dist", dist * 1000.0f);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to download earthquake data", Toast.LENGTH_LONG).show();
                Log.d("Main", "exception", e);
            }
        }
    }

    public InputStream getUrlData(String url)
            throws URISyntaxException, ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(new URI(url));
        HttpResponse res = client.execute(method);
        return res.getEntity().getContent();
    }
}
