package mindcet.natureg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import mindcet.natureg.Login.ForegroundService;
import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.MainMenu_Fragments.AdminFragment.Admin_Fragment;
import mindcet.natureg.MainMenu_Fragments.Information_Fragment;
import mindcet.natureg.MainMenu_Fragments.MainMenu_Fragment;
import mindcet.natureg.MainMenu_Fragments.Settings_Fragment;
import mindcet.natureg.MainMenu_Fragments.TeacherFragment.Teacher_Fragment;
import mindcet.natureg.MainMenu_Fragments.Trivia_Fragment;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.FragmentChangeListener;
import mindcet.natureg.Utilities.GetFragment;
import mindcet.natureg.Utilities.GlideApp;
import mindcet.natureg.Utilities.HideSysteUI;
import mindcet.natureg.Utilities.ImagePickerActivity;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentChangeListener {
    private DrawerLayout drawer;
    public MainMenu_Fragment mainMenu_fragment = new MainMenu_Fragment();
    private Settings_Fragment settings_fragment = new Settings_Fragment();
    private Information_Fragment information_fragment = new Information_Fragment();
    private Trivia_Fragment trivia_fragment = new Trivia_Fragment();
    private Teacher_Fragment teacher_fragment = new Teacher_Fragment();
    private Admin_Fragment admin_fragment = new Admin_Fragment();
    private String userStatus;
    private TextView welcome;
    private String userName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String imageFileName;
    private String currentPhotoPath;
    private String RootDir = Environment.getExternalStorageDirectory() + File.separator + "NatureGo";
    private File storageDir = new File(RootDir);


    private ImageView userPic;
    private ImageView userPlus;

    private static final String TAG = MainMenu.class.getSimpleName();
    public static final int REQUEST_IMAGE = 100;


    {
        try {
            userStatus = LoginActivity.getUserDetails().get("Status").toString();
            userName = LoginActivity.getUserDetails().get("Name").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int CAMERA_PERMISSION_CODE = 1;
    private Bundle b;
    private List projectData;
    private List projectUserData;
    private Gson gson;
    private Intent serviceIntent;
    private NavigationView navigationView;

    private static  GetFragment mListener;
    public static  void setOnGetFrag(GetFragment listener){
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main_menu);
        HideSysteUI.hideSystemUI(this);
        ButterKnife.bind(this);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        Toolbar toolbar = findViewById(R.id.nav_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        drawer = findViewById(R.id.draw_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainMenu.this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        hideMenu();
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mainMenu_fragment).commit();
            navigationView.setCheckedItem(R.id.nav_main_menu);
        }

        View hView =  navigationView.getHeaderView(0);
        welcome = (TextView)hView.findViewById(R.id.welcome_string);
        userPic = (ImageView)hView.findViewById(R.id.user_profile_pic);
        userPlus = (ImageView)hView.findViewById(R.id.img_plus);
        welcome.setText(getString(R.string.welcome)+ " " + userName);

        loadUserData();
        loadUserPrfile();

        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProfileImageClick();
            }
        });

        userPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onProfileImageClick();
            }
        });

    }

    private void loadUserPrfile() {
        if( projectUserData != null) {
            loadProfile(projectUserData.get(0).toString());
        }
        else {
            loadProfileDefault();
        }
    }

    private void hideMenu() {
        if(userStatus.equals("Teacher")) {
            MenuItem mi = navigationView.getMenu().getItem(1);
            mi.setVisible(false);
        }
        else if(userStatus.equals("User")){
            MenuItem mi = navigationView.getMenu().getItem(1);
            mi.setVisible(false);
            mi = navigationView.getMenu().getItem(2);
            mi.setVisible(false);
        }
    }


    private void saveUserDetails() {
        int userScore = Trivia_Fragment.getScore();
        String loginPhone = LoginActivity.getLoginPhone();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Score",userScore);
            jsonObject.put("loginPhone",loginPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CloudRequest cloudRequest = new CloudRequest(this,jsonObject,"/ScoreUpdate");
        cloudRequest.initConnection();
    }



    private void showLogOutDialog(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.logout))
                .setMessage(R.string.logout_varification)
                .setPositiveButton(R.string.logout, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mainMenu_fragment.getHikeButton().getText().equals("Start Hike")) {
                            dialog.dismiss();
                            logoutFunction();
                        }
                        else {
                            dialog.dismiss();
                            saveTrack();
                        }
                    }

                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        if(mainMenu_fragment.getHikeButton().getText().equals("Resume Hike") &&
                getIntent().getBooleanExtra("flag", false)){
            getIntent().putExtra("flag",false);
            MainMenu.super.onBackPressed();
            return;
        }



        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            showLogOutDialog();
        } else {
            getSupportFragmentManager().popBackStack();
            Fragment currentFragment = getTopFragment();

            if(currentFragment instanceof Settings_Fragment){
                navigationView.setCheckedItem(R.id.nav_settings);
            }
            else if(currentFragment instanceof  Trivia_Fragment){
                navigationView.setCheckedItem(R.id.nav_trivia);
            }
            else if( currentFragment instanceof  Information_Fragment){
                navigationView.setCheckedItem(R.id.nav_information);
            }
            else if( currentFragment instanceof  Teacher_Fragment){
                navigationView.setCheckedItem(R.id.nav_teacher_panel);
            }
            else if( currentFragment instanceof  Admin_Fragment){
                navigationView.setCheckedItem(R.id.nav_admin_panel);
            }
            else{
                navigationView.setCheckedItem(R.id.nav_main_menu);
            }
        }
    }


    public Fragment getTopFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            return null;
        }
        String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
        return getSupportFragmentManager().findFragmentByTag(fragmentTag);
    }


    private void loadData() {
        gson = new Gson();
        sharedPreferences = getSharedPreferences("StoredData",MODE_PRIVATE);
        String loadJson = sharedPreferences.getString("DATA",null);
        Type type = new TypeToken<List<Object>>(){}.getType();
        projectData = gson.fromJson(loadJson,type);
        editor = sharedPreferences.edit();
    }
    private void loadUserData() {
        gson = new Gson();
        sharedPreferences = getSharedPreferences("StoredUserData",MODE_PRIVATE);
        String loadJson = sharedPreferences.getString("USER_DATA",null);
        Type type = new TypeToken<List<Object>>(){}.getType();
        projectUserData = gson.fromJson(loadJson,type);
        editor = sharedPreferences.edit();
    }




    public void saveTrack(){
        AlertDialog.Builder saveBuilder = new AlertDialog.Builder(MainMenu.this);
        saveBuilder.setCancelable(true);
        saveBuilder.setTitle(R.string.hike_in_progress);
        saveBuilder.setPositiveButton(R.string.save, null);
        saveBuilder.setNegativeButton(R.string.dont_save,null);
        saveBuilder.setNeutralButton(R.string.cancel,null);
        final EditText input = new EditText(MainMenu.this);
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
                        loadData();
                        if(uniqueName(hikeName)){
                            saveCurrentTrack(hikeName);
                            dialog.dismiss();
                            finishAffinity();
                            finish();
                        } else {
                            Toast.makeText(MainMenu.this, R.string.name_already_exists, Toast.LENGTH_LONG).show();
                        }

                    }



                });
                dontSaveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent serviceIntent = new Intent(MainMenu.this, ForegroundService.class);
                        stopService(serviceIntent);
                        logoutFunction();
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

    private boolean uniqueName(String hikeName) {
        if (projectData != null) {
            for (Object object : projectData) {
                Map templist = (Map) object;
                if (hikeName.equals(templist.get("HikeName"))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void logoutFunction() {
        saveUserDetails();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("finish", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finishAffinity();
        finish();
    }


    private void saveCurrentTrack(String name) {
        Intent intent = new Intent("hike_name");
        intent.putExtra("hike_name",name);
        sendBroadcast(intent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                logoutFunction();
            }
        }, 250);

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_main_menu:
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,mainMenu_fragment,
                        "Main_Menu").commit();

                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,settings_fragment,
                        "Settings").addToBackStack("Settings").commit();
                if(mListener!=null) {
                    mListener.getMenuFragment(mainMenu_fragment);
                }

                break;
            case R.id.nav_trivia:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,trivia_fragment,
                        "Trivia").addToBackStack("Trivia").commit();
                break;
            case R.id.nav_information:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,information_fragment,
                        "Information").addToBackStack("Information").commit();
                break;

            case R.id.nav_admin_panel:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,admin_fragment,
                        "Admin").addToBackStack("Admin").commit();
                break;

            case R.id.nav_teacher_panel:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,teacher_fragment,
                        "Teacher").addToBackStack("Teacher").commit();
                break;
            case R.id.nav_logout:
                showLogOutDialog();

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void replaceFragment(Fragment fragment, String newFragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        navigationView.setCheckedItem(R.id.nav_main_menu);
    }


    private void loadProfile(String url) {
        GlideApp.with(this).load(url)
                .fitCenter()
                .into(userPic);
        userPic.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
    }

    private void loadProfileDefault() {
        GlideApp.with(this).load(R.drawable.baseline_account_circle_black_48)
                .into(userPic);
        userPic.setColorFilter(ContextCompat.getColor(this, R.color.profile_default_tint));

    }


    void onProfileImageClick() {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            showImagePickerOptions();
                        }

                        if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    public void onResume() {
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                launchCameraIntent();
            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }
        });
    }

    private void launchCameraIntent() {


        Intent intent = new Intent(MainMenu.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);





        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(MainMenu.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getParcelableExtra("path");
                try {
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    File f = new File("" + uri);

                    String[] finalPhotoName = f.getName().split("\\.");
                    NewsaveImage(bitmap,finalPhotoName[0]);

                    //rotateEXIFANDSave(bitmap);



                    loadProfile(uri.toString());

                    projectUserData = new ArrayList();
                    projectUserData.add(uri.toString());
                    String json = gson.toJson(projectUserData);
                    editor.putString("USER_DATA",json);
                    editor.apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainMenu.this);
        builder.setTitle(getString(R.string.dialog_permission_title));
        builder.setMessage(getString(R.string.dialog_permission_message));
        builder.setPositiveButton(getString(R.string.go_to_settings), (dialog, which) -> {
            dialog.cancel();
            HideSysteUI.hideSystemUI(this);
            openSettings();
        });
        builder.setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();

    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }


    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    private void NewsaveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString() + File.separator + "NatureGo";
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentPhotoPath = file.getPath();
        refreshGallery();
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


}
