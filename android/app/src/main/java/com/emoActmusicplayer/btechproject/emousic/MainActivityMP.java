package com.emoActmusicplayer.btechproject.emousic;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import static android.os.Build.VERSION.SDK_INT;

public class MainActivityMP extends AppCompatActivity {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.valdioveliu.valdio.audioplayer.PlayNewAudio";
    String currentApp = "NULL";
    private MediaPlayerService player;
    boolean serviceBound = false;
    ArrayList<Audio> audioList;

    ImageView collapsingImageView;
    String moodfinal, songnav;
    int imageIndex = 0;

    public int audioIndextoPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String Pact = new String(getIntent().getStringExtra("Pact"));

        if (Build.VERSION.SDK_INT >= 21) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

            if (stats == null || stats.isEmpty()) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }

        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR, -1);
        long startTime = calendar.getTimeInMillis();
        PackageManager packageManager = getApplicationContext().getPackageManager();

        String Mact = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            //UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
            long time = System.currentTimeMillis();
            // We get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            // Sort the stats by the last time used
            if (stats != null || stats.isEmpty()) {
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//                startActivity(intent);
                SortedMap<Long, String> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats.getPackageName());
                }
                List a1 = new ArrayList();
                for (Map.Entry entry : mySortedMap.entrySet()) {
                    a1.add(entry.getValue());

                }

                //creating sorted list of apps
                List Chatt = new ArrayList();
                Chatt.add("WhatsApp");
                Chatt.add("Snapchat");
                Chatt.add("Telegram");
                Chatt.add("hike");

                List SocialMedia = new ArrayList();
                SocialMedia.add("facebook");
                SocialMedia.add("Instagram");
                SocialMedia.add("Lite");
                SocialMedia.add("Twitter");

                List browsing = new ArrayList();
                browsing.add("Chrome");
                browsing.add("Browser");
                browsing.add("Firefox");

                List reading = new ArrayList();
                reading.add("WPS Office");
                reading.add("Adobe Acrobat");

                List gaming = new ArrayList();
                gaming.add("Sudoku");
                gaming.add("Traffic Rider");


                Mact = "nothing";

                ListIterator i = a1.listIterator(a1.size());
                while (i.previousIndex() != a1.size() - 5) {
                    Object e = i.previous();

                    String packagename = (String) e;
                    try {
                        String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packagename, PackageManager.GET_META_DATA));
                        //Log.d("1 ","running apps CAT   "+ appName);
                        if (Chatt.contains(appName)) {
                            Mact = "chatting";
                            break;
                        } else if (SocialMedia.contains(appName)) {
                            Mact = "Social media";
                            break;
                        } else if (browsing.contains(appName)) {
                            Mact = "browsing";
                            break;
                        } else if (reading.contains(appName)) {
                            Mact = "reading";
                            break;
                        } else if (gaming.contains(appName)) {
                            Mact = "gaming";
                            break;
                        } else
                            continue;
                    } catch (PackageManager.NameNotFoundException e1) {
                        e1.printStackTrace();
                    }
                }
                Log.d("1 ", "running apps CAT   " + Mact);
            }
        }

        setContentView(R.layout.activity_mainmp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        collapsingImageView = (ImageView) findViewById(R.id.collapsingImageView);
        loadCollapsingImage(imageIndex);
//        Log.d("SongName",songname);

        moodfinal = getIntent().getStringExtra("mood2");

        try {
//            Log.d("song", songnav);
            Log.d("mood", moodfinal);
        } catch (Exception e) {
            Log.d("exception", "exception ala");
        }


        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String session = null;

        if (timeOfDay >= 0 && timeOfDay < 12)
            session = new String("morning");
        else if (timeOfDay >= 12 && timeOfDay < 16)
            session = new String("afternoon");
        else if (timeOfDay >= 16 && timeOfDay < 21)
            session = new String("evening");
        else if (timeOfDay >= 21 && timeOfDay < 24)
            session = new String("night");

        String myJson = "{\"Emot\":\"" + moodfinal + "\", " +
                "\"Pact\":\"" + Pact + "\", " +
                "\"Mact\":\"" + Mact + "\", " +
                "\"Session\":\"" + session + "\"}";
        Log.d("Json:", myJson);

        try {
            songnav = new Download().execute(myJson).get().toString();
            Log.d("HTTP_RESPONSE", songnav);

        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), moodfinal + " + " + Mact + " + " + Pact + " + " + session + " = " + songnav.substring(10, songnav.length() - 3),
                Toast.LENGTH_LONG).show();
        if (checkAndRequestPermissions()) {
            loadAudioList();
            for (int i = 0; i < audioList.size(); i++) {
                Audio as = audioList.get(i);
                Log.d("SOng",as.getTitle());
                if(as.getTitle().equals(songnav.substring(10, songnav.length() - 3))){
                    playAudio(i);
                }
            }
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
                //play the first audio in the ArrayList
//                playAudio(2);
                if (imageIndex == 4) {
                    imageIndex = 0;
                    loadCollapsingImage(imageIndex);
                } else {
                    loadCollapsingImage(++imageIndex);
                }
            }
        });

    }

    private void loadAudioList() {
        loadAudio();
        initRecyclerView();
        //playAudio(1);
    }

    private boolean checkAndRequestPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        String TAG = "LOG_PERMISSION";
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions

                    if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            ) {
                        Log.d(TAG, "Phone state and storage permissions granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        loadAudioList();
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                      //shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                            showDialogOK("Phone state and storage permissions required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private void initRecyclerView() {
        if (audioList != null && audioList.size() > 0) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
            RecyclerView_Adapter adapter = new RecyclerView_Adapter(audioList, getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            recyclerView.addOnItemTouchListener(new CustomTouchListener(this, new onItemClickListener() {
                @Override
                public void onClick(View view, int index) {
                    playAudio(index);
                }
            }));
        }
    }

    private void loadCollapsingImage(int i) {
        Log.d("---PPPPPP","ARRAY BEFORRRREEE ---- ");
        TypedArray array = getResources().obtainTypedArray(R.array.images);
        Log.d("---PPPPPP","ARRAY AFTERRRR ---- ");
        if(array==null)
            Log.d("---PPPPPP","ARRAY IS THERE ---- ");
        collapsingImageView.setImageDrawable(array.getDrawable(i));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }

    //Binding this Client to the AudioPlayer Service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    /**
     * Load audio files using {@link ContentResolver}
     *
     * If this don't works for you, load the audio files to audioList Array your oun way
     */
    private void loadAudio() {

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.ArtistColumns.ARTIST,};
        Cursor c = getContentResolver().query(uri,
                projection,
                MediaStore.Audio.Media.DATA + " like ? ",
                new String[]{"%"+moodfinal+"%"}, // yourFolderName
                MediaStore.Audio.Media.TITLE + " ASC");

        if (c != null) {
            audioList = new ArrayList<>();
            while (c.moveToNext()) {

                String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                String path = c.getString(0);
                String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                String name = path.substring(path.lastIndexOf("/") + 1);

                audioList.add(new Audio(data, name, album, artist));

            }
            c.close();
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }
    private  class Download extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection con = null;
            try {
                URL url = new URL("http://10.100.109.177:5000/");
                con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                con.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                con.setRequestProperty("Content-type", "application/json");
                con.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(con.getOutputStream());
                dStream.writeBytes(params[0]);

                con.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null){
                    sb.append(line);
                    sb.append("\n");
                }
                br.close();
                Log.d("PPPP- - ",sb.toString());
                return sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "executed!!";
        }
    }
}
