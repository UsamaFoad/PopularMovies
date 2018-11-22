package com.foad.usama.popularmovies.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.models.ApiResponse;
import com.foad.usama.popularmovies.models.Movie;
import com.foad.usama.popularmovies.rest.ApiClient;
import com.foad.usama.popularmovies.rest.ServiceGenerator;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static com.foad.usama.popularmovies.R.string.image_path_w92;
import static com.foad.usama.popularmovies.ui.MoviesAppWidgetConfig.CURRENT_SELECTION;

public class MoviesWidgetService extends RemoteViewsService {
    private static final String PAGE_ONE = "1";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MoviesWidgetItemFactory(getApplicationContext(), intent);
    }

    class MoviesWidgetItemFactory implements RemoteViewsFactory {
        private Context context;
        private int appWidgetId;
        private List<String> movies;
        private List<String> moviesRating;
        private List<Bitmap> moviePoster;
        private ApiClient apiClient;

        MoviesWidgetItemFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            apiClient = ServiceGenerator.createService(ApiClient.class);
        }

        /**
         * Called when your factory is first constructed. The same factory may be shared across
         * multiple RemoteViewAdapters depending on the intent passed.
         */
        @Override
        public void onCreate() {
            // connect to data source

        }

        /**
         * Called when notifyDataSetChanged() is triggered on the remote adapter. This allows a
         * RemoteViewsFactory to respond to data changes by updating any internal references.
         * <p>
         * Note: expensive tasks can be safely performed synchronously within this method. In the
         * interim, the old data will be displayed within the widget.
         *
         * @see AppWidgetManager#notifyAppWidgetViewDataChanged(int[], int)
         */
        @Override
        public void onDataSetChanged() {
            Date date = new Date(System.currentTimeMillis());
            String timeFormatted = date.toString();


            Call<ApiResponse<Movie>> call = apiClient
                    .getPopularMovies(context.getString(R.string.language), PAGE_ONE);

            switch (CURRENT_SELECTION) {
                case "Popular":
                    try {
                        Response<ApiResponse<Movie>> response = call.execute();
                        if (response.isSuccessful()) {
                            movies = new ArrayList<>();
                            moviesRating = new ArrayList<>();
                            moviePoster = new ArrayList<>();
                            String urlFull;
                            List<Movie> result = response.body().results;
                            for (Movie movie : result) {
                                movies.add(movie.movieTitle);
                                moviesRating.add(movie.userRating);
                                urlFull = (getResources().getString(image_path_w92)
                                        + movie.posterPath);
                                moviePoster.add(BitmapFactory.decodeStream((new URL(urlFull))
                                        .openConnection().getInputStream()));
                            }
                        }
                    } catch (IOException e) {
                        Log.e(MoviesWidgetService.class.getSimpleName(), e.getMessage());
                    }
                    break;

                case "Upcoming":
                    call = apiClient
                            .getUpcomingMovies(context.getString(R.string.language), PAGE_ONE);
                    try {
                        Response<ApiResponse<Movie>> response = call.execute();
                        if (response.isSuccessful()) {
                            movies = new ArrayList<>();
                            moviesRating = new ArrayList<>();
                            String urlFull;
                            moviePoster = new ArrayList<>();
                            List<Movie> result = response.body().results;
                            for (Movie movie : result) {
                                movies.add(movie.movieTitle);
                                moviesRating.add(movie.userRating);
                                urlFull = (getResources().getString(image_path_w92)
                                        + movie.posterPath);
                                moviePoster.add(BitmapFactory.decodeStream((new URL(urlFull))
                                        .openConnection().getInputStream()));
                            }
                        }
                    } catch (IOException e) {
                        Log.e(MoviesWidgetService.class.getSimpleName(), e.getMessage());
                    }
                    break;

                case "Top Rated":
                    call = apiClient
                            .getTopRatedMovies(context.getString(R.string.language), PAGE_ONE);
                    try {
                        Response<ApiResponse<Movie>> response = call.execute();
                        if (response.isSuccessful()) {
                            movies = new ArrayList<>();
                            moviesRating = new ArrayList<>();
                            String urlFull;
                            moviePoster = new ArrayList<>();
                            List<Movie> result = response.body().results;
                            for (Movie movie : result) {
                                movies.add(movie.movieTitle);
                                moviesRating.add(movie.userRating);
                                urlFull = (getResources().getString(image_path_w92)
                                        + movie.posterPath);
                                moviePoster.add(BitmapFactory.decodeStream((new URL(urlFull))
                                        .openConnection().getInputStream()));
                            }
                        }
                    } catch (IOException e) {
                        Log.e(MoviesWidgetService.class.getSimpleName(), e.getMessage());
                    }
                    break;

                case "Now Playing":
                    call = apiClient
                            .getNowPlayingMovies(context.getString(R.string.language), PAGE_ONE);
                    try {
                        Response<ApiResponse<Movie>> response = call.execute();
                        if (response.isSuccessful()) {
                            movies = new ArrayList<>();
                            moviesRating = new ArrayList<>();
                            String urlFull;
                            moviePoster = new ArrayList<>();
                            List<Movie> result = response.body().results;
                            for (Movie movie : result) {
                                movies.add(movie.movieTitle);
                                moviesRating.add(movie.userRating);
                                urlFull = (getResources().getString(image_path_w92)
                                        + movie.posterPath);
                                moviePoster.add(BitmapFactory.decodeStream((new URL(urlFull))
                                        .openConnection().getInputStream()));
                            }
                        }
                    } catch (IOException e) {
                        Log.e(MoviesWidgetService.class.getSimpleName(), e.getMessage());
                    }
                    break;
            }
        }

        /**
         * Called when the last RemoteViewsAdapter that is associated with this factory is
         * unbound.
         */
        @Override
        public void onDestroy() {
            // Close data source

        }

        /**
         * @return Count of items.
         */
        @Override
        public int getCount() {
//            return exampleData.length;
            if (movies == null) {
                return 0;
            }
            return movies.size();
        }

        /**
         * <p>
         * Note: expensive tasks can be safely performed synchronously within this method, and a
         * loading view will be displayed in the interim. See {@link #getLoadingView()}.
         *
         * @param position The position of the item within the Factory's data set of the item whose
         *                 view we want.
         * @return A RemoteViews object corresponding to the data at the specified position.
         */
        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || movies == null) {
                return null;
            }
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.movies_widget_item);
            rv.setTextViewText(R.id.movies_widget_item_text, movies.get(position));
            rv.setTextViewText(R.id.movies_widget_item_rating, moviesRating.get(position));
            rv.setImageViewBitmap(R.id.poster_imageView, moviePoster.get(position));

            return rv;
        }

        /**
         * This allows for the use of a custom loading view which appears between the time that
         * {@link #getViewAt(int)} is called and returns. If null is returned, a default loading
         * view will be used.
         *
         * @return The RemoteViews representing the desired loading view.
         */
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        /**
         * @return The number of types of Views that will be returned by this factory.
         */
        @Override
        public int getViewTypeCount() {
            // Different type of views in our collection
            return 1;
        }

        /**
         * See .
         *
         * @param position The position of the item within the data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * See .
         *
         * @return True if the same id always refers to the same object.
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}