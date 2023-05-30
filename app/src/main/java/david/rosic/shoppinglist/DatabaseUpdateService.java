package david.rosic.shoppinglist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class DatabaseUpdateService extends Service {

    private static final int DATABASE_CHECK_INTERVAL = 30 * 1000;
    private DbHelper dbHelper;
    private HttpHelper httpHelper;
    private boolean mRun = true;

    @Override
    public void onCreate() {
        super.onCreate();

        httpHelper = new HttpHelper();
        dbHelper = new DbHelper(this, MainActivity.DB_NAME, null, 1);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mRun) {
                    boolean dbUpdated = false;
                    // Perform database check here
                    // Compare local SQLite database with online MongoDB database

                    ShoppingList[] shoppingLists = dbHelper.getAllSharedLists();
                    for(ShoppingList shoppingList : shoppingLists){
                        Task[] tasksHTTP = null;
                        Task[] tasksLocal = dbHelper.getListItems(shoppingList.getmNaslov());
                        if(tasksLocal == null){continue;}//The shared list does not exist in the database

                        try {
                            JSONArray jsonArray = httpHelper.getJSONArrayFromURL(ShowListActivity.TASK_URL + "/" + shoppingList.getmNaslov());

                            tasksHTTP = new Task[jsonArray.length()];

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String name = jsonObject.getString("name");
                                boolean done = jsonObject.getBoolean("done");
                                long id = Long.parseLong(jsonObject.getString("taskId"), 36);

                                tasksHTTP[i] = new Task(name, done, id);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                        if( tasksLocal.length != tasksHTTP.length ){
                            dbUpdated = true;
                            dbHelper.updateListItems(shoppingList.getmNaslov(), tasksHTTP);
                        }else if( !arrayCompare(tasksLocal,tasksHTTP) ){
                            dbUpdated = true;
                            dbHelper.updateListItems(shoppingList.getmNaslov(), tasksHTTP);
                        }
                    }

                    // If there are updates, send a notification to the user
                    if (dbUpdated) {
                        sendNotification();
                    }

                    // Reschedule the database check after the specified interval
                    try {
                        Thread.sleep(DATABASE_CHECK_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRun = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        //Creating the channel for the notification
        String channelId = "channel_id";
        CharSequence channelName = "My Channel";
        String channelDescription = "My Channel Description";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel notificationChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //onClick for the notification
        Intent intent = new Intent(this, MainActivity.class);
        int uniqueId = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new NotificationCompat.Builder(this, channelId);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        builder.setSmallIcon(R.drawable.baseline_notifications_none_24)
                .setContentTitle("Database Update")
                .setContentText("New data is available in the database.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Send the notification
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ServiceThread", "Notifications disabled!");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build()); // Use a unique notification id
        Log.d("ServiceThread", "Notification");
    }

    private boolean arrayCompare(Task[] tasksLocal, Task[] tasksHTTP){
        for(int i =0; i < tasksLocal.length; i++){
            if ( !tasksLocal[i].equals(tasksHTTP[i]) ){
                return false;
            }
        }
        return true;
    }

}