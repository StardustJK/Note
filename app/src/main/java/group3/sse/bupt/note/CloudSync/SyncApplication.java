package group3.sse.bupt.note.CloudSync;

import android.app.Application;
import android.content.Context;

//这只是一个临时解决方案，因为获取context不方便
public class SyncApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {

        super.onCreate();
        context=getApplicationContext();
    }

    //返回上下文
    public static Context getContext(){
        return context;
    }
}
