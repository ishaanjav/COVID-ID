package app.ij.covid_id;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;

public class InfoRecyclerViewAdapter extends RecyclerView.Adapter<InfoRecyclerViewAdapter.ViewHolder> {

    static Context context;
    public static ArrayList<HashMap<String, Object>> list;
    HashMap<String, Bitmap> bitmapList;
    int mExpandedPosition;
    View details;
    static RecyclerView recyclerView;
    public String TAG = "RecyclerViewAdapter";
    ArrayList<Boolean> bools;
    String doctorID;
    FirebaseFirestore db;
    static HashMap<Integer, Pair<String, Pair<Integer, Boolean>>> notesSaved;
    public HashMap<String, Object> doctorInfo;

    public InfoRecyclerViewAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, HashMap<String, Bitmap> bitmaps, String doctorID, FirebaseFirestore db, HashMap<String, Object> info) {
        this.context = context;
        this.list = list;
        this.recyclerView = recyclerView;
        bitmapList = bitmaps;
        this.doctorID = doctorID;
        this.db = db;
        this.doctorInfo = info;
    }

    View view;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        RelativeLayout screen, bar1;
        TextView name, status, location;
        TextView details;
        RelativeLayout statusBox;

        public ViewHolder(final View itemView, final int pos) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            status = itemView.findViewById(R.id.status);
            bar1 = itemView.findViewById(R.id.bar1);
            location = itemView.findViewById(R.id.location);
            screen = itemView.findViewById(R.id.screen);
            mExpandedPosition = -1;
            details = itemView.findViewById(R.id.details);
            statusBox = itemView.findViewById(R.id.statusBox);
            bools = new ArrayList<>();
            notesSaved = new HashMap<>();
            // if(MyDebug.LOG) Log.wtf("*CALLED", "VIEWHOLDER");
            for (int i = 0; i < list.size() / 2; i++) {
                bools.add(false);
                bools.add(false);
            }
            bools.add(false);
            //UNCOMMENT BELOW
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkAvailable()) {
                        /*if (type.equals("Patient"))*/
                        showInfo(pos);
                        //else showDoctorInfo(pos);
                    } else {
                        makeSnackBar(3000, "You need a Wifi connection to update a user's status.");
                    }
                }
            });
            //TODO INFO the position is getting messed up. I think it happens when I call
            //  notifyItemChanged(position); Try to fix this for next update.
            /*image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleText(itemView, pos);
                }
            });*/
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void toggleText(View v, int pos) {
        boolean bool = bools.get(pos);
        bools.set(pos, !bool);
        //v.findViewById(R.id.details).setVisibility(View.VISIBLE);
        // if(MyDebug.LOG) Log.wtf("*toggleText", "pos: " + pos + " - Previous: " + bool + " Now: " + bools.get(pos));
  /*items[layoutPosition] = items[layoutPosition].let {
            it.first to !it.second
        }*/
        //   notifyItemChanged(pos);
    }

    int counter = 0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        // if(MyDebug.LOG) Log.wtf("*onCreateViewHolder", "Size: " + list.size() + "  Counter: " + counter);
        ViewHolder holder = new ViewHolder(view, counter);
        details = LayoutInflater.from(context).inflate(R.layout.extended_list_item, parent, false);
        counter++;

        return holder;
    }

    int previousExpandedPosition;
    String documentPath, username;

    public static int dx = 0, dy = 0;
    String status, country, name, plasmaDonated, willDonate, type;
    HashMap<String, Object> map;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // if(MyDebug.LOG) Log.wtf(TAG, "onBindViewHolder --  called");
        map = list.get(position);
        // if(MyDebug.LOG) Log.wtf(TAG, "onBindViewHolder List - " + list.toString() + " \n\t\t\\t\t\t\t\\t\tt\t\t\t\t\t" + map.toString());

        documentPath = map.get("Doc ID").toString();
        username = map.get("User").toString();
        status = map.get("Status").toString();
        name = map.get("Name").toString();
        type = map.get("Type").toString();

        if (type.equals("Doctor"))
            name = "Dr. " + name;
        if (position == list.size() - 1) holder.details.setVisibility(View.GONE);

        holder.name.setText(map.get("Name").toString());
        boolean foreign = map.get("State").toString().length() < 2;
        country = map.get("Country").toString();
        if (foreign)
            holder.location.setText(map.get("City").toString() + ", " + country.substring(country.indexOf(":") + 1));
        else
            holder.location.setText(map.get("City").toString() + ", " + map.get("State").toString());
        holder.status.setText(status);


        if (status.equals("Unknown")) {
            holder.statusBox.setBackgroundColor(Color.parseColor("#d4d4d4"));
        } else if (status.equals("Recovered")) {
            holder.statusBox.setBackgroundColor(Color.parseColor("#D7FFD8"));
        } else if (status.equals("Infected")) {
            holder.statusBox.setBackgroundColor(Color.parseColor("#FFCECE"));
        } else if (status.equals("Uninfected")) {
            holder.statusBox.setBackgroundColor(Color.parseColor("#ffe5ba"));
        }

