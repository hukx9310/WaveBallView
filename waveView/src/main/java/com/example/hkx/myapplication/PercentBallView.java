package com.example.hkx.myapplication;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hkx on 17-4-26.
 */

public class PercentBallView extends View{

    public final static int IDEL = 1;
    public final static int RUNNING = 2;
    public final static int READY = 3;

    int currState = IDEL;

    int mPercent;
    int mPercentForAnim;

    Paint mBallPaint = new Paint();
    int mDefaulViewtSize = 50;
    int mViewSize;

    TextPaint mTextPaint = new TextPaint();
    int mWaveColor;
    String mWaveText = "";
    int mUnitSize;
    int mWaveTextSize;
    int mPrecentStringSize;
    String mUnitString = "%";

    Paint mDialPaint = new Paint();
    float mDialWidth;
    float mDialStartX;
    float mDialHeight;
    boolean mHaveDial = false;

    //for wave ball
    int mOffset1;
    int mOffset2;
    Bitmap mWaveBitmap;
    Bitmap mShape;
    Paint mWavePaint = new Paint();
    int mBallSize = 0;

    public PercentBallView(Context context) {
        this(context, null);
    }

    public PercentBallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PercentBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int[] attrsArray = { android.R.attr.colorAccent };
        TypedArray androidTypedArray = context.obtainStyledAttributes(attrsArray);
        int accentColor = androidTypedArray.getColor(0, 0x000000);
        androidTypedArray.recycle();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PercentBallView);
        mHaveDial = typedArray.getBoolean(R.styleable.PercentBallView_haveDials, false);
        mWaveColor = typedArray.getColor(R.styleable.PercentBallView_waveColor, accentColor);
        String unitString = typedArray.getString(R.styleable.PercentBallView_unit);
        if(unitString != null){
            mUnitString = unitString;
        }
        String waveText = typedArray.getString(R.styleable.PercentBallView_text);
        if(waveText != null){
            mWaveText = waveText;
        }
        typedArray.recycle();

        mDialPaint.setColor(Color.WHITE);
        mDialPaint.setAntiAlias(true);
        mDialPaint.setStyle(Paint.Style.STROKE);

        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = resolveMeasure(widthMeasureSpec, mDefaulViewtSize);
        int height = resolveMeasure(heightMeasureSpec, mDefaulViewtSize);

        mViewSize = Math.min(width, height);
        setMeasuredDimension(width, height);
    }

    public int resolveMeasure(int measureSpec, int defaultSize){

        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)){

            case MeasureSpec.UNSPECIFIED:
                return defaultSize;
            case MeasureSpec.AT_MOST:
                return defaultSize;
            case MeasureSpec.EXACTLY:
                return specSize;
            default:
                return defaultSize;
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initAllSize();

        TimerTask wavingTask = new TimerTask() {
            @Override
            public void run() {
                if((mOffset1 + mBallSize /10) >= 0 ){
                    mOffset1 = -mBallSize;
                } else {
                    mOffset1 += mBallSize /10;
                }
                if((mOffset2 + mBallSize /10) >= 0 ){
                    mOffset2 = -mBallSize;
                } else {
                    mOffset2 += mBallSize /10;
                }
                postInvalidate();
            }
        };
        new Timer().schedule(wavingTask, 0 ,100);
    }

    private void initAllSize() {
        mDialWidth = mViewSize * 0.0125f;
        mDialStartX = mViewSize *0.45f;
        mDialHeight = mViewSize *0.05f;

        mWaveTextSize = mViewSize /12;
        mUnitSize = mViewSize/18;
        mPrecentStringSize = mViewSize/6;

        mBallSize = (int)(mViewSize * 0.85);
        mOffset1 = 0;
        mOffset2 = -mBallSize /2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mHaveDial) {
            drawDial(canvas);
        }
        drawBall(canvas);
        drawText(canvas);
    }

    void drawDial(Canvas canvas){
        canvas.save();
        mDialPaint.setStrokeWidth(mDialWidth);
        canvas.translate(mViewSize /2, mViewSize /2);
        canvas.rotate(30);
        for(int i = 0; i <= 100 ; i++) {
            if(mPercentForAnim == i){
                mDialPaint.setColor(Color.GRAY);
                mDialPaint.setAlpha(200);
            } else if (i < mPercentForAnim){
                int green = (i*255)/100;
                mDialPaint.setARGB(255, 255 - green, green, 188);
            }
            canvas.drawLine(0,  mDialStartX, 0, mDialStartX + mDialHeight, mDialPaint);
            canvas.rotate(3);
        }
        canvas.restore();
    }

    void drawBall(Canvas canvas){

        int saveLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), mBallPaint);

        if(mShape == null){
            mShape = getShape();
        }
        canvas.drawBitmap(mShape, 0.075f* mViewSize, 0.075f* mViewSize, mBallPaint);
        mBallPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        canvas.drawBitmap(getDoubleWave(), 0.075f* mViewSize, 0.075f* mViewSize + ((50-mPercentForAnim)/100f)* mBallSize, mBallPaint);

        mBallPaint.setXfermode(null);
        canvas.restoreToCount(saveLayer);
    }

    void drawText(Canvas canvas){

        mTextPaint.setTextSize(mPrecentStringSize);
        canvas.drawText(String.valueOf(mPercentForAnim), mViewSize /2, mViewSize /2, mTextPaint);

        mTextPaint.setTextSize(mUnitSize);
        canvas.drawText(mUnitString, mViewSize /2 + mPrecentStringSize, mViewSize /2 - mWaveTextSize,mTextPaint);

        mTextPaint.setTextSize(mWaveTextSize);
        canvas.drawText(mWaveText, mViewSize /2, mViewSize *0.75f, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(currState != IDEL){
                    return false;
                } else if(isTouchWithinBall(event.getX(), event.getY())){
                    currState = READY;
                    return true;
                } else {
                    return false;
                }
            case MotionEvent.ACTION_UP:
                if(currState != READY){
                    currState = IDEL;
                    return true;
                } else if(isTouchWithinBall(event.getX(), event.getY())){
                    currState = IDEL;
                    if(!performClick()){
                    }
                    return true;
                } else {
                    currState = IDEL;
                    return true;
                }
        }
        return false;
    }

    private boolean isTouchWithinBall(float x, float y){

        return Math.sqrt(Math.pow((x- mViewSize /2),2) + Math.pow((y- mViewSize /2),2)) <= mViewSize /2;
    }

    public int getPercent() {
        return mPercent;
    }

    public void setPercent(int mPercent) {
        this.mPercent = mPercent;

        ValueAnimator backAnimator = ValueAnimator.ofInt(mPercentForAnim, 0);
        ValueAnimator forwardAnimator = ValueAnimator.ofInt(0, mPercent);
        backAnimator.setDuration(10*mPercentForAnim);
        forwardAnimator.setDuration(10*mPercent);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mPercentForAnim = (int)animation.getAnimatedValue();
                invalidate();
            }
        };
        backAnimator.addUpdateListener(animatorUpdateListener);
        forwardAnimator.addUpdateListener(animatorUpdateListener);


        AnimatorSet animators = new AnimatorSet();
        animators.play(backAnimator).before(forwardAnimator);
        animators.setInterpolator(new AccelerateDecelerateInterpolator());
        animators.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                currState = RUNNING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currState = IDEL;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currState = IDEL;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                currState = RUNNING;
            }
        });
        animators.start();
    }

    private Bitmap getShape(){
        Bitmap bitmap = Bitmap.createBitmap(mBallSize *2, mBallSize *2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        canvas.drawCircle(mBallSize /2f, mBallSize /2f, mBallSize *0.5f, paint);

        return bitmap;
    }

    private Bitmap getDoubleWave(){
        Bitmap bitmap = Bitmap.createBitmap(mBallSize *2, mBallSize *2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if(mWaveBitmap == null){
            mWaveBitmap = getWaveBitmap();
        }
        mWavePaint.setAlpha(150);
        canvas.drawBitmap(mWaveBitmap, mOffset1, 0, mWavePaint); //first draw
        mWavePaint.setAlpha(255);
        canvas.drawBitmap(mWaveBitmap, mOffset2, 0, mWavePaint);//change alpha and draw again

        return bitmap;
    }

    private Bitmap getWaveBitmap(){

        Bitmap bitmap = Bitmap.createBitmap(mBallSize *2, mBallSize *2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(mWaveColor);
        Path path = new Path();
        path.moveTo(0, mBallSize *0.5f);
        path.cubicTo(mBallSize *0.33f, mBallSize *0.4f, mBallSize *0.66f, mBallSize *0.6f, mBallSize, mBallSize *0.5f);
        path.cubicTo(mBallSize + mBallSize *0.33f, mBallSize *0.4f, mBallSize + mBallSize *0.66f, mBallSize *0.6f, mBallSize *2, mBallSize *0.5f);
        path.lineTo(mBallSize *2f, mBallSize *2f);
        path.lineTo(0, mBallSize *2f);
        path.close();
        canvas.drawPath(path, paint);

        return bitmap;
    }
}
