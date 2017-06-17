package com.example.user.newsapp;

import android.app.LoaderManager.LoaderCallbacks;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<News>> {

    //Tag for log messages
    private static final String LOG_TAG = MainActivity.class.getName();

    //Initial Query which will be combined with the user's input
    private static final String API_INITIAL_QUERY = "https://content.guardianapis.com/search";

    //For this app I use "test" API KEY, but if have to I signed for an API KEY,
    //I can simply update this constant
    private static final String API_KEY = "test";

    //Adapter for the list of news
    private NewsAdapter mAdapter;

    //Loader manager object
    private LoaderManager mLoaderManager;

    //TextView that is displayed when the list is empty
    private TextView mEmptyStateTextView;

    //Progress bar to inform user that information is loading
    private ProgressBar mProgressSpinner;

    //This variable store users input formatted properly according to API guidelines
    private String formattedUserInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        //Find the empty_list_view that overlaps the listItems
        //When there is no listItems to display show this empty_list_view
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        //Find the progress spinner
        mProgressSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        // Create a new adapter that takes an empty list of News as input
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with the full article about the selected news.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //Find the current news that was clicked on
                News currentNews = mAdapter.getItem(position);

                //Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getUrl());

                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Start the intent
                startActivity(websiteIntent);
            }
        });

        //Create connectivity manager object to check for internet connection
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);

        //Check for internet connection
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //If there is connection start a loader
        if (isConnected) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            mLoaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int of 1 (its ID) and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            Log.i(LOG_TAG, "initLoader is called (Loader is initiated)!");
            mLoaderManager.initLoader(1, null, this);
        } else {
            //If there is no connection hide the progress spinner
            //and set a text on the empty view to inform the user
            mProgressSpinner.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
        }
    }

    //Create the loader if it does not already exists
    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        //Create SharedPreferences object to get the user's input
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Get the search topic input from preferences
        String searchTopic = sharedPrefs.getString(
                getString(R.string.settings_search_topic_key),
                getString(R.string.settings_search_topic_default));

        //Call a method to format the user input if there are spaces (multiple words)
        formattedUserInput = concatenateQuery(searchTopic);

        //Build the URI to pass it to the NewsLoader
        Uri baseUri = Uri.parse(API_INITIAL_QUERY);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("q", formattedUserInput);
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        //Hide the ProgressSpinner, since the data processing has finished
        mProgressSpinner.setVisibility(View.GONE);
        //If the query has not return any data, then there will be not listItems,
        //so only the empty_list_view will be left on screen.
        //Here the app sets a text on that empty_list_view to inform the user that there is
        //no data to be displayed
        mEmptyStateTextView.setText(R.string.no_news);
        // Clear the adapter of previous book data
        mAdapter.clear();
        // If there is a valid list of {@link News}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        Log.i(LOG_TAG, "Loader is reset..!");
        // Loader is reset, so the app can clear out the existing data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu from main.xml
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Start new intent on OptionsItemSelect, opens SettingsActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //If clicked option item is search_settings then
        //create an Intent to open the SettingsActivity
        if (id == R.id.search_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //This method clears the spaces between user's input
    //by replacing the spaces with "%20AND%20 "
    //Now when the query is send to the API it will look for content containing
    //all of the words entered by the user
    private String concatenateQuery(String usersTextInput) {
        //Split properly user's input because spaces are not allowed in the Query
        String[] wordsInput = usersTextInput.split("\\s+");
        String wordsToInputQuery = null;
        for (int i = 0; i < wordsInput.length; i++) {
            if (i == 0) {
                wordsToInputQuery = wordsInput[i];
            } else {
                //Concatinate the word in a proper format
                wordsToInputQuery = wordsToInputQuery + " AND " + wordsInput[i];
            }
        }
        //Returns the properly formatted user input
        return wordsToInputQuery;
    }
}
