package com.app.babyapp.helper;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.app.babyapp.R;
import com.app.babyapp.customviews.AdvancedRecyclerView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("rawtypes")
public class InfiniteScrollListView<T> implements AdvancedRecyclerView.CallBack {

    @BindView(R.id.advancedRecyclerView) AdvancedRecyclerView advancedRecyclerView;
    @BindView(R.id.loadMoreCon) RelativeLayout loadMoreCon;
    @BindView(R.id.shadowCon) RelativeLayout shadowCon;

    //Variables
    private Activity activity;
    private ArrayList<T> dataList;
    private Class<?> dataClass;
    private RecyclerView.Adapter adapter;
    private SQLiteDatabase database;
    private boolean enableShadow;
    private boolean loadMoreEnabled;
    private int loadMoreConHeight;
    private boolean isLoadingMore;
    private String dbQuery;
    private int dbQuerySteps;
    private boolean isBottomReached;

    //Finals
    private final static int LOAD_MORE_CONTAINER_HEIGHT_DP = 40;
    private final static long LOAD_MORE_ANIM_DURATION = 200;
    private final static long LOAD_MORE_ANIM_TOTAL_DELAY = 500;
    private final static int NO_NEW_ITEMS = -1;

    public InfiniteScrollListView(Activity activity, ViewGroup parentView, RecyclerView.Adapter adapter, Class<?> dataClass)
    {
        View view = View.inflate(activity, R.layout.infinite_scroll_async,null);
        ButterKnife.bind(this,view);

        this.activity=activity;
        this.adapter=adapter;
        this.dataClass = dataClass;

        loadMoreConHeight = (int) (LOAD_MORE_CONTAINER_HEIGHT_DP * Resources.getSystem().getDisplayMetrics().density);


        advancedRecyclerView.set(adapter,this);

        enableShadow = true;
        loadMoreEnabled = true;

        parentView.addView(view);
    }

    public void updateData(String dbQuery)
    {
        isBottomReached=false;
        this.dbQuery=dbQuery;
        final String query = dbQuery + " LIMIT " + dbQuerySteps;

        AsyncTask.execute(() ->
        {
            ArrayList<T> newDataList = (ArrayList<T>) getTableData(query,dataClass);
            dataList.clear();
            dataList.addAll(newDataList);

            activity.runOnUiThread(() ->
            {
                adapter.notifyDataSetChanged();
            });
        });
    }

    public InfiniteScrollListView setData(ArrayList<T> list)
    {
        if (dataList!=null) {
            dataList.clear();
            dataList.addAll(list);
            adapter.notifyDataSetChanged();
            isBottomReached=false;
        }
        else
            dataList = list;

        return this;
    }

    public void setDatabaseInfo(SQLiteDatabase database, String dbQuery, int dbQuerySteps)
    {
        this.database=database;
        this.dbQuery=dbQuery;
        this.dbQuerySteps=dbQuerySteps;
    }

    private int getData()
    {
        if (dataList.size()==0)
            return NO_NEW_ITEMS;

        String query = dbQuery + " LIMIT " + dataList.size() + "," + dbQuerySteps;
        //ArrayList<JSONObject> newDataList = getTableData(query);
        ArrayList<T> newDataList = (ArrayList<T>) getTableData(query,dataClass);

        if (newDataList.size()==0)
            return NO_NEW_ITEMS;

        dataList.addAll(newDataList);

        return newDataList.size();
    }

    private void onShowMore()
    {
        long onShowMoreTime = System.nanoTime();

        AsyncTask.execute(() ->
        {
            int totalNewItems = getData();

            long delay = Math.abs(onShowMoreTime-System.nanoTime())/1000000 < LOAD_MORE_ANIM_TOTAL_DELAY
                    ? LOAD_MORE_ANIM_TOTAL_DELAY : 0;

            activity.runOnUiThread(() ->
            {
                if (totalNewItems > 0) {
                    int addStartIndex = dataList.size() - totalNewItems;
                    adapter.notifyItemRangeInserted(addStartIndex, totalNewItems);
                    isBottomReached = totalNewItems < dbQuerySteps;;
                }
                else {
                    isBottomReached = true;
                }

                new Handler().postDelayed(()->
                {
                    if (isLoadingMore)
                    {
                        isLoadingMore=false;

                        moveViewAnimation(advancedRecyclerView,0,0,-loadMoreConHeight,0,50);

                        slideOutDown(loadMoreCon,50);

                        new Handler().postDelayed(()->advancedRecyclerView.smoothScrollBy(0,100),200);
                    }
                },delay);
            });
        });

    }

