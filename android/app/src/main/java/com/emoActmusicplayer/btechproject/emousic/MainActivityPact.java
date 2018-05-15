/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emoActmusicplayer.btechproject.emousic;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivityPact extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String TAG = "MainActivity";

    private Context mContext;

    /**
     * The entry point for interacting with activity recognition.
     */
    private ActivityRecognitionClient mActivityRecognitionClient;

    // UI elements.
    private Button mRequestActivityUpdatesButton;
    private Button mRemoveActivityUpdatesButton;

    /**
     * Adapter backed by a list of DetectedActivity objects.
     */
    private DetectedActivitiesAdapter mAdapter;


    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pact);

        mContext = this;

        // Get the UI widgets.
        // Enable either the Request Updates button or the Remove Updates button depending on
        // whether activity updates have been requested.
//        setButtonsEnabledState();

        ArrayList<DetectedActivity> detectedActivities = Utils.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                        Constants.KEY_DETECTED_ACTIVITIES, ""));

        // Bind the adapter to the ListView responsible for display data for detected activities.
//        mAdapter = new DetectedActivitiesAdapter(this, detectedActivities);

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        try {
            updateDetectedActivitiesList();
        }
        catch (Exception e){
            Log.d("DONT UD", "WHY THIS EXCPTN?");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
//        updateDetectedActivitiesList();
    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    public void requestActivityUpdatesButtonHandler(View view) {
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                        getString(R.string.activity_updates_enabled),
                        Toast.LENGTH_SHORT)
                        .show();
                setUpdatesRequestedState(true);
                updateDetectedActivitiesList();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, getString(R.string.activity_updates_not_enabled));
                Toast.makeText(mContext,
                        getString(R.string.activity_updates_not_enabled),
                        Toast.LENGTH_SHORT)
                        .show();
                setUpdatesRequestedState(false);
            }
        });
    }


    /**
     * Removes activity recognition updates using
     * {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. Registers success and
     * failure callbacks.
     */
    public void removeActivityUpdatesButtonHandler(View view) {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                        getString(R.string.activity_updates_removed),
                        Toast.LENGTH_SHORT)
                        .show();
                setUpdatesRequestedState(false);
                // Reset the display.
               //
                // mAdapter.updateActivities(new ArrayList<DetectedActivity>());
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to enable activity recognition.");
                Toast.makeText(mContext, getString(R.string.activity_updates_not_removed),
                        Toast.LENGTH_SHORT).show();
                setUpdatesRequestedState(true);
            }
        });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Ensures that only one button is enabled at any time. The Request Activity Updates button is
     * enabled if the user hasn't yet requested activity updates. The Remove Activity Updates button
     * is enabled if the user has requested activity updates.
     */
//    private void setButtonsEnabledState() {
//        if (getUpdatesRequestedState()) {
//            mRequestActivityUpdatesButton.setEnabled(false);
//            mRemoveActivityUpdatesButton.setEnabled(true);
//        } else {
//            mRequestActivityUpdatesButton.setEnabled(true);
//            mRemoveActivityUpdatesButton.setEnabled(false);
//        }
//    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private boolean getUpdatesRequestedState() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requesting) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, requesting)
                .apply();
      //  setButtonsEnabledState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Processes the list of freshly detected activities. Asks the adapter to update its list of
     * DetectedActivities with new {@code DetectedActivity} objects reflecting the latest detected
     * activities.
     */
    protected void updateDetectedActivitiesList() {

        ArrayList<DetectedActivity> detectedActivities = Utils.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(Constants.KEY_DETECTED_ACTIVITIES, ""));
        for( DetectedActivity activity : detectedActivities ) {
            switch (activity.getType()){
                case DetectedActivity.IN_VEHICLE: {
                    Log.e("ActivityRecogition", "In Vehicle: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("Are you in a Vehicle?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                        i.putExtra("Pact","Driving");
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Log.e("ActivityRecogition", "On Bicycle: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("Are you riding a Bicycle?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                       i.putExtra("Pact","Bicycling");
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Log.e("ActivityRecogition", "On Foot: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("on foot?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                        i.putExtra("Pact","Still");
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Log.e("ActivityRecogition", "Running: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("Are you Running?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                        i.putExtra("Pact","Running");startActivity(i);
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e("ActivityRecogition", "Still: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("Are you Still?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                        i.putExtra("Pact","Still");
                        Log.d("MOOOOOD",mood);
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.TILTING: {
                    Log.e("ActivityRecogition", "Tilting: " + activity.getConfidence());
                    
                    String mood = getIntent().getStringExtra("mood");

                    Intent i = new Intent(this,MainActivityMP.class);
                    i.putExtra("Pact","Still");
                    
                    i.putExtra("mood2",mood);
                    startActivity(i);

                    finish();
                    break;
                }
                case DetectedActivity.WALKING: {
                    Log.e("ActivityRecogition", "Walking: " + activity.getConfidence());
                    if (activity.getConfidence() >= 75) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText("Are you walking?");
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle(getString(R.string.app_name));
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                        
                        String mood = getIntent().getStringExtra("mood");

                        Intent i = new Intent(this,MainActivityMP.class);
                        
                        i.putExtra("mood2",mood);
                        i.putExtra("Pact","Walking");
                        startActivity(i);
                        finish();
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e("ActivityRecogition", "Unknown: " + activity.getConfidence());
                    finish();
                    break;
                }
            }
        }
        //mAdapter.updateActivities(detectedActivities);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Constants.KEY_DETECTED_ACTIVITIES)) {
            updateDetectedActivitiesList();
        }
    }
}