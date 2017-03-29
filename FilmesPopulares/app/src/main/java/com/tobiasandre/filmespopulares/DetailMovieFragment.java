package com.tobiasandre.filmespopulares;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tobiasandre.filmespopulares.adapter.TrailerAdapter;
import com.tobiasandre.filmespopulares.data.FilmesPopularesContract;
import com.tobiasandre.filmespopulares.networkutils.GetTrailersTask;
import com.tobiasandre.filmespopulares.networkutils.Movie;
import com.tobiasandre.filmespopulares.networkutils.Review;
import com.tobiasandre.filmespopulares.networkutils.Trailer;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DetailMovieFragment extends Fragment implements GetTrailersTask.Listener,
        TrailerAdapter.Callbacks {

    public static final String ARG_MOVIE = "ARG_MOVIE";
    public static final String TRAILERS_EXTRAS = "TRAILERS_EXTRAS";


    private Movie mMovie;
    private TrailerAdapter mTrailerListAdapter;
    private ShareActionProvider mShareActionProvider;


    @Bind(R.id.trailer_list)
    RecyclerView mRecyclerViewForTrailers;
    @Bind(R.id.titulo_filme)
    TextView mMovieTitleView;
    @Bind(R.id.movie_overview)
    TextView mMovieOverviewView;
    @Bind(R.id.movie_release_date)
    TextView mMovieReleaseDateView;
    @Bind(R.id.movie_avaliacao)
    TextView mMovieRatingView;
    @Bind(R.id.movie_poster)
    ImageView mMoviePosterView;

    @Bind(R.id.button_watch_trailer)
    Button mButtonWatchTrailer;
    @Bind(R.id.btn_add_favoritos)
    Button mButtonMarkAsFavorite;
    @Bind(R.id.btn_remover_de_favoritos)
    Button mButtonRemoveFromFavorites;

    public DetailMovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
                activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null && activity instanceof DetailMovieActivity) {
            appBarLayout.setTitle(mMovie.getTitle());
        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));
        if (movieBackdrop != null) {
            Picasso.with(activity)
                    .load(mMovie.getBackdropUrl(getContext()))
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail_movie, container, false);
        ButterKnife.bind(this, rootView);

        mMovieTitleView.setText(mMovie.getTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate(getContext()));
        String userRatingStr = getResources().getString(R.string.avaliacao_filme,
                mMovie.getVoteAverage());
        mMovieRatingView.setText(userRatingStr);

        Picasso.with(getContext())
                .load(mMovie.getPosterUrl(getContext()))
                .config(Bitmap.Config.RGB_565)
                .into(mMoviePosterView);

        updateFavoriteButtons();


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
        mTrailerListAdapter = new TrailerAdapter(new ArrayList<Trailer>(), this);
        mRecyclerViewForTrailers.setAdapter(mTrailerListAdapter);
        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);

        if (savedInstanceState != null && savedInstanceState.containsKey(TRAILERS_EXTRAS)) {
            List<Trailer> trailers = savedInstanceState.getParcelableArrayList(TRAILERS_EXTRAS);
            mTrailerListAdapter.add(trailers);
            mButtonWatchTrailer.setEnabled(true);
        } else {
            fetchTrailers();
        }

        return rootView;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_compartilhar, menu);
        MenuItem shareTrailerMenuItem = menu.findItem(R.id.compartilhar);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareTrailerMenuItem);
    }

    @Override
    public void assistir(Trailer trailer, int position) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
    }

    @Override
    public void onFetchFinished(List<Trailer> trailers) {
        mTrailerListAdapter.add(trailers);
        mButtonWatchTrailer.setEnabled(!trailers.isEmpty());

        if (mTrailerListAdapter.getItemCount() > 0) {
            Trailer trailer = mTrailerListAdapter.getTrailers().get(0);
            updateShareActionProvider(trailer);
        }
    }

    private void fetchTrailers() {
        GetTrailersTask task = new GetTrailersTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    public void markAsFavorite() {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (!isFavorite()) {
                    ContentValues movieValues = new ContentValues();
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_ID,
                            mMovie.getId());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_TITLE,
                            mMovie.getTitle());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                            mMovie.getPosterPath());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                            mMovie.getOverview());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                            mMovie.getVoteAverage());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                            mMovie.getReleaseDate());
                    movieValues.put(FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                            mMovie.getBackdropPath());
                    getContext().getContentResolver().insert(
                            FilmesPopularesContract.MovieEntry.CONTENT_URI,
                            movieValues
                    );
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void removeFromFavorites() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    getContext().getContentResolver().delete(FilmesPopularesContract.MovieEntry.CONTENT_URI,
                            FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavoriteButtons();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void updateFavoriteButtons() {

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                return isFavorite();
            }

            @Override
            protected void onPostExecute(Boolean isFavorite) {
                if (isFavorite) {
                    mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
                    mButtonMarkAsFavorite.setVisibility(View.GONE);
                } else {
                    mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
                    mButtonRemoveFromFavorites.setVisibility(View.GONE);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markAsFavorite();
                    }
                });

        mButtonWatchTrailer.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTrailerListAdapter.getItemCount() > 0) {
                            assistir(mTrailerListAdapter.getTrailers().get(0), 0);
                        }
                    }
                });

        mButtonRemoveFromFavorites.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeFromFavorites();
                    }
                });
    }

    private boolean isFavorite() {
        Cursor movieCursor = getContext().getContentResolver().query(
                FilmesPopularesContract.MovieEntry.CONTENT_URI,
                new String[]{FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_ID},
                FilmesPopularesContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }

    private void updateShareActionProvider(Trailer trailer) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getTitle());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, trailer.getName() + ": "
                + trailer.getTrailerUrl());
        mShareActionProvider.setShareIntent(sharingIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Trailer> trailers = mTrailerListAdapter.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            outState.putParcelableArrayList(TRAILERS_EXTRAS, trailers);
        }
    }


}
