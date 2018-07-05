package com.example.paulinho.instantcarreco;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.example.paulinho.instantcarreco.ui.CarListActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
    @LargeTest
    public class CarListActivityTest {
        private String mStringToBetyped= "Manufacutre";
//        @Rule
//        public ActivityTestRule<CarListActivity> mActivityRule = new ActivityTestRule<>(
//                CarListActivity.class);
//
//        @Test
//        public void changeText_sameActivity() {
//            onView(withId(R.id.edtManufacture))
//                    .perform(typeText(mStringToBetyped), closeSoftKeyboard());
//            onView(withId(R.id.btnUpdate)).perform(click());
//
//            onView(withId(R.id.edtCom)).check(matches(withText(mStringToBetyped)));
//        }
//
//        @Test
//        public void changeText_newActivity() {
//            // Type text and then press the button.
//            onView(withId(R.id.edtManufacture)).perform(typeText(mStringToBetyped),
//                    closeSoftKeyboard());
//            onView(withId(R.id.btnUpdate)).perform(click());
//
//            // This view is in a different Activity, no need to tell Espresso.
//            onView(withId(R.id.txtName)).check(matches(withText(mStringToBetyped)));
//        }
}
