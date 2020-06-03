package group3.sse.bupt.note.Share;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class Clipboard {
    public static void CopyTextToClipboard(Context context, String text){
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newPlainText("Label", text);
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
    }
}
