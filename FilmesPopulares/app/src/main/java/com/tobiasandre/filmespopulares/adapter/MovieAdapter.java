package com.tobiasandre.filmespopulares.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Callback;
import com.tobiasandre.filmespopulares.R;
import com.tobiasandre.filmespopulares.data.FilmesPopularesContract;
import com.tobiasandre.filmespopulares.model.Movie;

import java.util.ArrayList;
import java.util.List;


import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder>{

    private final static String TAG = MovieAdapter.class.getSimpleName();

    private final ArrayList<Movie> mMovies;
    private final Callbacks mCallbacks;

    public interface Callbacks {
        void open(Movie movie, int position);
    }

    public MovieAdapter(ArrayList<Movie> movies, Callbacks callbacks) {
        mMovies = movies;
        this.mCallbacks = callbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_movie_list, parent, false);
        final Context context = view.getContext();

        int gridColsNumber = context.getResources()
                .getInteger(R.integer.grid_number_cols);

        view.getLayoutParams().height = (int) (parent.getWidth() / gridColsNumber *
                1.5f);

        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Movie movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        holder.mTitleView.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl(context);

        if (posterUrl == null) {
            holder.mTitleView.setVisibility(View.VISIBLE);
        }

        Picasso.with(context)
                .load(movie.getPosterUrl(context))
                .config(Bitmap.Config.RGB_565)
                .into(holder.mPosterView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (holder.mMovie.getId() != movie.getId()) {
                                    holder.cleanUp();
                                } else {
                                    holder.mPosterView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError() {
                                holder.mTitleView.setVisibility(View.VISIBLE);
                            }
                        }
                );

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    @Bind(R.id.poster)
    ImageView mPosterView;
    @Bind(R.id.titulo)
    TextView mTitleView;
    public Movie mMovie;

    public ViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        mView = view;
    }

    public void cleanUp() {
        final Context context = mView.getContext();
        Picasso.with(context).cancelRequest(mPosterView);
        mPosterView.setImageBitmap(null);
        mPosterView.setVisibility(View.INVISIBLE);
        mTitleView.setVisibility(View.GONE);
    }

}

    public void add(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(FilmesPopularesContract.MovieEntry.COL_MOVIE_ID);
                String title = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_TITLE);
                String posterPath = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_OVERVIEW);
                String rating = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(FilmesPopularesContract.MovieEntry.COL_MOVIE_BACKDROP_PATH);
                Movie movie = new Movie(id, title, posterPath, overview, rating, releaseDate, backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }


}
