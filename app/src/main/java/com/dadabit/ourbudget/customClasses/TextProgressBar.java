package com.dadabit.ourbudget.customClasses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;


public class TextProgressBar extends ProgressBar {
    private String text, text2;
    private Paint textPaintLeft, textPaintRight;

    public TextProgressBar(Context context) {
        super(context);
        text = "HP";
        text2 = "HP";
        textPaintLeft = new Paint();
        textPaintLeft.setColor(Color.BLACK);
        textPaintLeft.setTextSize(40);
        textPaintRight = new Paint();
        textPaintRight.setColor(Color.BLACK);
        textPaintRight.setTextSize(40);
    }

    public TextProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        text = "HP";
        text2 = "HP";
        textPaintLeft = new Paint();
        textPaintLeft.setColor(Color.BLACK);
        textPaintLeft.setTextSize(40);
        textPaintRight = new Paint();
        textPaintRight.setColor(Color.BLACK);
        textPaintRight.setTextSize(40);
    }

    public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        text = "HP";
        text2 = "HP";
        textPaintLeft = new Paint();
        textPaintLeft.setColor(Color.BLACK);
        textPaintLeft.setTextSize(40);
        textPaintRight = new Paint();
        textPaintRight.setColor(Color.BLACK);
        textPaintRight.setTextSize(40);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // First draw the regular progress bar, then custom draw our text
        super.onDraw(canvas);
        Rect bounds = new Rect();
        textPaintLeft.getTextBounds(text, 0, text.length(), bounds);
        int x = 20 ;
        int y = getHeight() / 2 - bounds.centerY();
        canvas.drawText(text, x, y, textPaintLeft);

        Rect bounds2 = new Rect();
        textPaintRight.getTextBounds(text2, 0, text2.length(), bounds);
        int x2 = getWidth() - bounds.centerX()*2 - 8 ;
        int y2 = getHeight() / 2 - bounds.centerY();
        canvas.drawText(text2, x2, y2, textPaintRight);
    }

    public synchronized void setText(String text, String text2) {
        this.text = text;
        this.text2 = text2;
        drawableStateChanged();
    }



    public void setTextColor(int color) {
        textPaintLeft.setColor(color);
        drawableStateChanged();
    }
}
