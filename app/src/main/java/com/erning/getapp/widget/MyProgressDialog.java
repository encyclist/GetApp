package com.erning.getapp.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.erning.getapp.R;

/**
 * Created by 二宁 on 2017/11/23.
 */

public class MyProgressDialog extends Dialog {
    private Context mContext;

    public MyProgressDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        //去掉dialog默认蓝线
        int dividerID=mContext.getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider=findViewById(dividerID);
        if(divider!=null){
            divider.setBackgroundColor(Color.TRANSPARENT);
        }

        // 不能取消
        setCancelable(false);
        //全屏显示
        Window window = this.getWindow();
        if(window != null){
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //清理背景变暗
//            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            //去掉默认背景的padding
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // 设置位置
            window.setGravity(Gravity.CENTER);
            //此方法刘海屏或全面屏不适配，状态栏会有一层半透明遮罩
//            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
