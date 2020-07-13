package mindcet.natureg.Map;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class MapHistory extends AppCompatActivity {

    private Map map;
    private MapView mapView;
    private List<Point> routeCoordinates;
    private Map symbolDict;
    private List<Style> myStyle;
    private SymbolManager symbolManager;
    private Gson gson;
    private String myExtra;
    private Map mapData;
    private List<Symbol> symbols = new ArrayList<>();
    private Dialog myDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideSysteUI.hideSystemUI(this);
        map = new HashMap();
        symbolDict = new HashMap();
        gson = new Gson();
        routeCoordinates = new ArrayList<>();
        myExtra = getIntent().getStringExtra("List");
        mapData = gson.fromJson(myExtra,Map.class);
        myDialog = new Dialog(this);

        List routeCoordinatesObject = (List) mapData.get("routeCoordinates");
        List symbolDictOjbect = (List) mapData.get("arrayOfSymbols");

        for(int  i = 0 ; i < routeCoordinatesObject.size() ; i ++ ) {
            JsonObject jsonObject = new Gson().fromJson(routeCoordinatesObject.get(i).toString(), JsonObject.class);
            JsonArray jsonPoint =  jsonObject.get("coordinates").getAsJsonArray();
            Point point = Point.fromLngLat(jsonPoint.get(0).getAsDouble(), jsonPoint.get(1).getAsDouble());
            routeCoordinates.add(point);
        }


        Mapbox.getInstance(this, "pk.eyJ1IjoibmF0dXJlZ28iLCJhIjoiY2p6ankwYXBjMGRyNjNtcW02cTlzN2g4MCJ9.ws8Grmb0aT6i-5f2lvMzsA");
        setContentView(R.layout.activity_map_history);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .target(new LatLng(routeCoordinates.get(0).latitude(),routeCoordinates.get(0).longitude()))
                        .zoom(15)
                        .build());

                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_icon);

                mapboxMap.setStyle(Style.OUTDOORS,
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                myStyle = new ArrayList<>();
                                myStyle.add(style);

                                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_icon);
                                mapboxMap.getStyle().addImage("my-marker", bm);
                                symbolManager = new SymbolManager(mapView, mapboxMap, style);
                                symbolManager.setIconAllowOverlap(true);  //your choice t/f
                                symbolManager.setTextAllowOverlap(true);
                                if(symbolDictOjbect != null && symbolDictOjbect.size() != 0) {
                                    for(int  i = 0 ; i < symbolDictOjbect.size() ; i ++ ) {
                                        List index = (List)symbolDictOjbect.get(i);
                                        initMarker((Double)index.get(0),(Double)index.get(1));

                                    }
                                }
                                style.addSource(new GeoJsonSource("line-source",
                                        FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                                LineString.fromLngLats(routeCoordinates)
                                        )})));

                                style.addLayer(new LineLayer("Linelayer","line-source").withProperties(PropertyFactory.
                                        lineDasharray(new Float[]{0.01f,2f}),PropertyFactory.lineCap(Property.LINE_CAP_ROUND),PropertyFactory.
                                        lineJoin(Property.LINE_JOIN_ROUND),PropertyFactory.lineWidth(5f),PropertyFactory.lineColor(Color.
                                        parseColor("#e55e5e"))));


                                symbolManager.addClickListener(new OnSymbolClickListener() {
                                    @Override
                                    public void onAnnotationClick(Symbol symbol) {
                                        int index = symbols.indexOf(symbol);
                                        List list = (List)symbolDictOjbect.get(index);

                                        sweetPopup((String)list.get(2), (String)list.get(3), (String)list.get(5));
                                    }

                                });

                            }
                        });
            }
        });
    }



    private Bitmap getBitmap(String filepath){
        File imgFile = new  File(filepath);
        Bitmap myBitmap = null;
        if(imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return myBitmap;
    }



    private void sweetPopup(String name,String pic,String data) {
        SweetAlertDialog myDialog = new SweetAlertDialog(MapHistory.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        myDialog.setContentView(R.layout.hike_marker_information);
        ImageView img = (ImageView) myDialog.findViewById(R.id.image_test);
        TextView text = (TextView) myDialog.findViewById(R.id.text_test);
        TextView data_text = myDialog.findViewById(R.id.data_test);
        LinearLayout layout = myDialog.findViewById(R.id.custom_layout);
        if(layout.getParent()!=null){
            ((ViewGroup)layout.getParent()).removeView(layout);
        }
        if(text.getParent()!=null){
            ((ViewGroup)text.getParent()).removeView(text);
        }
        if(img.getParent()!=null){
            ((ViewGroup)img.getParent()).removeView(img);
        }
        if(data_text.getParent()!=null){
            ((ViewGroup)data_text.getParent()).removeView(data_text);
        }
        try {
            img.setImageBitmap(rotateEXIF(getBitmap(pic),pic));
        } catch (IOException e) {
            e.printStackTrace();
        }
        text.setText("Animal name: "+name);
        data_text.setText(data);
        layout.addView(text);
        layout.addView(img);
        layout.addView(data_text);



        myDialog.setTitleText(R.string.information)
                .setCustomView(layout)
                .show();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap rotateEXIF(Bitmap bitmap,String bitmapPath) throws IOException {
        ExifInterface ei = new ExifInterface(bitmapPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap = null;
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        return rotatedBitmap;
    }

    private void initMarker(Double latitude,Double longitude){
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(latitude,longitude))
                .withIconImage("my-marker")
                //set the below attributes according to your requirements
                .withIconSize(0.3f)
                .withIconOffset(new Float[] {0f,-1.5f})
                .withZIndex(10)
                .withTextHaloColor("rgba(255, 255, 255, 100)")
                .withTextHaloWidth(5.0f)
                .withTextAnchor("top")
                .withTextOffset(new Float[] {0f, 1.5f})
                .setDraggable(false)
        );
        symbols.add(symbol);
    }




    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(symbolManager != null) {
            symbolManager.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
