package group3.sse.bupt.note;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import group3.sse.bupt.note.Alarm.PlanActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.bmob.v3.Bmob;
import group3.sse.bupt.note.CloudSync.SyncUtils;

//主界面
public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener {


    private NoteDatabase dbHelper;
    private NoteAdapter adapter;
    private TagAdapter tagAdapter;
    private List<Note> noteList = new ArrayList<>();
    //private List<HashMap<Long,Note>> noteList2=new ArrayList<>();

    FloatingActionButton btn;
    TextView textView;
    private ListView listView;//一条一条排列
    final String TAG = "tag";
    private Context context = this;
    private Toolbar myToolbar;
    private PopupWindow popupWindow;//弹出菜单
    private PopupWindow popupCover;//蒙版放在弹出菜单下，以便达到打开弹出窗口，底下是灰色的效果
    private ViewGroup customView;
    private ViewGroup coverView;
    private LayoutInflater layoutInflater;//渲染布局的
    private RelativeLayout main;//activity_main
    private WindowManager windowManager;//窗口管理器
    private DisplayMetrics metrics;//手机宽高
    private TextView setting_text;//使设置能点击
    private ImageView setting_image;
    private ImageView recyclebin_image;
    private TextView recyclebin_text;
    private ListView lv_tag;
    private TextView add_tag;
    private ImageView add_tag_image;
    private ImageView allNote_image;
    private TextView allNote;
    private SharedPreferences sharedPreferences;
    //初始化自动创建的标签
    private String defaultTag="未分类_加密";

    //回收站功能

    //手势滑动操作
    private GestureDetector gestureDetector; //手势检测
    private GestureDetector.OnGestureListener onSlideGestureListener = null;//左右滑动手势检测监听器

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //动态申请权限
        checkPermission();

        //初始化BmobSDK
        //Bmob.initialize(this, "706f2bfd8156941cd068ce74cbe48255");

        super.onCreate(savedInstanceState);
        //初始化BmobSDK
        Bmob.initialize(this, "706f2bfd8156941cd068ce74cbe48255");
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("curTag", 0);
        //editor.putBoolean("reverseMode", false);
        editor.commit();

        initView();
        initPrefs();



        //同步数据库
        SyncUtils su=new SyncUtils();
        su.syncDatabase();
    }
    void initView(){
        onSlideGestureListener = new OnSlideGestureListener();
        gestureDetector = new GestureDetector(this, onSlideGestureListener);
        btn = findViewById(R.id.floatingActionButton1);
        listView = findViewById(R.id.listView);
        myToolbar = findViewById(R.id.myToolbar);
        adapter = new NoteAdapter(context, noteList);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(ListViewLongClickListener);

        myToolbar.setTitle("全部笔记");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//设置toolbar代替actionbar
        refreshListView();

        //初始化弹出菜单
        initPopUpView();
        if (super.isNightMode())
            myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_white_24dp));
        else myToolbar.setNavigationIcon(getDrawable(R.drawable.ic_menu_black_24dp)); // 三道杠
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpView();//弹出菜单
            }
        });
        //添加Note按钮
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从主页跳转到编辑笔记界面
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode", 4);//新建笔记
                startActivityForResult(intent, 1);//传回结果
            }
        });

        //底部导航
        BottomNavigationView BottomNavigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        BottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }
    private void initPrefs() {
        //initialize all useful SharedPreferences for the first time the app runs

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (!sharedPreferences.contains("nightMode")) {
            editor.putBoolean("nightMode", false);
            editor.commit();
        }
        if (!sharedPreferences.contains("reverseMode")) {
            editor.putBoolean("reverseMode", false);
            editor.commit();
        }
        if (!sharedPreferences.contains("tagListString")) {
            editor.putString("tagListString", defaultTag);
            editor.commit();
        }
        if(!sharedPreferences.contains("noteTitle")){
            editor.putBoolean("noteTitle", true);
            editor.commit();
        }

    }

    @Override
    protected void needRefresh() {
        setNightMode();
        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra("opMode", 10);
        startActivity(intent);
        overridePendingTransition(R.anim.night_switch, R.anim.night_switch_over);
        popupWindow.dismiss();
        finish();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_bar_note:

                    return true;
                case R.id.bottom_bar_plan:
                    Intent intent=new Intent(MainActivity.this, PlanActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    MainActivity.this.finish();
                    return true;
            }

            return false;
        }
    };

    public void initPopUpView() {
        layoutInflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customView = (ViewGroup) layoutInflater.inflate(R.layout.menu_layout, null);
        coverView = (ViewGroup) layoutInflater.inflate(R.layout.menu_cover, null);
        main = findViewById(R.id.activity_main);
        windowManager = getWindowManager();
        metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

    }

    public void showPopUpView() {
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        //实例化弹出窗口
        popupWindow = new PopupWindow(customView, (int) (width * 0.7), height, true);//把menu_layout做成弹出窗口
        popupCover = new PopupWindow(coverView, width, height, false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));//设置背景色为白色
        popupWindow.setAnimationStyle(R.style.AnimationFade);
        popupCover.setAnimationStyle(R.style.AnimationCover);
        //在主界面加载成功后，显示弹出
        findViewById(R.id.activity_main).post( new Runnable() {
            @Override
            public void run() {
                popupCover.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);
                popupWindow.showAtLocation(main, Gravity.NO_GRAVITY, 0, 0);

                setting_image = customView.findViewById(R.id.menu_setting_image);
                setting_text = customView.findViewById(R.id.menu_setting_text);
                recyclebin_image=customView.findViewById(R.id.menu_recyclebin_image);
                recyclebin_text=customView.findViewById(R.id.menu_recyclebin_text);

                lv_tag = customView.findViewById(R.id.lv_tag);
                add_tag = customView.findViewById(R.id.add_tag);
                add_tag_image = customView.findViewById(R.id.add_tag_image);
                allNote = customView.findViewById(R.id.allNote);
                allNote_image = customView.findViewById(R.id.allNote_image);

                refreshTagList();
                allNote.setOnClickListener(allNoteListener);
                allNote_image.setOnClickListener(allNoteListener);
                add_tag.setOnClickListener(add_tagListener);
                add_tag_image.setOnClickListener(add_tagListener);
                List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
//                Log.d(TAG, "taglist"+tagList);
//                System.out.println(tagList);
//                System.out.println(defaultTag);
                tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
                lv_tag.setAdapter(tagAdapter);

                lv_tag.setOnItemClickListener(lv_tagListener);

                //长按标签，删除标签
                lv_tag.setOnItemLongClickListener(lv_tagLongClickListener);

                setting_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, UserSettingsActivity.class);
                        startActivityForResult(intent, 2);

                    }
                });
                setting_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, UserSettingsActivity.class);
                        startActivityForResult(intent, 2);
                    }
                });
