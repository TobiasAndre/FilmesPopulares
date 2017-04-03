package com.tobiasandre.filmespopulares.model.Lists;

import com.google.gson.annotations.SerializedName;
import com.tobiasandre.filmespopulares.model.Trailer;

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
