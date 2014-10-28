package com.example.chenduanjin.horizontaladapterviewexample;
import android.database.DataSetObservable;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.Scroller;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import android.os.Handler;

import android.view.animation.LinearInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/**
 * Created by chenduanjin on 7/28/14.
 */
public class HorizontalGallyView extends AdapterView<ListAdapter> implements GestureDetector.OnGestureListener{

    private  final String TAG = "HorizontalGallyView";
    private class HorizontalDataSetObserver extends DataSetObserver
    {
        @Override
        public void onChanged() {
            super.onChanged();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            requestLayout();
        }
    }

    private Context mContext;
    private Handler mHandler = new Handler();
    //view model and observer
    private ListAdapter mAdapter;
    protected DataSetObserver mObserver;

    private int mFirstVisiblePostion;
    private Queue<View> mRecycleList = new LinkedList<View>();

    //view format parameters
    private int mPaddingLeft = 0;
    private int mPaddingRight = 0;
    private int mPaddingTop = 0;
    private int mPaddingBottom = 0;
    private int mDividerWidth = 15;

    //view measure parameters
    private int mWidthMeasureSpec;
    private int mHeightMeasureSpec;

    //viewer scroll and gesture
    //private OverScroller mScroller;
    private FlingRunnable mFlingRunnable;
    private GestureDetector mGestureDetector;


    public HorizontalGallyView(Context context) {
        super(context);
        initViewer(context);
    }

    public HorizontalGallyView(Context context, AttributeSet attrs) {
        super(context,attrs);
        initViewer(context);
    }

