package com.tobiasandre.filmespopulares.networkutils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.StringDef;
import android.util.Log;

import com.tobiasandre.filmespopulares.BuildConfig;
import com.tobiasandre.filmespopulares.model.Lists.Movies;
import com.tobiasandre.filmespopulares.model.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class GetMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

    public static String TAG = GetMoviesTask.class.getSimpleName();

    public final static String POPULAR = "popular";
    public final static String MAIS_VOTADO = "top_rated";
    public final static String FAVORITO = "favorites";


    @StringDef({POPULAR, MAIS_VOTADO, FAVORITO})
    public @interface SORT_BY {
    }

    private final NotifyTaskCompletedCommand mCommand;
    private
    @SORT_BY
    String mSortBy = POPULAR;

    public interface Listener {
        void onGetFinished(CommandExec command);
    }

    public static class NotifyTaskCompletedCommand implements CommandExec {
        private GetMoviesTask.Listener mListener;
        private List<Movie> mMovies;

        public NotifyTaskCompletedCommand(GetMoviesTask.Listener listener) {
            mListener = listener;
        }

        @Override
        public void execute() {
            mListener.onGetFinished(this);
        }

        public List<Movie> getMovies() {
            return mMovies;
        }
    }

    public GetMoviesTask(@SORT_BY String sortBy, NotifyTaskCompletedCommand command) {
        mCommand = command;
        mSortBy = sortBy;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies != null) {
            mCommand.mMovies = movies;
        } else {
            mCommand.mMovies = new ArrayList<>();
        }
        mCommand.execute();

    }

    @Override
    protected List<Movie> doInBackground(Void... params) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MoviesService service = retrofit.create(MoviesService.class);
        Call<Movies> call = service.discoverMovies(mSortBy,
                BuildConfig.apy_key);// api_key ***************
        try {
            Response<Movies> response = call.execute();
            Movies movies = response.body();
            return movies.getMovies();

        } catch (IOException e) {
            Log.e(TAG, "Ocorreu um problema ao obter os filmes:", e);
        }
        return null;
    }


}
