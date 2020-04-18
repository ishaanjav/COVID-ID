package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    TextView title;
    EditText username, password;
    Button login, signup;
    ImageView toggle;
    RelativeLayout container, holder;
    boolean visible;
    TextView error;
    CardView card1, card2;

    boolean firstTime;
double screenW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title = findViewById(R.id.title);
        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.login);
        toggle = findViewById(R.id.passtoggle);
        container = findViewById(R.id.container);
        signup = findViewById(R.id.signup);
        holder = findViewById(R.id.holder);
        visible = false;
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        error = findViewById(R.id.error);
        holder.setVisibility(View.INVISIBLE);
        firstTime = true;

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;

        int color = Color.parseColor("#FFF9FF");
        toggle.setColorFilter(color);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (visible) {
                    toggle.setImageResource(R.drawable.hidepassword);
                    password.setTransformationMethod(new PasswordTransformationMethod());
                } else {
                    toggle.setImageResource(R.drawable.showpassword);
                    password.setTransformationMethod(null);
                }
                visible = !visible;
            }
        });
        //startActivity(new Intent(MainActivity.this, Registration.class));


        //holder.setVisibility(View.VISIBLE);
        initialStuff();
        clickers();


    }

    private void clickers() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String u = username.getText().toString();
                String p = password.getText().toString();
                if (u == null || p == null) {
                    longToast("Fill in the username and password.");
                    error.setText("*Fill in the username and password.");
                } else if (u.isEmpty() || p.isEmpty()) {
                    longToast("Fill in the username and password.");
                    error.setText("*Fill in the username and password.");
                } else {

                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Registration.class));
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                error.setText("");
            }
        };
        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);
    }

    private void initialStuff() {
        card1.setBackgroundResource(R.drawable.welcome_card);
        card2.setBackgroundResource(R.drawable.welcome_card);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(3000);
                title.setAnimation(fadeIn);
                title.setVisibility(View.VISIBLE);
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        showAboutApp();
                    }
                };
                Handler h = new Handler();
                h.postDelayed(r2, 1500);
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, 200);
    }

    public Menu menuOptions;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.about);
        item.setVisible(false);

        menuOptions = menu;

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
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.about_app_welcome);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button back = (Button) dialog.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                MenuItem item2 = menuOptions.findItem(R.id.about);
                item2.setVisible(true);
                holder.setVisibility(View.VISIBLE);
                if (firstTime) {
                    card1.setVisibility(View.INVISIBLE);
                  Animation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                    fadeIn.setDuration(2100);
                    card1.setAnimation(fadeIn);
                    Animation animZoomin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);

                    card1.startAnimation(animZoomin);
                    card1.setVisibility(View.VISIBLE);
                    card2.setVisibility(View.INVISIBLE);
                    Runnable r2 = new Runnable() {
                        @Override
                        public void run() {
                            Animation fadeIn = new AlphaAnimation(0, 1);
                            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                            fadeIn.setDuration(2000);
                            card2.setAnimation(fadeIn);
                            Animation animZoomin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);

                            card2.startAnimation(animZoomin);
                            card2.setVisibility(View.VISIBLE);
                        }
                    };
                    Handler h = new Handler();
                    h.postDelayed(r2, 1220);
                }
                firstTime = false;
            }
        });

        dialog.show();
    }

    public void longToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}