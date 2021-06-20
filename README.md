# Infinite Scroll List View - Android

The purpose of this library is to provide the easiest way to display data from sqlite database with the Load more functionality using asynchronous methods.  




To impelement this library follow the next steps:

<b>1.</b> Start by creating your adaptor and initiating it with your data as you would normally do. The adapter must extend RecyclerView.Adapter.

    ArrayList<MyData> dataList = ...;
    MyAdapter myAdapter = new MyAdapter(activity, dataList,...);

<b>2.</b> Add in your .xml file a ViewGroup container to provide a holder for the View.

        <FrameLayout
        android:id="@+id/viewHolderContainer" 
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
 

<b>3.</b> Create InfiniteScrollListView object and initiate it. You will need an instance of your database object and the sqlite query you used to get the data for your dataList.

    int totalItemsPerQuery = 100; // This will be the step 
    SQLiteDatabase database = ....;
    String query = "SELECT * FROM ....";

    InfiniteScrollListView infiniteScrollListView = new InfiniteScrollListView(activity,viewHolderContainer,myAdapter,MyData.class);
    
    infiniteScrollListView
                .setData(dataList)
                .setDatabaseInfo(database,query,totalItemsPerQuery);


If you need to use a different query at some point in your class, you can use the method:
  
    String newQuery = "SELECT * FROM....";
    infiniteScrollListView.updateData(newQuery);

That's it! Enjoy!
