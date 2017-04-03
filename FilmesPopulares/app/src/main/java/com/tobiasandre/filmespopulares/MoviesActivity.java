package com.tobiasandre.filmespopulares;

import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.tobiasandre.filmespopulares.adapter.MovieAdapter;
import com.tobiasandre.filmespopulares.data.FilmesPopularesContract;
import com.tobiasandre.filmespopulares.networkutils.CommandExec;
import com.tobiasandre.filmespopulares.networkutils.GetMoviesTask;
import com.tobiasandre.filmespopulares.model.Movie;
import com.tobiasandre.filmespopulares.networkutils.Utilidades;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MoviesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        GetMoviesTask.Listener ,MovieAdapter.Callbacks {


    private boolean mTablet;
    private MovieAdapter mAdapter;
    private String mSortBy = GetMoviesTask.POPULAR;
    private RetainedFragment mRetainedFragment;
    private static final int LOAD_FAVORITE_MOVIES = 0;
    private static final String FILMES_EXTRAS = "FILMES_EXTRAS";
    private static final String ORDEM_EXTRAS = "ORDEM_EXTRAS";

    @Bind(R.id.movie_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        ButterKnife.bind(this);


        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        String tag = RetainedFragment.class.getName();
        this.mRetainedFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (this.mRetainedFragment == null) {
            this.mRetainedFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(this.mRetainedFragment, tag).commit();
        }


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources()
                .getInteger(R.integer.grid_number_cols)));

        mAdapter = new MovieAdapter(new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);

        mTablet = findViewById(R.id.movie_detail_container) != null;


        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getString(ORDEM_EXTRAS);
            if (savedInstanceState.containsKey(FILMES_EXTRAS)) {
                List<Movie> movies = savedInstanceState.getParcelableArrayList(FILMES_EXTRAS);
                mAdapter.add(movies);
                findViewById(R.id.progressao).setVisibility(View.GONE);

                if (mSortBy.equals(GetMoviesTask.FAVORITO)) {
                    getSupportLoaderManager().initLoader(LOAD_FAVORITE_MOVIES, null, this);
                }
            }
            updateEmptyState();
        } else {
            getMovies(mSortBy);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Movie> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(FILMES_EXTRAS, movies);
        }
        outState.putString(ORDEM_EXTRAS, mSortBy);


        if (!mSortBy.equals(GetMoviesTask.FAVORITO)) {
            getSupportLoaderManager().destroyLoader(LOAD_FAVORITE_MOVIES);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_principal, menu);

        switch (mSortBy) {
            case GetMoviesTask.POPULAR:
                menu.findItem(R.id.ordem_mais_popular).setChecked(true);
                break;
            case GetMoviesTask.MAIS_VOTADO:
                menu.findItem(R.id.ordem_mais_votados).setChecked(true);
                break;
            case GetMoviesTask.FAVORITO:
                menu.findItem(R.id.ordem_favoritos).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ordem_mais_votados:
                if (mSortBy.equals(GetMoviesTask.FAVORITO)) {
                    getSupportLoaderManager().destroyLoader(LOAD_FAVORITE_MOVIES);
                }
                mSortBy = GetMoviesTask.MAIS_VOTADO;
                getMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.ordem_mais_popular:
                if (mSortBy.equals(GetMoviesTask.FAVORITO)) {
                    getSupportLoaderManager().destroyLoader(LOAD_FAVORITE_MOVIES);
                }
                mSortBy = GetMoviesTask.POPULAR;
                getMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.ordem_favoritos:
                mSortBy = GetMoviesTask.FAVORITO;
                item.setChecked(true);
                getMovies(mSortBy);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMovies(String sortBy) {

        final ImageButton btnReload = (ImageButton)findViewById(R.id.btn_reload);
        if(btnReload!=null){
            btnReload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getMovies(mSortBy);
                }
            });
        }

        if (Utilidades.isNetworkConnected(this.getBaseContext())) {
            if (!sortBy.equals(GetMoviesTask.FAVORITO)) {
                findViewById(R.id.progressao).setVisibility(View.VISIBLE);
                GetMoviesTask.NotifyTaskCompletedCommand command =
                        new GetMoviesTask.NotifyTaskCompletedCommand(this.mRetainedFragment);
                new GetMoviesTask(sortBy, command).execute();
            } else {
                getSupportLoaderManager().initLoader(LOAD_FAVORITE_MOVIES, null, this);
            }
        }else{


            final CoordinatorLayout MainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id
                    .MainCoordinatorLayout);

            Snackbar snackbar = Snackbar.make(MainCoordinatorLayout, getString(R.string.texto_offline), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(getString(R.string.retry), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnReload.callOnClick();
                }
            });
            snackbar.show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        findViewById(R.id.progressao).setVisibility(View.VISIBLE);
        return new CursorLoader(this,
                FilmesPopularesContract.MovieEntry.CONTENT_URI,
                FilmesPopularesContract.MovieEntry.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.add(cursor);
        updateEmptyState();
        findViewById(R.id.progressao).setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void open(Movie movie, int position) {
        Intent intent = new Intent(this, DetailMovieActivity.class);
        intent.putExtra(DetailMovieFragment.ARG_MOVIE, movie);
        startActivity(intent);
    }

    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            if (mSortBy.equals(GetMoviesTask.FAVORITO)) {
                findViewById(R.id.content_offline).setVisibility(View.GONE);
                findViewById(R.id.content_sem_favoritos).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.content_offline).setVisibility(View.VISIBLE);
                findViewById(R.id.content_sem_favoritos).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.content_offline).setVisibility(View.GONE);
            findViewById(R.id.content_sem_favoritos).setVisibility(View.GONE);
        }
    }

    @Override
    public void onGetFinished(CommandExec command) {
        if (command instanceof GetMoviesTask.NotifyTaskCompletedCommand) {
            mAdapter.add(((GetMoviesTask.NotifyTaskCompletedCommand) command).getMovies());
            updateEmptyState();
            findViewById(R.id.progressao).setVisibility(View.GONE);
        }
    }

    public static class RetainedFragment extends Fragment implements GetMoviesTask.Listener {

        private boolean mPaused = false;

        private CommandExec mWaitingCommand = null;

        public RetainedFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPaused = true;
        }

        @Override
        public void onResume() {
            super.onResume();
            mPaused = false;
            if (mWaitingCommand != null) {
                onGetFinished(mWaitingCommand);
            }
        }

        @Override
        public void onGetFinished(CommandExec command) {
            if (getActivity() instanceof GetMoviesTask.Listener && !mPaused) {
                GetMoviesTask.Listener listener = (GetMoviesTask.Listener) getActivity();
                listener.onGetFinished(command);
                mWaitingCommand = null;
            } else {
                mWaitingCommand = command;
            }
        }
    }
}