    public void setLoadMoreEnabled(boolean enable)
    {
        this.loadMoreEnabled=enable;
    }

    public void setEnableShadow(boolean enable)
    {
        this.enableShadow=enable;
    }

    @Override
    public void onScroll(int dy, boolean scrollingDown, int firstVisibleItemIndex, int lastVisibleItemIndex)
    {
        //On Bottom Reach
        if (lastVisibleItemIndex == adapter.getItemCount()-1 && !isLoadingMore && loadMoreEnabled && !isBottomReached)
        {
            moveViewAnimation(advancedRecyclerView,0,0,0,-loadMoreConHeight,LOAD_MORE_ANIM_DURATION);

            slideInUp(loadMoreCon,LOAD_MORE_ANIM_DURATION);
            new Handler().postDelayed(()->isLoadingMore=true,LOAD_MORE_ANIM_DURATION);
            onShowMore();
        }
        else if (lastVisibleItemIndex < adapter.getItemCount()-1 && isLoadingMore)
        {
            moveViewAnimation(advancedRecyclerView,0,0,-loadMoreConHeight,0,50);

            slideOutDown(loadMoreCon,50);
            new Handler().postDelayed(()->isLoadingMore=false,50);
        }

        if (dy == 0)
        {
            if (enableShadow)
                shadowCon.setVisibility(View.GONE);

        }
        else if (dy <= 10 && firstVisibleItemIndex!=0)
        {
            if (enableShadow)
                shadowCon.setVisibility(View.GONE);
        }
        else if (dy > 10 && firstVisibleItemIndex!=0)
        {
            if (enableShadow)
                shadowCon.setVisibility(View.VISIBLE);
        }
    }

    public <T> ArrayList<T> getTableData(String query, Class<T> classOfT)
    {
        ArrayList<T> list = new ArrayList<>();
        Cursor contentInfoCursor = database.rawQuery(query, null);

        try {
            if (contentInfoCursor != null) {
                if (contentInfoCursor.getCount()>0)
                {
                    contentInfoCursor.moveToFirst();

                    String[] columnNames = contentInfoCursor.getColumnNames();

                    do
                    {
                        Map<String, Object> itemsMapList = new HashMap<>();

                        for (int index=0; index<columnNames.length; index++)
                        {
                            int fieldType = contentInfoCursor.getType(contentInfoCursor.getColumnIndex(columnNames[index]));

                            if (fieldType == Cursor.FIELD_TYPE_STRING)
                                itemsMapList.put(columnNames[index],contentInfoCursor.getString(contentInfoCursor.getColumnIndex(columnNames[index])));
                            else if (fieldType == Cursor.FIELD_TYPE_INTEGER)
                                itemsMapList.put(columnNames[index],contentInfoCursor.getInt(contentInfoCursor.getColumnIndex(columnNames[index])));
                            else if (fieldType == Cursor.FIELD_TYPE_FLOAT)
                                itemsMapList.put(columnNames[index],contentInfoCursor.getFloat(contentInfoCursor.getColumnIndex(columnNames[index])));
                            else if (fieldType == Cursor.FIELD_TYPE_BLOB)
                                itemsMapList.put(columnNames[index],contentInfoCursor.getBlob(contentInfoCursor.getColumnIndex(columnNames[index])));
                        }

                        if (itemsMapList.size()>0)
                        {
                            Object gnrObj = jsonToGenericObject(new JSONObject(itemsMapList).toString(),classOfT);
                            list.add((T)gnrObj);
                        }
                    }
                    while (contentInfoCursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            contentInfoCursor.close();
        }

        return list;
    }

    private <T> T jsonToGenericObject(String jsonText, Class<T> classOfT)
    {
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonText);
        Gson gson = new Gson();

        return  gson.fromJson(mJson, classOfT);
    }

    private void moveViewAnimation(View view,float fromX,float toXDelta,float fromY,float toYDelta,long duration)
    {
        TranslateAnimation animation = new TranslateAnimation(fromX,toXDelta,fromY,toYDelta);
        animation.setRepeatMode(0);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        view.startAnimation(animation);
    }

    private void slideOutDown(View view,long duration)
    {
        YoYo.with(Techniques.SlideOutDown).duration(duration).playOn(view);
        new Handler().postDelayed(()->view.setVisibility(View.GONE),duration);
    }

    private void slideInUp(View view,long duration)
    {
        view.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.SlideInUp).duration(duration).playOn(view);
    }

}

