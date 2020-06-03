package group3.sse.bupt.note.Alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class AlarmUtils {
    Context context;
    AlarmManager alarmManager;

    public AlarmUtils(Context context){
        this.context=context;
        alarmManager= (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    public void cancelAlarm(Plan p){
        Intent intent=new Intent(context,AlarmReceiver.class);
        Log.i("hcccc","取消提醒的id"+(int)p.getId());
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context,(int)p.getId(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public void startAlarms(List<Plan> plans){
        for(int i=0;i<plans.size();i++)
            startAlarm(plans.get(i));
    }

    //取消多个提醒
    public void cancelAlarms(List<Plan> plans){
        for(int i=0;i<plans.size();i++)
            cancelAlarm(plans.get(i));
    }

    public void startAlarm(Plan p){
        Calendar c=p.getPlanTime();
        if(!c.before(Calendar.getInstance())){
            Intent intent=new Intent(context,AlarmReceiver.class);
            Log.i("hcccc","设置提醒的content"+p.getContent());
            Log.i("hcccc","设置提醒的id"+p.getId());

            intent.putExtra("content",p.getContent());
            intent.putExtra("id",(int)p.getId());
            Log.i("hcccc","intent content"+intent.getExtras().getString("content"));
            Log.i("hcccc","inten id"+intent.getExtras().getInt("id"));
            PendingIntent pendingIntent=PendingIntent.getBroadcast(context,(int)p.getId(),intent,PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
        }
    }

}
