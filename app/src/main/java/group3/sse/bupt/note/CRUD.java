package group3.sse.bupt.note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import group3.sse.bupt.note.Account.AccountUtils;
import group3.sse.bupt.note.CloudSync.SyncUtils;

public class CRUD {
SQLiteOpenHelper dbHandler;
SQLiteDatabase db;

private static final String[] columns={
        NoteDatabase.ID,
        NoteDatabase.CONTENT,
        NoteDatabase.TIME,
        NoteDatabase.TAG,
        NoteDatabase.IfDELETE,
        NoteDatabase.ADD,
        NoteDatabase.EDIT,
        NoteDatabase.DELETE,
        NoteDatabase.USER_ID,
        NoteDatabase.OBJECT_ID
};

    public CRUD(Context context){
    dbHandler=new NoteDatabase(context);
}
    public void open(){
    db=dbHandler.getWritableDatabase();
}
    public void close(){
    dbHandler.close();
}

    //新建笔记
    //在原来代码基础上修改，传入的这个note对象，应该是指定了内容、时间、标签三个属性
    public Note addNote(Note note){
    boolean isLogin = false;
    //判断是否登录，如果登录的话，要给笔记加上用户id。
    if (SyncUtils.isLogin()){
        note.setUser(SyncUtils.getCurrentUser());
        isLogin =true;
    }
    ContentValues contentValues=new ContentValues();
    contentValues.put(NoteDatabase.CONTENT,note.getContent());
    contentValues.put(NoteDatabase.TIME,note.getTime());
    contentValues.put(NoteDatabase.TAG,note.getTag());
    //如果用户是登录状态，可以获取到当前用户，将用户的id存到本地数据中
    if (isLogin){
        contentValues.put(NoteDatabase.USER_ID,note.getUser().getObjectId());
    }
    //插入新数据，数据库自动分配id
    contentValues.put(NoteDatabase.IfDELETE,note.getIf_delete());
    long insertID=db.insert(NoteDatabase.TABLE_NAME,null,contentValues);
    note.setId(insertID);
    //现在note身上有本地id，没有云端id，没有add标识
    if (isLogin){
        Log.i("TEST","是登录状态");
        SyncUtils su=new SyncUtils();
        su.addNote(note);
        //SyncUtils.addNote(note);
    }
    return note;

}


