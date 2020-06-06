package group3.sse.bupt.note;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ContentToSpanstr {

    public static SpannableString title_change(Context context,String content){
        //用正则表达式寻找音频路径
        Pattern voice = Pattern.compile("<voice src='(.*?)'/>");
        Matcher mVoice = voice.matcher(content);
        //用正则表达式寻找图片路径
        Pattern photo = Pattern.compile("<photo src='(.*?)'/>");
        Matcher mPhoto = photo.matcher(content);
        SpannableString spanStr = new SpannableString(content);
        while(mVoice.find()){
            int start = mVoice.start();
            int end = mVoice.end();
            ImageSpan imageSpan=new ImageSpan(context,R.drawable.ic_keyboard_voice_black_24dp);
            spanStr.setSpan(imageSpan,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        while(mPhoto.find()){
            int start = mPhoto.start();
            int end = mPhoto.end();
            ImageSpan imageSpan=new ImageSpan(context,R.drawable.ic_insert_photo_black_24dp);
            spanStr.setSpan(imageSpan,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spanStr;
    }
    public static SpannableString Content_to_SpanStr(Context context,String noteContent){
        //用正则表达式寻找音频路径
        Pattern voice = Pattern.compile("<voice src='(.*?)'/>");
        Matcher mVoice = voice.matcher(noteContent);
        //用正则表达式寻找图片路径
        Pattern photo = Pattern.compile("<photo src='(.*?)'/>");
        Matcher mPhoto = photo.matcher(noteContent);
        SpannableString spanStr = new SpannableString(noteContent);
        while(mVoice.find()){
            final String voicePath = mVoice.group(1);
            int start = mVoice.start();
            int end = mVoice.end();
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    //实现点击事件
                    MediaPlayer mp = new MediaPlayer();
                    try {
                        mp.setDataSource(voicePath);
                        mp.prepare();
                        mp.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            if(mediaPlayer != null){
                                mediaPlayer.stop();
                                mediaPlayer.release();
                            }
                        }
                    });
                }
            };
            spanStr.setSpan(clickableSpan,start,end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            ImageSpan imageSpan=new ImageSpan(context,R.drawable.wave);
            spanStr.setSpan(imageSpan,start,end,Spannable.SPAN_INCLUSIVE_EXCLUSIVE);


        }
        while(mPhoto.find()){

            Uri photoPath=Uri.parse(mPhoto.group(1));
            int start = mPhoto.start();
            int end = mPhoto.end();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),photoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 1000 || height > 2000) {
                // 缩小到五分之一
                float scaleWidth = (float) 0.2;
                float scaleHeight = (float) 0.2;
                //取得想要缩放的matrix参数
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                // 得到新的图片
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }

            ImageSpan imageSpan = new ImageSpan(context, bitmap);
            spanStr.setSpan(imageSpan,start,end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spanStr;
    }

}