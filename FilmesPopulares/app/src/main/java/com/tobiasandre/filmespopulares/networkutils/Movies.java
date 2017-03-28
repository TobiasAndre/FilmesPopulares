package com.tobiasandre.filmespopulares.networkutils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class Movies {
    @SerializedName("results")
    private List<Movie> movies = new ArrayList<>();

    public List<Movie> getMovies() {
        return movies;
    }
}
