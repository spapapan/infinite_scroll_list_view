package com.app.babyapp.customviews;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class AdvancedRecyclerView extends RecyclerView  {

    //Variables
    private AdvancedRecyclerView.CallBack callBack;
    private int previousVerticalScroll;

    public void set(@Nullable Adapter adapter, AdvancedRecyclerView.CallBack callBack)
    {
        setAdapter(adapter);
        this.callBack=callBack;
    }

    private void init(Context context)
    {
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);

        setLayoutManager(linearLayoutManager);
        setScrollListener(linearLayoutManager);
        OverScrollDecoratorHelper.setUpOverScroll(this,
                OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    private void setScrollListener(LinearLayoutManager linearLayoutManager)
    {
        addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                int verticalScroll = recyclerView.computeVerticalScrollOffset();
                boolean scrollingDown = verticalScroll > previousVerticalScroll;
                previousVerticalScroll = verticalScroll;

                if (callBack!=null)
                    callBack.onScroll(verticalScroll,scrollingDown,firstVisibleItem,lastVisibleItem);
            }
        });
    }

    public AdvancedRecyclerView(Context context) {
        this(context, null);
        init(context);
    }

    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public AdvancedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public interface CallBack {
        void onScroll(int dy, boolean scrollingDown, int firstVisibleItemIndex, int lastVisibleItemIndex);
    }
}
