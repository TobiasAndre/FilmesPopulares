package com.tobiasandre.filmespopulares.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class FilmesPopularesDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "filmespopulares.db";

    public FilmesPopularesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + FilmesPopularesContract.MovieEntry.TABLE_NAME
                + " (" +
                FilmesPopularesContract.MovieEntry._ID + " INTEGER PRIMARY KEY," +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE + " TEXT NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT NOT NULL " +
                " );";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FilmesPopularesContract.MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
