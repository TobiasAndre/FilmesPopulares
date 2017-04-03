package com.tobiasandre.filmespopulares.model.Lists;

import com.google.gson.annotations.SerializedName;
import com.tobiasandre.filmespopulares.model.Review;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 3/28/17.
 */

public class Reviews {
    @SerializedName("results")
    private List<Review> reviews = new ArrayList<>();

    public List<Review> getReviews() {
        return reviews;
    }
}
