package foosh.air.foi.hr.activities;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import foosh.air.foi.hr.R;
import foosh.air.foi.hr.fragments.EditMyProfileFragment;
import foosh.air.foi.hr.fragments.MyProfileViewFragment;

/**
 * Aktivnost za prikaz profila korisnika.
 */
public class MyProfileActivity extends NavigationDrawerBaseActivity implements EditMyProfileFragment.EditMyProfileInteraction {
    private static final String KEY_PREFIX = "foosh.air.foi.hr.MyListingsFragment.";
    private static final String ARG_TYPE_KEY = KEY_PREFIX + "fragment-key";

    private FragmentManager mFragmentManager;

    private String fragmentKey;
    private Boolean firstTime = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startProfileView();
    }

    /**
     * Pozivanje kreiranja fragmenta preko intenta.
     */
    public void startProfileView(){
        Toolbar toolbar = findViewById(R.id.id_toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        String mUserId;
        Bundle b = getIntent().getExtras();
        if (b != null) {
            fragmentKey = b.getString(ARG_TYPE_KEY);
            mUserId = b.getString("userId");
        }
        else{
            fragmentKey = "myProfile";
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            b = new Bundle();
            b.putString(ARG_TYPE_KEY, fragmentKey);
            b.putString("userId", mUserId);
        }
        startFragment(b);
    }

    /**
     * Kreiranje novog fragmenta i transkacije.
     * @param b
     */
    public void startFragment(Bundle b) {
        mFragmentManager = getSupportFragmentManager();

        // Create new fragment and transaction
        Fragment myProfileViewFragment = new MyProfileViewFragment();
        myProfileViewFragment.setArguments(b);
        if(!firstTime){
            firstTime = true;
            mFragmentManager.beginTransaction().replace(R.id.main_layout, myProfileViewFragment, fragmentKey).commit();
        }else{
            mFragmentManager.beginTransaction().replace(R.id.main_layout, myProfileViewFragment, fragmentKey).addToBackStack(null).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(mFragmentManager.getBackStackEntryCount() == 0){
            super.onBackPressed();
            finish();
        }else {
            mFragmentManager.popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onEditMyProfileInteraction(Fragment fragment) {

    }
}
