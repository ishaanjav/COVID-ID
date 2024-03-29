package app.ij.covid_id;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.ij.covid_id.ui.dashboard.DashboardFragment;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import app.ij.covid_id.ui.map.MapFragment;
import app.ij.covid_id.ui.settings.SettingsFragment;

public class DoctorDashboard extends AppCompatActivity {

    FirebaseFirestore db;
    RelativeLayout screen;
    double screenW;
    String updater;

    String firstDoctor = "";
    BottomNavigationView navView;
    String status, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        screen = findViewById(R.id.container);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_patients, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        overridePendingTransition(R.anim.medium_fade_in, R.anim.fast_fade_out);
        db = FirebaseFirestore.getInstance();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;
        //updater = readFromFile(getApplicationContext());
        //listenForUpdates();
        variable = 3;
        firstDoctor = readFirstDoctor(getApplicationContext());
        if (firstDoctor == null || firstDoctor.isEmpty() || firstDoctor.length() < 2) {
            //TODO This is the first time a doctor is signing in --> TourGuide and explanation of how to use app.

            //TODO Uncomment below when done testing TourGuide
            //writeToFirstDoctor("Doctor Finished");
        } else if (firstDoctor.contains("yes")) {
            //TODO This is the first time a doctor is signing in --> after creating an account.

            //TODO Uncomment below when done testing TourGuide
            //writeToFirstDoctor("Doctor Finished");
        }
        readStorage();
        readUpdate();
    }

    private void readStorage() {
        String info = readFromFile("info.txt", getApplicationContext());
        String[] contents = info.split("___________");
        username = (contents[2]);
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
            if (MyDebug.LOG) Log.wtf("login activity", "File not found: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (IOException e) {
            if (MyDebug.LOG) Log.wtf("login activity", "Can not read file: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return ret;
    }

    private void readUpdate() {
        String info = readFromFile("statusUpdate.txt", getApplicationContext());
        String[] contents = info.split("-----|\\W+|\\n|\\r");
        boolean match = false;
        String matchingStatus = "";
        //Bob-----Unknown-----
        int position = 0;
        String before = "";
        String after = "";
        String logger = "";
        //for (String s : contents) logger += "-" + s + "-\n";
        //before = contents[0] + "-----";
        ArrayList<String> al = new ArrayList<>();
        for (String s : contents) {
            if (s.isEmpty() || s.length() < 5) {

            } else {
                al.add(s);
                logger += "-" + s + "-\n";
            }
        }
        contents = al.toArray(new String[al.size()]);
        if (MyDebug.LOG) Log.wtf("*Logger", logger);
        for (int i = 0; i < contents.length - 1; i += 2) {
            if (contents[i].equals(username)) {
                match = true;
                position = i;
                if (i + 1 < contents.length)
                    matchingStatus = contents[i + 1];
                //break;
            } else if (match) {
                after += contents[i] + "-----" + contents[i + 1] + "-----";
            } else {
                before += contents[i] + "-----" + contents[i + 1] + "-----";
            }
        }
        if (MyDebug.LOG)
            Log.wtf("*readUpdate()", username + " " + match + ": " + matchingStatus + ", " + status + "--" + info + "  b4:--" + before + "--af: " + after);
        if (match) {
            //README Status right now (updated when they hit the login button)
            // is different from status from last sign in.
            if (!matchingStatus.equals(status)) {
                //DONE Make notification
                Vibrator vib = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 800, 250, 800, 250, 800, 250, 800, 250};
                if (vib.hasVibrator())
                    vib.vibrate(pattern, -1);
                largeToast("Your COVID Status was updated!");

                String replaceCurrentUser = before + username + "-----" + status + "-----" + after;
                if (MyDebug.LOG) Log.wtf("*replaceCurrentUser", replaceCurrentUser);
                //writeToInfo("statusUpdate.txt", replaceCurrentUser);
                writeToStatusUpdate(replaceCurrentUser);

            }
        } else {
            //DONE Write whatever their current username and status is.
            //README They are a new user and their info is not in statusUpdate.txt
            String writeNewUser = username + "-----" + status + "-----" + info;
            writeToStatusUpdate(writeNewUser);

        }
    }

    private void writeToStatusUpdate(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("statusUpdate.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            if (MyDebug.LOG) Log.wtf("*Exception", "File write failed: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    Toast toast;

    private void largeToast(String s) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //makeToast("HI");
    }

    private void writeToUpdate(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("update.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private String readUsers(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("users.txt");

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
            if (MyDebug.LOG) Log.wtf("login activity", "File not found: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (IOException e) {
            if (MyDebug.LOG) Log.wtf("login activity", "Can not read file: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return ret;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.dashboard_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();
                break;
            case R.id.about:
                showAboutApp();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showAboutApp() {
        Dialog dialog = new Dialog(DoctorDashboard.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.about_dashboard);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = (int) (screenW * .85);
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView text = dialog.findViewById(R.id.text);
        TextView title = dialog.findViewById(R.id.title);

        if (navView.getSelectedItemId() == R.id.navigation_home)
            text.setText("The dashboard contains the last updated status for your COVID test results as released by your doctor.\nYou can show this to potential employers or coworkers as verification of your status.\nFor more info/updates contact your doctor/medical provider.");
        else if (navView.getSelectedItemId() == R.id.navigation_patients) {
            text.setText("In this page, you can view a list of patients, sorted by the same city as you, then state, and finally by country." +
                    "\nSimply tap on a row to view additional details about the patient and update their status.");
            title.setText("Patients Page");
        } else if (navView.getSelectedItemId() == R.id.navigation_dashboard) {
            text.setText("In this page, you will be able to view a map that displays the different COVID statuses across cities, states, and countries.\n" +
                    "The users are kept anonymous and only the status will be displayed.");
            title.setText("About the Map");
        } else if (navView.getSelectedItemId() == R.id.navigation_notifications) {
            text.setText("In this page, you will be able to update your location, take a new photo of yourself, and specify additional features for the app.");
            title.setText("About Settings");
        }
        dialog.show();
    }

    private void signOut() {
        writeLogin("false", getApplicationContext());
        removeUncessaryFiles();
        removeListeners();
        startActivity(new Intent(DoctorDashboard.this, MainActivity.class));
    }

    public void removeListeners() {
        if (MyDebug.LOG) Log.wtf("INFO", "Doctor Dashboard: Removing Listeners");
        if (DoctorDashboardFragment.listener != null)
            DoctorDashboardFragment.listener.remove();
        if (DoctorDashboardFragment.listener2 != null)
            DoctorDashboardFragment.listener2.remove();
        if (DashboardFragment.listener != null)
            DashboardFragment.listener.remove();
        if (DashboardFragment.listener2 != null)
            DashboardFragment.listener2.remove();
        if (DoctorStatuses3.userPassListener != null)
            DoctorStatuses3.userPassListener.remove();
        if (MapFragment.listener != null)
            MapFragment.listener.remove();
        if (SettingsFragment.listener != null)
            SettingsFragment.listener.remove();
    }

    public static int variable = 3;

    @Override
    public void onBackPressed() {
        variable--;
        String time = (variable == 1) ? " more time " : " more times ";
        if (variable == 0)
            signOut();
        else makeToast("Press back " + variable + time + "to sign out.");
    }

    private String readFirstDoctor(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("firstDoctor.txt");

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
            if (MyDebug.LOG) Log.wtf("login activity", "File not found: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        } catch (IOException e) {
            if (MyDebug.LOG) Log.wtf("login activity", "Can not read file: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return ret;
    }


    //README "firstDoctor.txt" is used to check whether this is the first time a doctor is signing in.
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

    private void makeToast(String s) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onUserLeaveHint() {
        removeUncessaryFiles();
        removeListeners();
        super.onUserLeaveHint();
    }

    public boolean removeUncessaryFiles() {
        String directory = getApplicationContext().getApplicationInfo().dataDir + "/files";
        String readUsers = readUsers(getApplicationContext());
        List<String> users = Arrays.asList(readUsers.split("-------"));

        if (MyDebug.LOG)
            Log.wtf("*-* Users List:", "SIZE: " + users.size() + " " + users.toString());
        File location = new File(directory);
        File[] files = location.listFiles();
        ArrayList<String> locations = new ArrayList<>();
        //TODO See if below code works.
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getName();
            if (name.endsWith(".jpg")) {
                count++;
                if (!users.contains(name.substring(0, name.length() - 4))) {
                    files[i].delete();
                    if (MyDebug.LOG)
                        Log.wtf("*-* Deleting", users.contains(name.substring(0, name.length() - 4)) + " " + name);
                    count--;
                }
                //locations.add(name);
            }
        }
        for (String s : locations) {
            File temp = new File(directory + "/" + s);
            if (MyDebug.LOG) Log.wtf("*-* REMOVING", s);
            temp.delete();
        }
        //TODO Remove Below 2 lines and basically all  if(MyDebug.LOG) Log.wtf() that are not needed for deployment.
        files = location.listFiles();
        if (MyDebug.LOG)
            Log.wtf("*-* Files", "Path: " + directory + "  # of files: " + files.length + "   # of images: " + count);
        return true;
    }

    private void writeLogin(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("login.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }
}