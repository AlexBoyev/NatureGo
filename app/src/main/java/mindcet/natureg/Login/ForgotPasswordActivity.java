package mindcet.natureg.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.CloudRequest;
import mindcet.natureg.Utilities.CustomListener;
import mindcet.natureg.Utilities.HideSysteUI;

public class ForgotPasswordActivity extends AppCompatActivity {

    @BindView(R.id.input_mobile_recover) EditText phoneText;
    @BindView(R.id.input_email_recover) EditText emailText;
    @BindView(R.id.btn_PasswordRecover) Button passwordRecoverButton;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
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
        ButterKnife.bind(this);

        passwordRecoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
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

    private void recoverPassword() {
        if (validate()){
            progressDialog = new ProgressDialog(ForgotPasswordActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(getString(R.string.recovering_passowrd));
            progressDialog.show();
            JSONObject messageContent = new JSONObject();
            String email = emailText.getText().toString();
            String phone = phoneText.getText().toString();
            try {
                messageContent.put("Email",email);
                messageContent.put("Phone",phone);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            CloudRequest cloudRequest = new CloudRequest(this,messageContent,"/PasswordRecover");
            cloudRequest.initConnection();
            cloudRequest.setOnChangeListener(new CustomListener() {
                @Override
                public void onResponseTxt(String responseText) {
                    checkRecoverPassword(responseText);
                }
            });

        }
    }

    protected void onResume(){
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

    private void checkRecoverPassword(String responseText) {
        if(responseText.equals("Recovered")){
            Toast.makeText(ForgotPasswordActivity.this, R.string.password_sent_to_email, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK, null);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            progressDialog.dismiss();
            startActivity(intent);
            finish();
        }
        else if(responseText.equals("Invalid")) {
            Toast.makeText(ForgotPasswordActivity.this, R.string.invalid_details, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
        }
    }


    private boolean validate() {
        boolean valid = true;
        String email = emailText.getText().toString();
        String phone = phoneText.getText().toString();
        if (phone.isEmpty() || !Patterns.PHONE.matcher(phone).matches()) {
            phoneText.setError(getString(R.string.valid_phone));
            valid = false;
        } else {
            phoneText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.valid_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        return valid;
    }




}
