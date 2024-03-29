package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Registration extends AppCompatActivity {

    RadioButton medical, patient;
    RadioGroup radioGroup;
    CardView firstCard, patientCard1, doctorCard1, patientCard2, doctorCard2;
    Button next;
    boolean doctor;
    boolean updated;
    float screenW, screenH;
    Page page;
    Spinner state, country;
    EditText medicalLicense;

    public enum Page {PAGE1, PATIENT1, DOCTOR1, PATIENT2, DOCTOR2}

    ImageView phoneHelp, locationHelp, photoHelp, medicalCenterHelp, medicalHelp;
    CardView takeGallery, galleryCardView;
    Button patientContinue, patientFinish, doctorContinue, doctorFinish;
    Button patientBack1, patientBack2, doctorBack1, doctorBack2;
    ImageView photo;
    boolean userGood, passGood, nameGood, phoneGood, emailGood, cityGood, stateGood, countryGood;
    EditText user, pass, name, phone, email, city, medicalCenter;
    String sUser, sPass, sName, sPhone, sEmail, sCity, sState, sCountry;
    int backCounter;
    RadioButton unknown, covidPositive, covidRecovered, covidNegative;
    RadioGroup covidStatus;
    Button patientPrevious2;
    RadioButton plasmaYes, plasmaNo, willingYes, willingNo;
    RadioGroup haveDonatedGroup, willDonateGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        medical = findViewById(R.id.medical);
        patient = findViewById(R.id.patient);
        radioGroup = findViewById(R.id.userType);
        next = findViewById(R.id.next);
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        firstCard = findViewById(R.id.card1);
        patientCard1 = findViewById(R.id.card2);
        patientCard2 = findViewById(R.id.card4);
        doctorCard1 = findViewById(R.id.card3);
        doctorCard2 = findViewById(R.id.card5);
        db = FirebaseFirestore.getInstance();
        firstAsk = 0;
        denyCounter = 0;
        firstCard.setBackgroundResource(R.drawable.card_white);
        patientCard1.setBackgroundResource(R.drawable.card_white);
        patientCard2.setBackgroundResource(R.drawable.card_white);
        doctorCard1.setBackgroundResource(R.drawable.card_white);
        doctorCard2.setBackgroundResource(R.drawable.card_white);
        makeToast("It is recommended that you have a good internet connection when signing up.");
        patientCard1.setVisibility(View.INVISIBLE);
        patientCard2.setVisibility(View.INVISIBLE);
        doctorCard1.setVisibility(View.INVISIBLE);
        doctorCard2.setVisibility(View.INVISIBLE);

        page = Page.PAGE1;
        updated = false;
        doctor = false;
        pictureGood = false;
        takenPicture = false;
        phoneChanged = false;

        countryGood = true;
        backCounter = 3;

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;

        doctorContinue = doctorCard1.findViewById(R.id.doctorContinue);
        doctorBack1 = doctorCard1.findViewById(R.id.doctorPrevious1);
        doctorBack2 = doctorCard2.findViewById(R.id.doctorPrevious2);
        patientContinue = patientCard1.findViewById(R.id.patientContinue);
        patientBack1 = patientCard1.findViewById(R.id.patientPrevious1);
        patientBack2 = patientCard2.findViewById(R.id.patientPrevious2);
        //test();
        //test2();
        //patientCard1.setVisibility(View.INVISIBLE);

        //textWatcher();
        checkVisible = false;
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
                    //DONE Based on doctor or not, dispaly different things.
                    if (doctor) {
                        page = Page.DOCTOR1;
                        initializeDoctor();
                        doctorRegister();
                    } else {
                        initializePatient();
                        patientRegister();
                    }
                }
            }
        });
        //showRegister();
        patientPhoto = false;
        doctorPhoto = false;
    }

    boolean visible, confirmVisible;

    private void toggle() {
        visible = false;
        confirmVisible = false;
        passToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (visible) {
                    passToggle.setImageResource(R.drawable.hidepassword);
                    pass.setTransformationMethod(new PasswordTransformationMethod());
                    pass.setSelection(pass.getText().length());
                } else {
                    passToggle.setImageResource(R.drawable.showpassword);
                    pass.setTransformationMethod(null);
                    pass.setSelection(pass.getText().length());
                }
                visible = !visible;
            }
        });

        confirmPassToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmVisible) {
                    confirmPassToggle.setImageResource(R.drawable.hidepassword);
                    confirmPass.setTransformationMethod(new PasswordTransformationMethod());
                    confirmPass.setSelection(confirmPass.getText().length());
                } else {
                    confirmPassToggle.setImageResource(R.drawable.showpassword);
                    confirmPass.setTransformationMethod(null);
                    confirmPass.setSelection(confirmPass.getText().length());
                }
                confirmVisible = !confirmVisible;
            }
        });
    }

    Button take, delete;
    EditText confirmPass;

    private void initializePatient() {
        user = patientCard1.findViewById(R.id.user);
        pass = patientCard1.findViewById(R.id.pass);
        name = patientCard1.findViewById(R.id.name);
        phone = patientCard1.findViewById(R.id.phone);
        confirmPass = patientCard1.findViewById(R.id.confirmpass);
        check = patientCard1.findViewById(R.id.check);
        textWatcher();
        email = patientCard1.findViewById(R.id.email);
        city = patientCard1.findViewById(R.id.city);
        state = patientCard1.findViewById(R.id.state);
        country = patientCard1.findViewById(R.id.country);
        phoneHelp = patientCard1.findViewById(R.id.phoneHelp);
        locationHelp = patientCard1.findViewById(R.id.locationHelp);
        takeGallery = patientCard1.findViewById(R.id.cardholder);
        take = patientCard1.findViewById(R.id.takepicture);
        delete = patientCard1.findViewById(R.id.removepicture);
        galleryCardView = patientCard1.findViewById(R.id.removeholder);
        photo = patientCard1.findViewById(R.id.photoholder);
        photoHelp = patientCard1.findViewById(R.id.photohelp);
        patientContinue = patientCard1.findViewById(R.id.patientContinue);
        patientBack1 = patientCard1.findViewById(R.id.patientPrevious1);
        confirmPassToggle = patientCard1.findViewById(R.id.passtoggle2);
        passToggle = patientCard1.findViewById(R.id.passtoggle);
        spinners();
        helpers();
        allGood();
        previousClickers();
        test();
        toggle();
        take.setText(patientPhoto ? ("Retake") : ("Camera"));
    }

    boolean patientPhoto, doctorPhoto;

    private void initializeDoctor() {
        user = doctorCard1.findViewById(R.id.user);
        pass = doctorCard1.findViewById(R.id.pass);
        name = doctorCard1.findViewById(R.id.name);
        phone = doctorCard1.findViewById(R.id.phone);
        confirmPass = doctorCard1.findViewById(R.id.confirmpass);
        check = doctorCard1.findViewById(R.id.check);
        textWatcher();
        medicalLicense = doctorCard1.findViewById(R.id.doctorLicense);
        email = doctorCard1.findViewById(R.id.email);
        city = doctorCard1.findViewById(R.id.city);
        medicalCenter = doctorCard1.findViewById(R.id.medicalCenterName);
        medicalCenterHelp = doctorCard1.findViewById(R.id.medicalCenterHelp);
        state = doctorCard1.findViewById(R.id.state);
        country = doctorCard1.findViewById(R.id.country);
        phoneHelp = doctorCard1.findViewById(R.id.phoneHelp);
        medicalHelp = doctorCard1.findViewById(R.id.medicalLicenseHelp);
        locationHelp = doctorCard1.findViewById(R.id.locationHelp);
        takeGallery = doctorCard1.findViewById(R.id.cardholder);
        take = doctorCard1.findViewById(R.id.takepicture);
        delete = doctorCard1.findViewById(R.id.removepicture);
        galleryCardView = doctorCard1.findViewById(R.id.removeholder);
        photo = doctorCard1.findViewById(R.id.photoholder);
        photoHelp = doctorCard1.findViewById(R.id.photohelp);
        doctorContinue = doctorCard1.findViewById(R.id.doctorContinue);
        doctorBack1 = doctorCard1.findViewById(R.id.doctorPrevious1);
        confirmPassToggle = doctorCard1.findViewById(R.id.passtoggle2);
        passToggle = doctorCard1.findViewById(R.id.passtoggle);
        spinners();
        helpers();
        allGood();
        previousClickers();
        test();
        toggle();
        take.setText(doctorPhoto ? ("Retake") : ("Camera"));
    }

    @Override
    public void onBackPressed() {
        if (page == Page.DOCTOR2) {
            animateCards(doctorCard2, doctorCard1, R.anim.slide_out_right, R.anim.slide_in_right);
            page = Page.DOCTOR1;
        } else if (page == Page.DOCTOR1) {
            animateCards(doctorCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
            page = Page.PAGE1;
        } else if (page == Page.PATIENT2) {
            animateCards(patientCard2, patientCard1, R.anim.slide_out_right, R.anim.slide_in_right);
            page = Page.PATIENT1;
        } else if (page == Page.PATIENT1) {
            animateCards(patientCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
            page = Page.PAGE1;
        } else {
            if (backCounter > 0)
                makeSnackBar(5500, "Are you sure? Going back will delete your progress.\nIf you want to return to the Welcome Page, press back " + (backCounter) + ((backCounter == 1) ? " more time." : " more times."));
            else super.onBackPressed();
            backCounter--;
        }

       /* if (backCounter < 3)
            makeSnackBar(5500, "Going back will delete your progress. Instead, use the buttons or swipe to navigate between pages.\nIf you want to return to the Welcome Page, press back " + (3 - backCounter) + " times.");
        else super.onBackPressed();
        backCounter++;*/
    }

    FirebaseFirestore db;
    ImageView confirmPassToggle, passToggle;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveInformation2() {
        final String reference = (doctor) ? "Doctor" : "Patient";
        final Map<String, Object> map = new HashMap<>();
        map.put("Username", user.getText().toString().trim());
        map.put("Orig", user.getText().toString().trim());
        map.put("Password", pass.getText().toString().trim());
        map.put("Name", name.getText().toString().trim());
        map.put("Phone", phone.getText().toString().trim());
        map.put("Email", email.getText().toString().trim());
        map.put("City", city.getText().toString().trim());
        map.put("State", (country.getSelectedItem().toString().contains("United States")) ? state.getSelectedItem().toString() : "");
        map.put("Country", (country.getSelectedItem().toString()));

        final String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        final String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
        final String status = "Unknown";

        map.put("Stat", status);

        //map.put("Num of Dates", 1);
        //map.put("Status 1", status);
        //map.put("Date 1", currentDate + " " + time);
        map.put("Updated", currentDate + " " + time);
        map.put("Created", currentDate + " " + time);
        if (doctor)
            map.put("Verified", false);
        //} else {
        map.put("Donated", (haveDonatedPlasma));
        map.put("Willing", (willDonatePlasma));
        //}
         /*ProgressDialog dialog = ProgressDialog.show(Registration.this, "Creating Account",
                "Processing. Please wait...", true);*/
        final ProgressDialog dialog = new ProgressDialog(Registration.this);
        dialog.setMessage("Processing. Please wait...");
        dialog.setTitle("Creating Account");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.setIcon(R.drawable.circle);
        dialog.setProgress(1);
        dialog.setCancelable(false);
        dialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage("Processing. Please wait..." + "\nMake sure you have a good internet connection.");
            }
        }, 8250);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final int[] count = {8};
                new CountDownTimer(8000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        String s = count[0] + "";
                        count[0]--;
                        s += (millisUntilFinished / 1000 == 0) ? " second." : " seconds.";
                        if (dialog.isShowing())
                            dialog.setMessage("This took longer than expected.\nAre you connected to the internet?\n\nCheck your connection" +
                                    " and try again in " + s);
                    }

                    @Override
                    public void onFinish() {
                        if ((dialog).isShowing()) {
                            dialog.dismiss();
                            dialog.cancel();
                            makeSnackBar(8000, "Failed to create account.\nPlease make sure you have a good connection before trying again.");
                        }
                    }
                }.start();
            }
        }, 27460);
        final long start = System.currentTimeMillis();
        if (MyDebug.LOG) Log.wtf("-_--START", "" + start);
        final Map<String, Object> userPass = new HashMap<>();
        userPass.put("User", user.getText().toString().trim());
        userPass.put("Orig", user.getText().toString().trim());
        userPass.put("Pass", pass.getText().toString().trim());
        userPass.put("Verified", !doctor);
        userPass.put("Type", reference);
        userPass.put("Created", currentDate + " " + time);
        userPass.put("Updated", currentDate + " " + time);
        //userPass.put("Latest Update", currentDate + " " + time);
        userPass.put("Status", status);
