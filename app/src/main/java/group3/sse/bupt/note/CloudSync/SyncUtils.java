package group3.sse.bupt.note.CloudSync;


import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import group3.sse.bupt.note.Account.User;
import group3.sse.bupt.note.CRUD;
import group3.sse.bupt.note.Note;
import group3.sse.bupt.note.UserSettingsActivity;

//云同步相关的逻辑
//联网登录状态下，每个操作对应云端操作
//离线登录状态下（有登录缓存就是登录状态），每次操作给笔记加标记，包括增删改三种标记
//离线未登录的不管他
public class SyncUtils extends Application {
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
    public Note addNote(Note note){
        note.setAdd(1);
        note.save(new SaveListener<String>() {
            //异步方法
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Log.i("TEST","云端id是："+objectId);
                    Log.i("SUCCESS","新增笔记到云端，成功！");
                    //Snackbar.make(this, "新增成功：", Snackbar.LENGTH_LONG).show();
                    //如果云端保存成功,note就有了objectid，更新本地数据库
                    //如果新建成功，应该是云端有标识，本地没有
                    note.setAdd(0);
                } else {
                    Log.e("BMOB", e.toString());
                    //Snackbar.make(, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    //没有新建成功，打上标识，等待下一次同步的时候新建
                    Log.e("BMOB", "新增失败");
                }
                CRUD op=new CRUD(SyncApplication.getContext());
                op.open();
                op.updateLocalNote(note);
                op.close();
            }
        });
        Log.i("TEST","出来的云端id是："+note.getObjectId());
        return note;
    }

    //更新笔记
    //传入的参数需要有objectid
    public Note updateNote(Note note){
        //传进来的有objectid，有user
        note.setEdit(1);
        note.update(note.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    //成功就成功了，不用做什么
                    //Snackbar.make(mBtnUpdate, "更新成功", Snackbar.LENGTH_LONG).show();
                    Log.e("BMOB", "更新成功");
                } else {
                    Log.e("BMOB", e.toString());
                    Log.e("BMOB", "更新失败");
                    //不成功的话，添加编辑标记，等下次同步
                    CRUD op=new CRUD(SyncApplication.getContext());
                    op.open();
                    op.updateLocalNote(note);
                    op.close();

                    //Snackbar.make(mBtnUpdate, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return note;
    }

    //删除笔记
    //不是真删
    public Note deleteNote(Note note){
        //传进来的有objectid，有user
        note.setDelete(1);
        note.update(note.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    //成功就成功了，不用做什么
                    //Snackbar.make(mBtnUpdate, "更新成功", Snackbar.LENGTH_LONG).show();
                    Log.e("BMOB", "删除成功");
                } else {
                    Log.e("BMOB", e.toString());
                    Log.e("BMOB", "删除失败");

                    //Snackbar.make(mBtnUpdate, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
        return note;
    }

    //同步数据库，打开应用的时候同步一次，登录成功后同步一次
    public void syncDatabase(){
        if (isLogin()){
            //上传
            //找到所有有新增标记的笔记
            CRUD op=new CRUD(SyncApplication.getContext());
            op.open();
            List<Note> addNotes=op.getAllSignNote(1);
            List<Note> editNotes=op.getAllSignNote(2);
            List<Note> deleteNotes=op.getAllSignNote(3);
            //所有有新增标记的都添加到云端，然后本地删除新增标识
            for (Note noteAdd:addNotes){
                addNote(noteAdd);
                noteAdd.setAdd(0);
                op.updateLocalNote(noteAdd);
            }

            for (Note noteEdit:editNotes){
                BmobQuery<Note> bmobQuery = new BmobQuery<>();
                bmobQuery.getObject(noteEdit.getObjectId(), new QueryListener<Note>() {
                    @Override
                    public void done(Note note1, BmobException e) {
                        if (e == null) {
                            //判断查询到的这个结果是否也有编辑标识
                            CRUD op1=new CRUD(SyncApplication.getContext());
                            op1.open();
                            noteEdit.setEdit(1);
                            op1.updateNote(noteEdit);
                            op1.close();
                            //Snackbar.make(mBtnQuery, "查询成功：" + category.getName(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Log.e("BMOB", e.toString());
                            //Snackbar.make(mBtnQuery, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                noteEdit.setEdit(0);
                op.updateLocalNote(noteEdit);
            }
            //删除同理
            for (Note noteDelete:deleteNotes){
                BmobQuery<Note> bmobQuery = new BmobQuery<>();
                bmobQuery.getObject(noteDelete.getObjectId(), new QueryListener<Note>() {
                    @Override
                    public void done(Note note1, BmobException e) {
                        if (e == null) {
                            //判断查询到的这个结果是否也有删除标识
                            if (note1.getDelete()==0) {
                                CRUD op1 = new CRUD(SyncApplication.getContext());
                                op1.open();
                                noteDelete.setDelete(1);
                                op1.removeNote(noteDelete);
                                op1.close();
                            }
                            //Snackbar.make(mBtnQuery, "查询成功：" + category.getName(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Log.e("BMOB", e.toString());
                            //Snackbar.make(mBtnQuery, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
            //下载
            //先找出所有该用户的笔记
            BmobQuery<Note> noteQuery = new BmobQuery<>();
            //因为笔记的user属性的关联类型，所以不知道能不能用条件查询
            //经过测试，可以这样查询
            noteQuery.addWhereEqualTo("user", SyncUtils.getCurrentUser().getObjectId());
            noteQuery.findObjects(new FindListener<Note>() {
                @Override
                public void done(List<Note> object, BmobException e) {
                    if (e == null) {
                        //Snackbar.make(mBtnEqual, "查询成功：" + object.size(), Snackbar.LENGTH_LONG).show();
                        Log.i("SUCCESS","同步数据库下载查询成功，查到："+object.size());
                        CRUD op1=new CRUD(SyncApplication.getContext());
                        op1.open();
                        //遍历查询结果
                        for (Note note:object){
                            //如果云端有新建标识而本地没有这个笔记，则添加
                            if (note.getAdd()==1){
                                //判断本地数据库是否有这个笔记
                                Log.i("SUCCESS","有新建标识");
                                if (!op1.findNoteByObjectIdExist(note.getObjectId())){
                                    Log.i("SUCCESS","本地数据库没有这条笔记");
                                    note.setAdd(0);
                                    op1.addNote(note);
                                }
                            }

                            //编辑标记
                            if (note.getEdit()==1){
                                Note tmp=op1.findNoteByObjectId(note.getObjectId());
                                if (tmp!=null && tmp.getEdit()==0){
                                    note.setEdit(0);
                                    op1.updateLocalNote(note);
                                }
                            }

                            //删除标记
                            if (note.getDelete()==1){
                                Note tmp=op1.findNoteByObjectId(note.getObjectId());
                                if (tmp!=null && tmp.getDelete()==0){
                                    op1.updateLocalNote(note);
                                }
                            }
                        }
                        op1.close();
                    } else {
                        Log.e("BMOB", e.toString());
                        //Snackbar.make(mBtnEqual, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
            op.close();
        }
    }
}