    public HorizontalGallyView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        initViewer(context);
    }

    private void initViewer(Context context)
    {
       // mScroller = new OverScroller(context);
        mContext = context;
        mGestureDetector = new GestureDetector(context, this);
        mFlingRunnable = new FlingRunnable();
    }

    @Override
    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @Override
    public void setAdapter(ListAdapter listAdapter) {
        if(this.mAdapter != null)
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        this.mAdapter = listAdapter;
        this.mObserver = new HorizontalDataSetObserver();
        mAdapter.registerDataSetObserver(this.mObserver);
        this.requestLayout();

    }

    @Override
    public View getSelectedView() {
        return null;
    }

    @Override
    public void setSelection(int i) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
         mGestureDetector.onTouchEvent(event);
        return true;
    }

    //TODO need to measure child here
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int lwidthMeasureMode = MeasureSpec.getMode(widthMeasureSpec);
        int lwidth = MeasureSpec.getSize(widthMeasureSpec);
        int lheightMeasureMode = MeasureSpec.getMode(heightMeasureSpec);
        int lheight = MeasureSpec.getSize(heightMeasureSpec);
        //TODO delete test code for view measure parameter
        Log.i("DJ", "width: " + lwidth + "height: " + lheight);
        switch(lwidthMeasureMode)
        {
            case MeasureSpec.AT_MOST:
                Log.i("DJ", "At_most");
                break;
            case MeasureSpec.EXACTLY:
                Log.i("DJ", "Exactly");
                break;
            case MeasureSpec.UNSPECIFIED:
                Log.i("DJ", "Unspecified");
                break;
        }

        switch (lheightMeasureMode) {
            case MeasureSpec.AT_MOST:
                Log.i("DJ", "Height At_most");
                break;
            case MeasureSpec.EXACTLY:
                Log.i("DJ", "Height Exactly");
                break;
            case MeasureSpec.UNSPECIFIED:
                Log.i("DJ","Height Unspecified");
                break;
        }

        int itemCount = this.mAdapter == null ? 0 : mAdapter.getCount();
        int height = 0;
        int width =0;

        for (int i = mFirstVisiblePostion; i < itemCount; ++i) {
            View child = this.mAdapter.getView(i, null, this);
            LayoutParams params = child.getLayoutParams();
            if (params == null) {
                params = new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                child.setLayoutParams(params);
            }
            int lchildWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, params.width);
            int lchildHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, params.height);
            child.measure(lchildWidthSpec, lchildHeightSpec);
            width += child.getMeasuredWidth();
            height = Math.max(height,child.getMeasuredHeight());
        }

        if (lwidthMeasureMode == MeasureSpec.EXACTLY)
            width = lwidth;
        setMeasuredDimension(width, height);
        mWidthMeasureSpec = widthMeasureSpec;
        mHeightMeasureSpec = heightMeasureSpec;


    }

    //TODO need to layout child here
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.removeAllViewsInLayout();
        int itemCount = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        for (int i = mFirstVisiblePostion; i < itemCount; ++i) {
            makeAndAddView(i);
            if (getChildAt(i).getLeft() + getChildAt(i).getMeasuredWidth() > getWidth())
                break;
        }
        Log.i("DJ", "onLayout is called");
    }

    private void makeAndAddView(int pos) {

        View child = mAdapter.getView(pos, null, this);
        this.measureView(child);
        int childLeft = pos == 0 ? mPaddingLeft : this.getChildAt(pos - 1).getRight()
                + mDividerWidth;
        int childRight = childLeft + child.getMeasuredWidth();
        int childTop = mPaddingTop;
        int childBottom = childTop + child.getMeasuredHeight();
        child.layout(childLeft, childTop, childRight, childBottom);
        addViewInLayout(child, -1, child.getLayoutParams());
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        //Log.i("DJ", "onScrollChanged " + "cx: " + l + "cy: " + t + "ox: " + oldl + "oy: " + oldt  );
        removeInvisibleViews();
        fillView();
    }

    //GestureDetector.OnGestureListener implementation
    @Override
    public boolean onDown(MotionEvent e) {
//        Log.i("DJ", "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
//        Log.i("DJ", "onSingleTapUp");

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        Log.i("DJ", "onScroll");
        mFlingRunnable.onContentScroll(e1, e2, distanceX, distanceY);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        Log.i("DJ", "onFling");
        float x1 = e1.getX();
        float x2 = e2.getX();
        mFlingRunnable.onContentFling(e1,e2,velocityX,velocityY);
        return false;
    }

    //FlingRunnable is used to implement scroll
    private class FlingRunnable implements Runnable {
        private OverScroller mScroller;
        private int mCurrScrollX;
        private int mCurrScrollY;
        private boolean isScrolling;


        //Constructor
        public FlingRunnable() {
            LinearInterpolator linearInterpolator = new LinearInterpolator();
            DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
            //Create scroller and assign a interpolator to it.
            mScroller = new OverScroller(mContext, decelerateInterpolator);
        }

        public void stopScroller() {
            mScroller.abortAnimation();
            isScrolling = false;
        }
        @Override
        public void run() {
            //If we are still scrolling get the new x,y values.
            if(mScroller.computeScrollOffset()) {


                float oldScrollX = mCurrScrollX;
                float oldScrollY = mCurrScrollY;
                int scrollOffset = (int)(mScroller.getCurrX() - oldScrollX);
                if (canScroll(scrollOffset)) {
                    mCurrScrollX = mScroller.getCurrX();
                    mCurrScrollY = mScroller.getCurrY();
                    //here we scroll our view
                    scrollBy((int)(mCurrScrollX - oldScrollX), (int)(0));
                    //If we have not reached our scroll limit,then run this Runnable again on UI event thread.
                    post(this);
                }
            }
        }

        public boolean onContentFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isScrolling)
                stopScroller();
            isScrolling = true;
            mScroller.fling(mCurrScrollX, mCurrScrollY, (int)-velocityX, (int)-velocityY,
                    0, 10000, 0, 0);
            post(this);
            return true;
        }

        public boolean onContentScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScrolling)
                stopScroller();
            isScrolling = true;
            mScroller.startScroll(getScrollX(), 0, (int)distanceX, 0, 10);
            post(this);
            return true;
        }
    }

    private void removeInvisibleViews() {
        if (getChildCount() == 0)
            return;
        //remove left invisible views
        while(getChildCount() > 0 && getChildAt(0).getRight() < this.getScrollX()) {
            View child = getChildAt(0);
            mRecycleList.offer(child);
            removeViewInLayout(child);
            mFirstVisiblePostion ++;
            Log.i("DJ", "remove left view and show " + mFirstVisiblePostion);
        }

        while(getChildCount()> 0 && getChildAt(getChildCount() - 1).getLeft()
                > this.getScrollX() + getWidth()) {
            Log.i(TAG, "left " + getChildAt(getChildCount() - 1).getLeft() + " width " + getWidth() + "scrollX " + getScrollX());
            View child = getChildAt(getChildCount() - 1);
            mRecycleList.offer(child);
            removeViewInLayout(child);
            Log.i("DJ", "remove right view and show " + (mFirstVisiblePostion + getChildCount()) + "View is " + ((TextView)(((ViewGroup)child).getChildAt(0))).getText());
        }
        invalidate();
    }

    private void fillView() {
        fillLeft();
        fillRight();
    }
    private void fillLeft() {
        if (mFirstVisiblePostion ==0)
            return;
        while (getChildAt(0).getLeft() > getScrollX()) {
            if (mFirstVisiblePostion > 0) {
                --mFirstVisiblePostion;
                View reusedView = getRecycledView();
                View child;
                if (reusedView != null) {
                    Log.i("DJ", "You are reuse view now");
                     child = mAdapter.getView(mFirstVisiblePostion, reusedView,this);
                    if (child != reusedView) {
                        mRecycleList.offer(reusedView);
                    }
                }
                else
                {
                    child = mAdapter.getView(mFirstVisiblePostion, null, this);
                }

                //measure view width and height
                this.measureView(child);
                int childRight = getChildAt(0).getLeft() - mDividerWidth;
                int childLeft = childRight - child.getMeasuredWidth();
                int childTop = mPaddingTop;
                int childBottom = childTop + child.getMeasuredHeight();
                child.layout(childLeft, childTop, childRight, childBottom);
                addViewInLayout(child,0, child.getLayoutParams());
            }
        }
    }

    private void fillRight() {
        if (mFirstVisiblePostion + this.getChildCount()  == mAdapter.getCount() )
            return;
            while (getChildAt(this.getChildCount() - 1).getRight()
                    < getWidth() + getScrollX()) {
                int positon = mFirstVisiblePostion + this.getChildCount();
                if (positon >= mAdapter.getCount())
                    return;
                View reusedView = getRecycledView();
                View child;
                if (reusedView != null) {
                    Log.i("DJ", "You are reuse view now");
                    child = mAdapter.getView(positon, reusedView,this);
                    if (child != reusedView) {
                        mRecycleList.offer(reusedView);
                    }
                }
                else
                {
                    child = mAdapter.getView(mFirstVisiblePostion, null, this);
                }

                this.measureView(child);
                int childLeft = getChildAt(getChildCount() - 1).getRight() + mDividerWidth;
                int childRight = childLeft + child.getMeasuredWidth();
                int childTop = mPaddingTop;
                int childBottom = childTop + child.getMeasuredHeight();
                child.layout(childLeft, childTop, childRight, childBottom);
                addViewInLayout(child, -1, child.getLayoutParams());
            }
    }

    private View getRecycledView() {
        if (!mRecycleList.isEmpty()) {
            return mRecycleList.poll();
        }

        return null;
    }

    private void measureView(View child) {
        LayoutParams params  = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            child.setLayoutParams(params);
        }
        int lchildWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, 0, params.width);
        int lchildHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec, 0, params.height);
        child.measure(lchildWidthSpec, lchildHeightSpec);
    }

    /**
     * do not scroll if there is no data to show
     * @param scrollOffset
     * @return
     */
    private boolean canScroll(int scrollOffset) {
        int count = mAdapter == null ? 0 : mAdapter.getCount();
        if (scrollOffset > 0 && mFirstVisiblePostion + getChildCount() == count && getChildAt(getChildCount() - 1).getLeft()
                + getChildAt(getChildCount() - 1).getWidth() - getScrollX()  < getWidth())
            return false;
        return  true;
    }

}
