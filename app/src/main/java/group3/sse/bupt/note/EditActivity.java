package group3.sse.bupt.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.security.KeyException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.mob.MobSDK;

import cn.sharesdk.onekeyshare.OnekeyShare;
import group3.sse.bupt.note.Share.Clipboard;
import group3.sse.bupt.note.Share.Screenshot;

public class EditActivity extends AppCompatActivity {
    private EditText et;
    private Toolbar myToolbar;
    private String content;
    private String time;
    private Context context = this;

    private String old_content="";//传入的Content
    private String old_time="";
    private int old_tag=1;
    private long id=0;
    private int openMode=0;
    private int tag=1;
    private boolean tagChange=false;
    public Intent intent=new Intent();//发送信息的intent
    //初始化自动创建的标签
    private String defaultTag="未分类_加密";

    //指纹认证需要用到的一些变量
    private static final String TAG = "gryphon";
    private BiometricPrompt mBiometricPrompt;
    private CancellationSignal mCancellationSignal;
    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback;

    //因为用到了指纹认证，所以规定了最低版本的API
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        et=findViewById(R.id.et);
        myToolbar=findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        final Spinner tagSpinner = (Spinner)findViewById(R.id.tag_spinner);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<String>(this,R.layout.simple_spinner_item, tagList);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(tagAdapter);
        int returnTag=sharedPreferences.getInt("curTag",1);
        tagSpinner.setSelection(returnTag-1);//将spinner设为当前分类




        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tag = (int)id + 1;
                tagChange = true;
                tagSpinner.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent getIntent=getIntent();//获取启动EditActivity时的intent内容
        openMode=getIntent.getIntExtra("mode",0);

        if(openMode==3){
            id=getIntent.getLongExtra("id",0);
            old_content=getIntent.getStringExtra("content");
            old_time=getIntent.getStringExtra("time");
            old_tag=getIntent.getIntExtra("tag",1);
            et.setText(old_content);
            et.setSelection(old_content.length());//设置光标位置到尾端
            tagSpinner.setSelection(old_tag-1);
        }
        //点击toolbar上的返回键，自动保存笔记内容并返回到主页面
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSetMessage();
                setResult(RESULT_OK,intent);
                finish();

            }
        });


        //加密标签的int值是2
        int verifyMode=sharedPreferences.getInt("verify_mode",0);
        if(old_tag==2 ){
            //密码认证
            if (verifyMode==1) {
                final EditText et=new EditText(context);
                et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                AlertDialog passwordVerifyDialog = new AlertDialog.Builder(this).setTitle("密码验证")
                        .setMessage("请输入密码")
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //判断密码是否正确
                                String input=et.getText().toString();
                                String verifyPassword=sharedPreferences.getString("verify_password",null);

                                if (!input.equals(verifyPassword)){
                                    autoSetMessage();
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    Toast.makeText(context, "密码不正确", Toast.LENGTH_SHORT).show();

                                }

                                dialogInterface.dismiss();
                            }
                        }).create();
                passwordVerifyDialog.setCancelable(false);
                passwordVerifyDialog.getWindow().setDimAmount(1.0f);
                passwordVerifyDialog.show();
            }
            //指纹认证
            else if (verifyMode==2) {
                mBiometricPrompt = new BiometricPrompt.Builder(this)
                        .setTitle("指纹验证")
                        .setDescription("验证的是系统锁屏设置的指纹")
                        .setNegativeButton("取消", getMainExecutor(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                autoSetMessage();
                                setResult(RESULT_OK, intent);
                                finish();
                                Log.i(TAG, "Cancel button clicked");
                            }
                        })
                        .build();

                mCancellationSignal = new CancellationSignal();

                mCancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                    @Override
                    public void onCancel() {
                        //handle cancel result
                        Log.i(TAG, "Canceled");
                    }
                });


                mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        autoSetMessage();
                        setResult(RESULT_OK, intent);
                        finish();
                        Log.i(TAG, "onAuthenticationError " + errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);

                        Log.i(TAG, "onAuthenticationSucceeded " + result.toString());
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();

                        Log.i(TAG, "onAuthenticationFailed ");
                    }
                };

                mBiometricPrompt.authenticate(mCancellationSignal, getMainExecutor(), mAuthenticationCallback);
            }
        }

    }

    //判断按键情况
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_HOME){
            return true;
        }
        else if(keyCode==KeyEvent.KEYCODE_BACK){
            autoSetMessage();
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    public String dateToStr(){
        Date date=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }
    public void autoSetMessage(){
        if(openMode==4){
            if(et.getText().toString().length()==0){
                intent.putExtra("mode",-1);//什么都没发生
            }
            else{
                intent.putExtra("mode",0);//新建笔记
                intent.putExtra("content",et.getText().toString());
                intent.putExtra("time",dateToStr());
                intent.putExtra("tag",tag);

            }
        }
        else{
            if(et.getText().toString().equals(old_content)&&!tagChange){
                intent.putExtra("mode",-1);//什么都没有修改
            }
            else{
                intent.putExtra("mode",1);//修改笔记
                intent.putExtra("content",et.getText().toString());
                intent.putExtra("time",dateToStr());
                intent.putExtra("tag",tag);
                intent.putExtra("id",id);//id保证一致

            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()){
            case R.id.menu_delete:
                new AlertDialog.Builder(EditActivity.this)
                        .setMessage("确定删除吗？")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(openMode==4){
                                    intent.putExtra("mode",-1);
                                    setResult(RESULT_OK,intent);
                                }
                                else{
                                    intent.putExtra("mode",2);
                                    intent.putExtra("id",id);
                                    setResult(RESULT_OK,intent);
                                }
                                finish();
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();//关闭对话框
                    }
                }).create().show();
                break;

            case R.id.menu_copy:
                //将笔记内容复制到剪切板
                Clipboard.CopyTextToClipboard(context,et.getText().toString());
                Toast.makeText(context, "笔记内容已复制到剪切板", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menu_screenshot:
                //将笔记保存为图片
                Bitmap bmp=Screenshot.getViewBitmap(findViewById(R.id.et));
                Screenshot.savingBitmapIntoFile(bmp,this);
                Toast.makeText(context, "图片已保存到手机", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_share:
                OnekeyShare oks = new OnekeyShare();
                // title标题，微信、QQ和QQ空间等平台使用
                //oks.setTitle("标题");
                // titleUrl QQ和QQ空间跳转链接
                //oks.setTitleUrl("http://sharesdk.cn");
                // text是分享文本，所有平台都需要这个字段
                oks.setText(et.getText().toString());
                // setImageUrl是网络图片的url
                //oks.setImageUrl("https://hmls.hfbank.com.cn/hfapp-api/9.png");
                // url在微信、Facebook等平台中使用
                //oks.setUrl("http://sharesdk.cn");
                // 启动分享GUI
                oks.show(MobSDK.getContext());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
