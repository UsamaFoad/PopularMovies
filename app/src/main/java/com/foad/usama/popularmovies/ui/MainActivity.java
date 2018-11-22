package com.foad.usama.popularmovies.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.adapters.MovieAdapter;
import com.foad.usama.popularmovies.data.AppPreferences;
import com.foad.usama.popularmovies.databinding.ActivityMainBinding;
import com.foad.usama.popularmovies.models.Movie;
import com.foad.usama.popularmovies.utils.GridItemDecoration;
import com.foad.usama.popularmovies.utils.RecyclerViewScrollListener;
import com.foad.usama.popularmovies.viewmodel.MainViewModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.List;
import java.util.Objects;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.foad.usama.popularmovies.BuildConfig.API_KEY;

public class MainActivity extends AppCompatActivity {

    public static final int NO_INTERNET = 1;

    // Key index for the menu selection
    // FAVORITES should be public to be
    // available for MovieAdapter
    public static final int FAVORITES = 1;
    private static final int POPULAR = 0;
    private static final int UPCOMING = 2;
    private static final int TOP_RATED = 3;
    private static final int NOW_PLAYING = 4;

    // keys for savedInstanceState bundle
    private static final String BUNDLE_PAGE = "page";
    private static final String BUNDLE_COUNT = "count";
    private static final String BUNDLE_RECYCLER = "recycler";
    private static final String BUNDLE_PREF = "pref";

    // Display Width in Pixels
    private static final int WIDTH_1000_PIXELS = 1000;
    private static final int WIDTH_1200_PIXELS = 1200;
    private static final int WIDTH_1700_PIXELS = 1700;

    private ActivityMainBinding mBinding;
    private MovieAdapter mMoviesAdapter;
    private RecyclerViewScrollListener mScrollListener;
    private Bundle mSavedInstanceState;
    private GridLayoutManager mGridLayoutManager;
    private MainViewModel mViewModel;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        String noAPI_KEY = getString(R.string.no_api_key);
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setPresenter(this);

        mGridLayoutManager = new GridLayoutManager(this, numberOfColumns());
        mMoviesAdapter = new MovieAdapter(this);

        mBinding.moviesList.setLayoutManager(mGridLayoutManager);
        mBinding.moviesList.addItemDecoration(new GridItemDecoration(this));
        mBinding.moviesList.setAdapter(mMoviesAdapter);

        mBinding.swipeRefreshLayout.setEnabled(false);

        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        showInternetStatus();

