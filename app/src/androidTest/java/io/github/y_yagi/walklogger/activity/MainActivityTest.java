package io.github.y_yagi.walklogger.activity;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.y_yagi.walklogger.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityTest() {
        ViewInteraction circleButton = onView(
                allOf(withId(R.id.record_button), isDisplayed()));
        circleButton.perform(click());

        ViewInteraction circleButton2 = onView(
                allOf(withId(R.id.pause_button), isDisplayed()));
        circleButton2.perform(click());

        ViewInteraction circleButton3 = onView(
                allOf(withId(R.id.stop_button), isDisplayed()));
        circleButton3.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(android.R.id.input), isDisplayed()));
        appCompatEditText.perform(replaceText("てすと"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(android.R.id.input), withText("てすと"), isDisplayed()));
        appCompatEditText2.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(android.R.id.input), withText("てすと"), isDisplayed()));
        appCompatEditText3.perform(replaceText("てすと"), closeSoftKeyboard());

        ViewInteraction mDButton = onView(
                allOf(withId(R.id.md_buttonDefaultPositive), isDisplayed()));
        mDButton.perform(click());

    }

}