//TODO See if there is a way to load all the text stuff first and then load images once it is retrieved.
        //INFO Right now we have to load all the stuff after images are retrieved and it takes a while for
        // 3 images so search alternatives.
        // 3 images = 1457
        if (bitmapList.containsKey(username)) {
            holder.image.setImageBitmap(bitmapList.get(username));
            // if(MyDebug.LOG) Log.wtf("*-& Using Bitmap List", username);
        } else {
            holder.image.setImageBitmap(loadImageBitmap(context, map.get("Orig").toString(), "jpg"));
            // if(MyDebug.LOG) Log.wtf("*-& Getting from Storage", username);
        }
        if (DoctorStatuses3.loadingResults != null && position == (Math.min(list.size() - 1, 12))
                && DoctorStatuses3.loadingResults.isShowing()) {
            // if(MyDebug.LOG) Log.wtf("*CANCELLED", "From inside Adapter");
            DoctorStatuses3.loadingResults.cancel();
        }

         /*   if (bools.get(position)) {
            makeToast("ACTIVATED!");
            holder.details.setVisibility(View.VISIBLE);
             if(MyDebug.LOG) Log.wtf("onBindViewHolder", "pos: " + position + " ==> activated");
        } else {
            makeToast("Not Activated!");
            holder.details.setVisibility(View.GONE);
             if(MyDebug.LOG) Log.wtf("onBindViewHolder", "pos: " + position + " ==> not activated");
        }*/
        setAnimation(holder.itemView, position);
    }

    ProgressDialog progress;

    public void showInfo(final int position) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        double screenW = metrics.widthPixels;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        String t = list.get(position).get("Type").toString();
        dialog.setContentView(R.layout.update_info);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button back = dialog.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog.cancel();
                if (progress != null)
                    if (progress != null) progress.cancel();
            }
        });
        switcher = dialog.findViewById(R.id.switcher);

        final TextView plasmaT = dialog.findViewById(R.id.donatedText);
        TextView willingT = dialog.findViewById(R.id.willDonate);
        //  String s1 = "Donated Plasma: " + (list.get(position).get("Donated").equals(true) ? "yes" : "no");
        String s1 = (list.get(position).get("Donated").equals(true) ? "yes" : "no");
        String s2 = "Willing to Donate: " + (list.get(position).get("Willing").equals(true) ? "yes" : "no");
        // SpannableString ss1 = new SpannableString(s1);
        //ss1.setSpan(new StyleSpan(Typeface.BOLD), s1.indexOf(": ") + 2, s1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableString ss2 = new SpannableString(s2);
        ss2.setSpan(new StyleSpan(Typeface.BOLD), s2.indexOf(": ") + 2, s2.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        plasmaT.setText(s1);
        willingT.setText(ss2);

        if (notesSaved.containsKey(position)) {
            switcher.setChecked(notesSaved.get(position).second.second);
            if (notesSaved.get(position).second.second)
                plasmaT.setText("yes");
            else plasmaT.setText("no");

            if (MyDebug.LOG)
                Log.wtf("*CHECKED", "checked: " + notesSaved.get(position).second.second);
        } else {
            if (s1.equals("yes")) {
                switcher.setChecked(true);
            } else {
                switcher.setChecked(false);
            }
        }
        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) plasmaT.setText("yes");
                else plasmaT.setText("no");
            }
        });
        progress = new ProgressDialog((Activity) context);
        progress.setMessage("Processing. Please wait...");
        progress.setTitle("Loading Info");
        progress.setIndeterminate(true);
        progress.setIcon(R.drawable.circle);
        progress.setCancelable(true);
        progress.show();

        String userPath = list.get(position).get("Doc ID").toString();
        db.document(userPath).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {
                if (MyDebug.LOG)
                    Log.wtf("*Reading current users's data", snapshot.getData().toString());
                if (progress != null) {
                    if (progress != null) progress.cancel();
                    progress.dismiss();
                }
                if (!snapshot.getData().toString().equals(map.toString())) {
                    // HashMap<String, Object> temp = (HashMap<String, Object>) snapshot.getData();
                    //DoctorStatuses3.setAdapterAgain();
                    //DoctorStatuses3.setAdapterAgain();
                    HashMap<String, Object> temp = (HashMap<String, Object>) snapshot.getData();
                    temp = DoctorStatuses3.patientInfo.get(position);
                    temp.putAll((HashMap<String, Object>) snapshot.getData());
                    DoctorStatuses3.setAdapterAgain();
                }
                displayStuff(dialog, (HashMap<String, Object>) snapshot.getData(), position);
                dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                largeToast("Could not load user's data. Are you connected to the internet?");
                if (progress != null) progress.cancel();
                dialog.cancel();
            }
        });
    }

    boolean updated;
    Switch switcher;

    private void displayStuff(final Dialog dialog, final HashMap<String, Object> map, final int position) {
        TextView nameT = dialog.findViewById(R.id.nameText);
        final TextView statusT = dialog.findViewById(R.id.statusText);
        TextView providerT = dialog.findViewById(R.id.providerText);
        TextView doctorT = dialog.findViewById(R.id.doctorText);
        TextView previousDateT = dialog.findViewById(R.id.updatedText);
        final Spinner statusSelection = dialog.findViewById(R.id.statusSelection);

        String name = map.get("Name").toString();
        String username = map.get("User").toString();
        String status = map.get("Status").toString();
        String type = map.get("Type").toString();

        //DONE For dates, if it is yesterday, then I am saying "Yesterday: ____".
        //  This makes it wrap onto next line like with Dr.Doctor Jones. Should I do this?
        previousDateT.setText("Updated: " + cleanDate(map.get("Updated").toString()));

        nameT.setPaintFlags(nameT.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        //DONE Ask dad should I put Dr. in the name if they are a doctor or does that get too confusing
        // especially since we are displaying Doctor: Dr. _____ who updated them?
        // nameT.setText(type.equals("Doctor") ? "Dr. " + name : name + "'s Info");
        nameT.setText(name + "'s Info");
        //FUTURE FILES If user is signed out of app when their status is updated and now user signs
        // into app, they will not get buzzing that status changed.
        if (map.containsKey("CenterU"))
            providerT.setText("Center: " + map.get("CenterU").toString());
        if (map.containsKey("DoctorU")) {
            if (map.get("DoctorU").toString().equals("n/a"))
                doctorT.setText("Doctor: n/a");
            else
                doctorT.setText("Dr. " + map.get("DoctorU").toString());
        }
        handleColors_Spinners(statusSelection, statusT, status);
        if (notesSaved.containsKey(position))
            statusSelection.setSelection(notesSaved.get(position).second.first);

        final EditText notes = dialog.findViewById(R.id.noteText);
        final TextView wordCount = dialog.findViewById(R.id.wordCount);
        if (notesSaved.containsKey(position)) {
            notes.setText(notesSaved.get(position).first);
            int wordsLength = countWords(notes.getText().toString());// words.length;
            wordCount.setText(String.valueOf(wordsLength) + "/" + 200 + " words");
        }
        notes.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int wordsLength = countWords(s.toString());// words.length;
                // count == 0 means a new word is going to start
                if (wordsLength > 200)
                    makeToast("Word limit exceeded.");
                if (wordsLength >= 200) {
                    setCharLimit(notes, notes.getText().length());
                } else {
                    removeFilter(notes);
                }

                if (s.toString().length() == 0)
                    wordCount.setText(String.valueOf(wordsLength) + "/" + 200 + " words");
                else wordCount.setText(String.valueOf(wordsLength) + "/" + 200 + " words");

            }
        });
        updated = false;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Pair pair = new Pair(notes.getText().toString().trim(), new Pair(statusSelection.getSelectedItemPosition(), switcher.isChecked()));
                if (!updated)
                    notesSaved.put(position, pair);
                else
                    notesSaved.put(position, new Pair<String, Pair<Integer, Boolean>>("", new Pair<Integer, Boolean>(0, false)));
            }
        });
        Button update = dialog.findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DONE Check if there were any changes, otherwise do not write.
                if (switcher.isChecked() == Boolean.parseBoolean(map.get("Donated").toString()) &&
                        statusSelection.getSelectedItem().toString().equals(map.get("Status").toString()) && notes.getText().toString().length() < 1) {
                    makeToast("You did not change anything.");
                } else {
                    final String patientPath = map.get("Doc ID").toString();
                    final String newStatus = statusSelection.getSelectedItem().toString();
                    final String note = notes.getText().toString();
                    if (!isNetworkAvailable()) {
                        makeToast("A WiFi connection is required to update status.");
                    }
                    if (isNetworkAvailable()) {
                        progress = new ProgressDialog((Activity) context);
                        progress.setMessage("Processing. Please wait...");
                        progress.setTitle("Uploading Data");
                        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progress.setIcon(R.drawable.circle);
                        progress.setIndeterminate(false);
                        progress.setProgress(0);
                        //DONE Ask dad should this loading progress be cancelable?
                        progress.setCancelable(true);
                        progress.show();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                progress.setMessage("Processing. Please wait..." + "\nMake sure you have a good internet connection.");
                            }
                        }, 6200);

                        final String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        final String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
                        HashMap<String, Object> userPassMap = new HashMap<>();
                        userPassMap.put("Updated", currentDate + " " + time);
                        userPassMap.put("Status", newStatus);
                        //DONE Ask dad if city should be the doctor's city.
                        userPassMap.put("CityU", doctorInfo.get("City"));
                        //TODO Shouldn't plasma be something that the doctor can edit?
                        userPassMap.put("CenterU", doctorInfo.get("Center"));
                        userPassMap.put("DoctorU", doctorInfo.get("Doc"));
                        userPassMap.put("Donated", switcher.isChecked());
                        db.document(patientPath).set(userPassMap, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String, Object> mapMap = new HashMap<>();
                                mapMap.put("City", map.get("City"));
                                mapMap.put("Country", map.get("Country"));
                                mapMap.put("State", map.get("State"));
                                mapMap.put("Status", newStatus);
                                progress.setProgress(25);
                                db.document("Map/" + map.get("Orig")).set(mapMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //TODO Ask dad right now when user registers, for both patient and doctor the
                                                //  default city is their current city. For doctors the center is their own center.
                                                //DONE  if it is fine if Update document name is random key.
                                                //DONE  if updates should also contain the state/country of the doctor.
                                                progress.setProgress(50);
                                                HashMap<String, Object> patientUpdates = new HashMap<>();
                                                patientUpdates.put("Center", doctorInfo.get("Center"));
                                                patientUpdates.put("City", doctorInfo.get("City"));
                                                patientUpdates.put("State", doctorInfo.get("State"));
                                                patientUpdates.put("Country", doctorInfo.get("Country"));
                                                patientUpdates.put("Date", currentDate + " " + time);
                                                patientUpdates.put("Doc", doctorInfo.get("Doc"));
                                                patientUpdates.put("Em", doctorInfo.get("Email"));
                                                patientUpdates.put("Note", note);
                                                patientUpdates.put("Phone", doctorInfo.get("Phone"));
                                                patientUpdates.put("Prev", map.get("Status"));
                                                patientUpdates.put("Status", newStatus);
                                                patientUpdates.put("Donated", switcher.isChecked());

                                                db.collection(patientPath + "/Updates").add(patientUpdates).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        //TODO Ask dad what info goes into Patients for doctor?
                                                        // Is this sufficient?
                                                        //TODO Ask dad if Patient is a doctor, should I also
                                                        // be getting the patient doctor's medical center?
                                                        // What about foreign patients. Should I get country?
                                                        // Right now I am getting all 3. Should I?
                                                        progress.setProgress(75);
                                                        HashMap<String, Object> doctorPatients = new HashMap<>();
                                                        doctorPatients.put("Doc ID", map.get("Doc ID"));
                                                        doctorPatients.put("CenterU", doctorInfo.get("Center"));
                                                        doctorPatients.put("CityU", doctorInfo.get("City"));
                                                        if (map.get("Type").equals("Doctor"))
                                                            doctorPatients.put("Center", map.get("Center"));
                                                        doctorPatients.put("City", map.get("City"));
                                                        doctorPatients.put("State", map.get("State"));
                                                        doctorPatients.put("Country", map.get("Country"));
                                                        doctorPatients.put("Phone", map.get("Phone"));
                                                        doctorPatients.put("Email", map.get("Email"));
                                                        doctorPatients.put("Name", map.get("Name"));
                                                        doctorPatients.put("User", map.get("Orig"));
                                                        doctorPatients.put("Type", map.get("Type"));
                                                        doctorPatients.put("Prev", map.get("Status"));
                                                        doctorPatients.put("Status", newStatus);
                                                        doctorPatients.put("Date", currentDate + " " + time);
                                                        doctorPatients.put("Donated", switcher.isChecked());

                                                        db.collection("userPass/" + doctorID + "/Patients").add(doctorPatients)
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentReference documentReference) {
                                                                        dialog.dismiss();
                                                                        dialog.cancel();
                                                                        //TODO Ask Dad if below message is good.
                                                                        makeToast("Data uploaded. Thank you!");
                                                                        //TODO Check if below works and what happens.
                                                                        updated = true;
                                                                        //README Get Current HashMap from list and update the status.
                                                                        // Then update adapter again.
                                                                        HashMap<String, Object> temp/* = list.get(position)*/;
                                                                        if (progress != null)
                                                                            progress.cancel();
                                                                        temp = DoctorStatuses3.patientInfo.get(position);
                                                                        temp.put("Status", newStatus);
                                                                        temp.put("Donated", switcher.isChecked());
                                                                        DoctorStatuses3.setAdapterAgain();
                                                                        //list.set(position, temp);
                                                                        try {
                                                                            //notifyItemChanged(position);
                                                                            notifyDataSetChanged();
                                                                        } catch (Exception e) {
                                                                            if (MyDebug.LOG)
                                                                                Log.wtf("*-)notifyItemChanged", e.toString());
                                                                        }
                                                                        //notifyItemChanged();
                                                                    }
                                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                largeToast("Failed to save info. Please try again.");
                                                                if (progress != null)
                                                                    progress.cancel();
                                                                if (MyDebug.LOG)
                                                                    Log.wtf("*-)doctor/patients ERROR", e.toString());
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        if (progress != null) progress.cancel();
                                                        largeToast("Failed to save info. Please try again.");
                                                        if (MyDebug.LOG)
                                                            Log.wtf("*-)patient/updates ERROR", e.toString());
                                                    }
                                                });

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        largeToast("Failed to save info. Please try again.");
                                        if (progress != null) progress.cancel();
                                        if (MyDebug.LOG) Log.wtf("*-)map ERROR", e.toString());
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                largeToast("Failed to save info. Please try again.");
                                if (progress != null) progress.cancel();
                                if (MyDebug.LOG) Log.wtf("*-)userPass ERROR", e.toString());
                            }
                        });
                    }
                }
            }
        });
    }

    private InputFilter filter;

    private void removeFilter(EditText et) {
        if (filter != null) {
            et.setFilters(new InputFilter[0]);
            filter = null;
        }
    }

    private int countWords(String s) {
        String trim = s.trim();
        if (trim.isEmpty())
            return 0;
        if (trim.length() == 0)
            return 0;
        return trim.split("\\s+").length; // separate string around spaces
    }


    private void setCharLimit(EditText et, int max) {
        filter = new InputFilter.LengthFilter(max);
        et.setFilters(new InputFilter[]{filter});
    }

    private void handleColors_Spinners(Spinner statusSelection, TextView statusT, String status) {
        ArrayList<String> statusList = new ArrayList<>();
        statusList.add("Infected");
        statusList.add("Uninfected");
        statusList.add("Recovered");
        if (!status.equals("Unknown")) {
            String sel = "Infected";
            for (int i = 0; i < statusList.size(); i++)
                if (statusList.get(i).equals(status)) {
                    sel = statusList.remove(i);
                    break;
                }
            statusList.add(0, sel);
        } else {
            statusList.add(0, "Unknown");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.status_selected, statusList);
        statusSelection.setAdapter(adapter);

        String t1 = "Current Status: " + status;
        SpannableString ss1 = new SpannableString(t1);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss1.setSpan(new StyleSpan(Typeface.BOLD), 0, 20, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (status.equals("Unknown"))
            ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (status.equals("Infected"))
            ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#eb3838")), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (status.equals("Recovered"))
            ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#2bad0e")), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        else if (status.equals("Uninfected"))
            ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#bf950a")), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        statusT.setText(ss1);
        statusSelection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = ((TextView) adapterView.getChildAt(0)).getText().toString();
                if (text.equals("Unknown")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(0XFF000000);
                    ((TextView) adapterView.getChildAt(0)).setBackgroundColor(0XFFDFDFDF);
                } else if (text.equals("Infected")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(0XFFd11313);
                    ((TextView) adapterView.getChildAt(0)).setBackgroundColor(0XFFFFDDDD);
                } else if (text.equals("Recovered")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(0XFF35c215);
                    ((TextView) adapterView.getChildAt(0)).setBackgroundColor(0XFFDDFFDE);
                } else if (text.equals("Uninfected")) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(0XFFc4990a);
                    ((TextView) adapterView.getChildAt(0)).setBackgroundColor(0XFFFFEDDD);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                makeToast("Please select a state if you are in the US.");
            }
        });
        statusSelection.setPrompt("Update Status to ...");

    }


    public String cleanDate(String s) {
        String currentDate = new SimpleDateFormat("M/d/yy", Locale.getDefault()).format(new Date());
        String[] split = s.split(" ");
        String time = cleanTime(split[1]);
        if (currentDate.equals(split[0]))
            return "Today," + time;
       /* int curDay = Integer.parseInt(currentDate.substring(currentDate.indexOf("/") + 1, currentDate.indexOf("/", currentDate.indexOf("/") + 1)));
        int previousDay = Integer.parseInt(split[0].substring(split[0].indexOf("/") + 1, split[0].indexOf("/", split[0].indexOf("/") + 1)));
        if (curDay == previousDay + 1)
            return "Yesterday," + time;*/
        return split[0].substring(0, split[0].length() - 3) + "," + time;
    }

    public String cleanTime(String s) {
        Integer a = Integer.parseInt(s.substring(0, s.indexOf(":")));
        String end = "AM";
        if (a > 12) {
            a -= 12;
            end = "PM";
        } else if (a == 0) {
            a += 12;
        }
        int firstIndex = s.indexOf(":");
        int secondIdex = s.indexOf(":", firstIndex + 1);
        return " " + a + s.substring(s.indexOf(":"), secondIdex) + " " + end;
    }

    public void showDoctorInfo(int position) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        double screenW = metrics.widthPixels;
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.update_info);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .875);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        dialog.show();
    }

    /**
     * Here is the key method to apply the animation
     */
    public static int lastPosition = -1;
    public static int rowHeight, upper, lower = 0;

    public static void setAnimation(final View viewToAnimate, int position) {
        viewToAnimate.setVisibility(View.INVISIBLE);

        if (position > lastPosition) {
            // if(MyDebug.LOG) Log.wtf("*setAnimation", position + " " + lastPosition);
            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.list_item_animation1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewToAnimate.setVisibility(View.VISIBLE);
                    viewToAnimate.startAnimation(animation);
                }
            }, 400 * position);
            lastPosition = position;
        } else {
            viewToAnimate.setVisibility(View.VISIBLE);
        }
    }

    public static void setAnimationChanged(final View viewToAnimate, int position, int[] difs) {
        // If the bound view wasn't previously displayed on screen, it's animated

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.list_item_animation1);
        //viewToAnimate.startAnimation(animation);
        int pos = 0;
        for (int i : difs) {
            if (Math.abs(dy - difs[pos]) < 30)
                break;
            pos++;
        }
        int p1 = difs[pos];
        int p2 = difs[pos] + 3;
        if (lower != p1) {
            lower = p1;
            View row = recyclerView.getLayoutManager().findViewByPosition(position);
            viewToAnimate.startAnimation(animation);
        }
        if (upper != p2) {
            upper = p2;
            View row = recyclerView.getLayoutManager().findViewByPosition(position);
            viewToAnimate.startAnimation(animation);
        }
       /* final Animation animation = AnimationUtils.loadAnimation(context, R.anim.list_item_animation1);
        Handler h = new Handler();
        //if(Math.abs(DoctorStatuses3.position - position) < 2) {
         if(MyDebug.LOG) Log.wtf("*-&* setAnimation", position + " " + lastPosition);
        if (position >= lastPosition) {
             if(MyDebug.LOG) Log.wtf("*-&* Animating", position + " " + lastPosition);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //viewToAnimate.setVisibility(View.VISIBLE);
                    viewToAnimate.findViewById(R.id.image).setVisibility(View.VISIBLE);
                    viewToAnimate.findViewById(R.id.text).setVisibility(View.VISIBLE);
                    viewToAnimate.startAnimation(animation);
                }
            }, position * 500);
        }
        lastPosition = position;*/
    }

    public int position;

    public void updatePosition() {
        position = 0;
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    position = getCurrentItem();
                    // if(MyDebug.LOG) Log.wtf("*-&* POsition", position + " " + lastPosition);
                    View row = recyclerView.getLayoutManager().findViewByPosition(position);
                    //setAnimation(row, position);
                }
            }
        });
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    public boolean hasPreview() {
        return getCurrentItem() > 0;
    }

    public boolean hasNext() {
        return recyclerView.getAdapter() != null &&
                getCurrentItem() < (recyclerView.getAdapter().getItemCount() - 1);
    }

    public void preview() {
        int position = getCurrentItem();
        if (position > 0)
            setCurrentItem(position - 1, true);
    }

    public void next() {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null)
            return;

        int position = getCurrentItem();
        int count = adapter.getItemCount();
        if (position < (count - 1))
            setCurrentItem(position + 1, true);
    }

    private int getCurrentItem() {
        return ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
    }

    private void setCurrentItem(int position, boolean smooth) {
        if (smooth)
            recyclerView.smoothScrollToPosition(position);
        else
            recyclerView.scrollToPosition(position);
    }

    public Bitmap loadImageBitmap(Context context, String name, String extension) {
        name = name + "." + extension;
        FileInputStream fileInputStream;
        Bitmap bitmap = null;
        try {
            fileInputStream = context.openFileInput(name);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (MyDebug.LOG)
                Log.wtf("*InfoRecyclerViewAdapter", "Loading Images failed: " + e.toString());
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.usericon2);
        }
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void makeSnackBar(int duration, String s) {
        Snackbar mySnackbar = Snackbar.make(view.findViewById(R.id.screen), s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    public void makeToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    private void largeToast(String s) {
        Toast toast;
        toast = Toast.makeText(context, s, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(22);
        toast.show();

    }

    public void shortToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
