package foosh.air.foi.hr.interfaces;

import java.util.ArrayList;

import foosh.air.foi.hr.model.Listing;

public interface LoadCompletedListener {
    void onLoadCompleted(ArrayList<Listing> newListings);
}
