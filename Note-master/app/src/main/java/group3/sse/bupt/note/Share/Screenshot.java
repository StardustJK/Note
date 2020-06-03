package group3.sse.bupt.note.Share;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Screenshot {
    //获取一个view的bitmap
    public static Bitmap getViewBitmap(View view){
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        return drawBg4Bitmap(Color.WHITE,bmp);
    }

    public static void savingBitmapIntoFile(final Bitmap pic, final Activity context) {
        //判断上下文是否存在
        if (context == null || context.isFinishing()) {
            return;
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 获取当前时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-ms", Locale.getDefault());
                String data = sdf.format(new Date());
                // 获取内存路径
                // 设置图片路径+命名规范
                // 声明输出文件
                /**
                String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                String fileTitle = "Screenshot_" + data + "_group3.sse.bupt.note.jpg";
                String filePath = storagePath + "/DCIM/";
                final String fileAbsolutePath = filePath + fileTitle;//绝对路径
                 **/

                String SavePath = getSDCardPath() + "/note-master/ScreenImages";
                Log.d("debug","SavePath = "+SavePath);
                File path = new File(SavePath);
                String filePath = SavePath + "/Screenshot_" + data + "_group3.sse.bupt.note.png";
                Log.d("debug","filepath = "+filePath);

                File file = new File(filePath);
                if (!path.exists()) {
                    Log.d("debug","path is not exists");
                    path.mkdirs();
                }

                try {
                    if (!file.exists()) {
                        Log.d("debug","file create new ");
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    pic.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    long file_size = file.length() / 1024; // size of file(KB)
                    if (file_size < 0 || !(file.exists())) {
                        // 零级： 文件判空
                        throw new NullPointerException();
                    } else {
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
                    }
                    // 注销fos;
                    fos.flush();
                    fos.close();
                    Log.d("debug","save ok");
                    //Toast.makeText(context, "图片已保存到"+filePath, Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            });
        thread.start();
    }

    //获取SDCard的目录路径功能
    private static String getSDCardPath() {
        File sdcardDir = null;
        // 判断SDCard是否存在
        boolean sdcardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdcardExist) {
            sdcardDir = Environment.getExternalStorageDirectory();
        }
        return sdcardDir.toString();
    }

    //给bitmap添加背景颜色
    public static Bitmap drawBg4Bitmap(int color, Bitmap originBitmap) {
        Paint paint = new Paint();
        paint.setColor(color);
        Bitmap bitmap = Bitmap.createBitmap(originBitmap.getWidth(),
                originBitmap.getHeight(), originBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0, 0, originBitmap.getWidth(), originBitmap.getHeight(), paint);
        canvas.drawBitmap(originBitmap, 0, 0, paint);
        return bitmap;
    }
}
