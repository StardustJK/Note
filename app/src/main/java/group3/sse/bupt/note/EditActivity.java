package group3.sse.bupt.note;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.security.KeyException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EditActivity extends AppCompatActivity {

    private EditText et;
    private Toolbar myToolbar;
    private String content;
    private String time;

    private String old_content="";//传入的Content
    private String old_time="";
    private int old_tag=1;
    private long id=0;
    private int openMode=0;
    private int tag=1;
    private boolean tagChange=false;
    public Intent intent=new Intent();//发送信息的intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        et=findViewById(R.id.et);
        myToolbar=findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar


        Intent getIntent=getIntent();
        openMode=getIntent.getIntExtra("mode",0);

        if(openMode==3){
            id=getIntent.getLongExtra("id",0);
            old_content=getIntent.getStringExtra("content");
            old_time=getIntent.getStringExtra("time");
            old_tag=getIntent.getIntExtra("tag",1);
            et.setText(old_content);
            et.setSelection(old_content.length());//设置光标位置到尾端
        }
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoSetMessage();
                setResult(RESULT_OK,intent);
                finish();

            }
        });

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

        }
        return super.onOptionsItemSelected(item);
    }
}
