package com.example.user.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Alexander Rashkov on 6/17/2017.
 */

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = NewsLoader.class.getName();

    //Query URL, passed from MainActivity
    private String mUrl;

    /**
     * Constructs a new {@link NewsLoader}
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        //Here I check what url (query) is passed from MainActivity
        Log.i(LOG_TAG, "loadInBackground is started..The url query passed is:" + mUrl);
        //If there is a request URL, send it to it QueryUtils.fetchNewsData method
        //that will return a list of objects to be populated on screen
        List<News> news = QueryUtils.fetchNewsData(mUrl);
        return news;
    }
}