//TODO Write Updates Subscollection to userPass
        userPass.put("Name", name.getText().toString().trim());
        userPass.put("Phone", phone.getText().toString().trim());

        if (doctor) {
            userPass.put("Center", medicalCenter.getText().toString().trim());
            userPass.put("License", medicalLicense.getText().toString().trim());
        }
        userPass.put("Donated", (haveDonatedPlasma));
        userPass.put("Willing", (willDonatePlasma));
        userPass.put("CenterU", "n/a");
        userPass.put("DoctorU", "n/a");
        userPass.put("CityU", "n/a");
        userPass.put("City", city.getText().toString().trim());
        userPass.put("State", (country.getSelectedItem().toString().contains("United States")) ? state.getSelectedItem().toString() : "");
        userPass.put("Country", (country.getSelectedItem().toString()));
        userPass.put("Email", email.getText().toString().trim());
        db.collection("u")
                .whereEqualTo("u", user.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean unique = task.getResult().size() == 0;
                            if (unique) {
                                //INFO Username is unique.
                                final HashMap<String, String> uMap = new HashMap<>();
                                uMap.put("u", user.getText().toString().trim());
                                uMap.put("n", name.getText().toString().trim());
                                uMap.put("t", reference);
                                db.collection("u").document(user.getText().toString().trim())
                                        .set(uMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        String s = user.getText().toString().trim().trim() + ".jpg";
                                        StorageReference storageReference2 = FirebaseStorage.getInstance().getReference(s);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        final byte[] data2 = baos.toByteArray();
                                        if (MyDebug.LOG)
                                            Log.wtf("TIMES", "" + System.currentTimeMillis());
                                        dialog.setProgress(60);
                                        final HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("Status", status);
                                        hashMap.put("City", city.getText().toString().trim());
                                        hashMap.put("State", (country.getSelectedItem().toString().contains("United States")) ? state.getSelectedItem().toString() : "");
                                        hashMap.put("Country", (country.getSelectedItem().toString()));
                                        UploadTask uploadTask = storageReference2.putBytes(data2);
                                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                //INFO Adding the maint content
                                                db.collection("Map").document(user.getText().toString().trim())
                                                        .set(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                if (MyDebug.LOG)
                                                                    Log.wtf("TIMES", "" + System.currentTimeMillis());
                                                                dialog.setProgress(70);
                                                                db.collection("userPass")
                                                                        .add(userPass)
                                                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onSuccess(DocumentReference documentReference) {
                                                                                final String userPassDocumentID = documentReference.getPath();
                                                                                DocumentReference doc = db.document(userPassDocumentID);
                                                                                map.put("userPass", userPassDocumentID);
                                                                                Map<String, Object> data = new HashMap<>();
                                                                                //data.put("Doc ID", mainDocumentID);
                                                                                //IMPORTANT Change it to below because Patient/Doctor will not be used anymore.
                                                                                if (MyDebug.LOG)
                                                                                    Log.wtf("TIMES", "" + System.currentTimeMillis());
                                                                                dialog.setProgress(80);
                                                                                data.put("Doc ID", userPassDocumentID);
                                                                                //IMPORTANT After adding the main info, get the ID and go back to userPass to add it there.
                                                                                doc.set(data, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        //INFO Write to "Updates" and put in stuff.
                                                                                        if (MyDebug.LOG)
                                                                                            Log.wtf("TIMES", "" + System.currentTimeMillis());
                                                                                        HashMap<String, Object> updateMap = new HashMap<>();
                                                                                        updateMap.put("Status", status);
                                                                                        updateMap.put("Prev", "n/a");
                                                                                        updateMap.put("City", city.getText().toString().trim());
                                                                                        updateMap.put("State", state.getSelectedItem().toString());
                                                                                        updateMap.put("Country", country.getSelectedItem().toString());
                                                                                        updateMap.put("Date", currentDate + " " + time);
                                                                                        updateMap.put("Doc", (doctor) ? "You" : "n/a");
                                                                                        updateMap.put("Center", "n/a");
                                                                                        updateMap.put("Ph", (doctor) ? phone.getText().toString().trim() : "n/a");
                                                                                        updateMap.put("Donated", (haveDonatedPlasma));
                                                                                        String em = email.getText().toString();
                                                                                        if (em.isEmpty())
                                                                                            em = "";
                                                                                        dialog.setProgress(90);
                                                                                        updateMap.put("Em", (doctor) ? em : "n/a");
                                                                                        updateMap.put("Note", "n/a");
                                                                                        db.collection(userPassDocumentID + "/Updates")
                                                                                                .document("Update 1").set(updateMap)
                                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void aVoid) {
                                                                                                        makeToast("Account created");
                                                                                                        dialog.setProgress(100);
                                                                                                        dialog.cancel();
                                                                                                        if (MyDebug.LOG)
                                                                                                            Log.wtf("TIMES", "" + System.currentTimeMillis());
                                                                                                        writeToFile(doctor ? "Doctor" : "Patient", getApplicationContext());
                                                                                                        String update = readFromFile("statusUpdate.txt", getApplicationContext());
                                                                                                        writeToUpdate(user.getText().toString() + "-----Unknown-----" + update, getApplicationContext());
                                                                                                        //IMPORTANT Account has successfully been created.
                                                                                                        Intent finish = new Intent(Registration.this, MainActivity.class);
                                                                                                        finish.putExtra("Type", doctor ? "Doctor" : "Patient");
                                                                                                        long end = System.currentTimeMillis();
                                                                                                        if (MyDebug.LOG)
                                                                                                            Log.wtf("-_--END", "" + end);
                                                                                                        if (MyDebug.LOG)
                                                                                                            Log.wtf("-_--Upload Time", "" + (end - start));
                                                                                                        //DONE Uncomment if you want TourGuide each time doctor account is created.
                                                                                                        if (reference.contains("Doc"))
                                                                                                            writeToFirstDoctor("yes");
                                                                                                        else
                                                                                                            writeToFirstPatient("yes");
                                                                                                        startActivity(finish);
                                                                                                        if (MyDebug.LOG)
                                                                                                            Log.wtf("TESTING", "DocumentSnapshot added with ID: " + userPassDocumentID);
                                                                                                    }
                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                dialog.cancel();
                                                                                                makeToast(e.getMessage());
                                                                                                if (MyDebug.LOG)
                                                                                                    Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                                                                makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                    @Override
                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                        dialog.cancel();
                                                                                        if (MyDebug.LOG)
                                                                                            Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                                                        makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                                                    }
                                                                                });
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        dialog.cancel();
                                                                        if (MyDebug.LOG)
                                                                            Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                                        makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                                    }
                                                                });
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.cancel();
                                                        if (MyDebug.LOG)
                                                            Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                        makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                dialog.cancel();
                                                if (MyDebug.LOG)
                                                    Log.wtf("FAILED FAILED FAILED_____", "Error adding document" + exception.toString());
                                                makeSnackBar(5000, "Failed to upload information. Make sure you have a stable internet connection and try again.");
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.cancel();
                                        makeSnackBar(6000, "Could not process whether your username is unique. Please have a stable internet connection.");
                                        if (MyDebug.LOG)
                                            Log.wtf("SUCCESS", "Error getting documents: " + e.toString());
                                    }
                                });


                            } else {
                                makeSnackBar(3700, "Your username was just taken. Please choose another username.");
                                unique = false;
                                dialog.cancel();
                            }
                        } else {
                            dialog.cancel();
                            makeSnackBar(6000, "Could not process whether your username is unique. Please have a stable internet connection.");
                            if (MyDebug.LOG)
                                Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.cancel();
                        makeSnackBar(6000, "Could not process whether your username is unique. Please have a stable internet connection.");
                    }
                });


        //TODO Have a DB called userPassword that stores just username and password,
        // Even later on if you add a change username/password function, do not delete the original, just add it again to user password.
        // First ask dad if someone deletes account or changes account whether new users can use their username or not.
        // IF they can't use (that is easier a lot).

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
            //FirebaseCrashlytics.getInstance().recordException(e);
            if (MyDebug.LOG) Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {//FirebaseCrashlytics.getInstance().recordException(e);
            //
            if (MyDebug.LOG) Log.wtf("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private void writeToUpdate(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("statusUpdate.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("firstAccountCreated.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void doctorRegister() {
        animateCards(firstCard, doctorCard1, R.anim.slide_out_left, R.anim.slide_in_left);
        doctorContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDoctor2();
            }
        });
    }

    private void patientRegister() {
        page = Page.PATIENT1;
        animateCards(firstCard, patientCard1, R.anim.slide_out_left, R.anim.slide_in_left);
        patientContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Uncomment below verification
                goToPatient2();
                /*if (checkPatientContinue()) {
                    animateCards(patientCard1, patientCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                    page = Page.PATIENT2;
                    initializePatient2();
                }*/
            }
        });
    }

    String sMedicalCenterName, sMedicalLicense;
    boolean takenPicture;
    boolean phoneChanged;

    private void goToDoctor2() {
        sUser = user.getText().toString().trim();
        sPass = pass.getText().toString().trim();
        sName = name.getText().toString().trim();
        sPhone = phone.getText().toString().trim();
        String confirm = confirmPass.getText().toString();
        sEmail = email.getText().toString().trim();
        sCity = city.getText().toString().trim();
        sState = state.getSelectedItem().toString();
        sCountry = state.getSelectedItem().toString();
        sMedicalCenterName = medicalCenter.getText().toString().trim();
        sMedicalLicense = medicalLicense.getText().toString().trim();
        if (validPhone(sPhone) && phoneChanged) {
            if (digitLength(sPhone) == 9)
                shortToast("Your phone # is 9 digits long");
            if (firstDigit(sPhone) != '1' && digitLength(sPhone) > 10)
                shortToast("Your phone # is 10+ digits long");
            phoneChanged = false;
        }
        if (sPhone.length() > 0 && firstDigit(sPhone) == '1' && sPhone.contains("(") && (sPhone.indexOf("(") > sPhone.indexOf("1")) && digitLength(sPhone) >= 9 && digitLength(sPhone) != 11)
            shortToast("You only have " + (digitLength(sPhone) - 1) + " digits following the country code 1.");


        if (page == Page.DOCTOR1)
            pictureGood = true;
        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (sUser.contains(" "))
            makeSnackBar(2000, "No spaces allowed in the username.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (!confirm.matches(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (!validCity(sName))
            makeSnackBar(2500, "You have not entered vowels in your name.");
        else if (sMedicalCenterName.length() <= 3)
            makeSnackBar(3000, "Please enter a valid medical center name.");
        else if (!validPhone(sPhone.trim()))
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail)) {
            makeSnackBar(2000, "Please enter a valid email.");
        } else if (!validCity(sCity))
            makeSnackBar(2000, "Please enter a valid city.");
        else if (sMedicalLicense.length() <= 2 || sMedicalLicense.length() > 30)
            makeSnackBar(1800, "Please nter a valid medical number.");
        else if (!doctorPhoto)
            makeSnackBar(2000, "Please take a picture of yourself.");
        else {
            final ProgressDialog dialog = ProgressDialog.show(Registration.this, null,
                    "Checking username uniqueness...", true);
            dialog.setCancelable(true);
            db.collection("u")
                    .whereEqualTo("u", user.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean unique = task.getResult().size() == 0;
                            /*true;
                            String curUser = user.getText().toString().trim();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String r = document.get("User").toString();
                                 if(MyDebug.LOG) Log.wtf("DOCUMENT READ: ", curUser + " =>  " + document.get("User").toString());
                                if (r.equals(curUser)) {
                                    makeSnackBar(3700, "Your username was just taken. Please choose another username.");
                                    unique = false;
                                    break;
                                }
                            }*/
                                if (unique) {
                                    //INFO Username is unique.
                                    animateCards(doctorCard1, doctorCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                                    page = Page.DOCTOR2;
                                    initializeDoctor2();
                                    dialog.cancel();
                                } else {
                                    makeSnackBar(2400, "This username is taken. Please choose another.");
                                    unique = false;
                                    dialog.cancel();
                                }
                            } else {
                                dialog.cancel();
                                if (MyDebug.LOG)
                                    Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                                makeSnackBar(7000, "Could not process whether your username is unique. Please have a stable internet connection and try again.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.cancel();
                    makeSnackBar(7000, "Could not process whether your username is unique. Please have a stable internet connection and try again.");
                }
            });
        }
    }

    private char firstDigit(String sPhone) {
        for (char c : sPhone.toCharArray()) {
            if (Character.isDigit(c)) return c;
        }
        return ' ';
    }

    private int digitLength(String phone) {
        int count = 0;
        for (char c : phone.toCharArray()) {
            if (Character.isDigit(c)) count++;
            if (MyDebug.LOG) Log.wtf("***", "" + c);
        }
        return count;
    }

    private boolean validPhone(String phone) {
        int count = 0;
        for (char c : phone.toCharArray()) {
            if (Character.isDigit(c)) count++;
        }
        if (count > 8 && count <= 16) return true;
        else return false;
    }

    private boolean validCity(String city) {
        if (city.length() < 2) return false;
        for (char c : city.toCharArray())
            if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U')
                return true;

        return false;
    }

    private void goToPatient2() {
        sUser = user.getText().toString().trim();
        sPass = pass.getText().toString().trim();
        sName = name.getText().toString().trim();
        String confirm = confirmPass.getText().toString();
        sPhone = phone.getText().toString().trim();
        sEmail = email.getText().toString().trim();
        sCity = city.getText().toString().trim();
        sState = state.getSelectedItem().toString();
        sCountry = state.getSelectedItem().toString();

        if (validPhone(sPhone) && phoneChanged) {
            if (digitLength(sPhone) == 9)
                shortToast("Your phone # is 9 digits long");
            if (firstDigit(sPhone) != '1' && digitLength(sPhone) > 10)
                shortToast("Your phone # is 10+ digits long");
            phoneChanged = false;
        }
        if (sPhone.length() > 0 && firstDigit(sPhone) == '1' && sPhone.contains("(") && (sPhone.indexOf("(") > sPhone.indexOf("1")) && digitLength(sPhone) >= 9 && digitLength(sPhone) != 11)
            shortToast("You only have " + (digitLength(sPhone) - 1) + " digits following the country code 1.");

        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (sUser.contains(" "))
            makeSnackBar(2000, "No spaces allowed in the username.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (!confirm.equals(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (!validCity(sName))
            makeSnackBar(2500, "You have not entered vowels in your name.");
        else if (!validPhone(sPhone.trim()))
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail))
            makeSnackBar(2000, "Please enter a valid email.");
        else if (!validCity(sCity))
            makeSnackBar(2000, "Please enter a valid city.");
        else if (!patientPhoto)
            makeSnackBar(2000, "Please take a picture of yourself.");
        else {
            final ProgressDialog dialog = ProgressDialog.show(Registration.this, null,
                    "Checking username uniqueness...", true);
            dialog.setCancelable(true);
            db.collection("u")
                    .whereEqualTo("u", user.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean unique = task.getResult().size() == 0;
                            /*true;
                            String curUser = user.getText().toString().trim();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String r = document.get("User").toString();
                                 if(MyDebug.LOG) Log.wtf("DOCUMENT READ: ", curUser + " =>  " + document.get("User").toString());
                                if (r.equals(curUser)) {
                                    makeSnackBar(3700, "Your username was just taken. Please choose another username.");
                                    unique = false;
                                    break;
                                }
                            }*/
                                if (unique) {
                                    //INFO Username is unique.
                                    animateCards(patientCard1, patientCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                                    page = Page.PATIENT2;
                                    initializePatient2();
                                    dialog.cancel();
                                } else {
                                    makeSnackBar(2400, "This username is taken. Please choose another.");
                                    unique = false;
                                    dialog.cancel();
                                }
                            } else {
                                dialog.cancel();
                                if (MyDebug.LOG)
                                    Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                                makeSnackBar(7000, "Could not process whether your username is unique. Please have a stable internet connection and try again.");
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    dialog.cancel();
                    makeSnackBar(7000, "Could not process whether your username is unique. Please have a stable internet connection and try again.");
                }
            });
        }
    }

    ImageView check;

    private void initializeDoctor2() {
        doctorBack2 = doctorCard2.findViewById(R.id.doctorPrevious2);
        doctorFinish = doctorCard2.findViewById(R.id.doctorFinish);
        // unknown = patientCard2.findViewById(R.id.unknown);
        // covidPositive = patientCard2.findViewById(R.id.covidpositive);
        //  covidRecovered = patientCard2.findViewById(R.id.covidnegative);
        //  covidNegative = patientCard2.findViewById(R.id.covidn);
        plasmaYes = doctorCard2.findViewById(R.id.plasmayes);
        plasmaNo = doctorCard2.findViewById(R.id.plasmano);
        willingYes = doctorCard2.findViewById(R.id.willingyes);
        willingNo = doctorCard2.findViewById(R.id.willingno);
        //   covidStatus = patientCard2.findViewById(R.id.covidStatus);
        haveDonatedGroup = doctorCard2.findViewById(R.id.donationgroupStatus);
        willDonateGroup = doctorCard2.findViewById(R.id.willdonategroupstatus);
        /*try {
            getImageSize();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        doctorDone();
    }

    private void initializePatient2() {
        //statusHelp = patientCard2.findViewById(R.id.statusHelp);
        patientPrevious2 = patientCard2.findViewById(R.id.patientPrevious2);
        patientFinish = patientCard2.findViewById(R.id.patientFinish);
        // unknown = patientCard2.findViewById(R.id.unknown);
        // covidPositive = patientCard2.findViewById(R.id.covidpositive);
        //  covidRecovered = patientCard2.findViewById(R.id.covidnegative);
        //  covidNegative = patientCard2.findViewById(R.id.covidn);
        plasmaYes = patientCard2.findViewById(R.id.plasmayes);
        plasmaNo = patientCard2.findViewById(R.id.plasmano);
        willingYes = patientCard2.findViewById(R.id.willingyes);
        willingNo = patientCard2.findViewById(R.id.willingno);
        //   covidStatus = patientCard2.findViewById(R.id.covidStatus);
        haveDonatedGroup = patientCard2.findViewById(R.id.donationgroupStatus);
        willDonateGroup = patientCard2.findViewById(R.id.willdonategroupstatus);
       /* try {
            getImageSize();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        patientDone();
    }

    boolean u, covidP, covidR, covidN, haveDonatedPlasma, willDonatePlasma;

    private void doctorDone() {
        doctorFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Check if they have selected one of the 3 options from RadioGroup
                // Then ask if sure
               /* u = unknown.isChecked();
                covidP = covidPositive.isChecked();
                covidR = covidRecovered.isChecked();
                covidN = covidNegative.isChecked();*/

                haveDonatedPlasma = plasmaYes.isChecked();
                boolean noDonate = plasmaNo.isChecked();
                willDonatePlasma = willingYes.isChecked();
                boolean noWilling = willingNo.isChecked();

                if ((plasmaYes.isChecked() || plasmaNo.isChecked()) && (willingYes.isChecked() || willingNo.isChecked()))
                    showRegister();
                else
                    makeSnackBar(1900, "Select the options above.");
            }
        });
    }

    private void patientDone() {
        patientFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Check if they have selected something from each RadioGroup.
                // Then ask if sure

                // unknown.setChecked(true);
                haveDonatedPlasma = plasmaYes.isChecked();
                boolean noDonate = plasmaNo.isChecked();
                willDonatePlasma = willingYes.isChecked();
                boolean noWilling = willingNo.isChecked();

           /*     u = unknown.isChecked();
                covidP = covidPositive.isChecked();
                covidR = covidRecovered.isChecked();
                covidN = covidNegative.isChecked();
*/
                if ((plasmaYes.isChecked() || plasmaNo.isChecked()) && (willingYes.isChecked() || willingNo.isChecked()))
                    showRegister();
                else
                    makeSnackBar(1900, "Select the options above.");
            }
        });
    }

    private boolean uniqueUsername() {
        //TODO Write code to determine if username is unique.
        /*db.collection("cities")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean unique = true;
                            String curUser = user.getText().toString().trim();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String r = (String) document.get("Username");
                                if(r.equals(curUser))
                                    return false;
                                // if(MyDebug.LOG) Log.wtf("SUCCESSS", document.getId() + " => " + document.getData());
                            }
                        } else {
                             if(MyDebug.LOG) Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                        }
                    }
                });*/
        return true;
    }

    public static boolean isValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    boolean checkVisible;
    String confirmPrev, curPrev;

    private void textWatcher() {
        //FUTURE If you want to then make this where it makes the button green from grey if they can continue.
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                if (s.length() > 40) {
                    shortToast("Your name is too long.");
                    name.setText(s.substring(0, 40));
                    name.setSelection(40);
                }
            }
        });
        confirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                confirmPrev = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                check.setVisibility(View.INVISIBLE);
                if (!confirmPrev.equals(s)) {
                    if (s != null && !s.isEmpty()) {
                        if (s.equals(pass.getText().toString())) {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_50);
                            check.startAnimation(slide);

                            check.setVisibility(View.VISIBLE);
                            checkVisible = true;
                        } else {
                            if (checkVisible) {
                                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out_50);
                                check.startAnimation(slide);
                            }
                            checkVisible = false;
                            check.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (checkVisible) {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out_50);
                            check.startAnimation(slide);
                        }
                        checkVisible = false;
                        check.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                curPrev = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                if (s.length() > 30) {
                    shortToast("Your password is too long.");
                    pass.setText(s.substring(0, 30));
                    pass.setSelection(30);
                }
                if (!curPrev.equals(s)) {
                    check.setVisibility(View.INVISIBLE);
                    if (s != null && !s.isEmpty()) {
                        if (s.equals(confirmPass.getText().toString())) {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_50);
                            check.startAnimation(slide);

                            check.setVisibility(View.VISIBLE);
                            checkVisible = true;
                        } else {
                            if (checkVisible) {
                                Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out_50);
                                check.startAnimation(slide);
                            }
                            checkVisible = false;
                            check.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        if (checkVisible) {
                            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out_50);
                            check.startAnimation(slide);
                        }
                        checkVisible = false;
                        check.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        user.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = editable.toString().trim();
                if (s.length() > 30) {
                    shortToast("Your username is too long.");
                    user.setText(s.substring(0, 30));
                    user.setSelection(30);
                }
            }
        });
        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                phoneChanged = true;
                sPhone = editable.toString().trim();
                /*if (sPhone.length() > 0 && firstDigit(sPhone) == '1' && sPhone.contains("(") && (sPhone.indexOf("(") > sPhone.indexOf("1")) && digitLength(sPhone) >= 9 && digitLength(sPhone) != 11)
                        shortToast("You only have " + (digitLength(sPhone) - 1) + " digits following the country code 1.");
                if (sPhone.length() > 0 && firstDigit(sPhone) == '1' && sPhone.contains("(") && (sPhone.indexOf("(") > sPhone.indexOf("1")) && digitLength(sPhone) == 11)
                    toast.cancel();*/
            }
        });
    }

    boolean pictureGood;

    private void previousClickers() {
        //DONE Deal with previous button click.
        View.OnClickListener back = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.patientPrevious1) {
                    animateCards(patientCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
                    page = Page.PAGE1;
                } else if (view.getId() == R.id.doctorPrevious1) {
                    animateCards(doctorCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
                    page = Page.PAGE1;
                } else if (view.getId() == doctorBack2.getId()) {
                    animateCards(doctorCard2, doctorCard1, R.anim.slide_out_right, R.anim.slide_in_right);
                    page = Page.DOCTOR1;
                } else if (view.getId() == patientBack2.getId()) {
                    animateCards(patientCard2, patientCard1, R.anim.slide_out_right, R.anim.slide_in_right);
                    page = Page.PATIENT1;
                }
            }
        };
        patientBack1.setOnClickListener(back);
        patientBack2.setOnClickListener(back);
        doctorBack2.setOnClickListener(back);
        doctorBack1.setOnClickListener(back);
    }

    Uri extra;
    ContentValues values;
    Uri imageUri;

    int firstAsk = 0;
    int GALLERY = 1001;

    private void test() {
        View.OnClickListener clicker = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Registration.this, new String[]{Manifest.permission.CAMERA}, 1034);

                    } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        showPicQuality();
                       /* requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                3);*/

                        //firstAsk++;
                        return;
                    } else {
                        values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New Picture");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ;
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        makeToast("Take a picture of your face.");
                        startActivityForResult(intent, 100);
                       /* Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, extra);
                        startActivityForResult(cameraIntent, 100);*/
                    }
                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    makeToast(errorMessage);
                    FirebaseCrashlytics.getInstance().recordException(anfe);
                }
            }
        };
        takeGallery.setOnClickListener(clicker);
        take.setOnClickListener(clicker);
        View.OnClickListener gallery = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY);
               /* if (doctor)
                    photo.setImageResource(R.drawable.dbadge);
                else
                    photo.setImageResource(R.drawable.usericon);
                pictureGood = false;
                allGood();
                patientContinue.setBackgroundResource(R.drawable.greybutton);*/
            }
        };

        delete.setOnClickListener(gallery);
        galleryCardView.setOnClickListener(gallery);
    }

    boolean swapped = false;

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }*/

    int count = 21;

    private void doUpload(Bitmap bitmap, String id, ProgressDialog dialog) {
        String s = id + ".jpg";
        StorageReference storageReference2 = FirebaseStorage.getInstance().getReference(s);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data2 = baos.toByteArray();

        UploadTask uploadTask = storageReference2.putBytes(data2);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                makeToast("Exception " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                makeToast("Picture saved.");
            }
        });
    }

    public void showPicQuality() {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.image_quality);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .85);

        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        Window window = dialog.getWindow();

        WindowManager.LayoutParams wlp = window.getAttributes();
        makeToast("Take a picture of your face.");

        /*wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);*/

        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 100; // bottom margin
        dialog.getWindow().setAttributes(layoutParams);

        CardView card = dialog.findViewById(R.id.card1);
        card.setBackgroundResource(R.drawable.card_black);

        Button low = (Button) dialog.findViewById(R.id.low);
        Button high = (Button) dialog.findViewById(R.id.high);
        final boolean[] Idismissed = {false};
        low.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Idismissed[0] = true;
                dialog.cancel();
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 10);
            }
        });
        high.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Save their info then go back to welcome page.
                dialog.dismiss();
                Idismissed[0] = true;
                dialog.cancel();
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        3);
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                pictureGood = (doctor) ? doctorPhoto : patientPhoto;
                if (!pictureGood && !Idismissed[0])
                    makeToast("A picture is required. Click the help icon for more info.");
            }
        });
        dialog.show();
    }

    Bitmap bitmap;
    boolean toCompress;

    private long getImageSize() throws IOException {
        long lengthbmp = 0;
        /*if (toCompress) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] imageInByte = stream.toByteArray();
            lengthbmp = imageInByte.length;
             if(MyDebug.LOG) Log.wtf("-_- BEFORE IMAGE SIZE: ", "" + lengthbmp + " " + bitmap.getWidth() + " " + bitmap.getHeight());

            stream = new ByteArrayOutputStream();
            if (lengthbmp > 1000000) {
                if (lengthbmp > 4000000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 15, stream);
                else if (lengthbmp > 3000000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 18, stream);
                if (lengthbmp > 2500000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 29, stream);
                else if (lengthbmp > 2000000) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 35, stream);
                } else if (lengthbmp > 1500000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
                else if (lengthbmp > 1000000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 46, stream);
                else if(lengthbmp > 750000)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 55, stream);

                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(stream.toByteArray()));
                imageInByte = stream.toByteArray();
                lengthbmp = imageInByte.length;
                 if(MyDebug.LOG) Log.wtf("-_- AFTER IMAGE SIZE: ", "" + lengthbmp + " " + bitmap.getWidth() + " " + bitmap.getHeight());
                photo.setImageBitmap(bitmap);
            }
            toCompress = false;
        }*/
        return lengthbmp;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            //README Getting picture from gallery.
            if (requestCode == GALLERY) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), uri);
                    makeToast("Double tap or long press the image to rotate it.");
                    ;
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    if (MyDebug.LOG) Log.wtf("Error", e.toString());
                    makeToast("Error. Try again or use the camera.");
                }

                if (bitmap.getWidth() > bitmap.getHeight()) {
                    bitmap = Bitmap.createBitmap(
                            bitmap,
                            bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                            0,
                            bitmap.getHeight(),
                            bitmap.getHeight());
                } else if (bitmap.getHeight() > bitmap.getWidth()) {
                    bitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                            bitmap.getWidth(),
                            bitmap.getWidth());
                }
                pictureGood = true;
                if (doctor) {
                    doctorPhoto = true;
                    //patientPhoto = false;
                } else {
                    patientPhoto = true;
                    //doctorPhoto = false;
                }
                int targetDimension = 560;
                bitmap = Bitmap.createScaledBitmap(bitmap, targetDimension, targetDimension, true);
                photo.setImageBitmap(bitmap);
            }
            if (requestCode == 10) {
                toCompress = false;
                //README They choose to take low quality image.
               /* try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                } catch (IOException e) {*/
                bitmap = (Bitmap) data.getExtras().get("data");
            /*        e.printStackTrace();
                }*/

                Matrix matrix = new Matrix();
                matrix.postRotate(180);

                pictureGood = true;
                allGood();
                matrix.postRotate(90);

                if (!doctor) {
                    matrix.postRotate(90);
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                                0,
                                bitmap.getHeight(),
                                bitmap.getHeight(), matrix, true
                        );
                    } else if (bitmap.getHeight() > bitmap.getWidth()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                                bitmap.getWidth(),
                                bitmap.getWidth(),
                                matrix, true);
                    }
                } else {
                    matrix.postRotate(90);
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                                0,
                                bitmap.getHeight(),
                                bitmap.getHeight(), matrix, true
                        );
                    } else if (bitmap.getHeight() > bitmap.getWidth()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                                bitmap.getWidth(),
                                bitmap.getWidth(),
                                matrix, true);
                    }
                }
                makeToast("Double tap or long press the image to rotate it.");
                makeSnackBar(4000, "The image is low quality. Please consider taking a higher quality picture.");
                //saveImage(getApplicationContext(), bitmap, "temp", "jpg");

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream);
                if (doctor) {
                    doctorPhoto = true;
                    //patientPhoto = false;
                    take.setText("Retake");
                } else {
                    patientPhoto = true;
                    //doctorPhoto = false;
                    take.setText("Retake");
                }
                photo.setImageBitmap(bitmap);
                takenPicture = true;
            }
            if (requestCode == 100) {
                toCompress = true;
                //extra = data.getData();
                //extra = data.getData();
                //INFO Getting high quality image
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                } catch (IOException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    bitmap = (Bitmap) data.getExtras().get("data");
                    e.printStackTrace();
                }

                Matrix matrix = new Matrix();
                matrix.postRotate(180);

                pictureGood = true;
                allGood();
                matrix.postRotate(90);

                matrix.postRotate(0);
                if (!doctor) {
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                                0,
                                bitmap.getHeight(),
                                bitmap.getHeight(), matrix, true
                        );
                    } else if (bitmap.getHeight() > bitmap.getWidth()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                                bitmap.getWidth(),
                                bitmap.getWidth(),
                                matrix, true);
                    }
                } else {
                    matrix.postRotate(180);
                    if (bitmap.getWidth() > bitmap.getHeight()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                                0,
                                bitmap.getHeight(),
                                bitmap.getHeight(), matrix, true
                        );
                    } else if (bitmap.getHeight() > bitmap.getWidth()) {
                        bitmap = Bitmap.createBitmap(
                                bitmap,
                                0,
                                bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                                bitmap.getWidth(),
                                bitmap.getWidth(),
                                matrix, true);
                    }
                }
                //makeSnackBar(3700, "Double tap or long press the image to rotate it.");
