package com.erning.getapp.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.erning.getapp.widget.MyProgressDialog;

/**
 * @author 二宁
 * @date 2020/11/16 17:30
 * @des
 */
public class BaseActivity extends AppCompatActivity {
    private MyProgressDialog progressDialog;

    public void showProgressDialog() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new MyProgressDialog(this);
            progressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
