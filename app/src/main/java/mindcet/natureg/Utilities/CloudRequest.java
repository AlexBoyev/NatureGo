package mindcet.natureg.Utilities;

import android.app.Activity;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;


public class CloudRequest {


    private Activity activity;
    private JSONObject messageContent;
    private String funcActiveString;
    private String responseText;


    private CustomListener mListener;
    public void setOnChangeListener(CustomListener listener){
        mListener = listener;
    }

    public CloudRequest(Activity activity,JSONObject messageContent,String funcActiveString){
        this.activity = activity;
        this.messageContent = messageContent;
        this.funcActiveString = funcActiveString;
    }

    public void initConnection() {
        String ipv4Address = "34.76.154.55";
        String portNumber = "5000";
        String postUrl= "http://"+ipv4Address+":"+portNumber+funcActiveString;
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), messageContent.toString());
        postRequest(postUrl, body);
    }


    public void  postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request call, IOException e) {
                // Cancel the post on failure.

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()

               activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }


            @Override
            public void onResponse(Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            responseText = response.body().string();
                            if(mListener!=null){
                                mListener.onResponseTxt(responseText);
                            }

                            //Toast.makeText(LoginActivity.this, responseText, Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}
