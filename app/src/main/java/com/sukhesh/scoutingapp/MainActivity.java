package com.sukhesh.scoutingapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    /*
     * Zayn's guide on how this probably works (8/27/22):
     * Android Studio is pretty cool. You are able to design the layout of an app using a graphical editor.
     * Then you are able to add functionality to those components using, well, code.
     * You are also able to keep other data, RESOURCES, in a global variable called R (short for resources)
     * The format for the layout and the resource data is xml, and can be found under the res folder.
     *
     * As many of the pages have the same format (the same bottom navigation bar, background, etc),
     * instead of copying that for each page you want to have, you make it once, then change a section called a fragment.
     *
     * Kind of like those multicolor pens, where you just click the color down and you write with that color.
     * No need for 4 different pen--pages--when you can just select which color--fragment--you want to use
     * at any point in time.
     *
     * To show a fragment, you need to make a separate .java file for it, then this "main" file
     * does the work of "inflating" (showing) the corresponding fragment when you select it from the
     * shared bottom navigation bar.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemBars();

        //Set the content view to the layout file activity_main
        setContentView(R.layout.activity_main);

        // Store bottomNavigationView from design to variable to interact with
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initially call the fragment manager to show the first page
        // TODO: Instead of starting this at home every time, have this open to the fragment last opened
        transitionToFragment(new Home());
        bottomNavigationView.setSelectedItemId(R.id.home); // start the app in the home fragment.

        /*
         * An event listener to change the fragment based on which icon on the nav bar is pressed.
         */
        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment fragment = null;
            //If the case of a certain icon press is met, switch the view to the other view
            switch (item.getItemId()) {

                case R.id.home:
                    fragment = new Home();
                    break;

                case R.id.dashboard:
                    fragment = new RapidReactDashboard();
                    break;

                //case R.id.history:
                    //fragment = new History();
                    //break;

               // case R.id.settings:
                    //fragment = new Settings();
                   // break;

            }

            assert fragment != null; // a safety check, for sure, but for what? How could this be null?
            transitionToFragment(fragment);

            return true;
        });
    }

    private void transitionToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.body_container, fragment).commit();
    }

    private void hideSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }
        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
    }
}