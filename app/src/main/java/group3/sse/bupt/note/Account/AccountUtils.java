package group3.sse.bupt.note.Account;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import group3.sse.bupt.note.CloudSync.SyncApplication;
import group3.sse.bupt.note.CloudSync.SyncUtils;

public class AccountUtils {
    /**
     * 账号密码注册
     */
    public static void signUp(final View view,String username,String password) {
        //指定用户对象信息
        final User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "注册成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "尚未失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 账号密码登录
     */
    private void login(final View view,String username,String password) {
        final User user = new User();
        //此处替换为你的用户名
        user.setUsername(username);
        //此处替换为你的密码
        user.setPassword(password);
        user.login(new SaveListener<User>() {
            @Override
            public void done(User bmobUser, BmobException e) {
                if (e == null) {
                    User user = BmobUser.getCurrentUser(User.class);
                    //Snackbar.make(view, "登录成功：" + user.getUsername(), Snackbar.LENGTH_LONG).show();
                    Toast.makeText(SyncApplication.getContext(), "登录成功", Toast.LENGTH_SHORT).show();
                } else {
                    //Snackbar.make(view, "登录失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Toast.makeText(SyncApplication.getContext(), "登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /**
     * 账号密码登录
     */
    public static void loginByAccount(final View view,String username,String password) {
        //此处替换为你的用户名密码
        BmobUser.loginByAccount(username, password, new LogInListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "登录成功：" + user.getUsername(), Snackbar.LENGTH_LONG).show();
                    //同步数据库
                    SyncUtils su=new SyncUtils();
                    su.syncDatabase();
                } else {
                    Snackbar.make(view, "登录失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * 同步控制台数据到缓存中
     * @param view
     */
    private void fetchUserInfo(final View view) {
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    final User myUser = BmobUser.getCurrentUser(User.class);
                    Snackbar.make(view, "更新用户本地缓存信息成功："+myUser.getUsername(), Snackbar.LENGTH_LONG).show();
                } else {
                    Log.e("error",e.getMessage());
                    Snackbar.make(view, "更新用户本地缓存信息失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }


    /**
     * 获取控制台最新JSON数据
     * @param view
     */
    private void fetchUserJsonInfo(final View view) {
        BmobUser.fetchUserJsonInfo(new FetchUserInfoListener<String>() {
            @Override
            public void done(String json, BmobException e) {
                if (e == null) {
                    Log.e("success",json);
                    Snackbar.make(view, "获取控制台最新数据成功："+json, Snackbar.LENGTH_LONG).show();
                } else {
                    Log.e("error",e.getMessage());
                    Snackbar.make(view, "获取控制台最新数据失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 更新用户操作并同步更新本地的用户信息
     */
    private void updateUser(final View view) {
        final User user = BmobUser.getCurrentUser(User.class);
        user.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "更新用户信息成功：" , Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "更新用户信息失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            }
        });
    }


    /**
     * 查询用户表
     */
    private void queryUser(final View view) {
        BmobQuery<User> bmobQuery = new BmobQuery<>();
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "查询成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "查询失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 提供旧密码修改密码
     */
    private void updatePassword(final View view){
        //TODO 此处替换为你的旧密码和新密码
        BmobUser.updateCurrentUserPassword("oldPwd", "newPwd", new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    Snackbar.make(view, "查询成功", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(view, "查询失败：" + e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    //退出登录，同时清除缓存用户对象。
    public static void logOut(){
        BmobUser.logOut();
        //User user=new User();
    }
}
