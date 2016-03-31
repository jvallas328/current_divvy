package com.example.jordan.divvyupv12;

/**
 * Created by Brandon on 11/12/2015.
 * Came from http://stackoverflow.com/questions/1944656/android-global-variable for anyone that
 * wants to see it
 * We will probably have to use this for all global variables we use in our app.
 * To make another global variable just do exactly what I did for userID
 * To access the variables in other classes use Globals.getInstance().nameofvariableyouwanttoaccess
 * Fun fact of the day: according to the person who posted this, this is the singleton approach that
 * we learned about in class! Wow!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */
public class Globals {
    private static Globals Instance = null;
    public String userID;
    public String username;
    protected Globals(){};
    public static synchronized Globals getInstance(){
        if(null == Instance){
            Instance = new Globals();
        }
        return Instance;
    }
}
