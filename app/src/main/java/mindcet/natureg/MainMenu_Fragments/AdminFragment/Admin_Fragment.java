package mindcet.natureg.MainMenu_Fragments.AdminFragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.HideSysteUI;

public class Admin_Fragment extends Fragment implements View.OnClickListener {


    private EditText searchField;
    private Button search_name;
    private ArrayList<AdminItem> userItems;
    private RecyclerView recyclerView;
    private AdminAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private View RootView;
    private JSONObject responseJson = null;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RootView = inflater.inflate(R.layout.fragment_admin, container, false);
        searchField = RootView.findViewById(R.id.input_find_text);
        search_name = RootView.findViewById(R.id.search_name);
        search_name.setOnClickListener(this);


        return RootView;
    }

    @Override
    public void onClick(View view) {
        search_name.setEnabled(false);
        userItems = new ArrayList<>();
        BuildRecyclerView();
        JSONObject jsonObject;
        String searchValue = searchField.getText().toString();
        jsonObject = makeContent("SearchValue", searchValue);
        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching data...");
        progressDialog.setCancelable(false);

        progressDialog.show();
        sendHttprequest(jsonObject,"/Search");
    }

    private void sendHttprequest(JSONObject messageContent,String func) {
        CloudRequest cloudRequest = new CloudRequest(getActivity(),messageContent,func);
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {

                if(responseText.equals("StatusUpdated")){
                    Toast.makeText(getActivity(),"The status has been updated",Toast.LENGTH_SHORT).show();
                }
                else if(responseText.equals("User deleted")){
                    Toast.makeText(getActivity(),"The user has been deleted",Toast.LENGTH_SHORT).show();
                }
                else if(responseText.equals("Faild")){
                    Toast.makeText(getActivity(),"Cannot find the phone number",Toast.LENGTH_SHORT).show();
                }
                else{

                    try {
                        responseJson = new JSONObject(responseText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(responseJson!= null){
                        convertJson(responseJson);
                    }
                }
                progressDialog.dismiss();
                HideSysteUI.hideSystemUI(getActivity());
                search_name.setEnabled(true);
            }
        });
    }

    private void convertJson(JSONObject responseJson) {
        Iterator<String> keys = responseJson.keys();
        while(keys.hasNext()){
            String key = keys.next();
            try {
                if(responseJson.get(key) instanceof  JSONObject){
                    JSONObject temp = (JSONObject)responseJson.get(key);
                    createUserList(temp.get("Phone").toString(),temp.get("Status").toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject makeContent(String tag,String value){
        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put(tag,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messageContent;
    }

    public void BuildRecyclerView(){
        recyclerView = RootView.findViewById(R.id.recyclerview_admin);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        adapter =  new AdminAdapter(userItems);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new AdminAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    String key = userItems.get(position).getUserPhone();
                    JSONObject temp=null;
                    try {
                        temp = (JSONObject) responseJson.get(key);
                        String data = temp.get("Name").toString()+"\n"+temp.get("Family Name").toString()+"\n"+
                                temp.get("Gender").toString()+"\n"+temp.get("Email").toString()+"\n"+
                                temp.get("SchoolName").toString()+"\n"+temp.get("Class").toString()+"\n"+
                                temp.get("Date").toString();
                        showUserOutDialog(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            @Override
            public void onPromoteClick(int position) {
                alertDialogConfirmation(position,getString(R.string.promote),getString(R.string.varify_promote));
            }

            @Override
            public void onDemoteClick(int position) {
                alertDialogConfirmation(position,getString(R.string.demote),getString(R.string.varify_demote));

            }

            @Override
            public void onRemoveClick(int position) {
                alertDialogConfirmation(position,getString(R.string.delete),getString(R.string.varify_delete));
            }
        });

    }

    public void performeActionOnUser(int position, String func, String changed){
        JSONObject jsonObject = makeContent("Phone",userItems.get(position).getUserPhone());
        sendHttprequest(jsonObject,"/" + func);
        if(func.equals("Delete")){
            removeItem(position);
        } else {
            changeItem(position,changed);
        }
    }

    public void createUserList(String phone,String status) {
        userItems.add(new AdminItem(R.drawable.ic_person,phone,status));
        adapter.notifyDataSetChanged();

    }

    public void removeItem(int position){
        userItems.remove(position);
        adapter.notifyItemRemoved(position);
    }

    public void changeItem(int position,String changed){
        userItems.get(position).setUserStatus(changed);
        adapter.notifyItemChanged(position);
    }

    private void showUserOutDialog(String message){
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.user_information))
                .setMessage(message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        HideSysteUI.hideSystemUI(getActivity());
                    }
                })
                .show();
    }

    private void alertDialogConfirmation(int position,String title, String msg){
        new AlertDialog.Builder(getActivity())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton(title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       switch(title){
                           case "Promote":
                               performeActionOnUser(position,"Promote",getString(R.string.teacher));
                               HideSysteUI.hideSystemUI(getActivity());
                               break;
                           case "Demote":
                               performeActionOnUser(position,"Demote",getString(R.string.user));
                               HideSysteUI.hideSystemUI(getActivity());
                               break;
                           case "Delete":
                               performeActionOnUser(position,"Delete",null);
                               HideSysteUI.hideSystemUI(getActivity());
                               break;
                        }
                    }

                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        HideSysteUI.hideSystemUI(getActivity());
                    }
                })
                .show();
    }

}
