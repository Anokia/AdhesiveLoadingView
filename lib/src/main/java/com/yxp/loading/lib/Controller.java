package com.yxp.loading.lib;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;

/**
 * Created by yanxing on 16/1/9.
 */
public class Controller extends ValueAnimator {
    private View mView;
    private ArrayList<RabbitCircle> mRabbits = new ArrayList<>();
    private WolfCircle mWolf;

    private int startX;
    private int startY;
    private int centerX;
    private int centerY;
    private int bigR;
    private float rate = 1.5f;

    private int mAliveRabbits = 0;
    // wolf and rabbit mix when their degree are closing, this is the threshold that they mix
    private int mMixDegree;

    private Path mPath;
    private int mPathDegree = -1;


    public Controller(View view) {
        mView = view;
        centerX = view.getWidth() / 2;
        centerY = view.getHeight() / 2;
        startX = view.getWidth() / 2;
        startY = view.getHeight() / 5;
        initComponent();
        initAnimator();

        bigR = centerY - startY;
        mPath = new Path();
    }

    private void initComponent() {
        // create 6 rabbits
        int r = Math.min(mView.getWidth(), mView.getHeight()) / 20;
        int degree = 0;
        for (int i = 0; i < 6; i++) {
            mRabbits.add(new RabbitCircle(startX, startY, r, degree));
            degree += 60;
        }

        // create wolf
        if (mWolf == null) {
            mWolf = new WolfCircle(startX, startY, (int)(rate * r), 0);
        }
    }

    private void initAnimator() {
        this.setIntValues(0, 360);
        this.setDuration(2000);
        this.setRepeatMode(RESTART);
        this.setRepeatCount(INFINITE);
        this.setInterpolator(new AccelerateDecelerateInterpolator());
        this.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int degree = (int)animation.getAnimatedValue();
                startActivities(degree);

                mView.invalidate();
            }
        });
        this.start();
    }

    private void startActivities(int degree) {
        mWolf.runTo(degree);

        for (RabbitCircle rabbit : mRabbits) {
            if (mAliveRabbits < 6 && rabbit.getState() == RabbitCircle.DIED
                    && rabbit.getDegree() < degree) {
                rabbit.setState(RabbitCircle.ALIVE);
                mAliveRabbits++;
            }
            if (mWolf.getDegree() - rabbit.getDegree() > 0 && mWolf.getDegree() - rabbit.getDegree() <= 40) {
                float deg = (mWolf.getDegree() - rabbit.getDegree()) / 2f;
                mPathDegree = (int) (deg + rabbit.getDegree());
                int distance = (int) (Math.sin(Math.PI * deg / 180) * bigR);
                updatePath(distance);
            }
        }
    }

    private void updatePath(int distance){
        mPath.reset();
        int x1 = startX - distance;
        int y1 = startY - mRabbits.get(0).getRadius() + 1;

        int x2 = startX - distance;
        int y2 = startY + mRabbits.get(0).getRadius() - 1;

        int x3 = startX + distance;
        int y3 = startY + mWolf.getRadius() - 1;

        int x4 = startX + distance;
        int y4 = startY - mWolf.getRadius() + 1;

        int controlX1T4 = (x1 + x4) / 2;
        int controlY1T4 = (int) (y1 + (x4 - x1) * 0.5f);
        int controlX2T3 = (x2 + x3) / 2;
        int controlY2T3 = (int) (y2 - (x3 - x2) * 0.5f);

        mPath.moveTo(x1, y1);
        mPath.lineTo(x2, y2);
        mPath.quadTo(controlX2T3, controlY2T3, x3, y3);
        mPath.lineTo(x4, y4);
        mPath.quadTo(controlX1T4, controlY1T4, x1, y1);
        mPath.close();
    }

    public void draw(Canvas canvas, Paint paint) {

        for (Circle rabbit : mRabbits) {
            rabbit.draw(canvas, paint, centerX, centerY);
        }

        mWolf.draw(canvas, paint, centerX, centerY);

        if (mPathDegree > 0) {
            drawPath(canvas, paint);
        }

    }

    public void drawPath(Canvas canvas, Paint paint) {
        canvas.save();
        canvas.rotate(mPathDegree, centerX, centerY);
        canvas.drawPath(mPath, paint);
        canvas.restore();
        mPathDegree = -1;
    }
}