/*new Handler().postDelayed(new Runnable() {
    @Override
    public void run() {
    }
},2000);*/
                //saveImage(getApplicationContext(), bitmap, "temp", "jpg");

                //README Step 1 - Resize image
                int targetDimension = 560;
                bitmap = Bitmap.createScaledBitmap(bitmap, targetDimension, targetDimension, true);

                //README Step 2 - Compress Image
                long lengthbmp;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageInByte = stream.toByteArray();
                lengthbmp = imageInByte.length;
                if (MyDebug.LOG)
                    Log.wtf("-_- BEFORE IMAGE SIZE: ", "" + lengthbmp + " " + bitmap.getWidth() + " " + bitmap.getHeight());

                stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                imageInByte = stream.toByteArray();
                lengthbmp = imageInByte.length;
                if (MyDebug.LOG)
                    Log.wtf("-_- AFTER IMAGE SIZE: ", "" + lengthbmp + " " + bitmap.getWidth() + " " + bitmap.getHeight());

                photo.setImageBitmap(bitmap);
                takenPicture = true;
                if (doctor) {
                    doctorPhoto = true;
                    //patientPhoto = false;
                    take.setText("Retake");
                } else {
                    patientPhoto = true;
                    //doctorPhoto = false;
                    take.setText("Retake");
                }
                /*ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                photo.setImageBitmap(bitmap);*/
                makeToast("Double tap or long press the image to rotate it.");
