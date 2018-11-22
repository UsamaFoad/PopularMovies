package com.foad.usama.popularmovies.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.adapters.ReviewAdapter;
import com.foad.usama.popularmovies.adapters.VideoAdapter;
import com.foad.usama.popularmovies.data.AppDatabase;
import com.foad.usama.popularmovies.data.AppPreferences;
import com.foad.usama.popularmovies.databinding.ActivityDetailBinding;
import com.foad.usama.popularmovies.models.ApiResponse;
import com.foad.usama.popularmovies.models.MiniMovie;
import com.foad.usama.popularmovies.models.Movie;
import com.foad.usama.popularmovies.models.MovieDetail;
import com.foad.usama.popularmovies.models.Review;
import com.foad.usama.popularmovies.models.Video;
import com.foad.usama.popularmovies.rest.ApiClient;
import com.foad.usama.popularmovies.rest.ServiceGenerator;
import com.foad.usama.popularmovies.utils.HorizontalItemDecoration;
import com.foad.usama.popularmovies.utils.MyExecutor;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    public static final String DETAIL_INTENT_KEY = "com.foad.usama.popularmovies.ui.detail";
    public static final String MOVIE_NUMBER_KEY = "com.foad.usama.popularmovies.ui.movie_number";

    private static final String BUNDLE_VIDEOS = "videos";
    private static final String BUNDLE_REVIEWS = "reviews";

    private ActivityDetailBinding mBinding;
    private AppDatabase mDatabase;
    private boolean isFavorite;
    private VideoAdapter mVideoAdapter;
    private ReviewAdapter mReviewAdapter;
    private Movie movie;
    private ApiClient mApiClient;
    private Executor executor;
    private int movieNumber;
    private int color;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);


        // Making Collapsing Toolbar Width / Height Ratio = 3 / 2
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            int width = displaymetrics.widthPixels;
            mBinding.collapsingToolbar.getLayoutParams().height = (int) Math.round(width / 1.5);
        }
        // END OF Making Collapsing Toolbar Width / Height Ratio = 3 / 2

        mDatabase = AppDatabase.getDatabase(this);

        mApiClient = ServiceGenerator.createService(ApiClient.class);
        executor = new MyExecutor();

        Intent intent = getIntent();
        if (intent != null) {
            movieNumber = intent.getIntExtra(MOVIE_NUMBER_KEY, -1);
            movie = intent.getParcelableExtra(DETAIL_INTENT_KEY);
            mBinding.setMovie(movie);
            mBinding.setPresenter(this);
            setSupportActionBar(mBinding.toolbar);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        populateUI();
        populateVideos(savedInstanceState);
        populateReviews(savedInstanceState);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BUNDLE_VIDEOS, mVideoAdapter.getList());
        outState.putParcelableArrayList(BUNDLE_REVIEWS, mReviewAdapter.getList());
    }

    /**
     * populates UI of Detail Activity except Videos and Reviews
     */
    private void populateUI() {

        Target targetBackdrop = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mBinding.backdrop.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        color = palette.getMutedColor(R.attr.colorPrimary) | 0xFF000000;
                        mBinding.collapsingToolbar.setContentScrimColor(color);
                        mBinding.collapsingToolbar.setStatusBarScrimColor(color);
                    }
                });
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        Picasso.get()
                .load(getString(R.string.image_path_w780) + movie.backdropPath)
                .into(targetBackdrop);

        Picasso.get()
                .load(getString(R.string.image_path_w342) + movie.posterPath)
                .placeholder(R.drawable.template_movie)
                .error(R.drawable.error)
                .into(mBinding.movieDetails.poster);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                MiniMovie miniMovie = mDatabase.movieDao().getMovieById(movie.movieId);

                if (miniMovie != null) {
                    isFavorite = true;
                    mBinding.favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
                } else {
                    isFavorite = false;
                    mBinding.favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);
                }
            }
        });
    }

    /**
     * Fetch videos and populate their views
     */
    private void populateVideos(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieVideos.videosList.setLayoutManager(layoutManager);
        mBinding.movieVideos.videosList.setHasFixedSize(true);
        mBinding.movieVideos.videosList.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        mBinding.movieVideos.videosList.addItemDecoration(itemDecoration);

        mVideoAdapter = new VideoAdapter(this);
        mBinding.movieVideos.videosList.setAdapter(mVideoAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEOS)) {
            mVideoAdapter.addVideosList(savedInstanceState.
                    <Video>getParcelableArrayList(BUNDLE_VIDEOS));
        } else {
            Call<ApiResponse<Video>> call = mApiClient.getVideos(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Video>>() {
                @Override
                public void onResponse(Call<ApiResponse<Video>> call,
                                       Response<ApiResponse<Video>> response) {
                    List<Video> result = response.body().results;
                    mVideoAdapter.addVideosList(result);
                    if (result.size() == 0) {
                        mBinding.movieVideos.videosLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Fetch reviews and populate their views
     */
    private void populateReviews(Bundle savedInstanceState) {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBinding.movieReviews.reviewsList.setLayoutManager(layoutManager);
        mBinding.movieReviews.reviewsList.setHasFixedSize(true);
        mBinding.movieReviews.reviewsList.setNestedScrollingEnabled(false);

        RecyclerView.ItemDecoration itemDecoration = new HorizontalItemDecoration(this);
        mBinding.movieReviews.reviewsList.addItemDecoration(itemDecoration);

        mReviewAdapter = new ReviewAdapter(this);
        mBinding.movieReviews.reviewsList.setAdapter(mReviewAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEWS)) {
            mReviewAdapter.addReviewsList(savedInstanceState.<Review>getParcelableArrayList(BUNDLE_REVIEWS));
        } else {
            Call<ApiResponse<Review>> call = mApiClient.getReviews(movie.movieId);

            call.enqueue(new Callback<ApiResponse<Review>>() {
                @Override
                public void onResponse(Call<ApiResponse<Review>> call,
                                       Response<ApiResponse<Review>> response) {
                    List<Review> result = Objects.requireNonNull(response.body()).results;
                    mReviewAdapter.addReviewsList(result);
                    if (result.size() == 0) {
                        mBinding.movieReviews.reviewsLabel.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Review>> call, Throwable t) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Adds the movie to favorite or remove it if it already exists
     * adding favorite means adds it to sql database
     */
    public void onClickFavoriteButton() {
        AppPreferences.setChangedMovie(this, movieNumber);

        String snackBarText;
        if (isFavorite) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mDatabase.movieDao().delete(movie);
                }
            });
            isFavorite = false;
            mBinding.favoriteButton.setImageResource(R.drawable.ic_star_border_white_24px);
            snackBarText = getString(R.string.remove_favorite);
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    mDatabase.movieDao().insert(movie);
                }
            });
            isFavorite = true;
            mBinding.favoriteButton.setImageResource(R.drawable.ic_star_white_24px);
            snackBarText = getString(R.string.add_favorite);
        }
        Snackbar.make(mBinding.coordinatorLayout, snackBarText, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share:
                String shareText = getString(R.string.the_movie_uri) + movie.movieId;
                ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                        .setText(shareText)
                        .setType(getString(R.string.type_text_plain));
                recordSharedMovie(shareText);
                try {
                    intentBuilder.startChooser();
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, R.string.no_app, Toast.LENGTH_LONG).show();
                }
                return true;
            case android.R.id.home:
                mBinding.favoriteButton.setVisibility(View.INVISIBLE);
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mBinding.favoriteButton.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    public String formatReleaseDate(String releaseDate) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.simple_data_format));
        Date date;
        try {
            date = sdf.parse(releaseDate);
        } catch (ParseException e) {
            return releaseDate;
        }

        return DateFormat.getDateInstance(DateFormat.LONG).format(date);
    }

    public String getEnglishPlotSynopsis(String id) {
        Call<MovieDetail> call = mApiClient.getMovieById(id);

        call.enqueue(new Callback<MovieDetail>() {
            @Override
            public void onResponse(Call<MovieDetail> call, Response<MovieDetail> response) {
                try {
                    String plotSynopsis = Objects.requireNonNull(response.body()).plotSynopsis;
                    mBinding.movieDetails.plotSynopsisTv.setText(plotSynopsis);
                } catch (NullPointerException e) {
                    Toast.makeText(DetailActivity.this,
                            getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<MovieDetail> call, Throwable t) {
                Toast.makeText(DetailActivity.this,
                        getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }
        });

        return "";
    }

    public void onClickExpand(View view, Review review) {
        Intent intent = new Intent(this, ReviewActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this,
                        view,
                        ViewCompat.getTransitionName(view));
        intent.putExtra(ReviewActivity.REVIEW_INTENT_KEY, review);
        intent.putExtra(ReviewActivity.MOVIE_TITLE_KEY, movie.originalTitle);
        intent.putExtra(ReviewActivity.COLOR_ACTIONBAR_KEY, color);
        startActivity(intent, options.toBundle());
    }

    private void recordSharedMovie(String movieName) {

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, movie.movieId);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, movieName);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getString(R.string.type_for_firebase));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


    }
}