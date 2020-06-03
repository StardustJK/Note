package group3.sse.bupt.note.Alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.TreeMap;

import androidx.core.app.NotificationCompat;
import group3.sse.bupt.note.R;

public class AlarmReceiver extends BroadcastReceiver {

    private String channelId="Note";
    private String name="ChannelName";


    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();

        String content=intent.getStringExtra("content");
        int id=intent.getIntExtra("id",0);
        Intent intent1=new Intent(context,PlanActivity.class);
        Log.i("hccccc","alarm content:"+content);
        Log.i("hccccc","alarm id:"+id);


        PendingIntent pendingIntent=PendingIntent.getActivity(context,id,intent1,0);
        NotificationManager manager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel= new NotificationChannel(channelId,name,NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            mChannel.enableLights(true);
            manager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder=new NotificationCompat.Builder(context,channelId)
                .setContentTitle("待办事项")
                .setContentText(content)
                .setSmallIcon(R.drawable.red_alarm_24dp)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

                //.setFullScreenIntent(pendingIntent,true);

        Notification notification=builder.build();


        manager.notify(1,notification);


    }
}
