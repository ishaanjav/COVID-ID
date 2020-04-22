package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    public enum Page {PAGE1, PATIENT1, DOCTOR1, PATIENT2, DOCTOR2}

    ImageView phoneHelp, locationHelp, photoHelp;
    CardView takePicture, deletePicture;
    Button patientContinue, patientFinish, doctorContinue, doctorFinish;
    Button patientBack1, patientBack2, doctorBack1, doctorBack2;
    ImageView photo;
    boolean userGood, passGood, nameGood, phoneGood, emailGood, cityGood, stateGood, countryGood;
    EditText user, pass, name, phone, email, city;
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

        countryGood = true;
        backCounter = 0;

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
        email = patientCard1.findViewById(R.id.email);
        city = patientCard1.findViewById(R.id.city);
        state = patientCard1.findViewById(R.id.state);
        country = patientCard1.findViewById(R.id.country);
        phoneHelp = patientCard1.findViewById(R.id.phoneHelp);
        locationHelp = patientCard1.findViewById(R.id.locationHelp);
        takePicture = patientCard1.findViewById(R.id.cardholder);
        take = patientCard1.findViewById(R.id.takepicture);
        delete = patientCard1.findViewById(R.id.removepicture);
        deletePicture = patientCard1.findViewById(R.id.removeholder);
        photo = patientCard1.findViewById(R.id.photoholder);
        photoHelp = patientCard1.findViewById(R.id.photohelp);
        patientContinue = patientCard1.findViewById(R.id.patientContinue);
        patientBack1 = patientCard1.findViewById(R.id.patientPrevious1);
        confirmPassToggle = patientCard1.findViewById(R.id.passtoggle2);
        confirmPass = patientCard1.findViewById(R.id.confirmpass);
        passToggle = patientCard1.findViewById(R.id.passtoggle);
        spinners();
        helpers();
        allGood();
        previousClickers();
        test();
        toggle();
    }

    private void initializeDoctor() {
        user = doctorCard1.findViewById(R.id.user);
        pass = doctorCard1.findViewById(R.id.pass);
        name = doctorCard1.findViewById(R.id.name);
        phone = doctorCard1.findViewById(R.id.phone);
        email = doctorCard1.findViewById(R.id.email);
        city = doctorCard1.findViewById(R.id.city);
        state = doctorCard1.findViewById(R.id.state);
        confirmPass = doctorCard1.findViewById(R.id.confirmpass);
        country = doctorCard1.findViewById(R.id.country);
        phoneHelp = doctorCard1.findViewById(R.id.phoneHelp);
        locationHelp = doctorCard1.findViewById(R.id.locationHelp);
        takePicture = doctorCard1.findViewById(R.id.cardholder);
        take = doctorCard1.findViewById(R.id.takepicture);
        delete = doctorCard1.findViewById(R.id.removepicture);
        deletePicture = doctorCard1.findViewById(R.id.removeholder);
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
    }

    @Override
    public void onBackPressed() {
        if (backCounter < 3)
            makeSnackBar(5500, "Going back will delete your progress. Instead, use the buttons or swipe to navigate between pages.\nIf you want to return to the Welcome Page, continue the same motion " + (3 - backCounter) + " times.");
        else super.onBackPressed();
        backCounter++;
    }

    FirebaseFirestore db;
    ImageView confirmPassToggle, passToggle;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void saveInformation() {
        final String reference = (doctor) ? "Doctors" : "Patients";
        final Map<String, Object> map = new HashMap<>();
        map.put("Username", user.getText().toString().trim());
        map.put("Original Username", user.getText().toString().trim());
        map.put("Password", pass.getText().toString().trim());
        map.put("Name", name.getText().toString().trim());
        map.put("Phone", phone.getText().toString().trim());
        map.put("Email", email.getText().toString().trim());
        map.put("City", city.getText().toString().trim());
        map.put("State", (country.getSelectedItem().toString().contains("United States")) ? state.getSelectedItem().toString() : "");
        map.put("Country", (country.getSelectedItem().toString()));

        final String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        final String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
        final String status;
        if (u) status = "Unknown";
        else if (covidR) status = "Recovered";
        else if (covidN) status = "Negative";
        else status = "Positive";
        map.put("Covid Status", status);

        map.put("Num of Dates", 1);
        map.put("Status 1", status);
        map.put("Date 1", currentDate + " " + time);
        if (doctor) {
            map.put("Account Verified", false);
        } else {
            map.put("Donated Plasma", (haveDonatedPlasma));
            map.put("Will Donate Plasma", (willDonatePlasma));
        }

        final ProgressDialog dialog = ProgressDialog.show(Registration.this, "Creating Account",
                "Loading. Please wait...", true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage("Loading. Please wait..." + "\nMake sure you have a good internet connection.");
            }
        }, 5000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final int[] count = {5};
                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        String s = count[0] + "";
                        count[0]--;
                        s += (millisUntilFinished / 1000 == 0) ? " second." : " seconds.";
                        if (dialog.isShowing())
                            dialog.setMessage("Are you connected to the internet?\n\nCheck your connection" +
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
        }, 22500);
        final Map<String, Object> userPass = new HashMap<>();
        userPass.put("Username", user.getText().toString().trim());
        userPass.put("Password", pass.getText().toString().trim());
        userPass.put("Account Verified", !doctor);
        userPass.put("Type", reference.substring(0, reference.length() - 1));
        userPass.put("Account Created", currentDate + " " + time);

        userPass.put("Name", name.getText().toString().trim());
        //userPass.put("City", city.getText().toString().trim());
        //userPass.put("State", (country.getSelectedItem().toString().contains("United States")) ? state.getSelectedItem().toString() : "");
        //userPass.put("Country", (country.getSelectedItem().toString()));
        userPass.put("Phone", phone.getText().toString().trim());
        userPass.put("Email", email.getText().toString().trim());
        db.collection("userPass")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean unique = true;
                            String curUser = user.getText().toString().trim();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String r = document.get("Username").toString();
                                Log.wtf("DOCUMENT READ: ", curUser + " =>  " + document.get("Username").toString());
                                if (r.equals(curUser)) {
                                    makeSnackBar(3700, "Your username was just taken. Please choose another username.");
                                    unique = false;
                                    break;
                                }
                            }
                            if (unique) {
                                //INFO Username is unique.
                                db.collection("userPass")
                                        .add(userPass)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                String s = reference + "/" + user.getText().toString().trim().trim() + ".jpg";
                                                StorageReference storageReference2 = FirebaseStorage.getInstance().getReference(s);

                                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                                final byte[] data2 = baos.toByteArray();

                                                UploadTask uploadTask = storageReference2.putBytes(data2);
                                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        db.collection(reference)
                                                                .add(map)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        //INFO Write to "userPass/<documentId>/Dates" and put in stuff.
                                                                        HashMap<String, Object> dates = new HashMap<>();
                                                                        dates.put("Status", status);
                                                                        dates.put("Previous Status", "n/a");
                                                                        dates.put("Date", currentDate + " " + time);
                                                                        dates.put("Doctor", (doctor) ? "You" : "n/a");
                                                                        dates.put("Medical Center Phone Number", (doctor) ? phone.getText().toString() : "n/a");
                                                                        dates.put("Notes", "n/a");
                                                                        final String documentID = documentReference.getId();
                                                                        db.collection(reference + "/" + documentID + "/Updates")
                                                                                .document("Update 1").set(dates)
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        makeToast("Account created");
                                                                                        dialog.cancel();
                                                                                        writeToFile(doctor ? "Doctor" : "Patient", getApplicationContext());
                                                                                        //IMPORTANT Account has successfully been created.
                                                                                        Intent finish = new Intent(Registration.this, MainActivity.class);
                                                                                        finish.putExtra("Type", doctor ? "Doctor" : "Patient");
                                                                                        startActivity(finish);
                                                                                        Log.wtf("TESTING", "DocumentSnapshot added with ID: " + documentID);
                                                                                    }
                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                dialog.cancel();
                                                                                makeToast(e.getMessage());
                                                                                Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                                                makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                                            }
                                                                        });
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        dialog.cancel();
                                                                        Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                                        makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        dialog.cancel();
                                                        Log.wtf("FAILED FAILED FAILED_____", "Error adding document" + exception.toString());
                                                        makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                                    }
                                                });


                                                Log.wtf("TESTING", "DocumentSnapshot added with ID: " + documentReference.getId());
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialog.cancel();
                                                Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                                                makeSnackBar(5000, "Failed to save information. Make sure you have a stable internet connection and try again.");
                                            }
                                        });
                            } else {
                                dialog.cancel();
                            }
                        } else {
                            dialog.cancel();
                            Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
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

       /* db.collection("Patients").document("Patient1")
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.wtf("TESTING", "DocumentSnapshot added with ID: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                    }
                });

        db.collection("Patients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.wtf("READING: READING:  ", document.getId() + " => " + document.getData());
                                makeToast("Document ID: " + document.getId() + " =>  " + document.getData());
                            }
                        } else {
                            Log.w("FAILED TO READ ---", "Error getting documents.", task.getException());
                        }
                    }
                });*/
// Add a new document with a generated ID
        /*db.collection("Patients")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.wtf("TESTING", "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.wtf("FAILED FAILED FAILED_____", "Error adding document", e);
                    }
                });

        db.collection("Patients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.wtf("READING: READING:  ", document.getId() + " => " + document.getData());
                                makeToast("Document ID: " + document.getId() + " =>  " + document.getData());
                            }
                        } else {
                            Log.w("FAILED TO READ ---", "Error getting documents.", task.getException());
                        }
                    }
                });*/

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
        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (confirm.length() < 6)
            makeSnackBar(2000, "Confirm your password by retyping it.");
        else if (!confirm.equals(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (sPhone.length() < 10)
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail))
            makeSnackBar(2000, "Please enter a valid email.");
        else if (sCity.length() <= 2)
            makeSnackBar(2000, "Please enter a valid city.");
        else if (!pictureGood)
            makeSnackBar(2000, "Please take a picture of your face.");
        else {
            final ProgressDialog dialog = ProgressDialog.show(Registration.this, null,
                    "Checking username uniqueness...", true);
            dialog.setCancelable(true);
            db.collection("userPass")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean unique = true;
                                String curUser = user.getText().toString().trim();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String r = document.get("Username").toString();
                                    Log.wtf("DOCUMENT READ: ", curUser + " =>  " + document.get("Username").toString());
                                    if (r.equals(curUser)) {
                                        makeSnackBar(2400, "This username is taken. Please choose another.");
                                        dialog.cancel();
                                        unique = false;
                                        break;
                                    } else {

                                    }
                                }
                                if (unique) {
                                    //INFO Username is unique.
                                    animateCards(doctorCard1, doctorCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                                    page = Page.DOCTOR2;
                                    initializeDoctor2();
                                    dialog.cancel();
                                }
                            } else {
                                dialog.cancel();
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
        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (confirm.length() < 6)
            makeSnackBar(2000, "Confirm your password by retyping it.");
        else if (!confirm.equals(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (sPhone.length() < 10)
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail))
            makeSnackBar(2000, "Please enter a valid email.");
        else if (sCity.length() <= 2)
            makeSnackBar(2000, "Please enter a valid city.");
        else if (!pictureGood)
            makeSnackBar(2000, "Please take a picture of your face.");
        else {
            final ProgressDialog dialog = ProgressDialog.show(Registration.this, null,
                    "Checking username uniqueness...", true);
            dialog.setCancelable(true);
            db.collection("userPass")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean unique = true;
                                String curUser = user.getText().toString().trim();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String r = document.get("Username").toString();
                                    Log.wtf("DOCUMENT READ: ", curUser + " =>  " + document.get("Username").toString());
                                    if (r.equals(curUser)) {
                                        makeSnackBar(2400, "This username is taken. Please choose another.");
                                        unique = false;
                                        dialog.cancel();
                                        break;
                                    } else {

                                    }
                                }
                                if (unique) {
                                    //INFO Username is unique.
                                    animateCards(patientCard1, patientCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                                    page = Page.PATIENT2;
                                    initializePatient2();
                                    dialog.cancel();
                                }
                            } else {
                                dialog.cancel();
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

    public boolean checkDoctorContinue() {
        //return true;
        sUser = user.getText().toString().trim();
        sPass = pass.getText().toString().trim();
        sName = name.getText().toString().trim();
        String confirm = confirmPass.getText().toString();
        sPhone = phone.getText().toString().trim();
        sEmail = email.getText().toString().trim();
        sCity = city.getText().toString().trim();
        sState = state.getSelectedItem().toString();
        sCountry = state.getSelectedItem().toString();
        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (confirm.length() < 6)
            makeSnackBar(2000, "Confirm your password by retyping it.");
        else if (!confirm.equals(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (sPhone.length() < 10)
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail))
            makeSnackBar(2000, "Please enter a valid email.");
        else if (sCity.length() <= 2)
            makeSnackBar(2000, "Please enter a valid city.");
        else if (!pictureGood)
            makeSnackBar(2000, "Please take a picture of your face.");
        else {
            db.collection("cities")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                boolean unique = true;
                                String curUser = user.getText().toString().trim();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String r = (String) document.get("Username");
                                    if (r.equals(curUser)) {
                                        makeSnackBar(2400, "This username is taken. Please choose another.");
                                        unique = false;
                                        break;
                                    } else {
                                    }
                                }
                                if (unique) {

                                }
                            } else {
                                Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
                            }
                        }
                    });
            return true;
        }
        return false;
    }

    public boolean checkPatientContinue() {
        //return true;
        sUser = user.getText().toString().trim();
        sPass = pass.getText().toString().trim();
        String confirm = confirmPass.getText().toString();
        sName = name.getText().toString().trim();
        sPhone = phone.getText().toString().trim();
        sEmail = email.getText().toString().trim();
        sCity = city.getText().toString().trim();
        sState = state.getSelectedItem().toString();
        sCountry = state.getSelectedItem().toString();
        if (sUser.length() < 6)
            makeSnackBar(2000, "Please make your username longer.");
        else if (!uniqueUsername())
            makeSnackBar(2600, "This username already exists. Please choose another.");
        else if (sPass.length() < 6)
            makeSnackBar(2000, "Please make your password longer.");
        else if (confirm.length() < 6)
            makeSnackBar(2000, "Confirm your password by retyping it.");
        else if (!confirm.equals(sPass))
            makeSnackBar(2000, "Your passwords do not match.");
        else if (sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
            makeSnackBar(2000, "Please enter your full name.");
        else if (sPhone.length() < 10)
            makeSnackBar(2000, "Please enter a valid phone #.");
        else if (sEmail.length() > 0 && !isValid(sEmail))
            makeSnackBar(2000, "Please enter a valid email.");
        else if (sCity.length() <= 2)
            makeSnackBar(2000, "Please enter a valid city.");
        else if (!pictureGood)
            makeSnackBar(2000, "Please take a picture.");
        else {
            return true;
        }
        return false;
    }

    ImageView statusHelp;

    private void initializeDoctor2() {
        statusHelp = doctorCard2.findViewById(R.id.statusHelp);
        doctorBack2 = doctorCard2.findViewById(R.id.doctorPrevious2);
        doctorFinish = doctorCard2.findViewById(R.id.doctorFinish);
        unknown = doctorCard2.findViewById(R.id.unknown);
        covidPositive = doctorCard2.findViewById(R.id.covidpositive);
        covidRecovered = doctorCard2.findViewById(R.id.covidnegative);
        covidNegative = doctorCard2.findViewById(R.id.covidn);
        doctor2Helpers();
        doctorDone();
    }

    private void initializePatient2() {
        statusHelp = patientCard2.findViewById(R.id.statusHelp);
        patientPrevious2 = patientCard2.findViewById(R.id.patientPrevious2);
        patientFinish = patientCard2.findViewById(R.id.patientFinish);
        unknown = patientCard2.findViewById(R.id.unknown);
        covidPositive = patientCard2.findViewById(R.id.covidpositive);
        covidRecovered = patientCard2.findViewById(R.id.covidnegative);
        covidNegative = patientCard2.findViewById(R.id.covidn);
        plasmaYes = patientCard2.findViewById(R.id.plasmayes);
        plasmaNo = patientCard2.findViewById(R.id.plasmano);
        willingYes = patientCard2.findViewById(R.id.willingyes);
        willingNo = patientCard2.findViewById(R.id.willingno);
        covidStatus = patientCard2.findViewById(R.id.covidStatus);
        haveDonatedGroup = patientCard2.findViewById(R.id.donationgroupStatus);
        willDonateGroup = patientCard2.findViewById(R.id.willdonategroupstatus);
        patient2Helpers();
        patientDone();
    }

    boolean u, covidP, covidR, covidN, haveDonatedPlasma, willDonatePlasma;

    private void doctorDone() {
        doctorFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Check if they have selected one of the 3 options from RadioGroup
                // Then ask if sure
                u = unknown.isChecked();
                covidP = covidPositive.isChecked();
                covidR = covidRecovered.isChecked();
                covidN = covidNegative.isChecked();

                if (u || covidP || covidR || covidN)
                    showRegister();
                else
                    makeSnackBar(1900, "Select an option above");
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

                u = unknown.isChecked();
                covidP = covidPositive.isChecked();
                covidR = covidRecovered.isChecked();
                covidN = covidNegative.isChecked();

                if ((unknown.isChecked() || covidPositive.isChecked() || covidRecovered.isChecked() || covidN) &&
                        (plasmaYes.isChecked() || plasmaNo.isChecked()) && (willingYes.isChecked() || willingNo.isChecked()))
                    showRegister();
                else
                    makeSnackBar(1900, "Select the options above.");
            }
        });
    }

    private void doctor2Helpers() {
        statusHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAboutStatus();
            }
        });
    }

    private void patient2Helpers() {
        statusHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAboutStatus();
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
                                //Log.wtf("SUCCESSS", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.wtf("SUCCESS", "Error getting documents: ", task.getException());
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


    private void textWatcher() {
        //FUTURE If you want to then make this where it makes the button green from grey if they can continue.
        TextWatcher user1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = user.getText().toString().trim();
                //if(s.length()> 4)

            }
        };
        user.addTextChangedListener(user1);
    }

    private void test2() {
        ArrayList<String> name = new ArrayList<>();
        name.add("1");
        name.add("2");
        name.add("3");
        name.add("4");
        name.add("5");
        name.add("6");
        name.add("7");
        name.add("8");
        name.add("9");
        name.add("10");
        name.add("11");
        name.add("12");
        name.add("13");
        name.add("14");
        name.add("15");
        name.add("16");
        name.add("17");
        name.add("18");
        for (int i = 1; i <= 18; i++) {
            final StorageReference mImageRef = FirebaseStorage.getInstance().getReference("Patients/" + i + ".jpg");
            final long ONE_MEGABYTE = 1024 * 1024;

            final int finalI = i;
            mImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    saveImage(getApplicationContext(), bm, "" + finalI, "jpg");
                    Log.wtf("**SAVED IMAGE", "IMAGE " + finalI + " SAVED");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "FAILED", Toast.LENGTH_LONG).show();
                }
            });
        }
        TestAdapter adapter = new TestAdapter(getApplicationContext(), R.layout.test_list_view_layout, name);
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    public void saveImage(Context context, Bitmap bitmap, String name, String extension) {
        name = name + "." + extension;
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        startActivityForResult(intent, 100);
                       /* Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, extra);
                        startActivityForResult(cameraIntent, 100);*/
                    }
                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    makeToast(errorMessage);

                }
            }
        };
        takePicture.setOnClickListener(clicker);
        take.setOnClickListener(clicker);
        View.OnClickListener delete2 = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (doctor)
                    photo.setImageResource(R.drawable.dbadge);
                else
                    photo.setImageResource(R.drawable.usericon);
                pictureGood = false;
                allGood();
                patientContinue.setBackgroundResource(R.drawable.greybutton);
            }
        };

        delete.setOnClickListener(delete2);
        deletePicture.setOnClickListener(delete2);
    }

    boolean swapped = false;

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }*/

    int count = 21;

    private void doUpload(Bitmap bitmap, String id, ProgressDialog dialog) {
        String s = "Patients/" + id + ".jpg";
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
                if (!pictureGood && !Idismissed[0])
                    makeToast("A picture is required. Click the help icon for more info.");
            }
        });
        dialog.show();
    }

    Bitmap bitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == 10) {
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

                photo.setImageBitmap(bitmap);
            }
            if (requestCode == 100) {
                //extra = data.getData();
                //extra = data.getData();
                //INFO Getting high quality image
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                } catch (IOException e) {
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

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, out);
                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                photo.setImageBitmap(bitmap);
                makeToast("Double tap or long press the image to rotate it.");


            }
        }

    }

    int denyCounter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
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
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
                        makeSnackBar(3000, "The location is used to authenticate your account.");
                    if (view.getId() == R.id.photohelp)
                        makeSnackBar(4000, "Please take a picture of your ID as verification of your profession.");
                } else {
                    if (view.getId() == R.id.phoneHelp)
                        makeSnackBar(3500, "Your phone # is required for doctors to contact you.");
                    if (view.getId() == R.id.locationHelp)
                        makeSnackBar(6000, "Your location is visible only by medical professionals to monitor COVID statuses in a particular area.");
                    if (view.getId() == R.id.photohelp)
                        makeSnackBar(9000, "Please take a close picture of only your face. Your photo is only visible to doctors and can be used as verification (if allowed).");
                }
            }
        };
        photoHelp.setOnClickListener(listener);
        phoneHelp.setOnClickListener(listener);
        locationHelp.setOnClickListener(listener);

        photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (bitmap != null) {
                    bitmap = RotateBitmap(bitmap, 270f);
                    photo.setImageBitmap(bitmap);
                    Vibrator vibrator = (Vibrator) Registration.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(300);
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
                        vibrator.vibrate(300);
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
        tv.setMaxLines(4);
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
        countries.add("ID: Indonesia Indonesia.");
        countries.add("IR: Iran");
        countries.add("IQ: Iraq");
        countries.add("IE: Ireland");
        countries.add("IM: Isle of Man.");
        countries.add("IL: Israel");
        countries.add("IT: Italy");
        countries.add("JM: Jamaica");
        countries.add("JP: Japan");
        countries.add("JE: Jersey");
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
        Log.wtf("**CurrentPage", page + " " + b);
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

    public void animateCards(CardView card1, final CardView card2, final int animation1, final int animation2) {
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), animation1);

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

    public void shortToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
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

    public void showAboutStatus() {
        final Dialog dialog = new Dialog(Registration.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.covid_status_help);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text1 = dialog.findViewById(R.id.text2), text2 = dialog.findViewById(R.id.text3), text3 = dialog.findViewById(R.id.text4);
        String s1 = text1.getText().toString().trim();
        String s2 = text2.getText().toString().trim();
        String s3 = text3.getText().toString().trim();
        SpannableString ss1 = new SpannableString(s1);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#505050")), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        text1.setText(ss1);
        SpannableString ss2 = new SpannableString(s2);
        ss2.setSpan(new StyleSpan(Typeface.BOLD), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss2.setSpan(new ForegroundColorSpan(Color.parseColor("#D63636")), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        text2.setText(ss2);
        SpannableString ss3 = new SpannableString(s3);
        ss3.setSpan(new StyleSpan(Typeface.BOLD), 0, 24, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss3.setSpan(new ForegroundColorSpan(Color.parseColor("#31B115")), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        text3.setText(ss3);
        TextView text4 = dialog.findViewById(R.id.text5);
        SpannableString ss4 = new SpannableString(text4.getText().toString().trim());
        ss4.setSpan(new StyleSpan(Typeface.BOLD), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss4.setSpan(new ForegroundColorSpan(Color.parseColor("#2E88F6")), 0, 22, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        text4.setText(ss4);

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
        Button yes = (Button) dialog.findViewById(R.id.yes);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO Save their info then go back to welcome page.
                dialog.dismiss();
                dialog.cancel();
                if (isNetworkAvailable())
                    saveInformation();
                else
                    makeSnackBar(10000, "Please connect your device to a network. It is currently not connected and to create your account, an internet connection is required.");
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