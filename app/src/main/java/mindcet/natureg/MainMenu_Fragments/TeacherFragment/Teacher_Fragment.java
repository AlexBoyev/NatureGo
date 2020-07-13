package mindcet.natureg.MainMenu_Fragments.TeacherFragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;

public class Teacher_Fragment extends Fragment {
    private EditText startDate;
    private EditText endDate;
    private EditText schoolName;
    private EditText className;
    private EditText city;
    private DatePickerDialog picker;
    private ArrayList<StatisticItem> statisticItems;
    private RecyclerView recyclerView;
    private StatisticsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private View RootView;
    private Button searchByDates;
    private Button getAll;
    private Button avrageByDates;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private JSONObject jsonObject = new JSONObject();
    private JSONObject jsonObjectAll = new JSONObject();
    private JSONObject responseJson;
    JSONObject correctDict;
    private boolean correct;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RootView = inflater.inflate(R.layout.fragment_teacher, container, false);
        startDate = RootView.findViewById(R.id.start_date);
        endDate = RootView.findViewById(R.id.end_date);
        startDate.setInputType(InputType.TYPE_NULL);
        endDate.setInputType(InputType.TYPE_NULL);
        schoolName = RootView.findViewById(R.id.input_School);
        className = RootView.findViewById(R.id.input_Class);
        city = RootView.findViewById(R.id.input_city);
        searchByDates = RootView.findViewById(R.id.get_all_statistics_by_dates);
        getAll = RootView.findViewById(R.id.Get_all_statistics);
        avrageByDates = RootView.findViewById(R.id.get_all_statistics_by_dates_summery);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                startDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                endDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        getAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendHttprequest("/GetAllStatistics",jsonObjectAll);
            }
        });

        avrageByDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    statisticItems = new ArrayList<>();
                    BuildRecyclerView();
                    progressDialog = new ProgressDialog(getActivity(),
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.fetching_data));
                    progressDialog.setCancelable(false);

                    progressDialog.show();
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("StartDate",startDate.getText().toString());
                        jsonObject.put("EndDate",endDate.getText().toString());
                        jsonObject.put("SchoolName",schoolName.getText().toString());
                        jsonObject.put("Class",className.getText().toString());
                        jsonObject.put("City",city.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendHttprequest("/StatisticsByDatesAVG",jsonObject);
                }
            }
        });

        searchByDates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    statisticItems = new ArrayList<>();
                    BuildRecyclerView();
                    progressDialog = new ProgressDialog(getActivity(),
                            R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.fetching_data));
                    progressDialog.setCancelable(false);

                    progressDialog.show();
                    searchByDates.setEnabled(false);
                    jsonObject = new JSONObject();
                    try {
                        jsonObject.put("StartDate",startDate.getText().toString());
                        jsonObject.put("EndDate",endDate.getText().toString());
                        jsonObject.put("SchoolName",schoolName.getText().toString());
                        jsonObject.put("Class",className.getText().toString());
                        jsonObject.put("City",city.getText().toString());
                    } catch (JSONException e) {
                         e.printStackTrace();
                    }
                    sendHttprequest("/StatisticsByDates",jsonObject);
                }
            }
        });

        return RootView;
    }

    private boolean validate(){
        boolean valid = true;
        if(startDate.getText().toString().equals("")){
            startDate.setError(getString(R.string.please_select_date));
            valid = false;
        }
        if(endDate.getText().toString().equals("")){
            endDate.setError(getString(R.string.please_select_date));
            valid = false;
        }
        return valid;
    }

    private void sendHttprequest(String func,JSONObject jsonObject) {
        CloudRequest cloudRequest = new CloudRequest(getActivity(),jsonObject,func);
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                    responseJson = null;
                    if(responseText.equals("Failed")){
                        Toast.makeText(getActivity(),R.string.failed_to_fetch_data,Toast.LENGTH_SHORT).show();
                    }

                    else{
                        try {
                            responseJson = new JSONObject(responseText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(responseJson != null){
                            try {
                                convertJson(responseJson);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            }
        });
    }

    private void convertJson(JSONObject responseJson) throws JSONException {
        switch (responseJson.get("KEY").toString()) {
            case "All_Statistics":

                showAllStatistics(buildStringData(), getString(R.string.naturego_statistics));
                break;
            case "ByDates":
                jsonObjectAll = responseJson;
                correct = false;
                Iterator<String> keys = responseJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    try {
                        if (responseJson.get(key) instanceof JSONObject) {
                            JSONObject temp = (JSONObject) responseJson.get(key);
                            createStatisticsList(temp.get("HikeName").toString(), temp.get("Date").toString() + " " + temp.get("Time").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                progressDialog.dismiss();
                searchByDates.setEnabled(true);
                break;
            case "DatesSummery":
                correct = true;
                correctDict = (JSONObject) responseJson.get("CORRECT");

                Iterator<String> correctKeys = correctDict.keys();
                while (correctKeys.hasNext()) {
                    String key = correctKeys.next();
                    try {
                        if (correctDict.get(key) instanceof JSONObject) {
                            JSONObject temp = (JSONObject) correctDict.get(key);
                            createStatisticsList(temp.get("HikeName").toString(), temp.get("Date").toString() + " " + temp.get("Time").toString());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                progressDialog.dismiss();

                showAllStatistics(buildStringData(), getString(R.string.date_summery));

        }


    }
    private String buildStringData(){
        String data = null;
        try {
            String cuttedDistance  = String.format(Locale.US, "%.2f", Float.valueOf(responseJson.get("TotalDistance").toString()));
             data = getString(R.string.total_hikes) + responseJson.get("NumOfHikes").toString() + "\n"
                    + getString(R.string.total_photos) + responseJson.get("NumOfPhotos").toString() + "\n"
                    + getString(R.string.total_distance) + cuttedDistance + getString(R.string.meters) + "\n"
                    + getString(R.string.total_time_on_track) + responseJson.get("TotalTime").toString() + "\n";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }

    public void BuildRecyclerView(){
        recyclerView = RootView.findViewById(R.id.recyclerview_statistics);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter =  new StatisticsAdapter(statisticItems);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new StatisticsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(correct){
                    responseJson = correctDict;
                }
                JSONObject temp = null;
                try {
                    temp = (JSONObject) jsonObjectAll.get(String.valueOf(position));
                    String cuttedDistance  = String.format(Locale.US, "%.2f", Float.valueOf(temp.get("totalDistance").toString()));
                    String data = getString(R.string.total_photos) + temp.get("photoCount").toString()+"\n"
                            +   getString(R.string.total_distance) + cuttedDistance  + getString(R.string.meters) + "\n"
                            +   getString(R.string.total_time_on_track) + temp.get("Duration").toString() + "\n";
                    showAllStatistics(data,getString(R.string.hike_details));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void showAllStatistics(String message,String title){
        alertDialog = new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }



    public void createStatisticsList(String hikename, String date) {
      statisticItems.add(new StatisticItem(R.drawable.ic_statistics,hikename,date));
      adapter.notifyDataSetChanged();
    }
}
