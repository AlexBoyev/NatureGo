package mindcet.natureg.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class TrackHistory extends AppCompatActivity {

    private List loadedData;
    private ArrayList<HikeHistoryItem> historyItems;
    private RecyclerView mRecyclerView;
    private HistoryItemAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_history);
        HideSysteUI.hideSystemUI(this);
        loadData();
        historyItems = new ArrayList<>();
        AddHistoryItems();
        ConfigureRecycleView();
        OnClickItem();

    }

    @Override
    public void onResume() {
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }

    private void OnClickItem() {
        mAdapter.setOnItemClickListener(new HistoryItemAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(int position) {
                Intent intent = new Intent(TrackHistory.this, MapHistory.class);
                Gson gson = new Gson();
                String json = gson.toJson(loadedData.get(position));
                intent.putExtra("List",json);
                startActivity(intent);

            }

            @Override
            public void OnDeleteClick(int position) {
                showDeleteDialog(position);
            }
        });
    }

    private void ConfigureRecycleView() {
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        if(historyItems != null) {
            mAdapter = new HistoryItemAdapter(historyItems);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    private void loadData(){
        sharedPreferences = getSharedPreferences("StoredData",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("DATA",null);
        Type type = new TypeToken<List<Object>>(){}.getType();
        loadedData = gson.fromJson(json,type);
    }

    private void AddHistoryItems(){
        if(loadedData != null) {
            for (Object obj : loadedData) {
                if(obj != null) {
                    Map myData = (Map) obj;
                    historyItems.add(new HikeHistoryItem(R.drawable.my_hike, myData.get("HikeName").toString(), myData.get("Date").toString()));
                }
            }
        }

    }


    private void changeData(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loadedData);
        editor.putString("DATA",json);
        editor.apply();
    }

    private void showDeleteDialog(int position){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete current hike ?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        historyItems.remove(position);
                        loadedData.remove(position);
                        changeData();
                        dialog.dismiss();
                        mAdapter.notifyItemRemoved(position);
                    }

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}
