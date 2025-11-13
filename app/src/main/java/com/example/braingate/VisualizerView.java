package com.example.braingate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class VisualizerView extends View {

    private byte[] bytes;
    private Paint paint = new Paint();

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint.setStrokeWidth(5f);
        paint.setColor(Color.WHITE);
    }

    public void updateVisualizer(byte[] bytes) {
        this.bytes = bytes;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bytes == null) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        int barCount = bytes.length / 2;
        float barWidth = width / barCount;

        for (int i = 0; i < barCount; i++) {
            float x = i * barWidth;
            float magnitude = (float) (bytes[i * 2] * bytes[i * 2] + bytes[i * 2 + 1] * bytes[i * 2 + 1]);
            float heightValue = (float) (Math.log1p(magnitude) * (height * 0.01));

            float top = height / 2 - heightValue / 2;
            float bottom = height / 2 + heightValue / 2;

            canvas.drawRect(x, top, x + barWidth, bottom, paint);
        }
    }
}
