package com.scarfaiz.cluckinbell;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    public static MapView map;
    IMapController mapController;
    public boolean geodata_updated = false;
    MyLocationNewOverlay oMapLocationOverlay;
    Bitmap person_bitmap;
    Drawable person_drawable;
    BitmapFactory.Options person_options;
    GeoPoint startPoint;
    FloatingActionButton LocButton;
    int permsRequestCode;
    FrameLayout FrameLayoutBottom;
    TextView bottomSheetTextView;
    TextView bottomSheetTextViewSubtitle;
    FloatingActionButton NewMarkerButton;
    SharedPreferences prefs;
    String url;
    String tag = "LogDebug";

    int[] mDrawables = {
            R.drawable.cheese_3,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Configuration.getInstance().load(ctx, prefs);
        Configuration.getInstance().setUserAgentValue("CB");
        setContentView(R.layout.activity_main);
        permsRequestCode = 1;
        GetLocPermission();
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(13);
        startPoint = new GeoPoint(prefs.getFloat("Latitude",59.93863f), prefs.getFloat("Longitude",30.31413f));
        mapController.setCenter(startPoint);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //location marker overlay
        locationManager.removeUpdates(myLocationListener);
        oMapLocationOverlay = new MyLocationNewOverlay(map);
        oMapLocationOverlay.enableMyLocation();
        //oMapLocationOverlay.enableFollowLocation();
        oMapLocationOverlay.setDrawAccuracyEnabled(true);
        oMapLocationOverlay.setDirectionArrow(BitmapFactory.decodeResource(getResources(), R.drawable.direction_arrow),
                BitmapFactory.decodeResource(getResources(), R.drawable.direction_arrow));
        map.getOverlays().add(oMapLocationOverlay);
        //map.postInvalidate();
        /*person_options = new BitmapFactory.Options();
        person_options.inSampleSize = 4;
        person_drawable = getResources().getDrawable(R.drawable.ic_menu_mylocation);
        person_bitmap = ((BitmapDrawable) person_drawable).getBitmap();
        oMapLocationOverlay.setPersonIcon(person_bitmap);
        if (oMapLocationOverlay.isEnabled())
            Toast.makeText(getBaseContext(),"Enabled", Toast.LENGTH_LONG).show();*/
        //rotation
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);
        //asking for location

        int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            GetLocPermission();
        }

        //setting listener for locButton
        LocButton = (FloatingActionButton) findViewById(R.id.locButton);
        LocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLoc(lastLocation);
                    if (mapController.zoomIn())
                        mapController.zoomOut();
                    return;
                } else {
                    GetLocPermission();
                    return;
                }
            }
        });

        /**
         * If we want to listen for states callback
         */
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        LocButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                LocButton.setVisibility(View.INVISIBLE);
            }
        });
        AppBarLayout mergedAppBarLayout = (AppBarLayout) findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("Информация о месте");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });

        bottomSheetTextView = (TextView) bottomSheet.findViewById(R.id.bottom_sheet_title);
        FrameLayoutBottom = (FrameLayout)findViewById(R.id.dummy_framelayout_replacing_map);

        NewMarkerButton = (FloatingActionButton) findViewById(R.id.new_marker_button);

        NewMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewMarkerActivity.class);
                startActivity(intent);
            }
        });

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                //Toast.makeText(getBaseContext(),p.getLatitude() + " - "+p.getLongitude(), Toast.LENGTH_LONG).show();

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                ItemPagerAdapter adapter = new ItemPagerAdapter(MainActivity.this,mDrawables);
                ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
                viewPager.setAdapter(adapter);
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                url = "http://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + p.getLatitude() + "&lon=" + p.getLongitude() + "&zoom=18&addressdetails=1";
                Log.d(tag, "Sending request to: " + url);
                new GetUrlContentTask().execute(url);
                Marker startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                map.invalidate();
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

    }
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        int permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
            return;
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        locationManager.removeUpdates(myLocationListener);
    }

    private void updateLoc(Location loc) {
        int permission = PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
            mapController.animateTo(locGeoPoint);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            float Latitude = (float)loc.getLatitude();
            prefs.edit().putFloat("Latitude", Latitude);
            float Longitude = (float)loc.getLongitude();
            prefs.edit().putFloat("Latitude", Longitude);
            prefs.edit().apply();
        } else GetLocPermission();

    }

    private LocationListener myLocationListener
            = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (!geodata_updated) {
                //updateLoc(location);
                GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(locGeoPoint);
                mapController.zoomTo(17);
                map.invalidate();
                geodata_updated = true;
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    };

    public void GetLocPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermissions();
        } else {
            Toast.makeText(MainActivity.this, "Для работы приложения необходимо предоставить разрешение на использование геопозиции", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
        }
    }

    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), permsRequestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        //waitAndNavigateToOnboardingTutorial();
                    } else {
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        // show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Camera, Storage and Location Services Permission required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // proceed with logic by disabling the related features or quit the app.
                                            break;
                                    }
                                }
                            });
                        } //permission is denied (and never ask again is checked)
                        // shouldShowRequestPermissionRationale will return false else
                        {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG).show(); // //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }
    }
    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public class GetUrlContentTask extends AsyncTask<String, Void, List<XMLParser.Entry>> {

        @Override
        protected List<XMLParser.Entry> doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (XmlPullParserException e) {
                Log.d("LogDebug", e.getMessage());
                List error = null;
                error.add("connection_error");
                error.add(e.getMessage());
                return error;
            } catch (IOException e) {
                List error = null;
                error.add("xml error");
                error.add(e.getMessage());
                return error;
            }
        }

        private List<XMLParser.Entry> loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            XMLParser stackOverflowXmlParser = new XMLParser();
            List<XMLParser.Entry> entries = null;

            //StringBuilder htmlString = new StringBuilder();
            try {
                stream = downloadUrl(urlString);
                entries = stackOverflowXmlParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
            // Each Entry object represents a single post in the XML feed.
            // This section processes the entries list to combine each entry with HTML markup.
            // Each entry is displayed in the UI as a link that optionally includes
            // a text summary.
            /*for (XMLParser.Entry entry : entries) {
                htmlString.append(entry.city);
                htmlString.append(entry.road);
                htmlString.append(entry.house_number);
            }*/

            //return htmlString.toString();

            return  entries;
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
            int statusCode = 0;
            statusCode = conn.getResponseCode();
            InputStream is = null;
            if (statusCode >= 200 && statusCode < 400) {
                // Create an InputStream in order to extract the response object
                    is = conn.getInputStream();
            }
            else {
                is = conn.getErrorStream();
            }
            return is;
        }

        @Override
        protected void onPostExecute(List<XMLParser.Entry> result) {
            // Displays the HTML string in the UI via a WebView
            bottomSheetTextView = (TextView) findViewById(R.id.bottom_sheet_title);
            bottomSheetTextViewSubtitle = findViewById(R.id.bottom_sheet_subtitle);
            try {
                if(result.get(0).house_number!=null)
                    bottomSheetTextView.setText(result.get(0).road + " " + result.get(0).house_number);
                else
                    bottomSheetTextView.setText(result.get(0).road);
                if(result.get(0).city!=null)bottomSheetTextViewSubtitle.setText(result.get(0).city);
            }catch (Exception e) {
                bottomSheetTextView.setText("Невозможно загрузить адрес");
                bottomSheetTextViewSubtitle.setText(result.toString());
            }
            }
        }
    }