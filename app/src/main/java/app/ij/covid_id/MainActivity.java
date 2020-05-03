package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    TextView title;
    EditText username, password;
    Button login, signup;
    ImageView toggle;
    RelativeLayout container, holder;
    boolean visible;
    TextView error;
    CardView card1, card2;
    CheckBox remember;
    TextView forgot;

    boolean firstTime;
    double screenW;

    boolean patientShowing;

    FirebaseFirestore db;
    String sx, reader;
    String readUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //writeLogin("false", getApplicationContext());
        //startActivity(new Intent(MainActivity.this, DoctorDashboard.class));

        overridePendingTransition(R.anim.fast_fade_in, R.anim.fast_fade_out);

        //makeToast("REMOVED: " + removeFile("update"));
        String logged_in = readFromFile("login.txt", getApplicationContext());
        if (!logged_in.contains("fal") && !logged_in.isEmpty()) {
            //String info = readFromFile("info.txt", getApplicationContext());
            //String[] contents = info.split("___________");
            Log.wtf("MAINACTVITY", "Logging in");
            Intent next;
            if (logged_in.contains("Pat"))
                next = new Intent(MainActivity.this, PatientDashboard.class);
            else
                next = new Intent(MainActivity.this, DoctorDashboard.class);
            //writeToUpdate("yes", getApplicationContext());
            /*Log.wtf("Username", contents[2]);
            next.putExtra("Type", contents[0]);
            next.putExtra("Document ID", contents[1]);
            next.putExtra("Username", contents[2]);
            next.putExtra("Password", contents[3]);
            next.putExtra("Account Created", contents[4]);
            next.putExtra("Name", contents[5]);
            next.putExtra("Phone", contents[6]);
            //next.putExtra("Email", contents[7]);
            next.putExtra("Document ID", contents[7]);
            next.putExtra("Status", contents[8]);
            next.putExtra("userPass ID", contents[9]);*/
            startActivity(next);
        }
        //makeToast("Removed Doctor 2: " + removeFile("doctor2"));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(300000000)
                .build();
        db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(settings);
        title = findViewById(R.id.title);
        username = findViewById(R.id.user);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.login);
        toggle = findViewById(R.id.passtoggle);
        container = findViewById(R.id.container);
        signup = findViewById(R.id.signup);
        holder = findViewById(R.id.holder);
        remember = findViewById(R.id.remember);
        visible = false;
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        error = findViewById(R.id.error);
        holder.setVisibility(View.INVISIBLE);
        forgot = findViewById(R.id.forgot);
        firstTime = true;
        card1.setBackgroundResource(R.drawable.welcome_card);
        card2.setBackgroundResource(R.drawable.welcome_card);
        loggedIn = false;

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
                    password.setSelection(password.getText().length());
                } else {
                    toggle.setImageResource(R.drawable.showpassword);
                    password.setTransformationMethod(null);
                    password.setSelection(password.getText().length());
                }
                visible = !visible;
            }
        });
        clickers();
        patientShowing = false;

        Intent intent = getIntent();
        sx = intent.getStringExtra("Type");
        reader = readFromFile("firstAccountCreated.txt", getApplicationContext());

        readUpdate = readFromUpdate(getApplicationContext());
        //makeToast("REMOVED: " + removeFile("update"));

        if (sx == null) {
            //TODO Have to check if firstAccountCreated.txt exists. If it does, just make everything visible.
            // Otherwise call initialStuff()
            if (reader.isEmpty())
                initialStuff();
            else {
                //TODO Just make the stuff visible
                //makeToast("Not null");
                listenForUpdates();
                justShowStuff();
            }
        } else {
            //TODO They just created their account.
            // Show dialog based on whether the are patient or doctor.
            justShowStuff();
            listenForUpdates();
            //makeToast("MESSAGE: " + reader);
            if (sx.equals("Doctor")) {
                doctorVerification();
                writeToFile("Done", getApplicationContext());
            } else if (sx.equals("Patient")) {
                patientContinue();
                writeToFile("Done", getApplicationContext());
            }
        }

    }

    public boolean removeFile(String name) {
        String directory = getApplicationContext().getApplicationInfo().dataDir + "/files";
        File location = new File(directory);
        File[] files = location.listFiles();
        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            if (n.substring(0, n.indexOf(".")).equals(name)) {
                files[i].delete();
                return true;
            }
        }
        return false;
    }

    private void listenForUpdates() {
        //showUpdate();
        //makeToast(readUpdate);
        db.collection("Update").document("Update 1").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                Boolean update = Boolean.parseBoolean(document.get("Update").toString());
                if (update && sx == null && !reader.isEmpty()) {
                    if (readUpdate == null || !readUpdate.contains("no")) {
                        long[] pattern = {0, 100, 50, 100, 250, 100, 50, 100, 250, 100, 50, 100, 250, 100, 50, 100};
                        //vib.vibrate(3000);
                        if (vib.hasVibrator())
                            vib.vibrate(pattern, -1);
                        else longToast("COVID-ID app updates are available! Check the Play Store.");

                        writeToUpdate("no", getApplicationContext());
                        showUpdate();
                    } else {
                        longToast("COVID-ID app updates are available! Check the Play Store.");
                    }
                } else if (update && !sx.equals("Doctor") && !sx.equals("Patient") && !reader.isEmpty()) {
                    if (readUpdate == null || !readUpdate.contains("no")) {
                        long[] pattern = {0, 100, 50, 100, 250, 100, 50, 100, 250, 100, 50, 100, 250, 100, 50, 100};
                        //vib.vibrate(3000);
                        if (vib.hasVibrator())
                            vib.vibrate(pattern, -1);
                        else longToast("COVID-ID app updates are available! Check the Play Store.");
                        writeToUpdate("no", getApplicationContext());
                        showUpdate();
                    } else {
                        longToast("UCOVID-ID app updates are available! Check the Play Store.");
                    }
                } else if (update) {
                    longToast("COVID-ID app updates are available! Check the Play Store.");
                    //makeSnackBar(2120, "Sorry. No updates available yet.");
                }
            }
        });
    }

    private void showUpdate() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.update_dialog);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        Button btn = (Button) dialog.findViewById(R.id.btn);
        ImageView image = (ImageView) dialog.findViewById(R.id.play);
        View.OnClickListener listen = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = getApplicationContext().getPackageName(); // getPackageName() from Context or Activity object
                Log.wtf("Package name", appPackageName);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        };
        btn.setOnClickListener(listen);
        image.setOnClickListener(listen);
        dialog.show();
    }

    private String readFromUpdate(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("update.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.wtf("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeLogin(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("login.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToUpdate(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("update.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("firstAccountCreated.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToInfo(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("info.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void doctorVerification() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.doctor_verification);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        //lp.width = (int) (screenW * .875);
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
        ImageView email = dialog.findViewById(R.id.email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"covid.ijapps@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Verification Status");
                email.putExtra(Intent.EXTRA_TEXT, "");

//need this to prompts email client only
                email.setType("message/rfc822");
                startActivity(email);
            }
        });

        dialog.show();
    }

    private void patientContinue() {
        patientShowing = true;

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.patient_congratulations);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        //lp.width = (int) (screenW * .875);
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

        new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView dismiss = dialog.findViewById(R.id.text2);
                String s = "Message closing in " + (millisUntilFinished / 1000 + 1);
                s += (millisUntilFinished / 1000 == 0) ? " second." : " seconds.";
                dismiss.setText(s);
            }

            @Override
            public void onFinish() {
                if ((dialog).isShowing()) {
                    dialog.dismiss();
                    dialog.cancel();
                }
            }
        }.start();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                patientShowing = false;
            }
        });

        dialog.show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //makeToast("Action not available");
    }

    private String readFromFile(String file, Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(file);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.wtf("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    boolean loggedIn;

    ProgressDialog dialog;

    private void clickers() {
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotPassword();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loggedIn = false;
                error.setText("");
                if (mySnackbar != null) mySnackbar.dismiss();
                final String u = username.getText().toString();
                final String p = password.getText().toString();
                if (u == null || p == null) {
                    makeSnackBar(3500, "Fill in the username and password.");
                    error.setText("*Fill in the username and password.");
                } else if (u.isEmpty() || p.isEmpty()) {
                    makeSnackBar(3000, "Fill in the username and password.");
                    error.setText("*Fill in the username and password.");
                } else {
                    //TODO Check credentials and also boolean from Verified Account.
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!loggedIn) {
                                dialog = ProgressDialog.show(MainActivity.this, "Verifying...",
                                        "Checking username and password.", true);
                                dialog.setCancelable(true);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!loggedIn && dialog.isShowing()) {
                                            dialog.setMessage("Please check your internet connection." +
                                                    "\nThen tap outside this box and try again.");
                                        }
                                    }
                                }, 9000);
                            }
                        }
                    }, 1100);

                    final long start = System.currentTimeMillis();
                    Log.wtf("-_--START", "" + start);
                    db.collection("userPass")
                            .whereEqualTo("User", u)
                            .whereEqualTo("Pass", p)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().size() == 0) {
                                            //INFO Credentials are wrong since there were no documents retreived with that username and password.
                                            loggedIn = true;
                                            if (dialog != null) dialog.dismiss();

                                            if (isNetworkAvailable()) {
                                                makeSnackBar(3000, "The username or password you entered is wrong.");
                                                error.setText("The username or password is wrong.");
                                            } else {
                                                error.setText("The username or password is wrong.");
                                                makeSnackBar(7700, "The username or password entered may be wrong. ALSO: Try connecting your device to the internet.");
                                            }
                                        } else {
                                            String user = u, pass = p;
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String userType = document.get("Type").toString();
                                                Log.wtf("Login SUCCESSFUL- ", user + " " + pass);
                                                boolean match = true;
                                                boolean verified = Boolean.parseBoolean(document.get("Verified").toString());
                                                String documentId = document.getId();
                                                if (verified) {
                                                    //TODO Login successful.
                                                    //TODO Send intent with the userType and documentId.
                                                    //userType = document.get("Type").toString();
                                                    error.setText("");
                                                    Intent intent;
                                                    String remaining = "";
                                                    if (userType.equals("Patient")) {
                                                        intent = new Intent(MainActivity.this, PatientDashboard.class);
                                                        remaining += "___________" + document.get("Donated").toString()
                                                                + "___________" + document.get("Willing").toString();
                                                    } else {//DONE Change to doctor dashboard
                                                        intent = new Intent(MainActivity.this, DoctorDashboard.class);
                                                        remaining += "___________" + document.get("Center").toString();
                                                    }
                                                    remaining += "___________" + document.get("CityU").toString() + "___________" + document.get("CenterU").toString();
                                                    String n = "Bob", p = "9999999999", city = "", state = "", country = "", docId;
                                                    docId = document.get("Doc ID").toString();
                                                    String userPassID = document.getId();
                                                    n = document.get("Name").toString();
                                                    p = document.get("Phone").toString();

                                                    String stat = document.get("Status").toString();
                                                    if (state.isEmpty() || state.length() == 0)
                                                        state = " ";
                                                    String e = document.get("Email").toString();
                                                    if (e.isEmpty() || e.length() == 0)
                                                        e = " ";

                                                    writeToInfo(userType + "___________" + documentId + "___________" + user + "___________" +
                                                            pass + "___________" + document.get("Updated").toString()
                                                            + "___________" + n + "___________" + p + "___________" + docId
                                                            + "___________" + stat + "___________" + userPassID + "___________" + document.get("Created").toString()
                                                            + "___________" + document.get("City").toString() + "___________" + document.get("State").toString()
                                                            + "___________" + document.get("Country").toString() + "___________" +
                                                            e + remaining, getApplicationContext());
                                                    //writeToUpdate("yes", getApplicationContext());
                                                    if (!remember.isChecked())
                                                        writeLogin("false", getApplicationContext());
                                                    else if (userType.contains("octor"))
                                                        writeLogin("Doctor", getApplicationContext());
                                                    else
                                                        writeLogin("Patient", getApplicationContext());

                                                    writeToFile("Done", getApplicationContext());

                                                    makeToast("Logged in.");
                                                    startActivity(intent);
                                                    loggedIn = true;
                                                    if (dialog != null) dialog.dismiss();
                                                    break;
                                                } else {
                                                    loggedIn = true;
                                                    if (dialog != null) dialog.dismiss();
                                                    makeSnackBar(7000, "Your account has not been verified yet. Contact covid.ijapps@gmail.com for more info.");
                                                    break;
                                                }

                                            }
                                        }
                                    } else {
                                        makeSnackBar(5000, "Could not verify your username and password. Please have a stable internet connection.");
                                        loggedIn = true;
                                        if (dialog != null) dialog.dismiss();
                                        Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                                    }
                                    long end = System.currentTimeMillis();
                                    Log.wtf("-_--END", "" + end);
                                    Log.wtf("-_--Reading Time", "" + (end - start));
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loggedIn = true;
                            if (dialog != null) dialog.dismiss();
                            makeSnackBar(5000, "Could not verify your username and password. Please have a stable internet connection.");

                        }
                    });


                    //README Old method of signing in.
                    /*db.collection("userPass").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean match = false;
                                String curUser = u;
                                String curPass = p;
                                boolean verified = false;
                                String userType = "", documentId;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String user = document.get("User").toString();
                                    String pass = document.get("Pass").toString();
                                    Log.wtf("Login - ", user + " " + pass);
                                    if (user.equals(curUser) && curPass.equals(pass)) {
                                        userType = document.get("Type").toString();
                                        Log.wtf("Login SUCCESSFUL- ", user + " " + pass);
                                        match = true;
                                        verified = Boolean.parseBoolean(document.get("Verified").toString());
                                        documentId = document.getId();
                                        if (verified) {
                                            //TODO Login successful.
                                            //TODO Send intent with the userType and documentId.
                                            //userType = document.get("Type").toString();
                                            error.setText("");
                                            Intent intent;
                                            if (userType.equals("Patient"))
                                                intent = new Intent(MainActivity.this, PatientDashboard.class);
                                            else //DONE Change to doctor dashboard
                                                intent = new Intent(MainActivity.this, DoctorDashboard.class);

                                            String n = "Bob", p = "9999999999", city = "", state = "", country = "", docId;
                                            docId = document.get("Document ID").toString();
                                            String userPassID = document.getId();
                                            n = document.get("Name").toString();
                                            p = document.get("Phone").toString();

                                            String stat = document.get("Status").toString();
                                            if (state.isEmpty() || state.length() == 0) state = " ";
                                            intent.putExtra("Type", userType);
                                            //intent.putExtra("Document ID", documentId);
                                            intent.putExtra("User", user);
                                            intent.putExtra("Pass", pass);
                                            intent.putExtra("Name", n);
                                            intent.putExtra("Phone", p);
                                            //intent.putExtra("Email", e);
                                            intent.putExtra("Document ID", docId);
                                            intent.putExtra("userPass ID", userPassID);
                                            intent.putExtra("Status", stat);

                                            intent.putExtra("Created", document.get("Updated").toString());

                                            writeToInfo(userType + "___________" + documentId + "___________" + user + "___________" +
                                                    pass + "___________" + document.get("Updated").toString()
                                                    + "___________" + n + "___________" + p + "___________" + docId
                                                    + "___________" + stat + "___________" + userPassID + "___________" + document.get("Created").toString(), getApplicationContext());
                                            if (!remember.isChecked())
                                                writeLogin("false", getApplicationContext());
                                            else if (userType.contains("octor"))
                                                writeLogin("Doctor", getApplicationContext());
                                            else writeLogin("Patient", getApplicationContext());

                                            writeToFile("Done", getApplicationContext());

                                            makeToast("Logged in.");
                                            startActivity(intent);
                                            loggedIn = true;
                                            if (dialog != null) dialog.dismiss();
                                            break;
                                        } else {
                                            loggedIn = true;
                                            if (dialog != null) dialog.dismiss();
                                            makeSnackBar(6800, "Your account has not yet been verified yet. Contact covid.ijapps@gmail.com for more info.");
                                            break;
                                        }
                                    }
                                }
                                if (!match) {
                                    loggedIn = true;
                                    if (dialog != null) dialog.dismiss();

                                    if (isNetworkAvailable()) {
                                        makeSnackBar(3000, "The username or password you entered is wrong.");
                                        error.setText("The username or password is wrong.");
                                    } else {
                                        if (task.getResult().size() == 0) {
                                            error.setText("Device not connected to network.");
                                            makeSnackBar(7000, "It appears you do not have an internet connection. Please connect before trying again.");
                                        } else {
                                            error.setText("The username or password is wrong.");
                                            makeSnackBar(7700, "The username or password you entered is wrong. ALSO: try connecting your device to the internet.");
                                        }
                                    }
                                }

                            } else {
                                makeSnackBar(5000, "Could not verify your username and password. Please have a stable internet connection.");
                                loggedIn = true;
                                if (dialog != null) dialog.dismiss();
                                Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loggedIn = true;
                            if (dialog != null) dialog.dismiss();
                            makeSnackBar(5000, "Could not verify your username and password. Please have a stable internet connection.");
                        }
                    });*/
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void justShowStuff() {
        title.setVisibility(View.VISIBLE);
        card1.setVisibility(View.VISIBLE);
        card2.setVisibility(View.VISIBLE);
        error.setVisibility(View.VISIBLE);
        holder.setVisibility(View.VISIBLE);
    }

    private void initialStuff() {
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
    boolean animate = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.about);
        item.setVisible(false);

        Intent intent = getIntent();
        String s = intent.getStringExtra("Type");
        String reader = readFromFile("firstAccountCreated.txt", getApplicationContext());
        if (s == null && !reader.isEmpty()) {
            item.setVisible(true);
            animate = false;
        }

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

    public void showForgotPassword() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.forgot_login);
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
        TextView text = dialog.findViewById(R.id.text2);
        SpannableString ss = new SpannableString(text.getText().toString());
        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#2E88F6")), 8, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setText(ss);

        ImageView email = dialog.findViewById(R.id.email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"covid.ijapps@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Forgot COVID-ID Login");
                email.putExtra(Intent.EXTRA_TEXT, "Hello,\n\tI forgot my login credentials for COVID-ID."
                        + "\n\tMy name is ______, my phone # is _______, and my username is ______.\n\tI am a (doctor, patient)\n\nThank you");

//need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email app:"));
            }
        });


        dialog.show();
    }

    Snackbar mySnackbar;

    private void makeSnackBar(int duration, String s) {
        mySnackbar = Snackbar.make(findViewById(R.id.screen), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
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

        if (!animate)
            dialog.getWindow().getAttributes().windowAnimations = R.style.SlidingDialogAnimation;

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
                if (animate) {
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