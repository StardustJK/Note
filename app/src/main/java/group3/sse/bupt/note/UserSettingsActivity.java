package group3.sse.bupt.note;


import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import group3.sse.bupt.note.Account.AccountUtils;
import group3.sse.bupt.note.CloudSync.SyncUtils;

public class UserSettingsActivity extends BaseActivity {

    private Switch nightMode;
    private Switch reverseMode;
    private SharedPreferences sharedPreferences;//偏好设置

    private Context context = this;//当前上下文

    private Button verifySettingButton;//加密笔记验证方式的设置按钮
    private Button syncSettingButton;//云同步设置按钮

    private EditText etun;
    private EditText etpw;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings_layout);

        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getBaseContext());//获取偏好设置
        Intent intent=getIntent();

        initView();
        Toolbar user_setting_toolbar=findViewById(R.id.user_setting_toolbar);
        setSupportActionBar(user_setting_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        if(isNightMode())user_setting_toolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_white_24dp));
        else user_setting_toolbar.setNavigationIcon(getDrawable(R.drawable.ic_settings_black_24dp));

        //设置加密笔记验证方式
        verifySettingButton=findViewById(R.id.verifyMode);
        verifySettingButton.setOnClickListener(verifyModeListener);//设置监听器

        //云同步
        syncSettingButton=findViewById(R.id.cloudSyncAccount);
        syncSettingButton.setOnClickListener(cloudSettingListener);

        //
        //etun=(EditText) findViewById(R.id.AccountEditText);
        //etpw=(EditText)findViewById(R.id.PasswordEditText);
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

    //设置验证方式
    View.OnClickListener verifyModeListener = new View.OnClickListener() {
        String[] items = new String[] { "无","密码验证", "指纹验证", "人脸验证（暂不可用）" };
        @Override
        public void onClick(View view) {
            //读取设置，查看当前是用什么验证方式
            int currentVerifyMode=sharedPreferences.getInt("verify_mode",0);
            //编辑偏好设置
            final SharedPreferences.Editor editor=sharedPreferences.edit();
            AlertDialog dialog = new AlertDialog.Builder(context).setTitle("验证方式")
                    .setCancelable(true)//可以点选择框外部返回
                    .setSingleChoiceItems(items, currentVerifyMode, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();
                            //dialog.dismiss();
                            switch(which){
                                //无
                                case 0:
                                    editor.putInt("verify_mode",0);
                                    editor.commit();
                                    Toast.makeText(context, "验证方式改为无", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    break;
                                //密码
                                case 1:
                                    //先判断之前是否已经有密码
                                    if (sharedPreferences.contains("verify_password")){
                                        //先验证之前的密码
                                        final EditText pet=new EditText(context);
                                        pet.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        AlertDialog passwordVerifyDialog = new AlertDialog.Builder(context).setTitle("修改密码")
                                                .setMessage("请输入旧密码")
                                                .setView(pet)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        //判断密码是否正确
                                                        String input=pet.getText().toString();
                                                        String verifyPassword=sharedPreferences.getString("verify_password",null);

                                                        if (!input.equals(verifyPassword)){
                                                            Toast.makeText(context, "密码不正确", Toast.LENGTH_SHORT).show();

                                                        }else {
                                                            //打开密码设置对话框
                                                            final EditText et = new EditText(context);
                                                            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                                            new AlertDialog.Builder(context).setTitle("设置密码")
                                                                    .setView(et)
                                                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                            String password = et.getText().toString();
                                                                            editor.putString("verify_password", password);
                                                                            editor.putInt("verify_mode", 1);
                                                                            editor.commit();
                                                                            Toast.makeText(context, "验证方式改为密码验证", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).show();

                                                        }

                                                        dialogInterface.dismiss();
                                                    }
                                                }).create();
                                        passwordVerifyDialog.show();
                                    }
                                    else {
                                        //打开设置密码的框
                                        final EditText et = new EditText(context);
                                        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        new AlertDialog.Builder(context).setTitle("设置密码")
                                                .setView(et)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        String password = et.getText().toString();
                                                        editor.putString("verify_password", password);
                                                        editor.putInt("verify_mode", 1);
                                                        editor.commit();
                                                        Toast.makeText(context, "验证方式改为密码验证", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).show();
                                    }
                                    dialog.dismiss();
                                    break;
                                case 2:
                                    editor.putInt("verify_mode",2);
                                    editor.commit();
                                    Toast.makeText(context, "验证方式改为指纹验证", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    break;
                                case 3:
                                    //editor.putInt("verify_mode",3);
                                    //editor.commit();
                                    Toast.makeText(context, "该验证方式暂不可用", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    }).create();
            dialog.show();
        }
    };

    //云端同步设置
    View.OnClickListener cloudSettingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //如果是登录状态，那么点击按钮就是注销
            //如果未登录，点击按钮登录或注册
            if (SyncUtils.isLogin()){
                //退出登录
                AccountUtils.logOut();
                Log.i("TAG","退出登录");
            }else {
                Log.i("TAG","准备打开选择对话框");
                AlertDialog dialog=new AlertDialog.Builder(UserSettingsActivity.this)
                        .setTitle("提示")
                        .setMessage("选择登录还是注册")
                        .setPositiveButton("登录",new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LayoutInflater factory = LayoutInflater
                                        .from(UserSettingsActivity.this);
                                final View DialogView = factory.inflate(
                                        R.layout.login, null);
                                EditText etun= DialogView.findViewById(R.id.LAccountEditText);
                                EditText etpw= DialogView.findViewById(R.id.LPasswordEditText);
                                //Log.i("TAG","读取输入框中的内容");
                                //final EditText etun=findViewById(R.id.AccountEditText);
                                //final EditText etpw=findViewById(R.id.PasswordEditText);
                                AlertDialog loginDialog= new AlertDialog.Builder(UserSettingsActivity.this)
                                        .setTitle("登录")
                                        .setView(DialogView)
                                        .setPositiveButton("登录",new DialogInterface.OnClickListener(){

                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Log.i("TAG","尝试登录");
                                                Log.i("TAG",etun.getText().toString());
                                                AccountUtils.loginByAccount(getWindow().getDecorView().findViewById(R.id.cloudSyncAccount),etun.getText().toString(),etpw.getText().toString());
                                            }
                                        }).create();
                                loginDialog.show();
                            }
                        })
                        .setNegativeButton("注册",new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).create();
                dialog.show();
            }
        }
    };
}
