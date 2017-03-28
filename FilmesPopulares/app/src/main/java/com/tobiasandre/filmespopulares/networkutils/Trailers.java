package com.tobiasandre.filmespopulares.networkutils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class Trailers {
    @SerializedName("results")
    private List<Trailer> trailers = new ArrayList<>();

    public List<Trailer> getTrailers() {
        return trailers;
    }
}
