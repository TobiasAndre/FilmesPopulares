package com.tobiasandre.filmespopulares.networkutils;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import android.util.Log;

import com.tobiasandre.filmespopulares.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class Movie implements Parcelable {

    public static final String TAG = Movie.class.getSimpleName();

    @SerializedName("id")
    private long idMovie;
    @SerializedName("original_title")
    private String mOriginalTitle;
    @SerializedName("poster_path")
    private String mPosterPath;
    @SerializedName("overview")
    private String mOverview;
    @SerializedName("vote_average")
    private String mVoteAverage;
    @SerializedName("release_date")
    private String mReleaseDate;
    @SerializedName("backdrop_path")
    private String mBackdropPath;

    private Movie() {
    }

    public Movie(long id, String titleMovie, String posterPath, String detailOverview, String voteAverage,
                 String releaseDate, String backdropPath) {
        idMovie = id;
        mOriginalTitle = titleMovie;
        mPosterPath = posterPath;
        mOverview = detailOverview;
        mVoteAverage = voteAverage;
        mReleaseDate = releaseDate;
        mBackdropPath = backdropPath;
    }

    @Nullable
    public String getTitle() {
        return mOriginalTitle;
    }

    public long getId() {
        return idMovie;
    }

    @Nullable
    public String getPosterUrl(Context context) {
        if (mPosterPath != null && !mPosterPath.isEmpty()) {
            return context.getResources().getString(R.string.url_downloading_poster) + mPosterPath;
        }
        return null;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getReleaseDate(Context context) {
        String inputPattern = "yyyy-MM-dd";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern, Locale.US);
        if (mReleaseDate != null && !mReleaseDate.isEmpty()) {
            try {
                Date date = inputFormat.parse(mReleaseDate);
                return DateFormat.getDateInstance().format(date);
            } catch (ParseException e) {
                Log.e(TAG, "Erro ao converter a data: " + mReleaseDate);
            }
        } else {
            mReleaseDate = context.getString(R.string.faltando_release_date);
        }

        return mReleaseDate;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    @Nullable
    public String getOverview() {
        return mOverview;
    }

    @Nullable
    public String getVoteAverage() {
        return mVoteAverage;
    }

    @Nullable
    public String getBackdropUrl(Context context) {
        if (mBackdropPath != null && !mBackdropPath.isEmpty()) {
            return context.getResources().getString(R.string.url_downloading_backdrop) +
                    mBackdropPath;
        }
        return null;
    }

    public String getBackdropPath() {
        return mBackdropPath;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Creator<Movie>() {
        public Movie createFromParcel(Parcel source) {
            Movie movie = new Movie();
            movie.idMovie = source.readLong();
            movie.mOriginalTitle = source.readString();
            movie.mPosterPath = source.readString();
            movie.mOverview = source.readString();
            movie.mVoteAverage = source.readString();
            movie.mReleaseDate = source.readString();
            movie.mBackdropPath = source.readString();
            return movie;
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(idMovie);
        parcel.writeString(mOriginalTitle);
        parcel.writeString(mPosterPath);
        parcel.writeString(mOverview);
        parcel.writeString(mVoteAverage);
        parcel.writeString(mReleaseDate);
        parcel.writeString(mBackdropPath);
    }
}
