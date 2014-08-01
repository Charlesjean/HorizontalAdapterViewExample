package com.example.chenduanjin.horizontaladapterviewexample;
import android.database.DataSetObservable;

import android.database.DataSetObserver;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by chenduanjin on 7/28/14.
 */
public class HorizontalGallyView extends AdapterView<ListAdapter>{

    private class HorizontalDataSetObserver extends DataSetObserver
    {
        @Override
        public void onChanged() {
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    }

    private ListAdapter mAdapter;
    protected DataSetObserver mObserver;

    public HorizontalGallyView(Context context) {
        super(context);
    }

    public HorizontalGallyView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public HorizontalGallyView(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
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

    //TODO need to measure child here
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //TODO need to layout child here
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
}
