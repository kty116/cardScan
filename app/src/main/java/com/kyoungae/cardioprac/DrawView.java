package com.kyoungae.cardioprac;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.FileOutputStream;

public class DrawView extends FrameLayout {

    private Paint mPaint;

    public DrawView(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        canvas.drawRect(10, 10, 300, 300, mPaint);
    }

//    @Override
//    public void onDrawForeground(Canvas canvas) {
//        super.onDrawForeground(canvas);
//
//
//
//    }

    //    public Bitmap drawBitmap() {
//        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//
////        surfaceDestroyed(null); //Thread 잠시 멈춤(pause)
//        onDraw(canvas);
////        surfaceCreated(null); //Thread 재개(resume)
//
//        return bitmap;
//    }


//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        canvas.drawRect(0, 0, 0, 0, mPaint);
////        canvas.drawARGB(0x80, 0xff, 0, 0 );
////        Paint paint = new Paint();
//        mPaint.setColor(Color.BLUE);
//        mPaint.setTextSize(48);
//        canvas.drawText("...Some view...", 10, canvas.getHeight() / 2,
//                mPaint);

//        mPaint.setColor(Color.GREEN);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(3);
//        canvas.drawRect(10,10,100,100,mPaint);
//    }
}
