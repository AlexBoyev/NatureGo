package mindcet.natureg.MainMenu_Fragments;

import android.app.ProgressDialog;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.MainMenu;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.FragmentChangeListener;
import mindcet.natureg.Utilities.GetFragment;
import mindcet.natureg.Utilities.HideSysteUI;

import static mindcet.natureg.Login.SignupActivity.getDateFromDatePicker;

public class Settings_Fragment extends Fragment {
    private EditText personalName;
    private EditText familiyName;
    private EditText email;
    private EditText phone;
    private EditText schoolName;
    private EditText classNum;
    private EditText city;
    private EditText password;
    private EditText re_pass;
    private Button confirm;
    private DatePicker datepicker;
    private RadioButton radioButton;
    private RadioGroup radioGroup;
    private String loginPhone;
    private String status;
    private View RootView;
    private ProgressDialog progressDialog;
    private MainMenu_Fragment mainMenu_fragment;
    private static String className = "";
    private static String updatedSchoolName = "";
    private static String updatedCity = "";

    public static String getUpdatedSchoolName() {
        return updatedSchoolName;
    }

    public static String getClassName() {
        return className;
    }

    public static String getUpdatedCity() {
        return updatedCity;
    }

    private void hideSystemUI() {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RootView = inflater.inflate(R.layout.fragment_settings, container, false);
        personalName = RootView.findViewById(R.id.input_name);
        familiyName = RootView.findViewById(R.id.input_familyName);
        email = RootView.findViewById(R.id.input_email);
        phone = RootView.findViewById(R.id.input_mobile);
        schoolName = RootView.findViewById(R.id.input_School);
        classNum = RootView.findViewById(R.id.input_Class);
        city = RootView.findViewById(R.id.input_city);
        password = RootView.findViewById(R.id.input_password);
        re_pass = RootView.findViewById(R.id.input_reEnterPassword);
        confirm = RootView.findViewById(R.id.btn_confirm_info);
        datepicker = RootView.findViewById(R.id.datePicker);
        radioGroup = RootView.findViewById(R.id.gender_selection);
        Objects.requireNonNull(getActivity()).getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect rect = new Rect();
                if (getActivity() != null) {
                    Objects.requireNonNull(getActivity()).getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                    int screenHeight = getActivity().getWindow().getDecorView().getRootView().getHeight();

                    int keyboardHeight = screenHeight - rect.bottom;

                    if (keyboardHeight > screenHeight * 0.15) {
                        hideSystemUI();
                    }
                }
            }
        });
        MainMenu.setOnGetFrag(new GetFragment() {
            @Override
            public void getMenuFragment(MainMenu_Fragment frag) {
                mainMenu_fragment = frag;
            }
        });

        loginPhone = LoginActivity.getLoginPhone();
        try {
            status = LoginActivity.getUserDetails().get("Status").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        requestUserDetails();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    return;
                }

                update();

            }

        });
        return RootView;

    }

    private void requestUserDetails() {
        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put("Phone",loginPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CloudRequest cloudRequest = new CloudRequest(getActivity(),messageContent,"/GetUserDetails");
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                getUserDetails(responseText);
            }
        });

    }

    private void update() {

       progressDialog = new ProgressDialog(getActivity(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Update account...");
        progressDialog.show();

        String name = personalName.getText().toString();
        String familyName = familiyName.getText().toString();
        int genderIndex = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) RootView.findViewById(genderIndex);
        String gender = radioButton.getText().toString();
        Calendar calendar = getDateFromDatePicker(datepicker);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date = day + "/" + month + "/" + year;
        String mail = email.getText().toString();
        String mobile = phone.getText().toString();
        String password = re_pass.getText().toString();
        String school = schoolName.getText().toString();
        String classN = classNum.getText().toString();
        String _city = city.getText().toString();
        updatedSchoolName = school;
        className = classN;
        updatedCity = _city;

        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put("loginPhone",loginPhone);
            messageContent.put("Name",name);
            messageContent.put("Family Name",familyName);
            messageContent.put("Gender",gender);
            messageContent.put("Date",date);
            messageContent.put("Email",mail);
            messageContent.put("Phone",mobile);
            messageContent.put("Password",password);
            messageContent.put("SchoolName",school);
            messageContent.put("Class",classN);
            messageContent.put("City",_city);
            messageContent.put("Status",status);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CloudRequest cloudRequest = new CloudRequest(getActivity(),messageContent,"/UpdateUserDetails");
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                if(responseText.equals("Updated")){
                    progressDialog.dismiss();
                    loginPhone = phone.getText().toString();
                    LoginActivity.setLoginPhone(phone.getText().toString());
                    Toast.makeText(getActivity(),"Changes has been saved.",Toast.LENGTH_SHORT).show();
                    showOtherFragment("Main_Menu", mainMenu_fragment);

                }
                else{
                    Toast.makeText(getActivity(),"Fail to Update",Toast.LENGTH_SHORT).show();
                }
                HideSysteUI.hideSystemUI(getActivity());
            }
        });
    }

    private void getUserDetails(String responseText) {

        if(responseText.equals("false")){
            Toast.makeText(getActivity(), "False returned", Toast.LENGTH_LONG).show();
        }
        else {
            JSONObject responseJson = null;
            try {
                responseJson = new JSONObject(responseText);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setEditTextArgs(responseJson);
        }
    }

    public void showOtherFragment(String newFragment,Fragment frag) {
        FragmentChangeListener fc=(FragmentChangeListener)getActivity();
        fc.replaceFragment(frag,newFragment);
    }


    private void setEditTextArgs(JSONObject json) {


        try {
            personalName.setText(json.get("Name").toString());
            familiyName.setText(json.get("Family Name").toString());
            email.setText(json.get("Email").toString());
            password.setText(json.get("Password").toString());
            re_pass.setText(json.get("Password").toString());
            phone.setText(json.get("Phone").toString());
            String tempDate = json.get("Date").toString();
            String result [] = tempDate.split("-");
            String gender = json.get("Gender").toString();
            if(gender.equals("Male")){
                radioGroup.check(R.id.male_selection);
            }
            else{
                radioGroup.check(R.id.female_selection);
            }
            datepicker.updateDate(Integer.valueOf(result[0]),Integer.valueOf(result[1])-1,Integer.valueOf(result[2]));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            schoolName.setText(json.get("SchoolName").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            classNum.setText(json.get("Class").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            city.setText(json.get("City").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public boolean validate() {
        boolean valid = true;

        String name = personalName.getText().toString();
        String familyName = familiyName.getText().toString();
        int genderIndex = radioGroup.getCheckedRadioButtonId();
        if (genderIndex == -1) {
            Toast.makeText(getActivity(), R.string.select_gender, Toast.LENGTH_LONG).show();
            valid = false;
        }
        Calendar calendar = getDateFromDatePicker(datepicker);

        int year = calendar.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if(currentYear - year  <= 6){
            Toast.makeText(getActivity(), R.string.age_varification, Toast.LENGTH_LONG).show();
            valid = false;
        }
        String email = this.email.getText().toString();
        String mobile = phone.getText().toString();
        String password = this.password.getText().toString();
        String reEnterPassword = re_pass.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            personalName.setError(getString(R.string.atleast_2_characters));
            valid = false;
        } else {
            personalName.setError(null);
        }

        if (familyName.isEmpty() || familyName.length() < 3) {
            familiyName.setError(getString(R.string.atleast_2_characters));
            valid = false;
        } else {
            familiyName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError(getString(R.string.valid_email));
            valid = false;
        } else {
           this.email.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=10) {
           phone.setError(getString(R.string.valid_phone));
            valid = false;
        } else {
            phone.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            this.password.setError(getString(R.string.valid_password));
            valid = false;
        } else {
            this.password.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            re_pass.setError(getString(R.string.valid_repassword));
            valid = false;
        } else {
            re_pass.setError(null);
        }

        return valid;
    }
}