    //通过id查询Note
    public Note getNote(long id){
    Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.ID+"=?",
            new String[]{String.valueOf(id)},null,null,null,null);
    if(cursor!=null){
        cursor.moveToFirst();
    }
    Note e=new Note(cursor.getString(1),cursor.getString(2),cursor.getInt(3),cursor.getInt(4));
    //设置objectid
    e.setObjectId(cursor.getString(9));
    System.out.println("getid"+id);
    return e;
}


    //获取全部笔记
    public List<Note> getAllNotes(){
    Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,null,
            null,null,null,null);
    List<Note> notes=new ArrayList<>();
    if(cursor.getCount()>0){
        while(cursor.moveToNext()){
            //封装Note对象
            Note note=new Note();
            note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
            note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
            note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
            note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
            note.setIf_delete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.IfDELETE)));
            note.setObjectId(cursor.getString(cursor.getColumnIndex(NoteDatabase.OBJECT_ID)));
            note.setAdd(cursor.getInt(cursor.getColumnIndex(NoteDatabase.ADD)));
            note.setEdit(cursor.getInt(cursor.getColumnIndex(NoteDatabase.EDIT)));
            note.setDelete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.DELETE)));
            //排除掉回收站
            if(note.getIf_delete()!=1 && note.getDelete()!=1) {
                notes.add(note);
            }
        }
    }
    return notes;
}


    //获取被删除的笔记
    public List<Note> getDeleteNotes(){
        List<Note> notes=new ArrayList<>();
        Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.IfDELETE+"=1",
                null,null,null,null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                Note note=new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
                note.setIf_delete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.IfDELETE)));
                note.setObjectId(cursor.getString(cursor.getColumnIndex(NoteDatabase.OBJECT_ID)));
                note.setAdd(cursor.getInt(cursor.getColumnIndex(NoteDatabase.ADD)));
                note.setEdit(cursor.getInt(cursor.getColumnIndex(NoteDatabase.EDIT)));
                note.setDelete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.DELETE)));
                if(note.getDelete()!=1) {
                    notes.add(note);
                }
            }
        }
        return notes;
}
    //仅更新本地数据库
    //这个函数是给云同步使用的
    public void updateLocalNote(Note note){
        Log.i("TEST","笔记的ocjectid传到update函数中的值是："+note.getObjectId());
        Log.i("TEST","笔记的id传到update函数中的值是："+note.getId());
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.TAG, note.getTag());
        values.put(NoteDatabase.IfDELETE,note.getIf_delete());
        values.put(NoteDatabase.OBJECT_ID, note.getObjectId());
        values.put(NoteDatabase.ADD, note.getAdd());
        values.put(NoteDatabase.EDIT, note.getEdit());
        values.put(NoteDatabase.DELETE, note.getDelete());
        values.put(NoteDatabase.USER_ID, note.getUser().getObjectId());
        // updating row
        db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?",new String[] { String.valueOf(note.getId())});

    }

    //更新笔记
    public int updateNote(Note note) {
        //传进来的note是有本地id的，本地的本地id不一定跟云端的本地id对应，但是objectid一定是相同的
        //所以要通过本地id在本地数据库查objectid
        Note tmpNote=getNote(note.getId());//创建一个临时note
        note.setObjectId(tmpNote.getObjectId());
        //作者
        if (SyncUtils.isLogin()){
            note.setUser(SyncUtils.getCurrentUser());
        }
        //update the info of an existing note
        ContentValues values = new ContentValues();
        values.put(NoteDatabase.CONTENT, note.getContent());
        values.put(NoteDatabase.TIME, note.getTime());
        values.put(NoteDatabase.TAG, note.getTag());
        values.put(NoteDatabase.IfDELETE,note.getIf_delete());
        System.out.println("更新id"+note.getId());
        // updating row
        int result=db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?",new String[] { String.valueOf(note.getId())});
        //如果登录的话，就同步
        if (SyncUtils.isLogin()){
            SyncUtils su=new SyncUtils();
            su.updateNote(note);
        }
        return result;
    }
    //删除笔记
    public void removeNote(Note note) {
        //不是真删，只是打上删除标识，不显示
        note.setDelete(1);
        Note tmpNote=getNote(note.getId());//创建一个临时note
        note.setObjectId(tmpNote.getObjectId());
        //作者
        if (SyncUtils.isLogin()){
            note.setUser(SyncUtils.getCurrentUser());
        }
        //remove a note according to ID value
        //db.delete(NoteDatabase.TABLE_NAME, NoteDatabase.ID + "=" + note.getId(), null);
        ContentValues values = new ContentValues();
        //values.put(NoteDatabase.CONTENT, note.getContent());
        //values.put(NoteDatabase.TIME, note.getTime());
        //values.put(NoteDatabase.TAG, note.getTag());
        //values.put(NoteDatabase.IfDELETE,note.getIf_delete());
        //删除标识
        values.put(NoteDatabase.DELETE, note.getDelete());
        db.update(NoteDatabase.TABLE_NAME, values,
                NoteDatabase.ID + "=?",new String[] { String.valueOf(note.getId())});
        //如果登录的话，就同步
        if (SyncUtils.isLogin()){
            SyncUtils su=new SyncUtils();
            su.deleteNote(note);
        }
    }

    //删除一个分类下的所有笔记
    //获取某个标签的所有笔记
    public List<Note> getAllNoteByTag(int tag){
        List<Note> notes=new ArrayList<>();
        Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.TAG+"="+tag,
                null,null,null,null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                Note note=new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
                note.setIf_delete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.IfDELETE)));
                notes.add(note);
            }

        //db.delete(NoteDatabase.TABLE_NAME,NoteDatabase.TAG+"="+tag,null);
        }
        return notes;
    }

    //清空回收站
    public void deleteRecycleBin(){
        //给所有ifdelete=1的笔记打上删除标识
        //db.delete(NoteDatabase.TABLE_NAME,NoteDatabase.IfDELETE+"=1",null);

        Cursor cursor=db.query(NoteDatabase.TABLE_NAME,columns,NoteDatabase.IfDELETE+"=1",
                null,null,null,null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                Note note=new Note();
                note.setId(cursor.getLong(cursor.getColumnIndex(NoteDatabase.ID)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteDatabase.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteDatabase.TIME)));
                note.setTag(cursor.getInt(cursor.getColumnIndex(NoteDatabase.TAG)));
                note.setIf_delete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.IfDELETE)));
                note.setObjectId(cursor.getString(cursor.getColumnIndex(NoteDatabase.OBJECT_ID)));
                note.setAdd(cursor.getInt(cursor.getColumnIndex(NoteDatabase.ADD)));
                note.setEdit(cursor.getInt(cursor.getColumnIndex(NoteDatabase.EDIT)));
                note.setDelete(cursor.getInt(cursor.getColumnIndex(NoteDatabase.DELETE)));
                if(note.getDelete()!=1) {
                    removeNote(note);
                }
            }
        }
    }
}
