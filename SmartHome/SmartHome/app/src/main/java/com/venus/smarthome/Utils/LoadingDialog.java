package com.venus.smarthome.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import com.venus.smarthome.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
        initDialog();
    }

    private void initDialog() {
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.loading_dialog, null);
        setContentView(view);
    }
}
