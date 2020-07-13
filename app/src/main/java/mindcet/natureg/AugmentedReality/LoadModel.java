package mindcet.natureg.AugmentedReality;


import android.animation.Animator;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mindcet.natureg.Map.StartHike;
import mindcet.natureg.R;
import mindcet.natureg.Utilities.HideSysteUI;

public class LoadModel extends AppCompatActivity implements  View.OnClickListener {

    ArFragment arFragment;
    private ModelRenderable animal;
    private int animalCount;
    private String animalName;
    private TransformableNode animalTrans = null;
    private Dialog myDialog;
    private ModelAnimator modelAnimator;
    private int nextAnimation = 0;
    private MediaPlayer mp;
    private int infoID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_model);
        HideSysteUI.hideSystemUI(this);
        Button Reset = (Button) findViewById(R.id.Reset);
        ImageButton Sound = findViewById(R.id.Sound);
        ImageButton information = findViewById(R.id.Information);
        Sound.setBackgroundResource(R.drawable.volume_off);
        myDialog = new Dialog(this);
        animalCount = 0;
        Bundle b = getIntent().getExtras();
        animalName = null;
        if(b != null){
            animalName = b.getString("animalKey");
        }
        int soundID = getResources().getIdentifier(animalName+"sound", "raw",this.getPackageName());
        infoID = getResources().getIdentifier(animalName+"_information", "string",this.getPackageName());
        mp = MediaPlayer.create(this,soundID );

        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadModel.this.recreate();
                mp.stop();
               /* arFragment.getArSceneView().getPlaneRenderer().setVisible(true);
                animalCount = 0;
                setupModel(animalName);
                if(animalTrans != null){
                    animalTrans.setParent(null);
                }
                */
            }
        });

        Sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    mp.pause();
                    Sound.setBackgroundResource(R.drawable.volume_off);
                }
                else{
                    Sound.setBackgroundResource(R.drawable.volume_on);
                    mp.start();
                }
            }
        });

        information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sweetPopup();
            }
        });
        arFragment = (ArFragment)getSupportFragmentManager().findFragmentById(R.id.ArModel);
        setupModel(animalName);
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
                if(animalCount == 0) {
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    createModel(anchorNode);
                    animalCount = 1;
                    if(animal.getAnimationDataCount() != 0)
                        animateModel(animal);



                }

            }
        });
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    public void onResume() {
        super.onResume();
        HideSysteUI.hideSystemUI(this);
    }

    private void createModel(AnchorNode anchorNode){
            animalTrans = new TransformableNode(arFragment.getTransformationSystem());
            animalTrans.setParent(anchorNode);
            animalTrans.setRenderable(animal);
            animalTrans.select();
            arFragment.getArSceneView().getPlaneRenderer().setVisible(false);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            HideSysteUI.hideSystemUI(this);
        }
    }

    private void setupModel(String animalName){
        Resources res = this.getResources();
        int animalId = res.getIdentifier(animalName,"raw",this.getPackageName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable.builder().setSource(this,animalId).build()
                    .thenAccept(renderable -> animal = renderable).exceptionally(throwable ->{
                    return null;}
            );
        }

    }



    private void animateModel(ModelRenderable renderable){
        if(modelAnimator==null || !modelAnimator.isRunning()) {

            AnimationData data = renderable.getAnimationData(nextAnimation);

                nextAnimation = (nextAnimation + 1) % renderable.getAnimationDataCount();
                modelAnimator = new ModelAnimator(data, renderable);
                modelAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animateModel(renderable);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                modelAnimator.start();
        }



    }
    @Override
    public void onBackPressed(){
        mp.stop();
        super.onBackPressed();
    }

    public void sweetPopup() {
        SweetAlertDialog myDialog = new SweetAlertDialog(LoadModel.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE);
        myDialog.setContentView(R.layout.animal_information);
        TextView text = (TextView) myDialog.findViewById(R.id.animal_info_text);
        text.setMovementMethod(new ScrollingMovementMethod());

        LinearLayout layout = myDialog.findViewById(R.id.animal_info_custom_layout);
        if(layout.getParent()!=null){
            ((ViewGroup)layout.getParent()).removeView(layout);
        }
        if(text.getParent()!=null){
            ((ViewGroup)text.getParent()).removeView(text);
        }
        text.setText(infoID);
        layout.addView(text);



        myDialog.setTitleText(animalName)
                .setCustomView(layout)
                .show();
    }
}
