package com.example.user.newsapp;

/**
 * Created by Alexander Rashkov on 6/16/2017.
 */

public class News {

    //Tile of the news
    private String mTitle;

    //Section of the news
    private String mSection;

    //Url of the news
    private String mUrl;

    //Date of the news
    private String mDate;

    /**
     * Constructs a new {@link News} object.
     *
     * @param title   is the title of the news
     * @param section is the section of the news
     * @param url     is the web address of the news
     * @param date    is the date when the news was published
     */

    public News(String title, String section, String url, String date) {
        mTitle = title;
        mSection = section;
        mUrl = url;
        mDate = date;
    }

    //Gets the title method
    public String getTitle() {
        return mTitle;
    }

    //Gets the section method
    public String getSection() {
        return mSection;
    }

    //Gets the url method
    public String getUrl() {
        return mUrl;
    }

    //Gets the date method
    public String getDate() {
        return mDate;
    }

}
