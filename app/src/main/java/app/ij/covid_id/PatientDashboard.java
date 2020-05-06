package app.ij.covid_id;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import java.io.IOException;
import java.io.OutputStreamWriter;

import app.ij.covid_id.ui.dashboard.DashboardFragment;
import app.ij.covid_id.ui.doctor_dashboard.DoctorDashboardFragment;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import app.ij.covid_id.ui.map.MapFragment;
import app.ij.covid_id.ui.settings.SettingsFragment;

public class PatientDashboard extends AppCompatActivity {
    double screenW;
    BottomNavigationView navView;

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

    Toast toast;

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