package com.scarfaiz.cluckinbell;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static MapView map;
    public boolean geodata_updated = false;
    private static LocationManager locationManager;
    private static IMapController mapController;
    private FloatingActionButton LocButton;
    private FloatingActionButton SearchButton;
    private static int permsRequestCode;
    private TextView bottomSheetTextView;
    private TextView bottomSheetTextViewSubtitle;
    private TextView bottom_sheet_button_desc;
    private static SharedPreferences prefs;
    private static String url;
    private static String tag = "LogDebug";
    private static GeoPoint marker_geopostition;
    private String city;
    private String address;
    private LinearLayout bottom_sheet_elements_layout;
    private FrameLayout bottom_sheet_comments_layout;
    private RelativeLayout bottom_sheet_rel;

    private LocationListener myLocationListener
            = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (!geodata_updated) {
                GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(locGeoPoint);
                mapController.zoomTo(17);
                map.invalidate();
                prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                float Latitude = (float) location.getLatitude();
                editor.putFloat("Latitude", Latitude);
                float Longitude = (float) location.getLongitude();
                editor.putFloat("Longitude", Longitude);
                Log.d(tag, "Last saved Geoposition: " + String.valueOf(prefs.getFloat("Latitude", Latitude)) + "  " + String.valueOf(prefs.getFloat("Longitude", Longitude)));
                editor.apply();
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

    @SafeVarargs
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        prefs = this.getPreferences(Context.MODE_PRIVATE);
        Configuration.getInstance().load(ctx, prefs);
        Configuration.getInstance().setUserAgentValue("CB");
        setContentView(R.layout.activity_main);
        permsRequestCode = 1;
        GetLocPermission();
        map =  findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(13);
        GeoPoint startPoint = new GeoPoint(prefs.getFloat("Latitude", 59.93863f), prefs.getFloat("Longitude", 30.31413f));
        Log.d(tag, "Loaded Geoposition: " + String.valueOf(prefs.getFloat("Latitude", 59.93863f)) + "  " + String.valueOf(prefs.getFloat("Longitude", 30.31413f)));
        mapController.setCenter(startPoint);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.removeUpdates(myLocationListener);
        MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(map);
        oMapLocationOverlay.enableMyLocation();
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
        marker_geopostition = null;
        address = null;
        city = null;
        int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PermissionChecker.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
            Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        } else {
            GetLocPermission();
        }


        CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);

        //setting listener for locButton
        LocButton = findViewById(R.id.locButton);
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
                } else {
                    GetLocPermission();
                }
            }
        });

        bottomSheetTextView =  bottomSheet.findViewById(R.id.bottom_sheet_title);
        bottomSheetTextViewSubtitle =  bottomSheet.findViewById(R.id.bottom_sheet_subtitle);
        bottom_sheet_elements_layout = findViewById(R.id.bottom_sheet_elements_layout);
        bottom_sheet_comments_layout = findViewById(R.id.bottom_sheet_comments_layout);
        bottom_sheet_rel = findViewById(R.id.bottom_sheet_rel);
        bottom_sheet_button_desc = findViewById(R.id.bottom_sheet_button_desc);

        SearchButton = findViewById(R.id.searchButton);
        SearchButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                                                executeAsyncTask(new SearchForMarkersTask(String.valueOf(prefs.getFloat("Latitude", 59.93863f)), String.valueOf(prefs.getFloat("Longitude", 30.31413f)), prefs.getString("city", "Санкт-Петербург"),
                                                new SearchForMarkersTask.AsyncResponse() {


                                                    @Override
                                                    public void processFinish(List<String> output) {
                                                        Double latitude;
                                                        Double longitude;
                                                        int id;
                                                        String[] latitude_split = output.get(1).split("\\s+");
                                                        String[] longitude_split = output.get(2).split("\\s+");
                                                        String[] id_split = output.get(0).split("\\s+");
                                                        final SearchForMarkersTask.GenSet<Marker> startMarker = new SearchForMarkersTask.GenSet<>(Marker.class, id_split.length);
                                                        final String req_server_address = "http://178.162.41.115/get_entry_details.php";
                                                        for (int i = 0; i < id_split.length; i++) {
                                                            latitude = Double.valueOf(latitude_split[i]);
                                                            longitude = Double.valueOf(longitude_split[i]);
                                                            id = Integer.valueOf(id_split[i]);
                                                            Log.d(tag, "Split markers data info: " + latitude + " " + longitude + " " + id + " with array length: " + id_split.length);
                                                            GeoPoint p = new GeoPoint(latitude, longitude);
                                                            startMarker.a[i] = new Marker(map);
                                                            startMarker.a[i].setPosition(p);
                                                            startMarker.a[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                                            startMarker.a[i].setTitle(String.valueOf(id));
                                                            final int final_i = i;
                                                            prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                                                            SharedPreferences.Editor editor = prefs.edit();
                                                            editor.putInt("marker_id", Integer.valueOf(startMarker.a[final_i].getTitle()));
                                                            editor.apply();
                                                            map.getOverlays().add(startMarker.a[i]);
                                                            map.invalidate();
                                                            startMarker.a[i].setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                                                @Override
                                                                public boolean onMarkerClick(Marker item, MapView arg1) {
                                                                    bottom_sheet_button_desc.setText("Добавить комментарий");
                                                                    final ProgressBar bottom_sheet_pb = new ProgressBar(MainActivity.this);
                                                                    bottom_sheet_pb.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                                                                    bottom_sheet_rel.addView(bottom_sheet_pb);
                                                                    executeAsyncTask(new GetEntryDataTask(req_server_address, Integer.valueOf(startMarker.a[final_i].getTitle()), new GetEntryDataTask.AsyncResponse() {

                                                                        @Override
                                                                        public void processFinish(List<String> output) {
                                                                            marker_geopostition = new GeoPoint(Double.valueOf(output.get(6)), Double.valueOf(output.get(7)));
                                                                            bottom_sheet_rel.removeView(bottom_sheet_pb);
                                                                            bottom_sheet_elements_layout.removeAllViews();
                                                                            behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                                                                            bottomSheetTextView.setText(output.get(0));
                                                                            bottomSheetTextViewSubtitle.setText(output.get(1));
                                                                            TextView wh_textView = new TextView(MainActivity.this);
                                                                            TextView r_textView = new TextView(MainActivity.this);
                                                                            if(Integer.valueOf(output.get(2)) == 1)
                                                                                wh_textView.setText("Магазин работает до 22:00");
                                                                                    else
                                                                                wh_textView.setText("Магазин работает круглосуточно");
                                                                            if(Integer.valueOf(output.get(3)) == 1)
                                                                                r_textView.setText("В продаже имеются только слабоалкогольные напитки");
                                                                            else
                                                                                r_textView.setText("Широкий ассортимент");
                                                                            wh_textView.setLayoutParams(bottom_sheet_elements_layout.getLayoutParams());
                                                                            r_textView.setLayoutParams(bottom_sheet_elements_layout.getLayoutParams());
                                                                            bottom_sheet_elements_layout.addView(wh_textView);
                                                                            bottom_sheet_elements_layout.addView(r_textView);
                                                                            bottom_sheet_comments_layout.removeAllViews();
                                                                            TextView comments_textview = new TextView(MainActivity.this);
                                                                            if (output.get(5) != null)
                                                                                comments_textview.setText(output.get(5));
                                                                            comments_textview.setLayoutParams(bottom_sheet_comments_layout.getLayoutParams());
                                                                            bottom_sheet_comments_layout.addView(comments_textview);
                                                                        }
                                                                    }));
                                                                    return true;
                                                                }
                                                            });
                                                        }
                                                    }
                                                }));
                                            }
                                        });
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permission == PermissionChecker.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            updateLoc(lastLocation);
                        }
                        LocButton.setVisibility(View.VISIBLE);
                        SearchButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        GeoPoint marker_geoposition_to_animate = new GeoPoint(marker_geopostition.getLatitude() - 0.0025, marker_geopostition.getLongitude());
                        mapController.animateTo(marker_geoposition_to_animate);
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                LocButton.setVisibility(View.INVISIBLE);
                SearchButton.setVisibility(View.INVISIBLE);
            }
        });
        AppBarLayout mergedAppBarLayout = findViewById(R.id.merged_appbarlayout);
        MergedAppBarLayoutBehavior mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout);
        mergedAppBarLayoutBehavior.setToolbarTitle("Информация о месте");
        mergedAppBarLayoutBehavior.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
            }
        });
        FrameLayout frameLayoutBottom = findViewById(R.id.dummy_framelayout_replacing_map);
        FloatingActionButton newMarkerButton = findViewById(R.id.new_marker_button);

        newMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottom_sheet_button_desc.getText() == "Добавить место"){
                Intent intent = new Intent(MainActivity.this, NewMarkerActivity.class);
                try {
                    intent.putExtra("city", city);
                    intent.putExtra("latitude", marker_geopostition.getLatitude());
                    intent.putExtra("longitude", marker_geopostition.getLongitude());
                    intent.putExtra("address", address);
                    startActivity(intent);
                } catch (NullPointerException e) {
                    Log.d(tag, "A failure accured while creating new marker:" + e.getMessage());
                }
                }
                else if(bottom_sheet_button_desc.getText() == "Добавить комментарий"){
                    prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                    onButtonShowPopupWindowClick(findViewById(R.id.coordinatorlayout), prefs.getInt("marker_id", 0));
                }
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
                ItemPagerAdapter adapter = new ItemPagerAdapter(MainActivity.this, new int[]{0});
                ViewPager viewPager = findViewById(R.id.pager);
                viewPager.setAdapter(adapter);
                bottom_sheet_button_desc.setText("Добавить место");
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                final ProgressBar bottom_sheet_pb = new ProgressBar(MainActivity.this);
                bottom_sheet_pb.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                bottom_sheet_rel.addView(bottom_sheet_pb);
                url = "http://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + p.getLatitude() + "&lon=" + p.getLongitude() + "&zoom=18&addressdetails=1";
                Log.d(tag, "Sending request to: " + url);
                prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                bottomSheetTextView = findViewById(R.id.bottom_sheet_title);
                bottomSheetTextViewSubtitle = findViewById(R.id.bottom_sheet_subtitle);
                executeAsyncTask(new GetUrlContentTask(prefs, url, new GetUrlContentTask.AsyncResponse(){

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void processFinish(List<XMLParser.Entry> output){
                                bottom_sheet_rel.removeView(bottom_sheet_pb);
                                try {
                                    if (output.get(0).house_number != null){
                                        bottomSheetTextView.setText(output.get(0).road + " " + output.get(0).house_number);
                                        address = (output.get(0).road + " " + output.get(0).house_number);
                                    }
                                    else
                                        bottomSheetTextView.setText(output.get(0).road);
                                    if (output.get(0).city != null) {
                                        bottomSheetTextViewSubtitle.setText(output.get(0).city);
                                        city = output.get(0).city;
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("city", String.valueOf(output.get(0).city));
                                        editor.apply();
                                    }
                                } catch (Exception e) {
                                    bottomSheetTextView.setText("Невозможно загрузить адрес");
                                    bottomSheetTextViewSubtitle.setText(output.toString());
                                }
                            }
                        }));
                Marker startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                map.invalidate();
                marker_geopostition = p;
                Log.d(tag, "Marker geoposition: " + String.valueOf(p.getLatitude() - prefs.getFloat("Latitude", 59.93863f)) + ", " + String.valueOf(p.getLongitude() - prefs.getFloat("Longitude", 30.31413f)));
                bottom_sheet_elements_layout.removeAllViews();
                bottom_sheet_comments_layout.removeAllViews();
                TextView elements_textView = new TextView(MainActivity.this);
                elements_textView.setText("По этой точке еще нет информации о доступных магазинах. Вы можете добавить новое место, нажав на кнопку.");
                elements_textView.setLayoutParams(bottom_sheet_elements_layout.getLayoutParams());
                bottom_sheet_elements_layout.addView(elements_textView);
                startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker item, MapView arg1)
                    {
                        bottom_sheet_button_desc.setText("Добавить место");
                        bottom_sheet_rel.addView(bottom_sheet_pb);
                        url = "http://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + marker_geopostition.getLatitude() + "&lon=" + marker_geopostition.getLongitude() + "&zoom=18&addressdetails=1";
                        Log.d(tag, "Sending request to: " + url);
                        prefs = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                        bottomSheetTextView = findViewById(R.id.bottom_sheet_title);
                        bottomSheetTextViewSubtitle = findViewById(R.id.bottom_sheet_subtitle);
                        executeAsyncTask(new GetUrlContentTask(prefs, url, new GetUrlContentTask.AsyncResponse(){

                            @SuppressLint("SetTextI18n")
                            @Override
                            public void processFinish(List<XMLParser.Entry> output){
                                bottom_sheet_rel.removeView(bottom_sheet_pb);
                                try {
                                    if (output.get(0).house_number != null){
                                        bottomSheetTextView.setText(output.get(0).road + " " + output.get(0).house_number);
                                        address = (output.get(0).road + " " + output.get(0).house_number);
                                    }
                                    else
                                        bottomSheetTextView.setText(output.get(0).road);
                                    if (output.get(0).city != null) {
                                        bottomSheetTextViewSubtitle.setText(output.get(0).city);
                                        city = output.get(0).city;
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("city", String.valueOf(output.get(0).city));
                                        editor.apply();
                                    }
                                } catch (Exception e) {
                                    bottomSheetTextView.setText("Невозможно загрузить адрес");
                                    bottomSheetTextViewSubtitle.setText(output.toString());
                                }
                            }
                        }));
                        bottom_sheet_elements_layout.removeAllViews();
                        bottom_sheet_comments_layout.removeAllViews();
                        TextView elements_textView = new TextView(MainActivity.this);
                        elements_textView.setText("По этой точке еще нет информации о доступных магазинах. Вы можете добавить новое место, нажав на кнопку.");
                        elements_textView.setLayoutParams(bottom_sheet_elements_layout.getLayoutParams());
                        bottom_sheet_elements_layout.addView(elements_textView);
                        return true;
                    }
                });
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

    }

    public void onButtonShowPopupWindowClick(View view, final int id) {
        CoordinatorLayout mainLayout = findViewById(R.id.coordinatorlayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = inflater.inflate(R.layout.popup_window_layout, null);
        popupView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.popup_window_animation));
        final PopupWindow popup_window = new PopupWindow(popupView);
        popup_window.setWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
        popup_window.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
        popup_window.setFocusable(true);
        popup_window.setBackgroundDrawable(new BitmapDrawable());
        popup_window.setOutsideTouchable(false);
        popup_window.showAtLocation(mainLayout, Gravity.CENTER, 0 , 0);

        final TextInputEditText popup_window_text = popupView.findViewById(R.id.popup_comment);
        final String add_comment_server_address = "http://178.162.41.115/add_comment.php";

        final Button popup_cancel_button = popupView.findViewById(R.id.popup_cancel);
        popup_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup_window.setAnimationStyle(R.anim.popup_window_animation);
                popup_window.dismiss();
            }
        });
        final Button popup_ok_button = popupView.findViewById(R.id.popup_ok);
        popup_ok_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                List<NameValuePair> comment_data = new ArrayList<>();
                comment_data.add(new BasicNameValuePair("id", String.valueOf(id)));
                comment_data.add(new BasicNameValuePair("comments", popup_window_text.getText().toString()));
                Log.d(tag, "Comment data:" + id + " " + popup_window_text.getText().toString());
                executeAsyncTask(new AddCommentTask(add_comment_server_address, comment_data));
                Toast.makeText(MainActivity.this, "Комментарий успешно добавлен", Toast.LENGTH_SHORT);
                popup_window.setAnimationStyle(R.anim.popup_window_animation);
                popup_window.dismiss();
            }
        });
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
            prefs = this.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            float Latitude = (float) loc.getLatitude();
            editor.putFloat("Latitude", Latitude);
            float Longitude = (float) loc.getLongitude();
            editor.putFloat("Longitude", Longitude);
            Log.d(tag, "Last saved Geoposition: " + String.valueOf(prefs.getFloat("Latitude", Latitude)) + "  " + String.valueOf(prefs.getFloat("Longitude", Longitude)));
            editor.apply();
        } else GetLocPermission();

    }

    public void GetLocPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermissions();
        } else {
            Toast.makeText(MainActivity.this, "Для работы приложения необходимо предоставить разрешение на использование геопозиции", Toast.LENGTH_LONG).show();
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
        }
    }

    private void checkAndRequestPermissions() {
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
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            || perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                                // shouldShowRequestPermissionRationale will return true
                                // show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    showDialogOK(new DialogInterface.OnClickListener() {
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
                    // process the normal flow
                    //else any one or both the permissions are not granted
                    //waitAndNavigateToOnboardingTutorial();
                }
            }
        }
    }

    private void showDialogOK(DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage("Camera, Storage and Location Services Permission required for this app")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

}