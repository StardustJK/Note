package group3.sse.bupt.note;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import group3.sse.bupt.note.Alarm.PlanActivity;

public class RecycleActivity extends BaseActivity {
    private Toolbar myToolbar;
    private TextView tv_content;
    private TextView tv_time;
    private BottomNavigationView BottomNavigation;
    private Context context =this;
    private Intent getIntent;
    public Intent intent=new Intent();//发送信息的intent
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle);
        myToolbar=findViewById(R.id.myToolbar);
        tv_content=findViewById(R.id.tv_content);
        tv_time=findViewById(R.id.tv_time);

        myToolbar.setTitle("");

        if(isNightMode()) myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_keyboard_arrow_left_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_keyboard_arrow_left_black_24dp));
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar

        BottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        BottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecycleActivity.this.finish();
            }
        });
        getIntent=getIntent();//获取启动RecycleActivity时的intent内容
        tv_content.setText(getIntent.getStringExtra("content"));
        tv_time.setText(getIntent.getStringExtra("time"));
    }

    @Override
    protected void needRefresh() {
        setNightMode();
        startActivity(new Intent(this, RecycleActivity.class));
        //overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        finish();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_foreverdelete:
                    new AlertDialog.Builder(RecycleActivity.this)
                            .setMessage("确定永久删除该笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CRUD op = new CRUD(context);
                                    op.open();
                                    Note note=op.getNote(getIntent.getLongExtra("id",0));
                                    long id=getIntent.getLongExtra("id",0);

                                    intent.putExtra("id",id);//id保证一致
                                    intent.putExtra("returnMode",0);//0代表永久删除笔记
                                    setResult(RESULT_OK,intent);
                                    finish();

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();

                    break;
                case R.id.menu_recover:
                    new AlertDialog.Builder(RecycleActivity.this)
                            .setMessage("确定复原该笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CRUD op = new CRUD(context);
                                    op.open();
                                    Note note=op.getNote(getIntent.getLongExtra("id",0));
                                    long id=getIntent.getLongExtra("id",0);
                                    intent.putExtra("content",note.getContent());
                                    intent.putExtra("time",note.getTime());
                                    intent.putExtra("tag",note.getTag());
                                    intent.putExtra("id",id);//id保证一致
                                    intent.putExtra("returnMode",1);//1代表复原笔记
                                    setResult(RESULT_OK,intent);
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

            return false;
        }
    };
}
