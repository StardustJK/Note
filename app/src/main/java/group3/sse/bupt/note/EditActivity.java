package group3.sse.bupt.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.biometrics.BiometricPrompt;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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


    //开始录音、结束录音
    private Button record_start;
    private Button record_stop;
    //录音名称、录音存放路径
    private String fileName;
    private String filePath;
    //录音器
    private MediaRecorder mediaRecorder;
    //选择照片
    private Button choose_photo;
    //处理后的文本
    private SpannableString oldToSpan;

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
        //可以点击播放语音
        et.setMovementMethod(LinkMovementMethod.getInstance());

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
            oldToSpan = ContentToSpanstr.Content_to_SpanStr(context, old_content);
            et.append(oldToSpan);
           // et.setText(old_content);
            //et.setSelection(old_content.length());//设置光标位置到尾端
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

        record_start = (Button) findViewById(R.id.record_start);
        record_stop = (Button) findViewById(R.id.record_stop);

        //设置按钮可否点击
        record_start.setEnabled(true);
        record_stop.setEnabled(false);
        record_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置按钮可否点击
                record_start.setEnabled(false);
                record_stop.setEnabled(true);

                //创建文件名和路径名
                fileName = DateFormat.format("yyyyMMdd_HH：mm：ss", Calendar.getInstance(Locale.CHINA)) + ".mp3";
                filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName;

                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //设置文件输出格式THREE_GPP(3gp格式，H263视频/ARM音频编码)
                // MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样
                mediaRecorder.setOutputFile(filePath);//设置路径
                try {
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaRecorder.start();

                //et.append("\uD83C\uDFA4");
                //et.append("\n");

                //音频在edittext存储的路径
                String voice = "<voice src='" + filePath + "'/>";
                SpannableString spanStr = new SpannableString(voice);

                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        //实现点击事件
                        MediaPlayer mp = new MediaPlayer();
                        try {
                            mp.setDataSource(filePath);
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if (mediaPlayer != null) {
                                    mediaPlayer.stop();
                                    mediaPlayer.release();
                                }
                            }
                        });
                    }
                };
                spanStr.setSpan(clickableSpan, 0, voice.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                ImageSpan imageSpan=new ImageSpan(context,R.drawable.voice);
                spanStr.setSpan(imageSpan,0,voice.length(),Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                et.append(spanStr);
            }
        });

//结束录音
        record_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //设置按钮可否点击
                record_start.setEnabled(true);
                record_stop.setEnabled(false);
                //结束录音
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;

            }
        });

        choose_photo = (Button) findViewById(R.id.choose_photo);
        choose_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, 1);
            }
        });


        //加密标签的int值是2
        int verifyMode=sharedPreferences.getInt("verify_mode",0);
        if(old_tag==2 ){
            //密码认证
            if (verifyMode==1) {
                final EditText et=new EditText(context);
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
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1000 || height > 2000) {
                // 缩小到十分之一
                float scaleWidth = (float) 0.2;
                float scaleHeight = (float) 0.2;
                //取得想要缩放的matrix参数
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                // 得到新的图片
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }
            //选取照片存储在edittext的路径
            String photopath = "<photo src='" + data.getData() + "'/>";
            //String photopath = "<photo src='" + data.getData().toString() + "'/>";
            SpannableString spanStr = new SpannableString(photopath);
            ImageSpan imageSpan = new ImageSpan(this, bitmap);
            spanStr.setSpan(imageSpan, 0, photopath.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            et.append(spanStr);
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
