package mindcet.natureg.MainMenu_Fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import mindcet.natureg.AugmentedReality.ArModels;
import mindcet.natureg.Login.ForegroundService;
import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.Map.StartHike;
import mindcet.natureg.Map.TrackHistory;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class MainMenu_Fragment extends Fragment {
    private Button startHike;
    private Button hikeHistory;
    private Button myCollection;
    private String[] permissions = {Manifest.permission.FOREGROUND_SERVICE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA};
    private Intent serviceIntent;
    private Bundle b;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        boolean finish = getActivity().getIntent().getBooleanExtra("finish", false);
        HideSysteUI.hideSystemUI(getActivity());

        startHike = RootView.findViewById(R.id.StartHike);
        if (finish) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().fileList();
            return null;
        }

        b = getActivity().getIntent().getExtras();
        String popUp_flag;
        if (b != null) {
            popUp_flag = b.getString("popUp_flag");
            if (popUp_flag != null && popUp_flag.equals("back_to_menu")) {
                startHike.setText("Resume Hike");
            }
        }
        startHike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b = getActivity().getIntent().getExtras();
                String popUp_flag;
                if (b != null) {
                    popUp_flag = b.getString("popUp_flag");
                    if (popUp_flag != null && popUp_flag.equals("back_to_menu")) {
                        getActivity().getIntent().putExtra("flag",true);
                        getActivity().onBackPressed();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (arePermissionsEnabled()) {
                                StartActivities();
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestMultiplePermissions();
                                }
                            }
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (arePermissionsEnabled()) {
                            // permission granted.
                            StartActivities();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestMultiplePermissions();
                            }

                        }
                    }
                }
            }

        });


        hikeHistory = RootView.findViewById(R.id.TrackHistory);
        hikeHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TrackHistory.class));

            }
        });

        myCollection = RootView.findViewById(R.id.MyCollection);
        myCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(arePermissionsEnabled())
                        startActivity(new Intent(getContext(), ArModels.class));
                    else{
                        requestMultiplePermissions();
                    }
                }
            }
        });
        return RootView;

    }

    public void StartActivities(){
        serviceIntent = new Intent(getActivity(), ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");
        startActivity(new Intent(getActivity(), StartHike.class));
        ContextCompat.startForegroundService(getActivity(), serviceIntent);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public  boolean arePermissionsEnabled(){
        for(String permission : permissions){

            if(ActivityCompat.checkSelfPermission(getContext(),permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public Button getHikeButton(){
        return startHike;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getContext(),permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }


}
