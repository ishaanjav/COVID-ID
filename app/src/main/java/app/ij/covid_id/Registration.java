package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Registration extends AppCompatActivity {

    RadioButton medical, patient;
    RadioGroup radioGroup;
    CardView card1, card2;
    Button next;
    boolean doctor;
    boolean updated;
    float screenW, screenH;
    Page page;

    public enum Page {PAGE1, PATIENT1, DOCTOR1}

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        medical = findViewById(R.id.medical);
        patient = findViewById(R.id.patient);
        radioGroup = findViewById(R.id.userType);
        next = findViewById(R.id.next);
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card1.setBackgroundResource(R.drawable.card_white);
        card2.setBackgroundResource(R.drawable.card_white);
        page = Page.PAGE1;
        updated = false;
        doctor = false;
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;

        RadioGroup.OnCheckedChangeListener changeListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton patient = radioGroup.findViewById(R.id.patient);
                RadioButton medical = radioGroup.findViewById(R.id.medical);
                if (medical.isChecked() || patient.isChecked()) {
                    doctor = medical.isChecked();
                    updated = true;
                    next.setBackgroundResource(R.drawable.green_button);
                    next.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        };
        radioGroup.setOnCheckedChangeListener(changeListener);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!updated)
                    makeToast("Please choose one of the options above.");
                else {
                    //TODO Based on doctor or not, dispaly different things.
                    if (doctor) {

                    } else {
                        patientRegister();
                    }
                }
            }
        });
    }

    private void patientRegister() {
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_left);

        card1.startAnimation(slide);
        card2.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_right);
                card2.startAnimation(slide2);
                card2.setVisibility(View.VISIBLE);
            }
        }, 1000);
        card1.setVisibility(View.INVISIBLE);
    }

    float x1 = 0, x2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float MIN_DISTANCE = screenW / 4.1f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                Log.wtf("**POSITIONS: ", x1 + " " + x2 + "---- " + deltaX + "   . MIN: " + MIN_DISTANCE);
                if (deltaX > MIN_DISTANCE) {
                    makeToast("Left");
                    handleSwipe(true);
                } else if (deltaX * -1 > MIN_DISTANCE) {
                    makeToast("Right");
                    handleSwipe(false);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void handleSwipe(boolean b) {

    }

    public void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                showAboutApp();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAboutApp() {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.about_app_welcome);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button back = (Button) dialog.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        dialog.show();
    }

}