package com.tobiasandre.filmespopulares.networkutils;

import android.os.AsyncTask;
import android.util.Log;

import com.tobiasandre.filmespopulares.BuildConfig;
import com.tobiasandre.filmespopulares.model.Lists.Reviews;
import com.tobiasandre.filmespopulares.model.Review;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tobias Andre Eggers on 3/31/17.
 */

public class GetReviewsTask extends AsyncTask<Long, Void, List<Review>> {

    public static String TAG = GetReviewsTask.class.getSimpleName();
    private final Listener mListener;


    public interface Listener {
        void onReviewsFetchFinished(List<Review> reviews);
    }

    public GetReviewsTask(Listener listener) {
        mListener = listener;
    }

    @Override
    protected List<Review> doInBackground(Long... params) {

        if (params.length == 0) {
            return null;
        }
        long movieId = params[0];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MoviesService service = retrofit.create(MoviesService.class);
        Call<Reviews> call = service.findReviewsById(movieId,
                BuildConfig.apy_key);
        try {
            Response<Reviews> response = call.execute();
            Reviews reviews = response.body();
            return reviews.getReviews();
        } catch (IOException e) {
            Log.e(TAG, "Ocorreu um problema ao obter as reviews ", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        if (reviews != null) {
            mListener.onReviewsFetchFinished(reviews);
        } else {
            mListener.onReviewsFetchFinished(new ArrayList<Review>());
        }
    }
}
