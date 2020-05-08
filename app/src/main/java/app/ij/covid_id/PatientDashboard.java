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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import app.ij.covid_id.ui.dashboard.DashboardFragment;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import app.ij.covid_id.ui.map.MapFragment;
import app.ij.covid_id.ui.settings.SettingsFragment;

public class PatientDashboard extends AppCompatActivity {
    double screenW;
    BottomNavigationView navView;
    String status, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_dashboard);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        overridePendingTransition(R.anim.medium_fade_in, R.anim.fast_fade_out);
        variable = 3;

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        screenW = metrics.widthPixels;

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
            Log.wtf("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.wtf("login activity", "Can not read file: " + e.toString());
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
        for(String s: contents){
            if(s.isEmpty() || s.length()< 5){

            }else{
                al.add(s);
                logger += "-" + s + "-\n";
            }
        }
        //contents = (String[]) al.toArray();
        contents = al.toArray(new String[al.size()]);
        Log.wtf("*Logger", logger);
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
                Log.wtf("*replaceCurrentUser", replaceCurrentUser);
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

    Toast toast;

    private void largeToast(String s) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        toast.show();

    }

    private void writeToStatusUpdate(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getApplicationContext().openFileOutput("statusUpdate.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.wtf("*Exception", "File write failed: " + e.toString());
        }

    }

    @Override
    protected void onUserLeaveHint() {
        removeListeners();
        super.onUserLeaveHint();
    }

    public void removeListeners() {
        Log.wtf("INFO", "Patient Dashboard: Removing Listeners");
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

    private void makeToast(String s) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT);
        toast.show();
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
        Dialog dialog = new Dialog(PatientDashboard.this);
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
        else if (navView.getSelectedItemId() == R.id.navigation_dashboard) {
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
        removeListeners();
        writeLogin("false", getApplicationContext());
        startActivity(new Intent(PatientDashboard.this, MainActivity.class));
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
}