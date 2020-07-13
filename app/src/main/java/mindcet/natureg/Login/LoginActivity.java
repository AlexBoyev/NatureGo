package mindcet.natureg.Login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.HideSysteUI;
import mindcet.natureg.Utilities.Language;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private static final int REQUEST_SIGNUP = 0;
    private static String loginPhone;
    private static JSONObject userDetails;
    private static ArrayList<String> animalData;
    private static final int REQUEST_PASSWORDRECOVERY = 0;
    private ProgressDialog progressDialog;
    private Language language;

    @BindView(R.id.input_phone) EditText phoneText;
    @BindView(R.id.input_password) EditText _passwordText;

    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;
    @BindView(R.id.link_passwordRecover) TextView _passwordRecover;
    @BindView(R.id.btn_change_language) Button changePassword;

    public static void setLoginPhone(String phone) {
        loginPhone = phone;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideSysteUI.hideSystemUI(this);

        language = new Language(this);
        Log.i("Languages",Arrays.toString(language.getLanguages()));
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        //cloudConnection();
        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    showChangeLanguageDialog();
            }
        });

        _passwordRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivityForResult(intent,REQUEST_PASSWORDRECOVERY);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    private void showChangeLanguageDialog() {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
            mBuilder.setTitle(R.string.change_language);
            mBuilder.setSingleChoiceItems(language.getLanguages(), -1, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if( i == 0 ){
                        if(language.getDefaultLanguage().equals("en")){
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            language.setLanguage("en");
                            applyLanguage();
                        }
                    }
                    else if( i == 1){
                        if(language.getDefaultLanguage().equals("fr")){
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            language.setLanguage("fr");
                            applyLanguage();
                        }
                    }
                    else if(i == 2){
                        if(language.getDefaultLanguage().equals("ru")){
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            language.setLanguage("ru");
                            applyLanguage();
                        }
                    }
                    else if(i == 3){
                        int iwhe = 0;
                        if(language.getDefaultLanguage().equals("iw")) {
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        }
                        else if(language.getDefaultLanguage().equals("he")){
                            iwhe = 1;
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            if( iwhe == 0)
                                language.setLanguage("iw");
                            else {
                                language.setLanguage("he");
                            }
                            applyLanguage();
                        }
                    }
                    else if(i == 4){
                        if(language.getDefaultLanguage().equals("ar")){
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            language.setLanguage("ar");
                            applyLanguage();
                        }
                    }
                    else if(i == 5){
                        if(language.getDefaultLanguage().equals("es")){
                            Toast.makeText(LoginActivity.this, R.string.language_already_set, Toast.LENGTH_LONG).show();
                        } else {
                            language.setLanguage("es");
                            applyLanguage();
                        }
                    }
                    dialog.dismiss();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
    }






    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }

    public static String getLoginPhone(){
        return loginPhone;
    }

    public static JSONObject getUserDetails(){
        return userDetails;
    }

    public static  ArrayList<String> getAnimalData(){ return animalData;}

    public static void setAnimalName(String name){
        animalData.add(name);
    }

    private void loadAnimalListFromDB() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("loginPhone", LoginActivity.getLoginPhone());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        CloudRequest cloudRequest = new CloudRequest(this,jsonObject,"/getAnimals");
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                String temp = responseText.replaceAll("\\[","");
                String temp2 =temp.replaceAll("]","");
                String temp3 = temp2.replace(" ","");
                String [] myAnimals = temp3.split(",");
                animalData = new ArrayList<String>();
                for(int i = 0; i<myAnimals.length;i++){
                    animalData.add(myAnimals[i]);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void applyLanguage(){
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authentication));

        progressDialog.show();

        String phone = phoneText.getText().toString();
        String password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.

        JSONObject messageContent = new JSONObject();
        try {
            messageContent.put("Phone", phone);
            messageContent.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CloudRequest cloudRequest = new CloudRequest(this,messageContent,"/LoginCheck");
        cloudRequest.initConnection();
        cloudRequest.setOnChangeListener(new CustomListener() {
            @Override
            public void onResponseTxt(String responseText) {
                checkLogin(responseText);
            }
        });

    }

    private void checkLogin(String responseText) {
        if(!responseText.equals("False")){
            try {
                userDetails = new JSONObject(responseText);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            onLoginSuccess();
            loadAnimalListFromDB();
            progressDialog.dismiss();
        }
        else{
            onLoginFailed();
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.exit)
                .setMessage(R.string.close_naturego)
                .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExitProcess();
                    }

                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void ExitProcess(){
        Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        finish();
        finishAffinity();
        System.exit(1);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        loginPhone = phoneText.getText().toString();

        startActivity(new Intent(LoginActivity.this, ForegroundDriver.class));
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.invalid_details, Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    public boolean validate() {
        boolean valid = true;

        String phone = phoneText.getText().toString();
        String password = _passwordText.getText().toString();

        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            phoneText.setError(getString(R.string.valid_phone));
            valid = false;
        } else {
            phoneText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getString(R.string.valid_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}
