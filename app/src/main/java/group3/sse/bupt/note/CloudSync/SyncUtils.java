package group3.sse.bupt.note.CloudSync;


import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import group3.sse.bupt.note.Account.User;
import group3.sse.bupt.note.Note;

//云同步相关的逻辑
//联网登录状态下，每个操作对应云端操作
//离线登录状态下（有登录缓存就是登录状态），每次操作给笔记加标记，包括增删改三种标记
//离线未登录的不管他
public class SyncUtils {
    //判断当前是否有用户登录
    public static boolean isLogin(){
        if (BmobUser.isLogin()) {
            User user = BmobUser.getCurrentUser(User.class);
            //Snackbar.make(view, "已经登录：" + user.getUsername(), Snackbar.LENGTH_LONG).show();
            return true;
        } else {
            //Snackbar.make(view, "尚未登录", Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    //获取当前用户
    public static User getCurrentUser(){
        return BmobUser.getCurrentUser(User.class);
    }

    //新建笔记
    public static Note addNote(Note note){
        note.save(new SaveListener<String>() {
            //异步方法
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Log.i("TEST","云端id是："+objectId);
                    Log.i("SUCCESS","新增笔记到云端，成功！");
                    //Snackbar.make(this, "新增成功：", Snackbar.LENGTH_LONG).show();
                } else {
                    Log.e("BMOB", e.toString());
                    //Snackbar.make(, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    //没有新建成功，打上标识，等待下一次同步的时候新建
                    note.setAdd(1);
                }
            }
        });
        Log.i("TEST","出来的云端id是："+note.getObjectId());
        return note;
    }
}
