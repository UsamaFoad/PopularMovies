package com.foad.usama.popularmovies.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.foad.usama.popularmovies.R;
import com.foad.usama.popularmovies.data.AppDatabase;
import com.foad.usama.popularmovies.models.ApiResponse;
import com.foad.usama.popularmovies.models.Movie;
import com.foad.usama.popularmovies.rest.ApiClient;
import com.foad.usama.popularmovies.rest.ServiceGenerator;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.foad.usama.popularmovies.ui.MainActivity.NO_INTERNET;

public class MainViewModel extends AndroidViewModel {
    private MutableLiveData<List<Movie>> popularMovies;
    private MutableLiveData<List<Movie>> highestMovies;
    private MutableLiveData<List<Movie>> upcomingMovies;
    private MutableLiveData<List<Movie>> nowPlayingMovies;
    private LiveData<List<Movie>> favoriteMovies;
    private MutableLiveData<Integer> status;
    private AppDatabase database;
    private ApiClient apiClient;

    private static final int POPULAR = 0;
    private static final int UPCOMING = 2;
    private static final int TOP_RATED = 3;
    private static final int NOW_PLAYING = 4;
    private static final int PAGE_ONE = 1;
    private static final int STATUS_VALUE = 0;


    public MainViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getDatabase(getApplication());
        apiClient = ServiceGenerator.createService(ApiClient.class);
    }

    public LiveData<List<Movie>> getPopularMovies() {
        if (popularMovies == null) {
            popularMovies = new MutableLiveData<>();
            loadMovies(POPULAR, PAGE_ONE);
        }
        return popularMovies;
    }

    public LiveData<List<Movie>> getFavoriteMovies() {
        if (favoriteMovies == null) {
            favoriteMovies = new MutableLiveData<>();
            getFavoritesFromDatabase();
        }
        return favoriteMovies;
    }

    public LiveData<List<Movie>> getUpcomingMovies() {
        if (upcomingMovies == null) {
            upcomingMovies = new MutableLiveData<>();
            loadMovies(UPCOMING, PAGE_ONE);
        }
        return upcomingMovies;
    }

    public LiveData<List<Movie>> getHighestMovies() {
        if (highestMovies == null) {
            highestMovies = new MutableLiveData<>();
            loadMovies(TOP_RATED, PAGE_ONE);
        }
        return highestMovies;
    }

    public LiveData<List<Movie>> getNowPlayingMovies() {
        if (nowPlayingMovies == null) {
            nowPlayingMovies = new MutableLiveData<>();
            loadMovies(NOW_PLAYING, PAGE_ONE);
        }
        return nowPlayingMovies;
    }

    public MutableLiveData<Integer> getStatus() {
        if (status == null) {
            status = new MutableLiveData<>();
            status.setValue(STATUS_VALUE);
        }
        return status;
    }

    public void loadMovies(int sorting, int page) {

        switch (sorting) {
            case POPULAR: {
                Call<ApiResponse<Movie>> call = apiClient.getPopularMovies(
                        getApplication().getString(R.string.language),
                        String.valueOf(page));

                call.enqueue(new Callback<ApiResponse<Movie>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Movie>> call,
                                           Response<ApiResponse<Movie>> response) {
                        if (response.isSuccessful()) {
                            List<Movie> result = Objects.requireNonNull(response.body()).results;
                            List<Movie> value = popularMovies.getValue();
                            if (value == null || value.isEmpty()) {
                                popularMovies.setValue(result);
                            } else {
                                value.addAll(result);
                                popularMovies.setValue(value);
                            }
                            status.setValue(STATUS_VALUE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                        popularMovies = null;
                        status.setValue(NO_INTERNET);
                    }
                });
                break;
            }

            case UPCOMING: {
                Call<ApiResponse<Movie>> call = apiClient.getUpcomingMovies(
                        getApplication().getString(R.string.language),
                        String.valueOf(page));

                call.enqueue(new Callback<ApiResponse<Movie>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Movie>> call,
                                           Response<ApiResponse<Movie>> response) {
                        if (response.isSuccessful()) {
                            List<Movie> result = Objects.requireNonNull(response.body()).results;
                            List<Movie> value = upcomingMovies.getValue();
                            if (value == null || value.isEmpty()) {
                                upcomingMovies.setValue(result);
                            } else {
                                value.addAll(result);
                                upcomingMovies.setValue(value);
                            }
                            status.setValue(STATUS_VALUE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                        upcomingMovies = null;
                        status.setValue(NO_INTERNET);
                    }
                });
                break;
            }
            case TOP_RATED: {
                Call<ApiResponse<Movie>> call = apiClient.getTopRatedMovies(
                        getApplication().getString(R.string.language),
                        String.valueOf(page));

                call.enqueue(new Callback<ApiResponse<Movie>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Movie>> call,
                                           Response<ApiResponse<Movie>> response) {
                        if (response.isSuccessful()) {
                            List<Movie> result = Objects.requireNonNull(response.body()).results;
                            List<Movie> value = highestMovies.getValue();
                            if (value == null || value.isEmpty()) {
                                highestMovies.setValue(result);
                            } else {
                                value.addAll(result);
                                highestMovies.setValue(value);
                            }
                            status.setValue(STATUS_VALUE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                        highestMovies = null;
                        status.setValue(NO_INTERNET);
                    }
                });
                break;
            }
            case NOW_PLAYING: {
                Call<ApiResponse<Movie>> call = apiClient.getNowPlayingMovies(
                        getApplication().getString(R.string.language),
                        String.valueOf(page));

                call.enqueue(new Callback<ApiResponse<Movie>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Movie>> call,
                                           Response<ApiResponse<Movie>> response) {
                        if (response.isSuccessful()) {
                            List<Movie> result = Objects.requireNonNull(response.body()).results;
                            List<Movie> value = nowPlayingMovies.getValue();
                            if (value == null || value.isEmpty()) {
                                nowPlayingMovies.setValue(result);
                            } else {
                                value.addAll(result);
                                nowPlayingMovies.setValue(value);
                            }
                            status.setValue(STATUS_VALUE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Movie>> call, Throwable t) {
                        nowPlayingMovies = null;
                        status.setValue(NO_INTERNET);
                    }
                });
                break;
            }
        }
    }

    private void getFavoritesFromDatabase() {
        favoriteMovies = database.movieDao().getAll();
    }
}