        mScrollListener = new RecyclerViewScrollListener(mGridLayoutManager) {
            @Override
            public void onLoadMore(int page) {
                int sorting = AppPreferences.getSorting(MainActivity.this);
                mViewModel.loadMovies(sorting, page);
            }
        };

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mViewModel.getStatus().setValue(0);
                int sorting = AppPreferences.getSorting(MainActivity.this);
                populateUI(sorting);
            }
        });
        if (Objects.equals(API_KEY, "")) {
            Toast.makeText(this, noAPI_KEY, Toast.LENGTH_LONG).show();
        }

        MobileAds.initialize(this, String.valueOf(R.string.AdMob_ID));
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_PAGE, mScrollListener.getPage());
        outState.putInt(BUNDLE_COUNT, mScrollListener.getCount());
        outState.putInt(BUNDLE_PREF, AppPreferences.getSorting(this));
        outState.putParcelable(BUNDLE_RECYCLER, mGridLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSavedInstanceState = savedInstanceState;
    }

    /**
     * Main reason is refresh the movie's favorite icon if it is selected in Detail Activity
     */
    @Override
    protected void onResume() {
        super.onResume();
        mMoviesAdapter.refreshFavorite();
    }

    private void populateUI(int selected) {
        mViewModel.getPopularMovies().removeObservers(MainActivity.this);
        mViewModel.getHighestMovies().removeObservers(MainActivity.this);
        mViewModel.getUpcomingMovies().removeObservers(MainActivity.this);
        mViewModel.getFavoriteMovies().removeObservers(MainActivity.this);
        mViewModel.getNowPlayingMovies().removeObservers(MainActivity.this);

        mMoviesAdapter.clearList();
        hideStatus();

        switch (selected) {
            case POPULAR:
                mViewModel.getPopularMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;

            case UPCOMING:
                mViewModel.getUpcomingMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;

            case TOP_RATED:
                mViewModel.getHighestMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;

            case NOW_PLAYING:
                mViewModel.getNowPlayingMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                mMoviesAdapter.addMoviesList(movies);
                            }
                        });
                break;

            default:
                mBinding.swipeRefreshLayout.setEnabled(false);
                mViewModel.getFavoriteMovies().observe(MainActivity.this,
                        new Observer<List<Movie>>() {
                            @Override
                            public void onChanged(@Nullable List<Movie> movies) {
                                if (mMoviesAdapter.getItemCount() < Objects.requireNonNull(movies).size()) {
                                    hideStatus();
                                    mMoviesAdapter.addMoviesList(movies);
                                } else if (movies.size() == 0) {
                                    showNoFavoriteStatus();
                                }
                            }
                        });
        }

        if (mSavedInstanceState != null && selected == mSavedInstanceState.getInt(BUNDLE_PREF)) {
            if (selected == FAVORITES) {
                mBinding.moviesList.clearOnScrollListeners();
            } else {
                mScrollListener.setState(
                        mSavedInstanceState.getInt(BUNDLE_PAGE),
                        mSavedInstanceState.getInt(BUNDLE_COUNT));
                mBinding.moviesList.addOnScrollListener(mScrollListener);
            }
            mGridLayoutManager
                    .onRestoreInstanceState(mSavedInstanceState.getParcelable(BUNDLE_RECYCLER));
        } else {
            if (selected == FAVORITES) {
                mBinding.moviesList.clearOnScrollListeners();
            } else {
                mScrollListener.resetState();
                mBinding.moviesList.addOnScrollListener(mScrollListener);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem item = menu.findItem(R.id.sort_spinner);
        Spinner spinner = (Spinner) item.getActionView();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_spinner_list, R.layout.sort_spinner_item);
        adapter.setDropDownViewResource(R.layout.sort_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(AppPreferences.getSorting(MainActivity.this));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int selected, long l) {
                AppPreferences.setSorting(MainActivity.this, selected);
                populateUI(selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        return true;
    }

    /**
     * calculates number of columns
     *
     * @return the number of columns in the grid layout of main ui
     */
    private int numberOfColumns() {
        final int SPAN_COUNT_TWO = 2;
        final int SPAN_COUNT_THREE = 3;
        final int SPAN_COUNT_FOUR = 4;
        final int SPAN_COUNT_FIVE = 5;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            if (width > WIDTH_1000_PIXELS) {
                return SPAN_COUNT_THREE;
            } else {
                return SPAN_COUNT_TWO;
            }
        } else {
            if (width > WIDTH_1700_PIXELS) {
                return SPAN_COUNT_FIVE;
            } else if (width > WIDTH_1200_PIXELS) {
                return SPAN_COUNT_FOUR;
            } else {
                return SPAN_COUNT_THREE;
            }
        }
    }

    /**
     * shows no internet text view and enables swipe refresh
     */
    private void showInternetStatus() {
        mViewModel.getStatus().observe(MainActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer status) {
                int sorting = AppPreferences.getSorting(MainActivity.this);
                if (sorting != FAVORITES) {
                    mBinding.swipeRefreshLayout.setRefreshing(false);
                    if (status != null) {
                        if (status == NO_INTERNET) {
                            mBinding.statusImage.setImageResource(R.drawable.ic_signal_wifi_off_white_24px);
                            mBinding.statusImage.setVisibility(View.VISIBLE);
                            mBinding.statusText.setText(R.string.no_internet);
                            mBinding.statusText.setVisibility(View.VISIBLE);
                            mBinding.swipeRefreshLayout.setEnabled(true);
                        }
                    } else {
                        mBinding.swipeRefreshLayout.setEnabled(false);
                        hideStatus();
                    }
                }
            }
        });
    }

    /**
     * Shows no favorite text view
     */
    private void showNoFavoriteStatus() {
        mBinding.statusImage.setImageResource(R.drawable.ic_star_border_white_24px);
        mBinding.statusImage.setVisibility(View.VISIBLE);
        mBinding.statusText.setText(R.string.no_favorite);
        mBinding.statusText.setVisibility(View.VISIBLE);
    }

    /**
     * Hides status text view
     */
    private void hideStatus() {
        mBinding.statusImage.setVisibility(View.INVISIBLE);
        mBinding.statusText.setVisibility(View.INVISIBLE);
    }

    public void notThisStar() {
        Snackbar.make(mBinding.mainLayout, getString(R.string.not_this_star), Snackbar.LENGTH_LONG).show();
    }
}