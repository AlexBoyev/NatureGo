package mindcet.natureg.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Hide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.HideSysteUI;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private ProgressDialog progressDialog;
    @BindView(R.id.gender_selection) RadioGroup radioGroup;
    @BindView(R.id.input_name) EditText _nameText;
    private RadioButton radioButton;
    @BindView(R.id.input_familyName) EditText _familiyName;
    @BindView(R.id.datePicker) DatePicker datepicker;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_mobile) EditText _mobileText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.input_reEnterPassword) EditText _reEnterPasswordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = getWindow().getDecorView().getRootView().getHeight();

                int keyboardHeight = screenHeight - rect.bottom;

                if (keyboardHeight > screenHeight * 0.15) {
                    hideSystemUI();
                }
            }
        });
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LOW_PROFILE |
                        View.SYSTEM_UI_FLAG_IMMERSIVE
        );
    }

    private final Handler mHideHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideSystemUI();
        }
    };

    private void delayedHide(int delayMillis) {
        mHideHandler.removeMessages(0);
        mHideHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
           HideSysteUI.hideSystemUI(this);
        }
    }

    public void signup() {
        if (!validate()) {
            return;
        }

        _signupButton.setEnabled(false);

        progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.creating_account));
        progressDialog.show();

        String name = _nameText.getText().toString();
        String familyName = _familiyName.getText().toString();
        int genderIndex = radioGroup.getCheckedRadioButtonId();
        radioButton = (RadioButton) findViewById(genderIndex);
        String gender = radioButton.getText().toString();
        Calendar calendar = getDateFromDatePicker(datepicker);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date = day + "/" + month + "/" + year;
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();

        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put("Name",name);
            messageContent.put("Family Name",familyName);
            messageContent.put("Gender",gender);
            messageContent.put("Date",date);
            messageContent.put("Email",email);
            messageContent.put("Phone",mobile);
            messageContent.put("Password",password);
            messageContent.put("Status","User");
            messageContent.put("Class","");
            messageContent.put("SchoolName","");
            messageContent.put("Score","0");
            messageContent.put("Animals","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: Implement your own signup logic here.
        CloudRequest cloudRequest = new CloudRequest(this,messageContent,"/RegisterNewUser");
        cloudRequest.initConnection();

        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                checkSign(responseText);
            }
        });
    }

    protected void onResume(){
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    public void checkSign(String responseText) {

        if(responseText.equals("Registered")){
            onSignupSuccess();
        }
        else if (responseText.equals("PhoneExists")){
            Toast.makeText(getBaseContext(), R.string.phone_already_registered, Toast.LENGTH_LONG).show();
        }
        else{
            onSignupFailed();
        }
    }


    public void onSignupSuccess() {
        Toast.makeText(getBaseContext(), R.string.registration_completed, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        progressDialog.dismiss();
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.registration_failed, Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        if( progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String familyName = _familiyName.getText().toString();
        int genderIndex = radioGroup.getCheckedRadioButtonId();
        if (genderIndex == -1) {
            Toast.makeText(SignupActivity.this, R.string.select_gender, Toast.LENGTH_LONG).show();
            valid = false;
        }
        Calendar calendar = getDateFromDatePicker(datepicker);

        int year = calendar.get(Calendar.YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if(currentYear - year  <= 6){
            Toast.makeText(SignupActivity.this, R.string.age_varification, Toast.LENGTH_LONG).show();
            valid = false;
        }
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError(getString(R.string.atleast_2_characters));
            valid = false;
        } else {
            _nameText.setError(null);
        }


        if (familyName.isEmpty() || familyName.length() < 3) {
            _familiyName.setError(getString(R.string.atleast_2_characters));
            valid = false;
        } else {
            _familiyName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.valid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=10) {
            _mobileText.setError(getString(R.string.valid_phone));
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getString(R.string.valid_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError(getString(R.string.valid_repassword));
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public static Calendar getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar;
    }
}