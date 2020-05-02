package app.ij.covid_id;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;

public class InfoRecyclerViewAdapter extends RecyclerView.Adapter<InfoRecyclerViewAdapter.ViewHolder> {

    static Context context;
    ArrayList<HashMap<String, Object>> list;
    HashMap<String, Bitmap> bitmapList;
    int mExpandedPosition;
    View details;
    static RecyclerView recyclerView;
    public String TAG = "RecyclerViewAdapter";
    ArrayList<Boolean> bools;

    public InfoRecyclerViewAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, HashMap<String, Bitmap> bitmaps) {
        this.context = context;
        this.list = list;
        this.recyclerView = recyclerView;
        bitmapList = bitmaps;
    }

    View view;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        RelativeLayout screen, bar1;
        TextView name, status, location;
        //TextView details;
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
            //details = itemView.findViewById(R.id.details);
            statusBox = itemView.findViewById(R.id.statusBox);
            bools = new ArrayList<>();
            //Log.wtf("*CALLED", "VIEWHOLDER");
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
                        if (type.equals("Patient")) showPatientInfo(pos);
                        else showDoctorInfo(pos);
                    } else {
                        //makeSnackBar(3000, "You need a Wifi connection to update a user's status.");
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
        //Log.wtf("*toggleText", "pos: " + pos + " - Previous: " + bool + " Now: " + bools.get(pos));
  /*items[layoutPosition] = items[layoutPosition].let {
            it.first to !it.second
        }*/
        notifyItemChanged(pos);
    }

    int counter = 0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        //Log.wtf("*onCreateViewHolder", "Size: " + list.size() + "  Counter: " + counter);
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
        //Log.wtf(TAG, "onBindViewHolder --  called");
        map = list.get(position);
        //Log.wtf(TAG, "onBindViewHolder List - " + list.toString() + " \n\t\t\\t\t\t\t\\t\tt\t\t\t\t\t" + map.toString());

        documentPath = map.get("Doc ID").toString();
        username = map.get("User").toString();
        status = map.get("Status").toString();
        name = map.get("Name").toString();
        type = map.get("Type").toString();

        if (type.equals("Doctor"))
            name = "Dr. " + name;

        holder.name.setText(map.get("Name").toString());
        boolean foreign = map.get("State").toString().length() < 2;
        country = map.get("Country").toString();
        if (foreign)
            holder.location.setText(map.get("City").toString() + ", " + country.substring(country.indexOf(":") + 1));
        else
            holder.location.setText(map.get("City").toString() + " " + map.get("State").toString());
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
            //Log.wtf("*-& Using Bitmap List", username);
        } else {
            holder.image.setImageBitmap(loadImageBitmap(context, map.get("User").toString(), "jpg"));
            //Log.wtf("*-& Getting from Storage", username);
        }
        if (DoctorStatuses3.loadingResults != null && position == (Math.min(list.size() - 1, 12))
                && DoctorStatuses3.loadingResults.isShowing()) {
            //Log.wtf("*CANCELLED", "From inside Adapter");
            DoctorStatuses3.loadingResults.cancel();
        }

         /*   if (bools.get(position)) {
            makeToast("ACTIVATED!");
            holder.details.setVisibility(View.VISIBLE);
            Log.wtf("onBindViewHolder", "pos: " + position + " ==> activated");
        } else {
            makeToast("Not Activated!");
            holder.details.setVisibility(View.GONE);
            Log.wtf("onBindViewHolder", "pos: " + position + " ==> not activated");
        }*/
        setAnimation(holder.itemView, position);
    }

    public void showPatientInfo(int position) {
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView nameT = dialog.findViewById(R.id.nameText);
        TextView statusT = dialog.findViewById(R.id.statusText);

        Map<String, Object> map = list.get(position);
        String name = map.get("Name").toString();
        String username = map.get("User").toString();
        String status = map.get("Status").toString();

        nameT.setText(name);

        String t1 = "Current Status: " + status;
        SpannableString ss1 = new SpannableString(t1);
        ss1.setSpan(new StyleSpan(Typeface.BOLD), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss1.setSpan(new ForegroundColorSpan(Color.parseColor("#000000")), t1.indexOf(": ") + 2, t1.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        statusT.setText(ss1);


        dialog.show();
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
            //Log.wtf("*setAnimation", position + " " + lastPosition);
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
        Log.wtf("*-&* setAnimation", position + " " + lastPosition);
        if (position >= lastPosition) {
            Log.wtf("*-&* Animating", position + " " + lastPosition);
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
                    //Log.wtf("*-&* POsition", position + " " + lastPosition);
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

    public void shortToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
