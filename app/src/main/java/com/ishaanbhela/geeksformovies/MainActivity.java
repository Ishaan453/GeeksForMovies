package com.ishaanbhela.geeksformovies;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.navigation.NavigationView;
import com.ishaanbhela.geeksformovies.Movies.movieHomeFragment;
import com.ishaanbhela.geeksformovies.Preferences.userPreferenceFragmentClass;
import com.ishaanbhela.geeksformovies.about.about;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private movieHomeFragment movieFragment;
    private about aboutFragment;
    private userPreferenceFragmentClass preferenceFragment;
    private TextView menuHead;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Apply window insets for edge-to-edge experience
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuHead = findViewById(R.id.menuHead);

        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Initialize fragments
        movieFragment = new movieHomeFragment();
        aboutFragment = new about();
        preferenceFragment = new userPreferenceFragmentClass();

        // Load the initial fragment only if savedInstanceState is null
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movieFrag, movieFragment, "MOVIE_FRAGMENT")
                    .add(R.id.movieFrag, aboutFragment, "ABOUT_FRAGMENT")
                    .add(R.id.movieFrag, preferenceFragment, "PREFERENCE_FRAGMENT")
                    .hide(aboutFragment) // Hide 'about' fragment initially
                    .hide(preferenceFragment)
                    .commit();
        }

        adView = findViewById(R.id.MainActivityBannerAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Show or hide fragments based on the selected item
        if (id == R.id.nav_movies) {
            transaction
                    .hide(aboutFragment)
                    .hide(preferenceFragment)
                    .show(movieFragment);
            menuHead.setText("Movies");
        }
        else if (id == R.id.nav_about) {
            transaction
                    .hide(movieFragment)
                    .hide(preferenceFragment)
                    .show(aboutFragment);
            menuHead.setText("About");
        }
        else if (id == R.id.nav_preference){
            transaction
                    .hide(movieFragment)
                    .hide(aboutFragment)
                    .show(preferenceFragment);
            menuHead.setText("Preference");
        }
        else {
            return false;
        }

        // Commit the transaction and close the drawer
        transaction.commit();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Helper method to get the currently displayed fragment, if needed in future
    private Fragment getCurrentFragment() {
        if (movieFragment.isVisible()) return movieFragment;
        if (aboutFragment.isVisible()) return aboutFragment;
        return null;
    }
}