//TODO Set takenpicture = to false if doctor presses discard.
// Maybe show message saying are you sure you don't want to take picture.

            }

        }

    }

    int denyCounter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //INFO If they choose to take high quality image.
        if (requestCode == 3) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                imageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, 100);
            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                if (denyCounter > 0)
                    makeSnackBar(3900, "To take a high quality picture, storage permission is required.");
                denyCounter++;
            }
        } else if (requestCode == 1034) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                    imageUri = getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, 100);
                } else {
                    showPicQuality();
                }
            } else if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                //makeToast("You must allow camera permission to take a picture");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = (float) bmp.getWidth() / (float) bmp.getHeight();
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(exception);
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(exception);
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private void helpers() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doctor) {
                    if (view.getId() == R.id.phoneHelp)
                        makeSnackBar(3000, "The phone # is used to authenticate your account.");
                    if (view.getId() == R.id.locationHelp)
                        makeSnackBar(8500, "The location is used to display a list of your patients in your area. If you don't provide an accurate location, you may not see your patients or related results at all.");
                    if (view.getId() == R.id.photohelp)
                        makeSnackBar(5500, "Please take a picture of your face (or ID as verification of your profession). This is only visible to other doctors.");
                    if (view.getId() == R.id.medicalCenterHelp)
                        makeSnackBar(3700, "This is the name of the medical center or hospital where you work.");
                    if (view.getId() == R.id.medicalLicenseHelp)
                        makeSnackBar(6000, "Please provide your legal medical license # (or registration #) as issued by competent authorities for verification.");
                } else {
                    if (view.getId() == R.id.phoneHelp)
                        makeSnackBar(3500, "Your phone # is required for doctors to contact you.");
                    if (view.getId() == R.id.locationHelp)
                        makeSnackBar(8900, "The location is used to show your COVID status to medical professionals in your area. If you don't provide your actual location, doctors may not be able to view or update your status.");
                    if (view.getId() == R.id.photohelp)
                        makeSnackBar(6100, "Please take a picture of only your face. Your photo is only visible to doctors and can be used as verification where accepted.");
                }
            }
        };
        photoHelp.setOnClickListener(listener);
        phoneHelp.setOnClickListener(listener);
        locationHelp.setOnClickListener(listener);
        if (medicalHelp != null)
            medicalHelp.setOnClickListener(listener);
        if (medicalCenterHelp != null)
            medicalCenterHelp.setOnClickListener(listener);

        photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (bitmap != null) {
                    bitmap = RotateBitmap(bitmap, 270f);
                    photo.setImageBitmap(bitmap);
                    Vibrator vibrator = (Vibrator) Registration.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(200);
                }
                return false;
            }
        });
        photo.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(Registration.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (bitmap != null) {
                        bitmap = RotateBitmap(bitmap, 90f);
                        photo.setImageBitmap(bitmap);
                        Vibrator vibrator = (Vibrator) Registration.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(200);
                    }
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void makeSnackBar(int duration, String s) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.registration), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(5);
        if (!s.contains("Going back"))
            tv.setTextSize(15);
        mySnackbar.show();
    }

    private void spinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.state_spinner, getStates());
        state.setAdapter(adapter);
        state.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(0xFFA456DC);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                makeToast("Please select a state if you are in the US.");
            }
        });

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getApplicationContext(), R.layout.country_spinner, getCountries());
        country.setAdapter(adapter2);
        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(0xFFA456DC);
                TextView text = (TextView) country.getSelectedView();
                String result = text.getText().toString().trim();
                if (result.contains("United States")) {
                    state.setEnabled(true);
                    ((TextView) state.getChildAt(0)).setTextColor(0xFFA456DC);
                } else {
                    state.setEnabled(false);
                    ((TextView) state.getChildAt(0)).setTextColor(0xFF8C8C8C);
                }
                countryGood = true;
                allGood();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                makeToast("Please select a country.");
            }
        });
    }

    private void allGood() {
        //FUTURE Uncomment if you decide to use this where it changes button color based on whether they can move ahead.
        //if(userGood && passGood && nameGood && phoneGood && emailGood && cityGood && countryGood && pictureGood)
        patientContinue.setBackgroundResource(R.drawable.green_button);
        doctorContinue.setBackgroundResource(R.drawable.green_button);
        /*else
            patientContinue.setBackgroundResource(R.drawable.greybutton);*/
    }

    private ArrayList<String> getCountries() {
        ArrayList<String> countries = new ArrayList<>();
        countries.add("US: United States");
        countries.add("AF: Afghanistan");
        countries.add("AL: Albania");
        countries.add("DZ: Algeria");
        countries.add("AS: American Samoa");
        countries.add("AD: Andorra");
        countries.add("AO: Angola");
        countries.add("AI: Anguilla");
        countries.add("AR: Argentina");
        countries.add("AM: Armenia");
        countries.add("AW: Aruba");
        countries.add("AU: Australia");
        countries.add("AT: Austria");
        countries.add("AZ: Azerbaijan");
        countries.add("BS: Bahamas");
        countries.add("BH: Bahrain");
        countries.add("BD: Bangladesh");
        countries.add("BB: Barbados");
        countries.add("BY: Belarus");
        countries.add("BE: Belgium");
        countries.add("BZ: Belize");
        countries.add("BJ: Benin");
        countries.add("BM: Bermuda");
        countries.add("BT: Bhutan");
        countries.add("BO: Bolivia");
        countries.add("BA: Bosnia");
        countries.add("BW: Botswana");
        countries.add("BR: Brazil");
        countries.add("BN: Darussalam");
        countries.add("BG: Bulgaria");
        countries.add("BF: Burkina Faso");
        countries.add("BI: Burundi");
        countries.add("CV: Verde");
        countries.add("KH: Cambodia");
        countries.add("CM: Cameroon");
        countries.add("CA: Canada");
        countries.add("CF: Central African Rep.");
        countries.add("TD: Chad");
        countries.add("CL: Chile");
        countries.add("CN: China");
        countries.add("CO: Colombia");
        countries.add("KM: Comoros");
        countries.add("CD: Congo D");
        countries.add("CG: Congo R");
        countries.add("CR: Costa Rica");
        countries.add("CI: Cote D'Ivoire");
        countries.add("HR: Croatia");
        countries.add("CU: Cuba");
        countries.add("CW: Curaçao");
        countries.add("CY: Cyprus");
        countries.add("CZ: Czechia");
        countries.add("DK: Denmark");
        countries.add("DJ: Djibouti");
        countries.add("DM: Dominica");
        countries.add("DO: Republic");
        countries.add("EC: Ecuador");
        countries.add("EG: Egypt");
        countries.add("SV: El Salvador");
        countries.add("GQ: Equatorial Guinea");
        countries.add("ER: Eritrea");
        countries.add("EE: Estonia");
        countries.add("SZ: Eswatini");
        countries.add("ET: Ethiopia");
        countries.add("FJ: Fiji");
        countries.add("FI: Finland");
        countries.add("FR: France");
        countries.add("GF: French Guiana");
        countries.add("PF: French Polynesia");
        countries.add("GA: Gabon");
        countries.add("GM: Gambia");
        countries.add("GE: Georgia");
        countries.add("DE: Germany");
        countries.add("GH: Ghana");
        countries.add("GI: Gibraltar");
        countries.add("GR: Greece");
        countries.add("GL: Greenland");
        countries.add("GD: Grenada");
        countries.add("GP: Guadeloupe");
        countries.add("GU: Guam");
        countries.add("GT: Guatemala Guatemala.");
        countries.add("GG: Guernsey");
        countries.add("GN: Guinea");
        countries.add("GW: Guinea-Bissau");
        countries.add("GY: Guyana");
        countries.add("HT: Haiti");
        countries.add("HN: Honduras");
        countries.add("HK: Hong Kong");
        countries.add("HU: Hungary");
        countries.add("IS: Iceland");
        countries.add("IN: India");
        countries.add("ID: Indonesia");
        countries.add("IR: Iran");
        countries.add("IQ: Iraq");
        countries.add("IE: Ireland");
        countries.add("IM: Isle of Man.");
        countries.add("IL: Israel");
        countries.add("IT: Italy");
        countries.add("JM: Jamaica");
        countries.add("JP: Japan");
        //countries.add("JE: Jersey");
        countries.add("JO: Jordan");
        countries.add("KZ: Kazakhstan");
        countries.add("KE: Kenya");
        countries.add("KI: Kiribati");
        countries.add("KP: North Korea");
        countries.add("KR: South Korea");
        countries.add("KW: Kuwait");
        countries.add("KG: Kyrgyzstan");
        countries.add("LA: Lao");
        countries.add("LV: Latvia");
        countries.add("LB: Lebanon");
        countries.add("LS: Lesotho");
        countries.add("LR: Liberia");
        countries.add("LY: Libya");
        countries.add("LI: Liechtenstein");
        countries.add("LT: Lithuania");
        countries.add("LU: Luxembourg");
        countries.add("MO: Macao");
        countries.add("MK: North Macedonia");
        countries.add("MG: Madagascar");
        countries.add("MW: Malawi");
        countries.add("MY: Malaysia");
        countries.add("MV: Maldives");
        countries.add("ML: Mali");
        countries.add("MT: Malta");
        countries.add("MQ: Martinique");
        countries.add("MR: Mauritania");
        countries.add("MU: Mauritius");
        countries.add("YT: Mayotte");
        countries.add("MX: Mexico");
        countries.add("FM: Micronesia");
        countries.add("MD: Moldova");
        countries.add("MC: Monaco");
        countries.add("MN: Mongolia");
        countries.add("ME: Montenegro");
        countries.add("MS: Montserrat");
        countries.add("MA: Morocco");
        countries.add("MZ: Mozambique");
        countries.add("MM: Myanmar");
        countries.add("NA: Namibia");
        countries.add("NR: Nauru");
        countries.add("NP: Nepal");
        countries.add("NL: Netherlands");
        countries.add("NC: New Caledonia");
        countries.add("NZ: New Zealand");
        countries.add("NI: Nicaragua");
        countries.add("NE: Niger");
        countries.add("NG: Nigeria");
        countries.add("NU: Niue");
        countries.add("NO: Norway");
        countries.add("OM: Oman");
        countries.add("PK: Pakistan");
        countries.add("PW: Palau");
        countries.add("PS: Palestine");
        countries.add("PA: Panama");
        countries.add("PG: New P");
        countries.add("PY: Paraguay");
        countries.add("PE: Peru");
        countries.add("PH: Philippines");
        countries.add("PL: Poland");
        countries.add("PT: Portugal");
        countries.add("PR: Puerto");
        countries.add("QA: Qatar");
        countries.add("RE: Réunion");
        countries.add("RO: Romania");
        countries.add("RU: Russia");
        countries.add("RW: Rwanda");
        countries.add("BL: Saint Barthélemy");
        countries.add("KN: Saint Kitts and Nev.");
        countries.add("LC: Saint Lucia");
        countries.add("WS: Samoa");
        countries.add("SM: San Marino");
        countries.add("ST: Sao Tome");
        countries.add("SA: Saudi Arabia");
        countries.add("SN: Senegal");
        countries.add("RS: Serbia");
        countries.add("SC: Seychelles");
        countries.add("SL: Sierra Leone");
        countries.add("SG: Singapore");
        countries.add("SK: Slovakia");
        countries.add("SI: Slovenia");
        countries.add("SO: Somalia");
        countries.add("ZA: South Africa");
        countries.add("SS: South Sudan");
        countries.add("ES: Spain");
        countries.add("LK: Sri Lanka");
        countries.add("SD: Sudan");
        countries.add("SR: Suriname");
        countries.add("SE: Sweden");
        countries.add("CH: Switzerland");
        countries.add("SY: Syria");
        countries.add("TW: Taiwan");
        countries.add("TJ: Tajikistan");
        countries.add("TZ: Tanzania");
        countries.add("TH: Thailand");
        countries.add("TL: Timor");
        countries.add("TG: Togo");
        countries.add("TK: Tokelau");
        countries.add("TO: Tonga");
        countries.add("TT: Trinidad and Tobago");
        countries.add("TN: Tunisia");
        countries.add("TR: Turkey");
        countries.add("TM: Turkmenistan");
        countries.add("TV: Tuvalu");
        countries.add("UG: Uganda");
        countries.add("UA: Ukraine");
        countries.add("AE: United Arab Emirate.");
        countries.add("GB: United Kingdom");
        countries.add("US: United States");
        countries.add("UY: Uruguay");
        countries.add("UZ: Uzbekistan");
        countries.add("VU: Vanuatu");
        countries.add("VE: Venezuela");
        countries.add("VN: Vietnam");
        countries.add("EH: Sahara");
        countries.add("YE: Yemen");
        countries.add("ZM: Zambia");
        return countries;
    }

    private ArrayList<String> getStates() {
        ArrayList<String> states = new ArrayList<>();
        states.add("AK");
        states.add("AL");
        states.add("AR");
        states.add("AZ");
        states.add("CA");
        states.add("CO");
        states.add("CT");
        states.add("DE");
        states.add("FL");
        states.add("GA");
        states.add("HI");
        states.add("IA");
        states.add("ID");
        states.add("IL");
        states.add("IN");
        states.add("KS");
        states.add("KY");
        states.add("LA");
        states.add("MA");
        states.add("MD");
        states.add("ME");
        states.add("MI");
        states.add("MN");
        states.add("MO");
        states.add("MS");
        states.add("MT");
        states.add("NC");
        states.add("ND");
        states.add("NE");
        states.add("NH");
        states.add("NJ");
        states.add("NM");
        states.add("NV");
        states.add("NY");
        states.add("OH");
        states.add("OK");
        states.add("OR");
        states.add("PA");
        states.add("RI");
        states.add("SC");
        states.add("SD");
        states.add("TN");
        states.add("TX");
        states.add("UT");
        states.add("VA");
        states.add("VT");
        states.add("WA");
        states.add("WI");
        states.add("WV");
        return states;
    }


    float x1 = 0, x2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float MIN_DISTANCE = screenW / 4.8f;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (MyDebug.LOG)
                    Log.wtf("**POSITIONS: ", x1 + " " + x2 + "---- " + deltaX + "   . MIN: " + MIN_DISTANCE);
                if (deltaX > MIN_DISTANCE) {
                    //makeToast("Left");
                    handleSwipe(true);
                } else if (deltaX * -1 > MIN_DISTANCE) {
                    //makeToast("Right");
                    handleSwipe(false);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    private void handleSwipe(boolean b) {
        //If b is true --> They want to see left page (previous).
        if (MyDebug.LOG) Log.wtf("**CurrentPage", page + " " + b);
        if (b) {
            //Current Page Direction A
            if (page == Page.PATIENT1) {
                animateCards(patientCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
                page = Page.PAGE1;
            } else if (page == Page.PATIENT2) {
                animateCards(patientCard2, patientCard1, R.anim.slide_out_right, R.anim.slide_in_right);
                page = Page.PATIENT1;
            } else if (page == Page.DOCTOR1) {
                animateCards(doctorCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
                page = Page.PAGE1;
            } else if (page == Page.DOCTOR2) {
                animateCards(doctorCard2, doctorCard1, R.anim.slide_out_right, R.anim.slide_in_right);
                page = Page.DOCTOR1;
            }

        } else {
            //Current Page - Direction B
            if (page == Page.PATIENT1) {
                goToPatient2();
                /*if (checkPatientContinue()) {
                    animateCards(patientCard1, patientCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                    page = Page.PATIENT2;
                    initializePatient2();
                }*/
            } else if (page == Page.PAGE1) {
                if (updated) {
                    if (doctor) {
                        animateCards(firstCard, doctorCard1, R.anim.slide_out_left, R.anim.slide_in_left);
                        page = Page.DOCTOR1;
                        initializeDoctor();
                        doctorRegister();
                    } else {
                        animateCards(firstCard, patientCard1, R.anim.slide_out_left, R.anim.slide_in_left);
                        page = Page.PATIENT1;
                        initializePatient();
                        patientRegister();
                    }
                } else makeToast("Please choose one of the options above.");
            } else if (page == Page.DOCTOR1) {
                goToDoctor2();
                /*if (checkDoctorContinue()) {
                    animateCards(doctorCard1, doctorCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                    page = Page.DOCTOR2;
                    initializeDoctor2();
                }*/
            }

        }
    }

    public void animateCards(CardView card1, final CardView card2, final int animation1,
                             final int animation2) {
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), animation1);
        backCounter = 3;
        card1.startAnimation(slide);
        card2.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation slide2 = AnimationUtils.loadAnimation(getApplicationContext(), animation2);
                card2.startAnimation(slide2);
                card2.setVisibility(View.VISIBLE);
            }
        }, 400);
        card1.setVisibility(View.INVISIBLE);
    }

    public void makeToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    Toast toast;

    public void shortToast(String s) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.show();
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

    private void writeToFirstPatient(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("firstPatient.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            //makeSnackBar(4000, "Could not load info. Try logging out and logging back in.");
            Log.e("Exception", "File write failed: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void writeToFirstDoctor(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("firstDoctor.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();

        } catch (IOException e) {
            //makeSnackBar(4000, "Could not load info. Try logging out and logging back in.");
            Log.e("Exception", "File write failed: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    Snackbar agreeSnack;

    public void showRegister() {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.register_sure);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .85);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text1 = dialog.findViewById(R.id.text2);
        String s1 = text1.getText().toString().trim();
        SpannableString ss1 = new SpannableString(s1);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), s1.indexOf("covid"), s1.indexOf("covid") + 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#1599e6")), s1.indexOf("covid"), s1.indexOf("covid") + 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setText(ss1);

        Button back = (Button) dialog.findViewById(R.id.back);
        final Button yes = (Button) dialog.findViewById(R.id.yes);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        final CheckBox agree = dialog.findViewById(R.id.agree);
        agree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (agreeSnack != null)
                        agreeSnack.dismiss();
                    yes.setBackgroundColor(Color.parseColor("#7BC871"));
                } else {
                    yes.setBackgroundColor(Color.parseColor("#959595"));
                }
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Save their info then go back to welcome page.

                if (isNetworkAvailable()) {
                    if (agree.isChecked()) {
                        saveInformation2();
                        dialog.dismiss();
                        dialog.cancel();
                    } else {
                        //makeSnackBar(3000, "You must agree to the privacy policy above.");
                        agreeSnack = Snackbar.make(dialog.findViewById(R.id.wrapper), "Agree to the privacy policy.", 3000);
                        /* View tr = agreeSnack.getView();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tr.getLayoutParams();
                        params.gravity = Gravity.TOP;
                        tr.setLayoutParams(params);*/
                        View snackbarView = agreeSnack.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#dbc6f7"));
                        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                        tv.setTextColor(Color.parseColor("#000000"));
                        tv.setTextSize(18);
                        agreeSnack.show();
                    }
                    //saveInformation();
                } else {
                    makeSnackBar(10000, "Please connect your device to a network. It is currently not connected and to create your account, an internet connection is required.");
                    dialog.dismiss();
                    dialog.cancel();
                }
            }
        });
        dialog.show();
    }


    public void showAboutApp() {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.about_app_welcome);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .85);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = dialog.findViewById(R.id.text);
        text.setText("The app provides users with a means of checking their COVID test results." +
                "\nUsers receive notifications from the app when their doctor has updated their COVID status.\n\nThis can help people return to jobs with digitial verification of their testing status.");
        if (page == Page.DOCTOR1 || page == Page.DOCTOR2)
            text.setText("As a doctor/medical provider, the app gives you the ability to update the COVID test results of other patients in your area as well as have your own status updated (in the dashboard)." +
                    "\nThis can help people return to jobs with digital verification of their testing status.");


        /* text.setText("The COVID-ID app notifies users of updates in their COVID test results and displays their status - (&quot;Infected&quot;, &quot;Uninfected&quot;, and &quot;Recovered&quot;) - on their dashboard.\n" +
                "        \\nIt provides users with a digital means of monitoring their status regarding the virus while receiving updates from their doctors.\n" +
                "        \\nBy doing so, it can help people return to their jobs and society, showing employers or others that they have recovered or are uninfected (for example).\n" +
                "        ");*/

        TextView privacy = dialog.findViewById(R.id.privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://ij-apps.wixsite.com/android/covid-id-privacy-policy";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

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