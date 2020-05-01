package app.ij.covid_id;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses;
import app.ij.covid_id.ui.doctor_statuses.DoctorStatuses3;
import de.hdodenhof.circleimageview.CircleImageView;

public class InfoRecyclerViewAdapter extends RecyclerView.Adapter<InfoRecyclerViewAdapter.ViewHolder> {

    static Context context;
    ArrayList<HashMap<String, Object>> list;
    HashMap<String, Bitmap> bitmapList;
    int mExpandedPosition;
    View details;
    static RecyclerView recyclerView;
    public String TAG = "RecyclerViewAdapter";

    public InfoRecyclerViewAdapter(Context context, ArrayList<HashMap<String, Object>> list, RecyclerView recyclerView, HashMap<String, Bitmap> bitmaps) {
        this.context = context;
        this.list = list;
        this.recyclerView = recyclerView;
        bitmapList = bitmaps;
    }

    View view;

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        RelativeLayout screen;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            screen = itemView.findViewById(R.id.screen);
            mExpandedPosition = -1;
        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        details = LayoutInflater.from(context).inflate(R.layout.extended_list_item, parent, false);

        return holder;
    }

    int previousExpandedPosition;
    String documentPath, username;

    public static int dx = 0, dy = 0;

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        //Log.wtf(TAG, "onBindViewHolder --  called");
        lastPosition = -1;
        HashMap<String, Object> map = list.get(position);
        //Log.wtf(TAG, "onBindViewHolder List - " + list.toString() + " \n\t\t\\t\t\t\t\\t\tt\t\t\t\t\t" + map.toString());

        documentPath = map.get("Doc ID").toString();
        username = map.get("User").toString();

        holder.text.setText(map.get("City").toString() + " " + map.get("State").toString() + ", " + map.get("Country").toString());
        /*Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.slide_in_left
                        : R.anim.slide_in_right);
        holder.itemView.startAnimation(animation);
        lastPosition = position;*/
        //Log.wtf("*-&* Animating", position + " " + lastPosition);

//TODO See if there is a way to load all the text stuff first and then load images once it is retrieved.
        //INFO Right now we have to load all the stuff after images are retrieved and it takes a while for
        // 3 images so search alternatives.
        // 3 images = 1457
        if (bitmapList.containsKey(username)) {
            holder.image.setImageBitmap(bitmapList.get(username));
            Log.wtf("*-& Using Bitmap List", username);
        } else {
            holder.image.setImageBitmap(loadImageBitmap(context, map.get("User").toString(), "jpg"));
            Log.wtf("*-& Getting from Storage", username);
        }
        //TODO Uncomment this
        if (DoctorStatuses3.loadingResults != null && position == (Math.min(list.size() - 1, 12))
                && DoctorStatuses3.loadingResults.isShowing()) {
            Log.wtf("CANCELLED", "From inside Adapter");
            DoctorStatuses3.loadingResults.cancel();
        }

        final boolean isExpanded = position == mExpandedPosition;
        details.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        if (isExpanded)
            previousExpandedPosition = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandedPosition = isExpanded ? -1 : position;
                //Approach 1
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();


                //Approach 2

            }
        });
        holder.itemView.setVisibility(View.INVISIBLE);
        setAnimation(holder.itemView, position);
    }

    /**
     * Here is the key method to apply the animation
     */
    public static int lastPosition = -1;
    public static int rowHeight, upper, lower = 0;

    public static void setAnimation(final View viewToAnimate, int position) {
        if (position > lastPosition) {
            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.list_item_animation1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewToAnimate.setVisibility(View.VISIBLE);
                    viewToAnimate.startAnimation(animation);
                }
            }, 500 * position);
            lastPosition = position;
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
                    Log.wtf("*-&* POsition", position + " " + lastPosition);
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