//                recyclebin_image.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(MainActivity.this, RecycleBinActivity.class);
//                        startActivityForResult(intent, 2);
//
//                    }
//                });
//                recyclebin_text.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(MainActivity.this, RecycleBinActivity.class);
//                        startActivityForResult(intent, 2);
//                    }
//                });
                recyclebin_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Note> temp = new ArrayList<>();
                        CRUD op = new CRUD(context);
                        op.open();
                        temp.addAll(op.getDeleteNotes());
                        op.close();

                        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                        listView.setAdapter(tempAdapter);
                        myToolbar.setTitle("回收站");
                        //将当前的标签写入
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putInt("curTag",-1);//curTag=-1代表回收站
//                        editor.commit();
                        popupWindow.dismiss();

                    }
                });
                recyclebin_text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<Note> temp = new ArrayList<>();
                        CRUD op = new CRUD(context);
                        op.open();
                        temp.addAll(op.getDeleteNotes());

                        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
                        listView.setAdapter(tempAdapter);
                        myToolbar.setTitle("回收站");
                        //将当前的标签写入
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putInt("curTag",-1);//curTag=-1代表回收站
//                        editor.commit();
                        popupWindow.dismiss();

                    }
                });
                //点击了coverView后关闭弹窗
                coverView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;

                    }
                });
                //弹窗关闭后关闭蒙版
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        popupCover.dismiss();
                    }
                });
            }
        });
    }

    //接收startActivityForResult的结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                int returnMode;//-1代表什么都不干，0代表新建笔记，1代表编辑当前笔记
                long note_id;
                returnMode = data.getExtras().getInt("mode", -1);
                note_id = data.getExtras().getLong("id", 0);

                if (returnMode == 0) {
                    //新建笔记
                    String content = data.getExtras().getString("content");
                    String time = data.getExtras().getString("time");
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, tag,0);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.addNote(newNote);
                    op.close();
                } else if (returnMode == 1) {
                    //更新笔记
                    //内容
                    String content = data.getExtras().getString("content");
                    //时间
                    String time = data.getExtras().getString("time");
                    //标签
                    int tag = data.getExtras().getInt("tag", 1);
                    Note newNote = new Note(content, time, tag,0);
                    newNote.setId(note_id);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.updateNote(newNote);
                    op.close();
                } else if (returnMode == 2) {//删除
                    //把笔记移到回收站
                    Note curNote = new Note();
                    curNote.setId(note_id);
                    curNote.setContent(data.getExtras().getString("content"));
                    curNote.setTag(data.getExtras().getInt("tag", 1));
                    curNote.setTime(data.getExtras().getString("time"));
                    curNote.setIf_delete(1);
                    CRUD op = new CRUD(context);
                    op.open();
                    //op.removeNote(curNote);
                    op.updateNote(curNote);
                    op.close();

                }
                int curTag = sharedPreferences.getInt("curTag", 1);
                if (curTag == 0)
                    refreshListView();//更改完就刷新一次
                else refreshTagListView(curTag);
                super.onActivityResult(requestCode, resultCode, data);
                break;
            case 2://从设置返回到全部笔记，并能自动刷新
                if(data!=null){
                    boolean reverseMode= Objects.requireNonNull(data.getExtras()).getBoolean("reverseMode",false);
                    if(reverseMode)refreshListView();
                }
                break;
            case 3://从recycleActivity返回到回收站，刷新回收站
                if(data!=null){
                int returnMode_r;
                returnMode_r=data.getExtras().getInt("returnMode", 0);
                long id=data.getExtras().getLong("id", 0);
                System.out.println("刷新回收站"+id);
                System.out.println("returnmode"+returnMode_r);
                if(returnMode_r==0){
                    Note note = new Note();
                    note.setId(id);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.removeNote(note);
                    op.close();
                    refreshRecycleBin();
                }
                else{
                    Note note=new Note();
                    note.setId(id);
                    note.setContent(data.getExtras().getString("content"));
                    note.setTag(data.getExtras().getInt("tag", 1));
                    note.setTime(data.getExtras().getString("time"));
                    note.setIf_delete(0);
                    CRUD op = new CRUD(context);
                    op.open();
                    op.updateNote(note);
                    op.close();
                    refreshRecycleBin();
                }
                break;

        }
        }

    }

    //刷新笔记列表
    public void refreshListView() {
        CRUD op = new CRUD(context);
        op.open();
        if (noteList.size() > 0) {
            noteList.clear();
        }

        noteList.addAll(op.getAllNotes());
        //如果未设置正序显示
        if (!sharedPreferences.getBoolean("reverseMode", false))
            Collections.reverse(noteList);
        myToolbar.setTitle("全部笔记");
        adapter = new NoteAdapter(context, noteList);
        listView.setAdapter(adapter);
        op.close();
        adapter.notifyDataSetChanged();
    }

    //刷新标签中的笔记列表
    public void refreshTagListView(int tag) {
        CRUD op = new CRUD(context);
        op.open();
        if (noteList.size() > 0) noteList.clear();
        noteList.addAll(op.getAllNotes());
        op.close();
        List<Note> temp = new ArrayList<>();
        for (int i = 0; i < noteList.size(); i++) {
            if (noteList.get(i).getTag() == tag) {
                Note note = noteList.get(i);
                temp.add(note);
            }
        }
        //如果未设置正序显示
        if (!sharedPreferences.getBoolean("reverseMode", false))
            Collections.reverse(temp);

        NoteAdapter tempAdapter = new NoteAdapter(context, temp);
        listView.setAdapter(tempAdapter);

    }
    //刷新回收站
    private void refreshRecycleBin(){
        CRUD op = new CRUD(context);
        op.open();
        List<Note> temp=new ArrayList<>();
        temp.addAll(op.getDeleteNotes());
        adapter = new NoteAdapter(context, temp);
        listView.setAdapter(adapter);

    }
    //刷新标签列表
    private void refreshTagList() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
        tagAdapter = new TagAdapter(context, tagList, numOfTagNotes(tagList));
        lv_tag.setAdapter(tagAdapter);
        tagAdapter.notifyDataSetChanged();

    }

    //监听主页面笔记列表中某个元素的点击
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.listView:
                Note curNote = (Note) parent.getItemAtPosition(position);//当前笔记
                if(myToolbar.getTitle()!="回收站"){
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra("content", curNote.getContent());
                    intent.putExtra("id", curNote.getId());
                    intent.putExtra("time", curNote.getTime());
                    intent.putExtra("mode", 3);//编辑一个已有笔记模式
                    intent.putExtra("tag", curNote.getTag());
                    startActivityForResult(intent, 1);//从编辑页面返回结果
                    break;
                }else{
                    Intent intent = new Intent(MainActivity.this, RecycleActivity.class);
                    intent.putExtra("content", curNote.getContent());
                    intent.putExtra("id", curNote.getId());
                    intent.putExtra("time", curNote.getTime());
                    intent.putExtra("mode", 100);//点击进入回收站的笔记的模式
                    intent.putExtra("tag", curNote.getTag());
                    startActivityForResult(intent,3);

                    break;
                }
        }

    }

    //长按笔记列表中某个元素，删除该笔记
    AdapterView.OnItemLongClickListener ListViewLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
            switch (parent.getId()) {
                case R.id.listView:

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("确定删除该笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Note curNote = (Note) parent.getItemAtPosition(position);//当前笔记
                                    if(myToolbar.getTitle()!="回收站"){
                                        //如果笔记不在回收站，将笔记移至回收站
                                        curNote.setId(curNote.getId());
                                        curNote.setIf_delete(1);
                                        CRUD op = new CRUD(context);
                                        op.open();
                                        op.updateNote(curNote);
                                        op.close();
                                        int curTag = sharedPreferences.getInt("curTag", 1);

                                        if (curTag == 0) refreshListView();
                                        else refreshTagListView(curTag);

                                }else{
                                        //如果在，删除笔记
                                        CRUD op = new CRUD(context);
                                        op.open();
                                        op.removeNote(curNote);
                                        op.close();
                                        refreshRecycleBin();

                                    }

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();

                    return true;
            }

            return false;
        }
    };
    @Override//主页面的toolbar
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //search setting
        MenuItem mSearch = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView mSearchView = (androidx.appcompat.widget.SearchView) mSearch.getActionView();

        mSearchView.setQueryHint("Search");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });



        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_clear:
                if(myToolbar.getTitle()!="回收站"){
                final int curTag = sharedPreferences.getInt("curTag", 1);
                if (curTag == 0) {//curTag=0代表全部笔记
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("确定删除全部笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    dbHelper = new NoteDatabase(context);
//                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
//                                    db.delete("notes", null, null);
//                                    db.execSQL("update sqlite_sequence set seq=0 where name='notes'");//设置笔记id从0开始
                                    CRUD op = new CRUD(context);
                                    op.open();
                                    List<Note> temp = new ArrayList<>();
                                    temp.addAll(op.getAllNotes());
                                    for(int i=0;i<temp.size();i++){
                                        temp.get(i).setIf_delete(1);
                                        op.updateNote(temp.get(i));
                                    }
                                    op.close();
                                    refreshListView();

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();
                } else {//curTag不为0时代表当前在分类下
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("确定删除该分类下全部笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    CRUD op = new CRUD(context);
                                    op.open();
                                    List<Note> temp=new ArrayList<>();
                                    temp.addAll(op.getAllNoteByTag(curTag));
                                    for(int i=0;i<temp.size();i++){
                                        temp.get(i).setIf_delete(1);
                                        op.updateNote(temp.get(i));
                                    }
                                    op.close();
                                    if (curTag == 0)
                                        refreshListView();
                                    else refreshTagListView(curTag);

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();
                }
                break;

        }
        else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("确定删除回收站里全部笔记吗？")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                    CRUD op = new CRUD(context);
                    op.open();
                    op.deleteRecycleBin();
                    op.close();
                    refreshRecycleBin();
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();//关闭对话框
                        }
                    }).create().show();
                }


        }

        return super.onOptionsItemSelected(item);
    }



    //统计不同标签的笔记数
    public List<Integer> numOfTagNotes(List<String> noteStringList) {
        Integer[] numbers = new Integer[noteStringList.size()];
        for (int i = 0; i < numbers.length; i++) numbers[i] = 0;
        for (int i = 0; i < noteList.size(); i++) {
            numbers[noteList.get(i).getTag() - 1]++;
        }
        return Arrays.asList(numbers);
    }

    private void resetTagsX(AdapterView<?> parent) {
        for (int i = 5; i < parent.getCount(); i++) {
            View view = parent.getChildAt(i);
            if (view.findViewById(R.id.delete_tag).getVisibility() == View.VISIBLE) {
                float length = 0;
                TextView blank = view.findViewById(R.id.blank_tag);
                blank.animate().translationX(length).setDuration(300).start();
                TextView text = view.findViewById(R.id.text_tag);
                text.animate().translationX(length).setDuration(300).start();
                ImageView del = view.findViewById(R.id.delete_tag);
                //del.setVisibility(GONE);
                del.animate().translationX(length).setDuration(300).start();
            }
        }
    }

    //点击全部笔记监听器
    View.OnClickListener allNoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            refreshListView();
            listView.setAdapter(adapter);
            myToolbar.setTitle("全部笔记");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("curTag", 0);
            editor.commit();
            popupWindow.dismiss();
        }
    };
    //点击添加标签监听器
    View.OnClickListener add_tagListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final EditText et = new EditText(context);
            new AlertDialog.Builder(context).setTitle("新建笔记分类")
                    .setIcon(R.drawable.ic_turned_in_not_black_24dp)
                    .setView(et)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //按下确定键后的事件
                            String name = et.getText().toString();
                            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
                            if (!tagList.contains(name)) {
                                String oldTagListString = sharedPreferences.getString("tagListString", defaultTag);
                                String newTagListString = oldTagListString + "_" + name;
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("tagListString", newTagListString);
                                editor.commit();
                                refreshTagList();
                                //Toast.makeText(getApplicationContext(), et.getText().toString(),Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "标签重复！ ", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).setNegativeButton("取消", null).show();
        }
    };
    //点击标签分类Item监听器
    AdapterView.OnItemClickListener lv_tagListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
            int tag = position + 1;
            List<Note> temp = new ArrayList<>();
            for (int i = 0; i < noteList.size(); i++) {
                if (noteList.get(i).getTag() == tag) {
                    Note note = noteList.get(i);
                    temp.add(note);
                }
            }
            NoteAdapter tempAdapter = new NoteAdapter(context, temp);
            listView.setAdapter(tempAdapter);
            myToolbar.setTitle(tagList.get(position));
            //将当前的标签写入
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("curTag", tag);
            editor.commit();
            popupWindow.dismiss();
            Log.d(TAG, position + "");
        }
    };
    //长按标签分类监听器，删除该标签,不能删除未分类标签
    AdapterView.OnItemLongClickListener lv_tagLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            if (id > 1) {
                float length = getResources().getDimensionPixelSize(R.dimen.distance);
                TextView blank = view.findViewById(R.id.blank_tag);
                blank.animate().translationX(length).setDuration(300).start();
                TextView text = view.findViewById(R.id.text_tag);
                text.animate().translationX(length).setDuration(300).start();
                ImageView del = view.findViewById(R.id.delete_tag);
                del.setVisibility(View.VISIBLE);
                del.animate().translationX(length).setDuration(300).start();

                del.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setMessage("该分类下的所有笔记的分类将变为未分类!")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int tag = position + 1;
                                        for (int i = 0; i < noteList.size(); i++) {
                                            //被删除tag的对应notes tag = 1
                                            Note temp = noteList.get(i);
                                            if (temp.getTag() == tag) {
                                                temp.setTag(1);
                                                CRUD op = new CRUD(context);
                                                op.open();
                                                op.updateNote(temp);
                                                op.close();
                                            }
                                        }
                                        List<String> tagList = Arrays.asList(sharedPreferences.getString("tagListString", defaultTag).split("_")); //获取tags
                                        if (tag + 1 < tagList.size()) {
                                            for (int j = tag + 1; j < tagList.size() + 1; j++) {
                                                //大于被删除的tag的所有tag减一
                                                for (int i = 0; i < noteList.size(); i++) {
                                                    Note temp = noteList.get(i);
                                                    if (temp.getTag() == j) {
                                                        temp.setTag(j - 1);
                                                        CRUD op = new CRUD(context);
                                                        op.open();
                                                        op.updateNote(temp);
                                                        op.close();
                                                    }
                                                }
                                            }
                                        }
                                        //edit the preference
                                        List<String> newTagList = new ArrayList<>();
                                        newTagList.addAll(tagList);
                                        newTagList.remove(position);
                                        String newTagListString = TextUtils.join("_", newTagList);
                                        Log.d(TAG, "onClick: " + newTagListString);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("tagListString", newTagListString);
                                        editor.commit();
                                        //刷新分类列表
                                        tagAdapter = new TagAdapter(context, newTagList, numOfTagNotes(newTagList));
                                        lv_tag.setAdapter(tagAdapter);
                                        myToolbar.setTitle("未分类");
                                        refreshTagListView(1);
                                        popupWindow.dismiss();

                                    }
                                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                            }
                        }).create().show();
                    }
                });

                return true;
            }
            return false;
        }
    };


    //动态申请
    //分享功能需要用到
    //新版的API中文件读写被视作危险权限，在配置文件中不生效，需要启动程序时动态申请
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,Manifest.permission.WAKE_LOCK

    };
    List<String> mPermissionList = new ArrayList<>();

    // private ImageView welcomeImg = null;
    private static final int PERMISSION_REQUEST = 1;
    // 检查权限
    private void checkPermission() {
        mPermissionList.clear();

        //判断哪些权限未授予
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        //判断是否为空
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了

        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, PERMISSION_REQUEST);
        }
    }

    //响应授权
    //这里不管用户是否拒绝，都进入首页，不再重复申请权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST:

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }
    //将touch动作事件交由手势检测监听器来处理
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }


    /*********************************************
     * 左右滑动手势监听器
     ********************************************/
    private class OnSlideGestureListener implements GestureDetector.OnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // TODO Auto-generated method stub
            System.out.println("长按");
            Log.d(TAG, "onLongPress: ");

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY)
        {
            // 参数解释：
            // e1：第1个ACTION_DOWN MotionEvent
            // e2：最后一个ACTION_MOVE MotionEvent
            // velocityX：X轴上的移动速度，像素/秒
            // velocityY：Y轴上的移动速度，像素/秒
            // 触发条件 ：
            // X轴的坐标位移大于FLING_MIN_DISTANCE，且移动速度大于FLING_MIN_VELOCITY个像素/秒
            if ((e1 == null) || (e2 == null)){
                return false;
            }
            int FLING_MIN_DISTANCE = 100;
            int FLING_MIN_VELOCITY = 100;
            if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                    && Math.abs(velocityX) > FLING_MIN_VELOCITY)
            {
                // 向左滑动
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, PlanActivity.class);
                startActivity(intent);
                //overridePendingTransition(R.anim.move_right_in, R.anim.move_left_out);
                overridePendingTransition(0, 0);
                finish();
            } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
//此处也可以加入对滑动速度的要求
//			             && Math.abs(velocityX) > FLING_MIN_VELOCITY
            )
            {
                // 向右滑动打开弹出菜单
                showPopUpView();

            }
            return false;
        }
    }
}

