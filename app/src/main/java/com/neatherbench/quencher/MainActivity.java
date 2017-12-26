package com.neatherbench.quencher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
    public SharedPreferences prefs;
    private static String url;
    private static String tag = "LogDebug";
    private static GeoPoint marker_geopostition;
    private String city;
    private String address;
    private LinearLayout bottom_sheet_comments_layout;
    private LinearLayout bottom_sheet_rel;
    public Marker my_location_marker;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        setContentView(R.layout.activity_main);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, prefs);
        Configuration.getInstance().setUserAgentValue("CB");
        permsRequestCode = 1;
        GetLocPermission();
        map =  findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(13);
        GeoPoint startPoint = new GeoPoint(prefs.getFloat("Latitude", 59.93863f), prefs.getFloat("Longitude", 30.31413f));
        Log.d(tag, "Loaded Geoposition: " + String.valueOf(prefs.getFloat("Latitude", 59.93863f)) + "  " + String.valueOf(prefs.getFloat("Longitude", 30.31413f)));
        mapController.animateTo(startPoint);
        my_location_marker = new Marker(map);
        final BitmapFactory.Options person_options = new BitmapFactory.Options();
        person_options.inSampleSize = 4;
        final Drawable person_drawable = getResources().getDrawable(R.drawable.ic_my_location_marker);
        Bitmap person_bitmap = Bitmap.createBitmap(person_drawable.getIntrinsicWidth(),
                person_drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(person_bitmap);
        person_drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        person_drawable.draw(canvas);
        my_location_marker.setIcon(person_drawable);
        my_location_marker.setPosition(startPoint);
        my_location_marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(my_location_marker);
        map.invalidate();
        geodata_updated = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        locationManager.removeUpdates(myLocationListener);
        if(!prefs.contains("username")) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else if(!prefs.getString("username", "skipped").equals("skipped")) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            MenuItem nav_account = menu.findItem(R.id.nav_account);
            nav_account.setTitle("Информация об аккаунте");
            final String entrance_server_address = "https://178.162.41.115/get_daily.php";
            List<NameValuePair> username_data = new ArrayList<>();
            username_data.add(new BasicNameValuePair("username", prefs.getString("username", "skipped")));
            executeAsyncTask(new EntranceTask(entrance_server_address, username_data, MainActivity.this.getApplicationContext(), new EntranceTask.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    if (output.equals("1")) {
                        CoordinatorLayout mainLayout = findViewById(R.id.coordinatorlayout);
                        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                        final View popupView = inflater.inflate(R.layout.entrance_popup_window_layout, null);
                        popupView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.popup_window_animation));
                        final PopupWindow popup_window = new PopupWindow(popupView);
                        popup_window.setWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
                        popup_window.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
                        popup_window.setFocusable(true);
                        popup_window.setBackgroundDrawable(new BitmapDrawable());
                        popup_window.setOutsideTouchable(false);
                        popup_window.showAtLocation(mainLayout, Gravity.CENTER, 0, 0);
                        final Button popup_entrance_ok = popupView.findViewById(R.id.popup_entrance_ok);
                        popup_entrance_ok.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popup_window.setAnimationStyle(R.anim.popup_window_animation);
                                popup_window.dismiss();
                            }
                        });
                    }
                }
            }));
            final View nav_header = navigationView.getHeaderView(0);
            final TextView nav_title = nav_header.findViewById(R.id.nav_title);
            final TextView nav_subtitle = nav_header.findViewById(R.id.nav_subtitle);
            nav_title.setText(prefs.getString("username", "Имя пользователя"));
            nav_subtitle.setText(prefs.getString("status", "Участник закрытого бета-теста"));
        }
        /*MyLocationNewOverlay oMapLocationOverlay = new MyLocationNewOverlay(map);
        oMapLocationOverlay.enableMyLocation();
        oMapLocationOverlay.setDrawAccuracyEnabled(false);
        oMapLocationOverlay.getEnableAutoStop();
        map.getOverlays().add(oMapLocationOverlay);
        final BitmapFactory.Options person_options = new BitmapFactory.Options();
        person_options.inSampleSize = 4;
        final Drawable person_drawable = getResources().getDrawable(R.drawable.ic_my_location_marker);
        Bitmap person_bitmap = Bitmap.createBitmap(person_drawable.getIntrinsicWidth(),
                person_drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(person_bitmap);
        person_drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        person_drawable.draw(canvas);
        oMapLocationOverlay.setPersonIcon(person_bitmap);
        oMapLocationOverlay.setDirectionArrow(person_bitmap, person_bitmap);*/
        final Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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


        final CoordinatorLayout coordinatorLayout = findViewById(R.id.coordinatorlayout);
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        final BottomSheetBehaviorGoogleMapsLike behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet);
        behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN);

        //setting listener for locButton
        LocButton = coordinatorLayout.findViewById(R.id.locButton);
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
        bottom_sheet_comments_layout = findViewById(R.id.bottom_sheet_comments_layout);
        bottom_sheet_rel = findViewById(R.id.bottom_sheet_rel);
        bottom_sheet_button_desc = findViewById(R.id.bottom_sheet_button_desc);
        final ProgressBar bottom_sheet_pb = new ProgressBar(MainActivity.this);
        bottom_sheet_pb.setLayoutParams(bottom_sheet_rel.getLayoutParams());
        SearchButton = coordinatorLayout.findViewById(R.id.searchButton);
        SearchButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                executeAsyncTask(new SearchForMarkersTask(String.valueOf(prefs.getFloat("Latitude", 59.93863f)), String.valueOf(prefs.getFloat("Longitude", 30.31413f)), prefs.getString("city", "Санкт-Петербург"), MainActivity.this.getApplicationContext(),
                                                new SearchForMarkersTask.AsyncResponse() {
                                                    @Override
                                                    public void processFinish(List<String> output) {
                                                        try{
                                                        Double latitude;
                                                        Double longitude;
                                                        int id;
                                                        int conf_status;
                                                        String[] latitude_split = output.get(1).split("\\s+");
                                                        String[] longitude_split = output.get(2).split("\\s+");
                                                        String[] id_split = output.get(0).split("\\s+");
                                                        String[] conf_status_split = output.get(3).split("\\s+");
                                                        final SearchForMarkersTask.GenSet<Marker> startMarker = new SearchForMarkersTask.GenSet<>(Marker.class, id_split.length);
                                                        final String entry_data_server_address = "https://178.162.41.115/get_entry_details.php";
                                                        final String comments_data_server_address = "https://178.162.41.115/get_comments.php";
                                                        for (int i = 0; i < id_split.length; i++) {
                                                            latitude = Double.valueOf(latitude_split[i]);
                                                            longitude = Double.valueOf(longitude_split[i]);
                                                            id = Integer.valueOf(id_split[i]);
                                                            conf_status = Integer.valueOf(conf_status_split[i]);
                                                            Log.d(tag, "Split markers data info: " + latitude + " " + longitude + " " + id + " with array length: " + id_split.length);
                                                            GeoPoint p = new GeoPoint(latitude, longitude);
                                                            startMarker.a[i] = new Marker(map);
                                                            startMarker.a[i].setPosition(p);
                                                            startMarker.a[i].setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                                            startMarker.a[i].setTitle(String.valueOf(id));
                                                            if(conf_status>=200)
                                                                startMarker.a[i].setIcon(getResources().getDrawable(R.drawable.ic_marker_confirmed));
                                                            else
                                                                startMarker.a[i].setIcon(getResources().getDrawable(R.drawable.ic_marker_unconfirmed));
                                                            final int final_i = i;
                                                            map.getOverlays().add(startMarker.a[i]);
                                                            map.invalidate();
                                                            startMarker.a[i].setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                                                @Override
                                                                public boolean onMarkerClick(Marker item, MapView arg1) {
                                                                    bottom_sheet_rel.removeAllViews();
                                                                    bottom_sheet_button_desc.setText("Добавить комментарий");
                                                                    behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                                                                    bottom_sheet_rel.addView(bottom_sheet_pb);
                                                                    SharedPreferences.Editor editor = prefs.edit();
                                                                    editor.putInt("marker_id", Integer.valueOf(startMarker.a[final_i].getTitle()));
                                                                    editor.apply();
                                                                    /*ViewGroup.LayoutParams params = bottom_sheet_comments_layout.getLayoutParams();
                                                                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 420, getResources().getDisplayMetrics());
                                                                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                                                    bottom_sheet_comments_layout.setLayoutParams(params);*/
                                                                    executeAsyncTask(new GetEntryDataTask(entry_data_server_address, Integer.valueOf(startMarker.a[final_i].getTitle()), prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new GetEntryDataTask.AsyncResponse() {

                                                                        @Override
                                                                        public void processFinish(final List<String> output) {
                                                                            try{
                                                                            bottom_sheet_rel.removeAllViews();
                                                                            bottom_sheet_comments_layout.removeAllViews();
                                                                                if((!prefs.getString("username", "skipped").equals("skipped")) && output.get(9).equals("1") && (!output.get(8).equals(prefs.getString("username", "skipped")))) {
                                                                                    final Button confirm = new Button(MainActivity.this);
                                                                                    final Button dismiss = new Button(MainActivity.this);
                                                                                    confirm.setText("Подтвердить место");
                                                                                    dismiss.setText("Пожаловаться");
                                                                                    confirm.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
                                                                                    dismiss.setBackground(getResources().getDrawable(R.color.colorPrimaryDark));
                                                                                    confirm.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                                                                                    dismiss.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                                                                                    bottom_sheet_rel.addView(confirm);
                                                                                    bottom_sheet_rel.addView(dismiss);
                                                                                    final String conf_server_address = "https://178.162.41.115/change_entry_status.php";
                                                                                    confirm.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View view) {
                                                                                            final List<NameValuePair> conf_data = new ArrayList<>();
                                                                                            conf_data.add(new BasicNameValuePair("id",output.get(10)));
                                                                                            conf_data.add(new BasicNameValuePair("username", prefs.getString("username", "skipped")));
                                                                                            final String account_data_server_address = "https://178.162.41.115/get_account_data.php";
                                                                                            executeAsyncTask(new GetAccountDataTask(account_data_server_address, prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new GetAccountDataTask.AsyncResponse() {
                                                                                                @Override
                                                                                                public void processFinish(List<String> output) {
                                                                                                    try {
                                                                                                        conf_data.add(new BasicNameValuePair("confirmation_status", output.get(0)));
                                                                                                        executeAsyncTask(new ChangeEntryConfirmationTask(conf_server_address, conf_data, MainActivity.this.getApplicationContext()));
                                                                                                        confirm.setEnabled(false);
                                                                                                        dismiss.setEnabled(false);
                                                                                                        Toast.makeText(MainActivity.this, "Спасибо за Ваш голос! Вы получили несколько бутылок", Toast.LENGTH_SHORT).show();
                                                                                                    } catch (IndexOutOfBoundsException e) {
                                                                                                        Toast.makeText(MainActivity.this, "Не удалось загрузить информацию об аккаунте", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            }));
                                                                                        }
                                                                                    });
                                                                                    dismiss.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View view) {
                                                                                            final List<NameValuePair> conf_data = new ArrayList<>();
                                                                                            conf_data.add(new BasicNameValuePair("id", output.get(10)));
                                                                                            conf_data.add(new BasicNameValuePair("username", prefs.getString("username", "skipped")));
                                                                                            final String account_data_server_address = "https://178.162.41.115/get_account_data.php";
                                                                                            executeAsyncTask(new GetAccountDataTask(account_data_server_address, prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new GetAccountDataTask.AsyncResponse() {
                                                                                                @Override
                                                                                                public void processFinish(List<String> output) {
                                                                                                    try {
                                                                                                        conf_data.add(new BasicNameValuePair("confirmation_status", "-" + output.get(0)));
                                                                                                        executeAsyncTask(new ChangeEntryConfirmationTask(conf_server_address, conf_data, MainActivity.this.getApplicationContext()));
                                                                                                        confirm.setEnabled(false);
                                                                                                        dismiss.setEnabled(false);
                                                                                                        Toast.makeText(MainActivity.this, "Спасибо за Ваш голос! Вы получили несколько бутылок", Toast.LENGTH_SHORT).show();
                                                                                                    } catch (IndexOutOfBoundsException e) {
                                                                                                        Toast.makeText(MainActivity.this, "Не удалось загрузить информацию об аккаунте", Toast.LENGTH_SHORT).show();
                                                                                                    }
                                                                                                }
                                                                                            }));
                                                                                        }
                                                                                    });
                                                                                }
                                                                            marker_geopostition = new GeoPoint(Double.valueOf(output.get(6)), Double.valueOf(output.get(7)));
                                                                            bottomSheetTextView.setText(output.get(0));
                                                                            bottomSheetTextViewSubtitle.setText(output.get(1));
                                                                            TextView wh_textView = new TextView(MainActivity.this);
                                                                            TextView r_textView = new TextView(MainActivity.this);
                                                                            if (Integer.valueOf(output.get(2)) == 1)
                                                                                wh_textView.setText("Магазин работает до 22:00");
                                                                            else
                                                                                wh_textView.setText("Магазин работает круглосуточно");
                                                                            if (Integer.valueOf(output.get(3)) == 1)
                                                                                r_textView.setText("В продаже имеются только слабоалкогольные напитки");
                                                                            else
                                                                                r_textView.setText("Широкий ассортимент");
                                                                            wh_textView.setTextSize(18);
                                                                            r_textView.setTextSize(18);
                                                                            wh_textView.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                                                                            r_textView.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                                                                                bottom_sheet_rel.addView(wh_textView);
                                                                                bottom_sheet_rel.addView(r_textView);
                                                                                /*ViewGroup.LayoutParams params = bottom_sheet_comments_layout.getLayoutParams();
                                                                                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                                                                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                                                                bottom_sheet_comments_layout.setLayoutParams(params);*/
                                                                                TextView header = new TextView(MainActivity.this);
                                                                                header.setText("Комментарии:");
                                                                                header.setTextSize(18);
                                                                                header.setLayoutParams(bottom_sheet_comments_layout.getLayoutParams());
                                                                                bottom_sheet_comments_layout.addView(header);
                                                                                if (!output.get(5).equals("No value for comments")) {
                                                                                    TextView comments_textview = new TextView(MainActivity.this);
                                                                                    comments_textview.setText(output.get(8) + "\n" + output.get(5));
                                                                                    comments_textview.setTextSize(18);
                                                                                    comments_textview.setLayoutParams(bottom_sheet_comments_layout.getLayoutParams());
                                                                                    bottom_sheet_comments_layout.addView(comments_textview);
                                                                                }
                                                                                executeAsyncTask(new GetCommentsTask(comments_data_server_address, Integer.valueOf(startMarker.a[final_i].getTitle()), MainActivity.this.getApplicationContext(), new GetCommentsTask.AsyncResponse() {
                                                                                    @SuppressLint("SetTextI18n")
                                                                                    @Override
                                                                                    public void processFinish(List<String> output) {
                                                                                        try {
                                                                                            if(output.get(0).equals("1")) {
                                                                                                String username;
                                                                                                String comments;
                                                                                                String[] username_split = output.get(1).split("\\n+");
                                                                                                String[] comments_split = output.get(2).split("\\n+");
                                                                                                Log.d(tag, "Split username, length, comments:" + Arrays.toString(username_split) + " " + username_split.length + " " + Arrays.toString(comments_split));
                                                                                                final GetCommentsTask.GenSet<TextView> comments_data = new GetCommentsTask.GenSet<>(TextView.class, username_split.length);
                                                                                                for (int i = 0; i < username_split.length; i++) {
                                                                                                    username = username_split[i];
                                                                                                    comments = comments_split[i];
                                                                                                    comments_data.a[i] = new TextView(MainActivity.this);
                                                                                                    comments_data.a[i].setText(username + "\n" + comments);
                                                                                                    comments_data.a[i].setTextSize(18);
                                                                                                    comments_data.a[i].setLayoutParams(bottom_sheet_comments_layout.getLayoutParams());
                                                                                                    bottom_sheet_comments_layout.addView(comments_data.a[i]);
                                                                                                }
                                                                                            }
                                                                                        }catch (IndexOutOfBoundsException e)
                                                                                        {
                                                                                            Toast.makeText(MainActivity.this, "Не удалось запросить информацию о комментариях", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                }));
                                                                        }catch (IndexOutOfBoundsException e)
                                                                            {
                                                                                Toast.makeText(MainActivity.this, "Не удалось запросить информацию об этом месте", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                    }}));
                                                                    return true;
                                                                }
                                                            });
                                                        }
                                                    }catch (IndexOutOfBoundsException e) {
                                                        Toast.makeText(MainActivity.this, "Не удалось запросить информацию о доступных местах", Toast.LENGTH_SHORT).show();
                                                        }
                                                }}));
                                            }
                                        }
                                        );
        final FloatingActionButton newMarkerButton = coordinatorLayout.findViewById(R.id.new_marker_button);
        behavior.addBottomSheetCallback(new BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        /*int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permission == PermissionChecker.PERMISSION_GRANTED) {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, myLocationListener);
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListener);
                            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            GeoPoint last_location_to_animate = new GeoPoint(lastLocation.getLatitude() - 0.0025, lastLocation.getLongitude());
                            mapController.animateTo(last_location_to_animate);
                        }*/
                        toolbar.setVisibility(View.VISIBLE);
                        LocButton.setVisibility(View.VISIBLE);
                        SearchButton.setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED:
                        toolbar.setVisibility(View.INVISIBLE);
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT:
                        toolbar.setVisibility(View.VISIBLE);
                        GeoPoint marker_geoposition_to_animate = new GeoPoint(marker_geopostition.getLatitude() - 0.0025, marker_geopostition.getLongitude());
                        mapController.setZoom(17);
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

        //newMarkerButton.show();
        newMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottom_sheet_button_desc.getText() == "Добавить место") {
                    if (!prefs.getString("username", "skipped").equals("skipped")) {
                        final String account_data_server_address = "https://178.162.41.115/get_account_data.php";
                        executeAsyncTask(new GetAccountDataTask(account_data_server_address, prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new GetAccountDataTask.AsyncResponse() {
                            @Override
                            public void processFinish(List<String> output) {
                                try{
                                    if(Integer.valueOf(output.get(1))>0) {
                                        Intent intent = new Intent(MainActivity.this, NewMarkerActivity.class);
                                        try {
                                            intent.putExtra("city", city);
                                            intent.putExtra("latitude", marker_geopostition.getLatitude());
                                            intent.putExtra("longitude", marker_geopostition.getLongitude());
                                            intent.putExtra("address", address);
                                            intent.putExtra("reputation", output.get(0));
                                            startActivity(intent);
                                        } catch (NullPointerException e) {
                                            Log.d(tag, "A failure accured while creating new marker:" + e.getMessage());
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(MainActivity.this, "Недостаточно крышек :(", Toast.LENGTH_SHORT).show();
                                    }
                                }catch (IndexOutOfBoundsException e) {
                                    Toast.makeText(MainActivity.this, "Не удалось загрузить информацию об аккаунте", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }));
                    }
                    else
                        Toast.makeText(MainActivity.this, "Для добавления места необходима регистрация", Toast.LENGTH_SHORT).show();
                }
                else if(bottom_sheet_button_desc.getText() == "Добавить комментарий"){
                    if(!prefs.getString("username", "skipped").equals("skipped"))
                    onButtonShowPopupWindowClick(findViewById(R.id.coordinatorlayout), prefs.getInt("marker_id", 0));
                    else
                        Toast.makeText(MainActivity.this, "Для добавления комментария необходима регистрация", Toast.LENGTH_SHORT).show();
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
                newMarkerButton.show();
                ItemPagerAdapter adapter = new ItemPagerAdapter(MainActivity.this, new int[]{0});
                ViewPager viewPager = coordinatorLayout.findViewById(R.id.pager);
                viewPager.setAdapter(adapter);
                bottom_sheet_button_desc.setText("Добавить место");
                behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED);
                final ProgressBar bottom_sheet_pb = new ProgressBar(MainActivity.this);
                bottom_sheet_pb.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                bottom_sheet_rel.addView(bottom_sheet_pb);
                url = "http://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + p.getLatitude() + "&lon=" + p.getLongitude() + "&zoom=18&addressdetails=1";
                Log.d(tag, "Sending request to: " + url);
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
                bottom_sheet_rel.removeAllViews();
                bottom_sheet_comments_layout.removeAllViews();
                /*ViewGroup.LayoutParams params = bottom_sheet_comments_layout.getLayoutParams();
                params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 620, getResources().getDisplayMetrics());
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                bottom_sheet_comments_layout.setLayoutParams(params);*/
                TextView elements_textView = new TextView(MainActivity.this);
                elements_textView.setText("По этой точке еще нет информации о доступных магазинах. Вы можете добавить новое место, нажав на кнопку.");
                elements_textView.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                bottom_sheet_rel.addView(elements_textView);
                startMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_confirmed));
                startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker item, MapView arg1)
                    {
                        bottom_sheet_button_desc.setText("Добавить место");
                        bottom_sheet_rel.addView(bottom_sheet_pb);
                        url = "https://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + marker_geopostition.getLatitude() + "&lon=" + marker_geopostition.getLongitude() + "&zoom=18&addressdetails=1";
                        Log.d(tag, "Sending request to: " + url);
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
                        bottom_sheet_rel.removeAllViews();
                        bottom_sheet_comments_layout.removeAllViews();
                        TextView elements_textView = new TextView(MainActivity.this);
                        elements_textView.setText("По этой точке еще нет информации о доступных магазинах. Вы можете добавить новое место, нажав на кнопку.");
                        elements_textView.setLayoutParams(bottom_sheet_rel.getLayoutParams());
                        bottom_sheet_rel.addView(elements_textView);
                        return true;
                    }
                });
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

    }


    private LocationListener myLocationListener
            = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if (!geodata_updated) {
                GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(locGeoPoint);
                mapController.zoomTo(17);
                my_location_marker.setPosition(locGeoPoint);
                map.invalidate();
                prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = prefs.edit();
                float Latitude = (float) location.getLatitude();
                editor.putFloat("Latitude", Latitude);
                float Longitude = (float) location.getLongitude();
                editor.putFloat("Longitude", Longitude);
                Log.d(tag, "Last saved Geoposition: " + String.valueOf(prefs.getFloat("Latitude", Latitude)) + "  " + String.valueOf(prefs.getFloat("Longitude", Longitude)));
                editor.apply();
                geodata_updated = true;
            }
            else {
                GeoPoint locGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                my_location_marker.setPosition(locGeoPoint);
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
        final String add_comment_server_address = "https://178.162.41.115/add_comment.php";

        final Button popup_cancel_button = popupView.findViewById(R.id.popup_cancel);
        popup_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup_window.setAnimationStyle(R.anim.popup_window_animation);
                popup_window.dismiss();
            }
        });
        final Button popup_ok_button = popupView.findViewById(R.id.popup_account_ok);
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        popup_ok_button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View view) {
                List<NameValuePair> comment_data = new ArrayList<>();
                comment_data.add(new BasicNameValuePair("id", String.valueOf(id)));
                comment_data.add(new BasicNameValuePair("username", prefs.getString("username", "skipped")));
                comment_data.add(new BasicNameValuePair("comments", popup_window_text.getText().toString()));
                Log.d(tag, "Comment data:" + id + " " + prefs.getString("username", "skipped") + " " + popup_window_text.getText().toString());
                executeAsyncTask(new AddCommentTask(add_comment_server_address, comment_data, MainActivity.this.getApplicationContext()));
                Toast.makeText(MainActivity.this, "Комментарий успешно добавлен", Toast.LENGTH_SHORT).show();
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
            my_location_marker.setPosition(locGeoPoint);
            prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            SharedPreferences.Editor editor = prefs.edit();
            float Latitude = (float) loc.getLatitude();
            editor.putFloat("Latitude", Latitude);
            float Longitude = (float) loc.getLongitude();
            editor.putFloat("Longitude", Longitude);
            Log.d(tag, "Last saved Geoposition: " + String.valueOf(prefs.getFloat("Latitude", Latitude)) + "  " + String.valueOf(prefs.getFloat("Longitude", Longitude)));
            url = "http://nominatim.openstreetmap.org/reverse?email=netherbench@gmail.com&format=xml&lat=" + loc.getLatitude() + "&lon=" + loc.getLongitude() + "&zoom=18&addressdetails=1";
            Log.d(tag, "Sending request to: " + url);
            executeAsyncTask(new GetUrlContentTask(prefs, url, new GetUrlContentTask.AsyncResponse(){

                @SuppressLint("SetTextI18n")
                @Override
                public void processFinish(List<XMLParser.Entry> output){
                    try {
                        if (output.get(0).city != null) {
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity2, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (id == R.id.nav_leaderboard) {
                executeAsyncTask(new LeaderboardTask(prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new LeaderboardTask.AsyncResponse() {
                    @Override
                    public void processFinish(List<String> output) {
                        try {

                            CoordinatorLayout mainLayout = findViewById(R.id.coordinatorlayout);
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            final View popupView = inflater.inflate(R.layout.activity_leaderboard, null);
                            popupView.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.popup_window_animation));
                            final PopupWindow popup_window = new PopupWindow(popupView);
                            popup_window.setWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
                            popup_window.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
                            popup_window.setFocusable(true);
                            popup_window.setBackgroundDrawable(new BitmapDrawable());
                            popup_window.setOutsideTouchable(false);
                            popup_window.showAtLocation(mainLayout, Gravity.CENTER, 0 , 0);
                            LinearLayout linearLayout = popupView.findViewById(R.id.leaderboard_scroll);

                            final Button popup_leaderboard_ok = popupView.findViewById(R.id.popup_leaderboard_ok);
                            popup_leaderboard_ok.setOnClickListener(new View.OnClickListener() {
                                @SuppressLint("ShowToast")
                                @Override
                                public void onClick(View view) {
                                    popup_window.setAnimationStyle(R.anim.popup_window_animation);
                                    popup_window.dismiss();
                                }
                            });

                            String username;
                            String reputation;
                            String[] username_split = output.get(0).split("\\n+");
                            String[] reputation_split = output.get(1).split("\\n+");
                            final LeaderboardTask.GenSet<LinearLayout> entry_linear = new LeaderboardTask.GenSet<>(LinearLayout.class, username_split.length);
                            final LeaderboardTask.GenSet<TextView> username_text = new LeaderboardTask.GenSet<>(TextView.class, username_split.length);
                            final LeaderboardTask.GenSet<TextView> reputation_text = new LeaderboardTask.GenSet<>(TextView.class, username_split.length);
                            final LeaderboardTask.GenSet<TextView> place_text = new LeaderboardTask.GenSet<>(TextView.class, username_split.length);
                            for (int i = 0; i < username_split.length; i++) {
                                username = username_split[i];
                                reputation = reputation_split[i];
                                final int final_i = i + 1;
                                Log.d(tag, "Split leaderboard data info: " + username + " " + reputation + " with array length: " + username_split.length);
                                entry_linear.a[i] = new LinearLayout(MainActivity.this);
                                username_text.a[i] = new TextView(MainActivity.this);
                                reputation_text.a[i] = new TextView(MainActivity.this);
                                place_text.a[i] = new TextView(MainActivity.this);
                                username_text.a[i].setWidth(400);
                                username_text.a[i].setTextSize(25);
                                reputation_text.a[i].setWidth(200);
                                reputation_text.a[i].setTextSize(25);
                                place_text.a[i].setWidth(100);
                                place_text.a[i].setTextSize(25);
                                username_text.a[i].setText(username);
                                reputation_text.a[i].setText(reputation);
                                place_text.a[i].setText(String.valueOf(final_i));
                                entry_linear.a[i].setLayoutParams(linearLayout.getLayoutParams());
                                username_text.a[i].setLayoutParams(entry_linear.a[i].getLayoutParams());
                                reputation_text.a[i].setLayoutParams(entry_linear.a[i].getLayoutParams());
                                place_text.a[i].setLayoutParams(entry_linear.a[i].getLayoutParams());
                                linearLayout.addView(entry_linear.a[i]);
                                entry_linear.a[i].addView(place_text.a[i]);
                                entry_linear.a[i].addView(username_text.a[i]);
                                entry_linear.a[i].addView(reputation_text.a[i]);
                            }
                        } catch (IndexOutOfBoundsException e) {
                            Toast.makeText(MainActivity.this, "Не удалось запросить информацию о рейтинге", Toast.LENGTH_SHORT).show();
                        }
                    }}));
        } else if (id == R.id.nav_account)
        {
            if(item.getTitle().equals("Зарегистрироваться")) {
                final int REQUEST_SIGNUP = 0;
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                if(!prefs.getString("username", "skipped").equals("skipped"))
                {
                    item.setTitle("Информация об аккаунте");
                }
            }
            else
            {
                CoordinatorLayout mainLayout = findViewById(R.id.coordinatorlayout);
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View popupView = inflater.inflate(R.layout.popup_window_account_information, null);
                popupView.setAnimation(AnimationUtils.loadAnimation(this, R.anim.popup_window_animation));
                final PopupWindow popup_window = new PopupWindow(popupView);
                popup_window.setWidth(FrameLayout.LayoutParams.WRAP_CONTENT);
                popup_window.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
                popup_window.setFocusable(true);
                popup_window.setBackgroundDrawable(new BitmapDrawable());
                popup_window.setOutsideTouchable(false);
                popup_window.showAtLocation(mainLayout, Gravity.CENTER, 0 , 0);

                final TextView popup_username = popupView.findViewById(R.id.popup_username);
                final TextView popup_username_status = popupView.findViewById(R.id.popup_status);
                final TextView popup_rep = popupView.findViewById(R.id.popup_rep);
                final TextView popup_coins = popupView.findViewById(R.id.popup_coins);

                popup_username.setText(prefs.getString("username", "skipped"));

                final String account_data_server_address = "https://178.162.41.115/get_account_data.php";
                executeAsyncTask(new GetAccountDataTask(account_data_server_address, prefs.getString("username", "skipped"), MainActivity.this.getApplicationContext(), new GetAccountDataTask.AsyncResponse() {
                    @Override
                    public void processFinish(List<String> output) {
                        try {
                            popup_username_status.setText(prefs.getString("status", "Участник закрытого бета-теста"));
                            popup_rep.setText(output.get(0));
                            popup_coins.setText(output.get(1));
                        }
                        catch (IndexOutOfBoundsException e)
                        {
                            Toast.makeText(MainActivity.this, "Не удалось загрузить информацию об аккаунте", Toast.LENGTH_SHORT).show();
                        }
                    }
                }));

                final Button popup_ok_button = popupView.findViewById(R.id.popup_account_ok);
                prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                popup_ok_button.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void onClick(View view) {
                        popup_window.setAnimationStyle(R.anim.popup_window_animation);
                        popup_window.dismiss();
                    }
                });
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}