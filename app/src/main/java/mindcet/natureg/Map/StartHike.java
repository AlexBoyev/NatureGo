package mindcet.natureg.Map;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mindcet.natureg.Login.ForegroundService;
import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.MainMenu;
import mindcet.natureg.MainMenu_Fragments.Settings_Fragment;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.HideSysteUI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class StartHike extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {

    private FusedLocationProviderClient fusedLocationClient;

    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    private BroadcastReceiver broadcastReceiver;
    private double longitude;
    private double latitude;
    private List<Style> myStyle;
    private AlertDialog.Builder saveBuilder;
    private AlertDialog.Builder dialogBuilder;
    static final int REQUEST_TAKE_PHOTO = 1;
    private String currentPhotoPath;
    private String imageFileName;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String RootDir = Environment.getExternalStorageDirectory() + File.separator + "NatureGo";
    private File storageDir = new File(RootDir);
    private SymbolManager symbolManager;
    private Dialog myDialog;
    private Bitmap bitmap;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
    private String currentDateandTime;
    private Map symbolDict = new HashMap();
    private List<Symbol> symbols = new ArrayList<>();
    private List arrayOfSymbols = new ArrayList();
    private List<Point> routeCoordinates;
    private List<Location> locationCoordnates;
    private List projectData;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private Long startingTime;
    private Long endTime;
    private int photoCount = 0;
    private boolean firstRun = true;
    private PendingIntent pendingIntent;
    private AlarmManager manager;
    private static boolean hikeStarted = false;
    private LocationComponent locationComponent;
    private LocationComponentActivationOptions locationComponentActivationOptions;
    public  ArrayList<String> animalData;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        projectData = new ArrayList();
        startingTime = System.currentTimeMillis();
        hikeStarted = true;
        super.onCreate(savedInstanceState);
        HideSysteUI.hideSystemUI(this);
        Mapbox.getInstance(this, getString(R.string.mapboxtoken));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_start_hike);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        storageDir.mkdir();
        myDialog = new Dialog(this);
        sharedPreferences = getSharedPreferences("StoredData", MODE_PRIVATE);
        loadData();
        animalData = LoginActivity.getAnimalData();
        MenuOrStopDialog();
        Button camera = findViewById(R.id.CameraButt);
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        this.registerReceiver(locationSwitchStateReceiver, filter);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(!locationManager.isLocationEnabled()){
                isLocationEnabled();
            }
        }

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dispatchTakePictureIntent();


            }
        });

        if (!runtime_permission()) {

        }


    }



    private void addAnimalToDB(String animalName) {
        if(!animalData.contains(animalName.toLowerCase())) {
            animalData.add(animalName.toLowerCase());

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("loginPhone", LoginActivity.getLoginPhone());
                jsonObject.put("Animals", animalData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            CloudRequest cloudRequest = new CloudRequest(this, jsonObject, "/AddAnimal");
            cloudRequest.initConnection();
            LoginActivity.setAnimalName(animalName.toLowerCase());
        }
    }

    private void loadData() {
        gson = new Gson();
        String loadJson = sharedPreferences.getString("DATA", null);
        Type type = new TypeToken<List<Object>>() {
        }.getType();
        projectData = gson.fromJson(loadJson, type);
    }


    private BroadcastReceiver eventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String hikeName = intent.getStringExtra("hike_name");
            saveSharedData(hikeName);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent serviceIntent = new Intent(StartHike.this, ForegroundService.class);
                    stopService(serviceIntent);
                    finish();
                }
            }, 250);
        }
    };


    private void saveImage(Bitmap finalBitmap, String image_name) {

        File file = new File(storageDir, imageFileName);
        if (file.exists()) file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private Bitmap rotateEXIFANDSave(Bitmap bitmap) throws IOException {
        ExifInterface ei = new ExifInterface(currentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                saveImage(rotatedBitmap, imageFileName);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                saveImage(rotatedBitmap, imageFileName);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                saveImage(rotatedBitmap, imageFileName);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }
        refreshGallery();
        return rotatedBitmap;
    }


    private void refreshGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f1 = new File("file://" + getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            Uri contentUri = Uri.fromFile(f1);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } else {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(currentPhotoPath))));
    }


    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mindcet.natureg.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + "_";
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

                classify();
                progressDialog = new ProgressDialog(StartHike.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.authentication));

                progressDialog.show();


            }
        }
    }

    public void createAnimalMarker(String animalName){
        bitmap = BitmapFactory.decodeFile(currentPhotoPath);


      /*          call server for result

                String animalName;
                String animalData;
                */

        try {
            bitmap = rotateEXIFANDSave(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentDateandTime = sdf.format(new Date());
        Symbol symbol = initMarker();
        List list = new ArrayList();
        list.add(0,symbol.getLatLng().getLatitude());
        list.add(1,symbol.getLatLng().getLongitude());
        list.add(animalName);
        list.add(currentPhotoPath);
        list.add(bitmap);
        list.add(currentDateandTime);
        symbolDict.put(symbol, list);
        arrayOfSymbols.add(list);
        photoCount += 1;
        addAnimalToDB(animalName);
    }
    public void classify() {

        String postUrl = "http://" + "34.76.154.55" + ":" + "5000" + "/getPhoto";
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

                // Read BitMap by file path.
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, options);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            byte[] byteArray = stream.toByteArray();

            multipartBodyBuilder.addFormDataPart("image" , "Android_Flask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray));


        RequestBody postBodyImage = multipartBodyBuilder.build();
        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            createAnimalMarker(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }



    private boolean runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;

        }
        return false;
    }


// Map is set up and the style has loaded. Now you can add data or make other map adjustments


    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_icon);
            mapboxMap.getStyle().addImage("my-marker", bm);
            initGPS();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions

                return;
            }
            initLocationEngine();

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }


    private static class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<StartHike> activityWeakReference;

        MainActivityLocationCallback(StartHike activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            StartHike activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();


                if (location == null) {
                    return;
                }

// Create a Toast which displays the new location's coordinates
             /*  Toast.makeText(activity, String.format(activity.getString(R.string.new_location),
                        String.valueOf(result.getLastLocation().getLatitude()), String.valueOf(result.getLastLocation().getLongitude())),
                        Toast.LENGTH_SHORT).show();*/

// Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());

                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            StartHike activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        registerReceiver(locationSwitchStateReceiver, filter);
        super.onResume();
        mapView.onResume();
        HideSysteUI.hideSystemUI(this);

        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Location location = (Location) intent.getExtras().get("location");
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();

                    if (!routeCoordinates.isEmpty()) {
                        double lastLatitude = routeCoordinates.get(routeCoordinates.size() - 1).latitude();
                        double lastLongitude = routeCoordinates.get(routeCoordinates.size() - 1).longitude();

                        if (checkLastPoints(lastLongitude, lastLatitude)) {
                            initRouteCoordinates();

                        }
                    } else {
                        initRouteCoordinates();

                    }


                }
            };
        }
        if (firstRun == true) {
            registerReceiver(eventReceiver, new IntentFilter("hike_name"));
            firstRun = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
                }
            }, 3000);
        } else {
            registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
            registerReceiver(eventReceiver, new IntentFilter("hike_name"));
        }
    }

    private boolean checkLastPoints(double lastLongitude, double lastLatitude) {
        BigDecimal newLongitude = new BigDecimal(lastLongitude).setScale(4, BigDecimal.ROUND_DOWN);
        BigDecimal newLatitude = new BigDecimal(lastLatitude).setScale(4, BigDecimal.ROUND_DOWN);
        BigDecimal tempLongitude = new BigDecimal(longitude).setScale(4, BigDecimal.ROUND_DOWN);
        BigDecimal tempLatitude = new BigDecimal(latitude).setScale(4, BigDecimal.ROUND_DOWN);

        if (!newLongitude.equals(tempLongitude) || !newLatitude.equals(tempLatitude)) {
            return true;
        }
        return false;

    }

    @Override
    public void onPause() {
        unregisterReceiver(locationSwitchStateReceiver);
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

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        if (mapView != null) {
            mapView.onDestroy();
        }

        if (symbolManager != null) {
            symbolManager.onDestroy();
        }
        try {
            if (eventReceiver != null) {
                unregisterReceiver(eventReceiver);
                eventReceiver = null;

            }
        } catch (IllegalArgumentException e) {
            eventReceiver = null;

        }
        try {
            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
                broadcastReceiver = null;

            }
        } catch (IllegalArgumentException e) {
            broadcastReceiver = null;

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return (dist);
        }
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {


    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            if (mapboxMap.getStyle() != null) {
                enableLocationComponent(mapboxMap.getStyle());
            }
        } else {

            finish();
        }
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;


        mapboxMap.setStyle(Style.OUTDOORS,
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        myStyle = new ArrayList<>();
                        myStyle.add(style);
                        enableLocationComponent(style);

                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);  //your choice t/f
                        symbolManager.setTextAllowOverlap(true);
                        routeCoordinates = new ArrayList<>();


                        if (ActivityCompat.checkSelfPermission(StartHike.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(StartHike.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions

                            return;
                        }

                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(StartHike.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        // Got last known location. In some rare situations this can be null.
                                        if (location != null) {
                                            routeCoordinates.add(Point.fromLngLat(location.getLongitude(), location.getLatitude()));

                                        }
                                    }
                                });

                        locationCoordnates = new ArrayList<>();
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
                                List list = (List)symbolDict.get(symbol);
                                // arr = key : marker , value :animal name,bitmap,animaldata
                                if(list != null)
                                    //ShowPopup((String)list.get(2), (Bitmap)list.get(4), (String)list.get(5));
                                    sweetPopup((String)list.get(2), (Bitmap)list.get(4), (String)list.get(5));

                            }

                        });
                    }
                });

    }

    private void sweetPopup(String name,Bitmap pic,String data) {
        SweetAlertDialog myDialog = new SweetAlertDialog(StartHike.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
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
        img.setImageBitmap(pic);
        text.setText("Animal name: "+name);
        data_text.setText(data);
        layout.addView(text);
        layout.addView(img);
        layout.addView(data_text);



        myDialog.setTitleText(R.string.information)
                .setCustomView(layout)
                .show();
    }

    private Bitmap getBitmap(String filepath){
        File imgFile = new  File(filepath);
        Bitmap myBitmap = null;
        if(imgFile.exists()) {
            myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return myBitmap;
    }

    private Symbol initMarker() {
        Double latitude = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
        Double longitude = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();
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
        return symbol;
    }

    private void isLocationEnabled() {

        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.enable_location));
        alertDialog.setMessage(getString(R.string.location_settings_not_enabled));
        alertDialog.setPositiveButton(R.string.location_settings, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);

            }
        });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        AlertDialog alert=alertDialog.create();
        alert.show();

    }


    private void initRouteCoordinates() {
        if(routeCoordinates != null && myStyle != null ) {
            routeCoordinates.add(Point.fromLngLat(longitude, latitude));
            createAndAddLocationToarray();
            myStyle.get(0).removeLayer("Linelayer");
            myStyle.get(0).removeSource("line-source");
            myStyle.get(0).addSource(new GeoJsonSource("line-source",
                    FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates)
                    )})));

            myStyle.get(0).addLayer(new LineLayer("Linelayer", "line-source").withProperties(PropertyFactory.
                    lineDasharray(new Float[]{0.01f, 2f}), PropertyFactory.lineCap(Property.LINE_CAP_ROUND), PropertyFactory.
                    lineJoin(Property.LINE_JOIN_ROUND), PropertyFactory.lineWidth(5f), PropertyFactory.lineColor(Color.
                    parseColor("#e55e5e"))));
        }

    }

    private void createAndAddLocationToarray() {
        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(longitude);//your coords of course
        targetLocation.setLongitude(latitude);
        locationCoordnates.add(targetLocation);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);

        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1]== PackageManager.PERMISSION_GRANTED){


            }
            else{
                runtime_permission();
            }
        }
    }

    public void ZoomMe(View v) {

        if(mapboxMap.getLocationComponent().getLastKnownLocation() != null && locationComponent != null ) {
            mapboxMap.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation()))
                    .zoom(15)
                    .build());
        }
        else{
            Toast.makeText(StartHike.this, R.string.turnGPS, Toast.LENGTH_SHORT).show();
            initGPS();
        }
    }

    private void initGPS() {
        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, mapboxMap.getStyle()).build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
        locationComponent.zoomWhileTracking(15);
    }


    @Override
    public void onBackPressed() {
        AlertDialog alert = null;
        alert = dialogBuilder.create();
        alert.show();

    }

    private BroadcastReceiver locationSwitchStateReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, Intent intent) {

            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


                if (locationManager.isLocationEnabled()) {
                    //location is enabled
                } else {
                    isLocationEnabled();
                }
            }
        }
    };

    public void MenuOrStopDialog(){
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(true)
                .setPositiveButton(R.string.back_to_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle b = new Bundle();
                        b.putString("popUp_flag","back_to_menu");
                        Intent intent = new Intent(StartHike.this, MainMenu.class);
                        intent.putExtras(b);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.stop_hike, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        saveBuilder = new AlertDialog.Builder(StartHike.this);
                        saveBuilder.setCancelable(true);
                        saveBuilder.setTitle(R.string.enter_hike_name);
                        saveBuilder.setPositiveButton(R.string.save, null);
                        saveBuilder.setNegativeButton(R.string.dont_save,null);
                        saveBuilder.setNeutralButton(R.string.cancel,null);
                        final EditText input = new EditText(StartHike.this);
                        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        saveBuilder.setView(input);
                        final AlertDialog alertDialog = saveBuilder.create();
                        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialog) {

                                Button saveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                Button dontSaveButton = alertDialog.getButton(alertDialog.BUTTON_NEGATIVE);
                                Button cancelButton = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                                saveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String hikeName = input.getText().toString();
                                        boolean uniqName = true;
                                        if (projectData != null) {
                                            for (Object object : projectData) {
                                                Map templist = (Map) object;
                                                if (hikeName.equals(templist.get("HikeName"))) {
                                                    Toast.makeText(StartHike.this, R.string.name_already_exists, Toast.LENGTH_LONG).show();
                                                    uniqName = false;
                                                }

                                            }
                                        }
                                        if (uniqName) {
                                            dialog.dismiss();
                                            saveSharedData(hikeName);
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent serviceIntent = new Intent(StartHike.this, ForegroundService.class);
                                                    stopService(serviceIntent);
                                                    finish();
                                                }
                                            }, 250);
                                        }

                                    }
                                });


                                dontSaveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        Intent serviceIntent = new Intent(StartHike.this, ForegroundService.class);
                                        stopService(serviceIntent);
                                        finish();
                                    }
                                });

                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                            }
                        });
                        alertDialog.show();
                    }
                });
    }




    public void saveSharedData(String hikeName){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        endTime = System.currentTimeMillis();
        long timeSpend = TimeUnit.MILLISECONDS.toSeconds(endTime) - TimeUnit.MILLISECONDS.toSeconds(startingTime);
        Map map = new HashMap();
        map.put("HikeName",hikeName);
        map.put("Date",sdf.format(new Date()));
        map.put("Duration",timeSpend);
        map.put("photoCount",photoCount);
        map.put("routeCoordinates",routeCoordinates);
        map.put("totalDistance",calculateTotalDistance());
        map.put("arrayOfSymbols",arrayOfSymbols);

        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put("HikeName",hikeName);
            messageContent.put("Date",sdf.format(new Date()));
            messageContent.put("Duration",timeSpend);
            messageContent.put("photoCount",photoCount);
            messageContent.put("totalDistance",calculateTotalDistance());
            messageContent.put("SchoolName", Settings_Fragment.getUpdatedSchoolName());
            messageContent.put("Class",Settings_Fragment.getClassName());
            sendHttprequest(messageContent,"/SaveHike");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if(projectData == null){
            projectData = new ArrayList();
            projectData.add(map);
            String json = gson.toJson(projectData);
            editor.putString("DATA",json);
            editor.apply();
        }
        else{
            projectData.add(map);
            String json = gson.toJson(projectData);
            editor.putString("DATA",json);
            editor.apply();
        }


    }


    private void sendHttprequest(JSONObject messageContent,String func) {
        CloudRequest cloudRequest = new CloudRequest(this,messageContent,func);
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                    if(responseText.equals("True")){
                        Toast.makeText(StartHike.this, R.string.hike_saved, Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(StartHike.this, R.string.failed_to_save_hike, Toast.LENGTH_LONG).show();
                    }
            }
        });
    }



    private Double calculateTotalDistance() {
        Double totalDistance = 0.0;
        Double haversineDistance = 0.0;
        if(locationCoordnates.size() >= 2) {
            for (int i = 0; i + 1 < locationCoordnates.size(); i++) {
                totalDistance += locationCoordnates.get(i).distanceTo(locationCoordnates.get(i + 1));
                haversineDistance += HaversineDistance(locationCoordnates.get(i).getLatitude(),locationCoordnates.get(i).getLongitude(),
                        locationCoordnates.get(i+1).getLatitude(),locationCoordnates.get(i+1).getLongitude());

            }
        }
        return totalDistance;
    }



    private Double HaversineDistance(double lat1,double lon1,double lat2,double lon2) {
        final int R = 6371;
        Double latDistance = toRad(lat2 - lat1);
        Double lonDistance = toRad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Double distance = R * c;
        return distance;
    }

    private  Double toRad(Double value) {
        return value * Math.PI / 180;
    }

}