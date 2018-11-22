package com.foad.usama.popularmovies.rest;

import com.foad.usama.popularmovies.models.ApiResponse;
import com.foad.usama.popularmovies.models.Movie;
import com.foad.usama.popularmovies.models.MovieDetail;
import com.foad.usama.popularmovies.models.Review;
import com.foad.usama.popularmovies.models.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiClient {
    @GET("movie/popular")
    Call<ApiResponse<Movie>> getPopularMovies(@Query("language") String language,
                                              @Query("page") String page);

    @GET("movie/upcoming")
    Call<ApiResponse<Movie>> getUpcomingMovies(@Query("language") String language,
                                               @Query("page") String page);

    @GET("movie/top_rated")
    Call<ApiResponse<Movie>> getTopRatedMovies(@Query("language") String language,
                                               @Query("page") String page);

    @GET("movie/now_playing")
    Call<ApiResponse<Movie>> getNowPlayingMovies(@Query("language") String language,
                                                 @Query("page") String page);
    @GET("movie/{id}/reviews")
    Call<ApiResponse<Review>> getReviews(@Path("id") String id);

    @GET("movie/{id}/videos")
    Call<ApiResponse<Video>> getVideos(@Path("id") String id);

    @GET("movie/{id}")
    Call<MovieDetail> getMovieById(@Path("id") String id);
}