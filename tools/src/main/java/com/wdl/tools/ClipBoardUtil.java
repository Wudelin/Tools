package com.wdl.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * Create by: wdl at 2019/11/5 8:46
 * 剪切板管理工具类
 * Attention：Android Q(28) 之后限制了其部分功能
 *  任何时候剪切板只存在一个ClipData，新添加时上一个消失;可通过ClipData.addItem()添加
 */
@SuppressWarnings("unused")
public final class ClipBoardUtil
{
    private ClipBoardUtil()
    {
    }

    /**
     * 设置剪切板内容
     *
     * @param text 文本
     */
    public static void setPlainText(Context context,@NonNull CharSequence text)
    {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null)
        {
            return;
        }
        cm.setPrimaryClip(ClipData.newPlainText(null, text));
    }

    /**
     * 设置剪切板内容
     *
     * @param intent 上下文
     */
    public static void setIntent(Context context,@NonNull Intent intent)
    {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null)
        {
            return;
        }
        cm.setPrimaryClip(ClipData.newIntent(null, intent));
    }

    /**
     * 获取剪切板内容
     *
     * @return 剪切板内容
     */
    public static CharSequence getText(Context context)
    {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null)
        {
            return null;
        }
        if (cm.hasPrimaryClip())
        {
            return cm.getPrimaryClip().getItemAt(0).getText();
        } else
        {
            return null;
        }
    }

    /**
     * 获取剪切板内容(强制转化为文本信息)
     *
     * @return 剪切板内容
     */
    public static CharSequence getCoerceText(Context context)
    {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null)
        {
            return null;
        }
        if (cm.hasPrimaryClip())
        {
            return cm.getPrimaryClip().getItemAt(0).coerceToText(context);
        } else
        {
            return null;
        }
    }
}
