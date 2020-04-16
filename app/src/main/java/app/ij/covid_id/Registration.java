package app.ij.covid_id;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

import static java.lang.System.out;

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
    Button patientContinue, patientFinish, doctorContinue, doctorFinish, patientPrevious1, patientPrevious2;
    Button patientBack1, patientBack2, doctorBack1, doctorBack2;
    ImageView photo;
    boolean userGood, passGood, nameGood, phoneGood, emailGood, cityGood, stateGood, countryGood;
    EditText user, pass, name, phone, email, city;
    String sUser, sPass, sName, sPhone, sEmail, sCity, sState, sCountry;

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
        firstCard.setBackgroundResource(R.drawable.card_white);
        patientCard1.setBackgroundResource(R.drawable.card_white);
        page = Page.PAGE1;
        updated = false;
        doctor = false;
        pictureGood = false;
        patientCard1.setVisibility(View.INVISIBLE);
        countryGood = true;

        user = findViewById(R.id.user);
        pass = findViewById(R.id.pass);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        city = findViewById(R.id.city);

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        state = findViewById(R.id.state);
        country = findViewById(R.id.country);
        phoneHelp = findViewById(R.id.phoneHelp);
        locationHelp = findViewById(R.id.locationHelp);
        takePicture = findViewById(R.id.cardholder);
        deletePicture = findViewById(R.id.removeholder);
        photo = findViewById(R.id.photoholder);
        photoHelp = findViewById(R.id.photohelp);
        patientContinue = patientCard1.findViewById(R.id.patientContinue);
        patientPrevious1 = findViewById(R.id.patientPrevious1);
        patientBack1 = findViewById(R.id.patientPrevious1);

        screenW = metrics.widthPixels;
        screenH = metrics.heightPixels;
        test();
        //test2();
        //patientCard1.setVisibility(View.INVISIBLE);
        spinners();
        helpers();
        allGood();
        previousClickers();
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
                    //TODO Based on doctor or not, dispaly different things.
                    if (doctor) {
                        page = Page.DOCTOR1;

                    } else {
                        patientRegister();
                    }
                }
            }
        });
    }

    private void patientRegister() {
        page = Page.PATIENT1;
        animateCards(firstCard, patientCard1, R.anim.slide_out_left, R.anim.slide_in_left);
        patientContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sUser = user.getText().toString();
                sPass = pass.getText().toString();
                sName = name.getText().toString();
                sPhone = phone.getText().toString();
                sEmail = email.getText().toString();
                sCity = city.getText().toString();
                sState = state.getSelectedItem().toString();
                sCountry = state.getSelectedItem().toString();
                if(sUser.length() < 6)
                    makeSnackBar(2000,"Please make your username longer.");
                else if(!uniqueUsername())
                    makeSnackBar(2600,"This username already exists. Please choose another.");
                else if(sPass.length() < 6)
                    makeSnackBar(2000,"Please make your password longer.");
                else if(sName.length() < 4 || sName.indexOf(" ") < 2 || sName.indexOf(" ") == sName.length() - 1)
                    makeSnackBar(2000,"Please enter your full name.");
                else if(sPhone.length() < 10)
                    makeSnackBar(2000,"Please enter a valid phone #.");
                else if(!isValid(sEmail))
                    makeSnackBar(2000,"Please enter a valid email.");
                else if(sCity.length() <=2)
                    makeSnackBar(2000,"Please enter a valid city.");
                else if(!pictureGood)
                    makeSnackBar(2000,"Please take a picture.");
                else{
                    animateCards(patientCard1, patientCard2, R.anim.slide_out_left, R.anim.slide_in_left);
                }
            }
        });
    }

    private boolean uniqueUsername() {
        //TODO Write code to determine if username is unique.
        return true;
    }

    public static boolean isValid(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }


    private void textWatcher() {
        //TODO If you want to then make this where it makes the button purple if they can continue.
        TextWatcher user1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String s = user.getText().toString();
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
        //TODO Deal with previous button click.
        View.OnClickListener back = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == patientPrevious1.getId())
                    animateCards(patientCard1, firstCard, R.anim.slide_out_right, R.anim.slide_in_right);
            }
        };
        patientPrevious1.setOnClickListener(back);
    }

    Uri extra;

    private void test() {
        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Registration.this, new String[]{Manifest.permission.CAMERA}, 1034);

                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, extra);
                        startActivityForResult(cameraIntent, 100);
                    }
                } catch (ActivityNotFoundException anfe) {
                    //display an error message
                    String errorMessage = "Whoops - your device doesn't support capturing images!";
                    makeToast(errorMessage);

                }
            }
        });
        deletePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photo.setImageResource(R.drawable.usericon);
                pictureGood = false;
                allGood();
                patientContinue.setBackgroundResource(R.drawable.greybutton);
            }
        });
    }

    int count = 0;

    private void doUpload(Bitmap bitmap, StorageReference storageReference2) {
        String s = "Patients/" + ++count + ".jpg";
        storageReference2 = FirebaseStorage.getInstance().getReference(s);

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

    Bitmap bitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                extra = data.getData();
                extra = data.getData();

                bitmap = (Bitmap) data.getExtras().get("data");
                pictureGood = true;
                allGood();

                if (bitmap.getWidth() > bitmap.getHeight()) {

                    bitmap = Bitmap.createBitmap(
                            bitmap,
                            bitmap.getWidth() / 2 - bitmap.getHeight() / 2,
                            0,
                            bitmap.getHeight(),
                            bitmap.getHeight()
                    );

                } else if (bitmap.getHeight() > bitmap.getWidth()) {

                    bitmap = Bitmap.createBitmap(
                            bitmap,
                            0,
                            bitmap.getHeight() / 2 - bitmap.getWidth() / 2,
                            bitmap.getWidth(),
                            bitmap.getWidth()
                    );

                }
                saveImage(getApplicationContext(), bitmap, "temp", "jpg");
                photo.setImageBitmap(bitmap);

            }
        }

    }

    private void helpers() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.phoneHelp)
                    makeSnackBar(3500, "Your phone # is required for doctors to contact you.");
                if (view.getId() == R.id.locationHelp)
                    makeSnackBar(6000, "Your location is visible only by medical professionals to monitor COVID statuses in a particular area.");
                if (view.getId() == R.id.photohelp)
                    makeSnackBar(9000, "Please take a close picture of only your face. Your photo is only visible to doctors and can be used as verification (if allowed).");
            }
        };
        photoHelp.setOnClickListener(listener);
        phoneHelp.setOnClickListener(listener);
        locationHelp.setOnClickListener(listener);
    }

    private void makeSnackBar(int duration, String s) {
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.registration), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv= (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(3);
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
                String result = text.getText().toString();
                makeToast("RESULT: " + result);
                if (result.contains("United States")) {
                    state.setEnabled(true);
                    ((TextView) state.getChildAt(0)).setTextColor(0xFFA456DC);
                } else {
                    state.setEnabled(false);
                    ((TextView) state.getChildAt(0)).setTextColor(0xFF454545);
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
        //TODO Uncomment if you decide to use this where it changes button color based on whether they can move ahead.
        //if(userGood && passGood && nameGood && phoneGood && emailGood && cityGood && countryGood && pictureGood)
        patientContinue.setBackgroundResource(R.drawable.green_button);
        /*else
            patientContinue.setBackgroundResource(R.drawable.greybutton);*/
    }

    private ArrayList<String> getCountries() {
        ArrayList<String> countries = new ArrayList<>();
        countries.add("US: United States");
        countries.add("AF: Afghanistan");
        countries.add("AL: Albania");
        countries.add("DZ: Algeria");
        countries.add("AS: American");
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
                animateCards(patientCard1, firstCard, R.anim.slide_out_left, R.anim.slide_in_left);
                page = Page.PAGE1;
            } else if (page == Page.PAGE1) {
                if (updated) {
                    if (doctor) {
                        animateCards(firstCard, doctorCard1, R.anim.slide_out_left, R.anim.slide_in_left);
                        page = Page.DOCTOR1;
                    } else {
                        animateCards(firstCard, patientCard1, R.anim.slide_out_left, R.anim.slide_in_left);
                        page = Page.PATIENT1;
                    }
                } else makeToast("Please choose one of the options above.");
            } else if (page == Page.DOCTOR1) {
                animateCards(doctorCard1, firstCard, R.anim.slide_out_left, R.anim.slide_in_left);
                page = Page.PAGE1;
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