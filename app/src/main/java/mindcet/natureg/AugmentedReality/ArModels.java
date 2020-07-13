package mindcet.natureg.AugmentedReality;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

import mindcet.natureg.Login.LoginActivity;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class ArModels extends AppCompatActivity implements View.OnClickListener{

    private Intent intent;
    private Bundle b;
    private String status;
    private Button ciconia;
    private Button frog;
    private Button gecko;
    private Button hoopoe;
    private Button mongoose;
    private Button otter;
    private Button stellagama;
    private Button honeyBadger;
    private Button fruitBat;
    private ArrayList<Button> allButtons;
    private ArrayList<String> animalData;

    @Override
    public void onResume() {
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_models);
        HideSysteUI.hideSystemUI(this);
        try {
            status = LoginActivity.getUserDetails().get("Status").toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
        animalData = LoginActivity.getAnimalData();
        /*
        String temp = animalData.replaceAll("\\[","");
        String temp2 =temp.replaceAll("]","");
        String temp3 = temp2.replace(" ","");
        myAnimals = temp3.split(",");
        */

        Button Back = (Button) findViewById(R.id.back);
        ciconia = (Button) findViewById(R.id.ciconia);
        ciconia.setOnClickListener(this);
        frog = (Button) findViewById(R.id.frog);
        frog.setOnClickListener(this);
        gecko = (Button) findViewById(R.id.gecko);
        gecko.setOnClickListener(this);
        hoopoe = (Button) findViewById(R.id.hoopoe);
        hoopoe.setOnClickListener(this);
        mongoose = (Button) findViewById(R.id.mongoose);
        mongoose.setOnClickListener(this);
        otter = (Button) findViewById(R.id.otter);
        otter.setOnClickListener(this);
        stellagama = (Button) findViewById(R.id.stellagama);
        stellagama.setOnClickListener(this);
        honeyBadger = (Button) findViewById(R.id.honeyBadger);
        honeyBadger.setOnClickListener(this);
        fruitBat = (Button) findViewById(R.id.fruitBat);
        fruitBat.setOnClickListener(this);

        allButtons = new ArrayList<Button>() {
            {
                add(ciconia); add(frog); add(gecko);
                add(hoopoe); add(mongoose); add(otter);
                add(stellagama); add(honeyBadger); add(fruitBat);

            }
        };

        intent =  new Intent(ArModels.this,LoadModel.class);
        b = new Bundle();
        checkAnimalAccess();
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        exampleModels();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }


    private void exampleModels() {
        Button lion = findViewById(R.id.lion);
        Button cat = findViewById(R.id.cat);
        Button wolf = findViewById(R.id.penguin);
        lion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initArModelActivity(v);
            }
        });
        cat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initArModelActivity(v);
            }
        });
        wolf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initArModelActivity(v);
            }
        });

    }

    private void checkAnimalAccess() {
        if(status.equals("Teacher")|| status.equals("Admin")){
            for(Button btn: allButtons){
                int resId = getResources().getIdentifier(btn.getTag()+"_color", "drawable",this.getPackageName());
                btn.setBackgroundResource(resId);

            }
        }
        else{
            for(int i = 0; i<animalData.size();i++){
                for(Button btn: allButtons){
                    if(btn.getTag().equals(animalData.get(i))){
                        int resId = getResources().getIdentifier(btn.getTag()+"_color", "drawable",this.getPackageName());
                        btn.setBackgroundResource(resId);
                    }
                }
            }
        }

    }



    @Override
    public void onClick(View view) {
        if (status.equals("Teacher") || status.equals("Admin")) {
            //initArModelActivity(view);
            Toast.makeText(this,"Access denied",Toast.LENGTH_SHORT).show();
        }
        else{
            for(int i = 0; i <animalData.size(); i++){
                if(view.getTag().equals(animalData.get(i))){
                    //initArModelActivity(view);
                    Toast.makeText(this,"Access denied",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"Access denied",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initArModelActivity(View view){
        b.putString("animalKey", view.getTag().toString());
        intent.putExtras(b);
        startActivity(intent);
    }

}
