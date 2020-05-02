package app.ij.covid_id.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import app.ij.covid_id.DoctorDashboard;
import app.ij.covid_id.PatientDashboard;
import app.ij.covid_id.R;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;
import app.ij.covid_id.ui.map.MapViewModel;

public class SettingsFragment extends Fragment {
    View root;
    RelativeLayout screen;
    private SettingsViewModel settingsViewModel;
    Button update;

    public View findViewById(int id) {
        return root.findViewById(id);
    }

    FirebaseFirestore db;
    String userPassID;
    String status;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel =
                ViewModelProviders.of(this).get(SettingsViewModel.class);
        root = inflater.inflate(R.layout.fragment_map, container, false);
        update = (Button) findViewById(R.id.update);
        screen = (RelativeLayout) findViewById(R.id.screen);
        db = FirebaseFirestore.getInstance();
        DoctorDashboard.variable = 3;
        PatientDashboard.variable = 3;
        readStorage();
        updateInfoTxt();
        return root;
    }

    protected boolean isSafe() {
        return !(this.isRemoving() || this.getActivity() == null || this.isDetached() || !this.isAdded() || this.getView() == null);
    }
ListenerRegistration listener;

    @Override
    public void onDestroy() {
        super.onDestroy();
        listener.remove();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener.remove();
    }

    public void updateInfoTxt() {
        final DocumentReference docRef = db.collection("userPass").document(userPassID);
        listener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (isSafe()) {
                    if (e != null) {
                        Log.wtf("USERPATH ERROR", "Listen failed.", e);
                        makeSnackBar(3000, "Could not load your data. Are you connected to the internet?");
                        return;
                    }
                    String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";
                    String source2 = snapshot.getMetadata().isFromCache() ?
                            "local cache" : "server";
                    //README Local changes (taking place in Settings) will update info.txt in Settings
                    if (snapshot != null && snapshot.exists() && source.equals("Server")) {
                        //TODO Write new info to info.txt
                        //makeToast("HI");
                        if (!status.equals(snapshot.getString("Status"))) {
                            DoctorDashboardFragment.status = snapshot.getString("Status");
                            //TODO Status changed --> Consider making a notification. Do vibrations at very least.
                            Vibrator vib = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                            long[] pattern = {0, 800, 250, 800, 250, 800, 250, 800, 250};
                            if (vib.hasVibrator())
                                vib.vibrate(pattern, -1);
                            makeToast("Your COVID Status was updated! Check the dashboard.");
                        }
                        //writeNewInfo(snapshot.getData());
                        Log.wtf("*------ INFO RETRIEVED (Settings) -----", source + " data: " + snapshot.getData());
                    } else if (source2.contains("cach")) {
                        makeSnackBar(4000, "Loaded offline data. Connect to the internet for updated information.");
                        //writeNewInfo(snapshot.getData());
                    } else if (snapshot == null) {
                        Log.wtf("ERROR", source + " data: null");
                    } else {
                        Log.wtf("ERROR", source + " data: null");
                    }
                }
                // IMPORTANT -- Logic for notification
                //  If status is not "" and !tempStatus.equals(status)
                //  Then do vibration notification
                //NOTES For above, may have to call isSafe() before doing vibration.
                //String tempStatus = snapshot.getData().get("Status").toString();
                //if (!tempStatus.equals(status)) {
                //TODO Status changed --> Consider making a notification.
                //makeToast("IMPORTANT: Your COVID Status changed. Visit the dashboard for more info.");
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //makeToast("IMPORTANT: Your COVID Status changed. Visit the dashboard for more info.");
                    }
                }, 3500);*/
                //}

            }
        });

    }

    private void readStorage() {
        String info = readFromFile("info.txt", getContext());
        String[] contents = info.split("___________");
        Log.wtf("Read Status-", contents[8]);
        userPassID = (contents[9]);
        status = (contents[8]);
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateButton();

    }

    boolean previous;

    private void updateButton() {
        previous = true;
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isNetworkAvailable()) {
                    //https://play.google.com/store/apps/details?id=app.anany.faceanalyzer
                    final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                    Log.wtf("Package name", appPackageName);
                    makeToast("Connect to the internet first.");
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                } else {
                    db.collection("Update")
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Boolean update = Boolean.parseBoolean(document.get("Update").toString());
                                if (update) {
                                    final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                                    Log.wtf("Package name", appPackageName);
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                    previous = true;
                                } else {
                                    previous = false;
                                    makeSnackBar(2125, "Sorry. No updates available yet.");
                                }
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeSnackBar(2600, "Couldn't check for updates. Try again.");
                        }
                    });
                }
            }
        });
    }

    Snackbar mySnackbar;

    private void makeSnackBar(int duration, String s) {
        mySnackbar = Snackbar.make(screen, s, duration);
        View snackbarView = mySnackbar.getView();
        TextView tv = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setMaxLines(4);
        mySnackbar.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void makeToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_LONG).show();
    }
}