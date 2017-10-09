package com.scarfaiz.cluckinbell;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.PermissionChecker;
import android.view.View;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
public class MainActivity extends Activity {

    LocationManager locationManager;
    MapView map;
    IMapController mapController;
    public boolean geodata_updated = false;
    MyLocationNewOverlay oMapLocationOverlay;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue("CB");
        setContentView(R.layout.activity_main);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.animateTo(startPoint);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //location marker overlay
        oMapLocationOverlay = new MyLocationNewOverlay(map);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Drawable drawable = getResources().getDrawable(R.drawable.direction_arrow);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        oMapLocationOverlay.setPersonIcon(bitmap);
        oMapLocationOverlay.enableMyLocation(); // not on by default
        oMapLocationOverlay.enableFollowLocation();
        oMapLocationOverlay.setDrawAccuracyEnabled(true);
        oMapLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapController.animateTo(oMapLocationOverlay
                        .getMyLocation());
            }
        });
        map.getOverlays().add(oMapLocationOverlay);

        //rotation
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);

        //who the fuck would use compass nowadays
        /*CompassOverlay compassOverlay = new CompassOverlay(this, map);
        compassOverlay.enableCompass();
        compassOverlay.getOrientation();
        map.getOverlays().add(compassOverlay);*/

        FloatingActionButton LocButton = findViewById(R.id.locButton);
        LocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
                int permission = PermissionChecker.checkSelfPermission(MainActivity.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (permission == PermissionChecker.PERMISSION_GRANTED) {
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateLoc(lastLocation);
                    return;
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
                Intent intent = new Intent(MainActivity.this, NewMarkerActivity.class);
                startActivity(intent);
                /*Marker startMarker = new Marker(map);
                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                map.invalidate();*/
                return false;
            }
        };


        MapEventsOverlay OverlayEvents = new MapEventsOverlay(getBaseContext(), mReceive);
        map.getOverlays().add(OverlayEvents);

    }


    public void onResume(){
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
            return;}
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        locationManager.removeUpdates(myLocationListener);
    }

    private void updateLoc(Location loc){
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        mapController.animateTo(locGeoPoint);
        map.invalidate();
    }

    private LocationListener myLocationListener
            = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
            if(!geodata_updated){
                updateLoc(location);
                geodata_updated = true;}
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

}
