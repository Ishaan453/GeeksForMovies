package com.ishaanbhela.geeksformovies;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.ishaanbhela.geeksformovies.Movies.movieHomeFragment;
import com.ishaanbhela.geeksformovies.about.about;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private movieHomeFragment movieFragment;
    private about aboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
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
        ImageView menuIcon = findViewById(R.id.menu_icon);

        menuIcon.setOnClickListener(v -> {
            // Open the drawer
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Initialize fragments
        movieFragment = new movieHomeFragment();
        aboutFragment = new about();

        // Load the initial fragment if savedInstanceState is null
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movieFrag, movieFragment, "MOVIE_FRAGMENT_TAG") // Add movie fragment
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment = getCurrentFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (id == R.id.nav_movies) {
            // Check if the movie fragment is already added
            if (!movieFragment.isAdded()) {
                transaction.add(R.id.movieFrag, movieFragment, "MOVIE_FRAGMENT_TAG");
            }
            // Hide current fragment and show the movie fragment
            if (fragment != null) {
                transaction.hide(aboutFragment);
            }
            transaction.show(movieFragment);
            transaction.commit();
        } else if (id == R.id.nav_about) {
            // Check if the about fragment is already added
            if (!aboutFragment.isAdded()) {
                System.out.println("ABOUT ADDED");
                transaction.add(R.id.movieFrag, aboutFragment, "ABOUT_FRAGMENT_TAG");
            }
            // Hide current fragment and show the about fragment
            if (fragment != null) {
                System.out.println("MOVIES HIDDEN");
                transaction.hide(movieFragment);
            }
            System.out.println("ABOUT SHOWN");
            transaction.show(aboutFragment);
            transaction.commit();
        } else {
            return false; // Invalid menu item selected
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Helper method to get the currently displayed fragment
    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.movieFrag);
    }
}
