package com.example.user.newsapp;

/**
 * Created by Alexander Rashkov on 6/17/2017.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving news data from The Guardian API.
 */
public final class QueryUtils {
    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    //Keys for JSON parsing
    private static final String KEY_WEB_TITLE = "webTitle";
    private static final String KEY_SECTION = "sectionName";
    private static final String KEY_URL_NEWS = "webUrl";
    private static final String KEY_DATE = "webPublicationDate";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    public QueryUtils() {
    }

    /*
    * Puts all the methods together to get the List of News from the request URL
    * @param requestUrl: the target URL of the HTTP request
    * @return List<News>: the list of News objects
    */
    public static List<News> fetchNewsData(String requestUrl) {
        //Create URL object
        URL url = createUrl(requestUrl);

        //Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        //Create a list of the results by the extractDataFromJson method
        List<News> news = extractDataFromJson(jsonResponse);

        //return the list of news
        return news;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /*
    * Extracts the data from the JSON response
    * @param jsonResponse: the jsonResponse read from the InputStream of the HTTP request
    * @return List<News>: the results as a List of News objects
    */
    private static List<News> extractDataFromJson(String jsonResponse) {
        //If the response is empty, return early
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        List<News> news = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            //Try to the create a new JSONObject from the response and search for the result list of news
            JSONObject response = new JSONObject(jsonResponse);
            JSONObject newsResponse = response.getJSONObject("response");
            JSONArray newsArray = newsResponse.getJSONArray("results");

            //Loop through the results
            for (int i = 0; i < newsArray.length(); i++) {
                //Get the title, section, url and date of the current news
                JSONObject currentNews = newsArray.getJSONObject(i);

                // Variables for JSON parsing
                String title;
                String section;
                String url;
                String rawDate;
                String date;

                //Check if key "webTitle" exists and if yes, return value
                if (currentNews.has(KEY_WEB_TITLE)) {
                    title = currentNews.getString(KEY_WEB_TITLE);
                } else {
                    title = null;
                }

                //Check if key "sectionName" exists and if yes, return value
                if (currentNews.has(KEY_SECTION)) {
                    section = currentNews.getString(KEY_SECTION);
                } else {
                    section = null;
                }

                //Check if key "webUrl" exists and if yes, return value
                if (currentNews.has(KEY_URL_NEWS)) {
                    url = currentNews.getString(KEY_URL_NEWS);
                } else {
                    url = null;
                }

                //Check if key "webPublicationDate" exists and if yes, return value.
                //Show only the first 10 characters of the date
                if (currentNews.has(KEY_DATE)) {
                    rawDate = currentNews.getString(KEY_DATE);
                    date = rawDate.substring(0, 10);
                } else {
                    date = null;
                }

                //Create a new News object from the data and add it to the list
                news.add(new News(title, section, url, date));
            }

        } catch (JSONException e) {
            //If it fails, print the error to the log
            Log.e(LOG_TAG, "Fetching data from JSON failed", e);
        }
        return news;
    }
}
