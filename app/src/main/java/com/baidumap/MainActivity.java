package com.baidumap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geolocation;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapProjection;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapServices;
import com.microsoft.maps.MapStyleSheet;
import com.microsoft.maps.MapStyleSheets;
import com.microsoft.maps.MapTappedEventArgs;
import com.microsoft.maps.MapView;
import com.microsoft.maps.Optional;
import com.microsoft.maps.search.MapLocation;
import com.microsoft.maps.search.MapLocationFinder;
import com.microsoft.maps.search.MapLocationFinderResult;
import com.microsoft.maps.search.MapLocationFinderStatus;
import com.microsoft.maps.search.MapLocationOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final MapStyleSheet[] MAP_STYLES = {
            MapStyleSheets.roadLight(),
            MapStyleSheets.roadDark(),
            MapStyleSheets.roadCanvasLight(),
            MapStyleSheets.aerial(),
            MapStyleSheets.aerialWithOverlay(),
            MapStyleSheets.roadHighContrastLight(),
            MapStyleSheets.roadHighContrastDark(),
    };
    private static final int POSITION_CUSTOM = MAP_STYLES.length;

    private static final Geolocation LOCATION_LAKE_WASHINGTON =
            new Geolocation(39.921901702880859,116.44355010986328);

    private MapView mMapView;
    private MapElementLayer mPinLayer;
    private MapImage mPinImage;

    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        search = findViewById(R.id.search);

        mMapView = new MapView(this, MapRenderMode.VECTOR);
        mMapView.setCredentialsKey(LocalSearch.CREDENTIALS_KEY);
        ((FrameLayout)findViewById(R.id.map_view)).addView(mMapView);

        mPinLayer = new MapElementLayer();

        mMapView.getLayers().add(mPinLayer);
        mPinImage = getPinImage();

        mMapView.setMapProjection(MapProjection.GLOBE);
        mMapView.setMapProjection(MapProjection.WEB_MERCATOR);
        mMapView.setLanguage("zh-cn");

        MapServices.setCredentialsKey(LocalSearch.CREDENTIALS_KEY);


        getLocation();

        /*mMapView.addOnMapTappedListener((MapTappedEventArgs e) -> {

            Optional<Geolocation> location = mMapView.getLocationFromOffset(e.position);
            if (location.isPresent()) {
                addPin(location.get(), "");
            }

            return false;
        });*/


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*LocalSearch.sendRequest(MainActivity.this,
                        "HARROW+INTERNATIONAL+SCHOOL,+BEIJING",
                        mMapView.getBounds(), new LocalSearch.Callback() {
                    @Override
                    public void onSuccess(List<LocalSearch.Poi> results) {
                        clearPins();
                        for (LocalSearch.Poi result : results) {
                            addPin(result.location, result.name);
                        }
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(MainActivity.this,"No search results found", Toast.LENGTH_LONG).show();
                    }
                });*/

                String query = "Level 15 NCI Tower, No. 12 A Jianguomenwai Ave, Chaoyang District, Beijing, 100022";

                MapLocationOptions options = new MapLocationOptions();
                options.setRegion("CN");
                Geolocation referenceLocation = mMapView.getCenter();


                MapLocationFinder.findLocations(query, referenceLocation, mMapView.getBounds(), options, (MapLocationFinderResult result) -> {
                    MapLocationFinderStatus status = result.getStatus();

                    if (status == MapLocationFinderStatus.SUCCESS) {
                        List<Geolocation> points = new ArrayList<>();

                        Log.d("TAG", "points = "+points);

                        for (MapLocation mapLocation : result.getLocations()) {
                            Geolocation pinLocation = new Geolocation(
                                    mapLocation.getPoint().getLatitude(),
                                    mapLocation.getPoint().getLongitude(),
                                    0,
                                    AltitudeReferenceSystem.TERRAIN);
                            addPin(pinLocation, mapLocation.getDisplayName());
                            points.add(mapLocation.getPoint());
                        }
                        mMapView.setScene(MapScene.createFromLocations(points), MapAnimationKind.DEFAULT);

                    } else if (status == MapLocationFinderStatus.EMPTY_RESPONSE) {
                        Toast.makeText(MainActivity.this, "No results were found", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(MainActivity.this, "Error processing the request, code " + status.toString(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
    }



    private void getLocation(){

        String query = "Level 15 NCI Tower, No. 12 A Jianguomenwai Ave, Chaoyang District, Beijing, 100022";

        query = query.replace(" ", "+");
        LocalSearch.sendRequest(MainActivity.this,
                query, mMapView.getBounds(), new LocalSearch.Callback() {
                    @Override
                    public void onSuccess(List<LocalSearch.Poi> results) {
                        clearPins();
                        for (LocalSearch.Poi result : results) {
                            addPin(result.location, result.name);
                        }
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(MainActivity.this,
                                "No search results found", Toast.LENGTH_LONG).show();

                       // getLocation();
                    }
                });

    }


    @Override
    protected void onStart() {
        super.onStart();
        mMapView.setScene(
                MapScene.createFromLocationAndZoomLevel(LOCATION_LAKE_WASHINGTON, 11),
                MapAnimationKind.NONE);

       // addPin(LOCATION_LAKE_WASHINGTON, "hello");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.suspend();
        }
    }

    private void addPin(Geolocation location, String title) {
        MapIcon pushpin = new MapIcon();
        pushpin.setLocation(location);
        pushpin.setTitle(title);
        pushpin.setImage(mPinImage);
        mPinLayer.getElements().add(pushpin);
    }

    private void clearPins() {
        mPinLayer.getElements().clear();
    }


    private MapImage getPinImage() {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pin, null);

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return new MapImage(bitmap);
    }







}
