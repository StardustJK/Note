package group3.sse.bupt.note;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;

public class UserSettingsActivity extends BaseActivity {

    private Switch nightMode;
    private Switch reverseMode;
    private SharedPreferences sharedPreferences;
    private static boolean night_change;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings_layout);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Intent intent=getIntent();
        if(intent.getExtras() != null) night_change = intent.getBooleanExtra("night_change", false);
        else night_change = false;
        Log.d(TAG, "UserSetting onCreate: "+night_change);

        initView();
        Toolbar user_setting_toolbar=findViewById(R.id.user_setting_toolbar);
        setSupportActionBar(user_setting_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        if(isNightMode())user_setting_toolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_white_24dp));
        else user_setting_toolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_black_24dp));
    }

    public void initView(){
        nightMode=findViewById(R.id.nightMode);
        reverseMode=findViewById(R.id.reverseMode);
        nightMode.setChecked(sharedPreferences.getBoolean("nightMode",false));
        reverseMode.setChecked(sharedPreferences.getBoolean("reverseMode",false));
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setNightModePref(isChecked);
                setSelfNightMode();
            }
        });
        reverseMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setReverseModePref(isChecked);
                setSelfReverseMode();

            }
        });


    }

    //设置黑夜模式，写进sharedPreference
    private void setNightModePref(boolean night){
        //通过nightMode switch 修改pref中的nightMode
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("nightMode",night);
        editor.commit();
    }

    //重启Activity
    private void setSelfNightMode(){
        //重新赋值并重启本Activity
        super.setNightMode();
        Intent intent=new Intent(this,UserSettingsActivity.class);
        intent.putExtra("night_change", !night_change); //重启一次，正负颠倒。最终为正值时重启MainActivity。

        startActivity(intent);
        finish();//结束之前的设置界面
    }

    //设置时间正序显示，写进sharedPreferences
    private void setReverseModePref(boolean reverse){
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("reverseMode",reverse);
        editor.commit();
    }
    //从本Activity跳回主页
    private void setSelfReverseMode(){
//        Intent intent=new Intent(this,MainActivity.class);
//        intent.putExtra("reverseMode",true);
//        startActivityForResult(intent,1);

        Intent intent=new Intent();
        intent.putExtra("reverseMode",true);
        setResult(RESULT_OK,intent);
        finish();//结束之前的设置界面
    }
    @Override
    protected void needRefresh() {

    }
}
