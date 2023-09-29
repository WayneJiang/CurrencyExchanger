package com.wayne.currencyexchanger

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.textview.MaterialTextView
import com.wayne.currencyexchanger.view.CurrencyRateAdapter
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal
import java.math.RoundingMode


/*
 * Copyright (c) 2023 Wayne Jiang All rights reserved.
 *
 * Created by Wayne Jiang on 2023/09/29
 */
@RunWith(AndroidJUnit4::class)
class ActivityTest {
    private lateinit var mActivityScenario: ActivityScenario<MainActivity>
    private lateinit var mViewIdlingResource: ViewIdlingResource
    private lateinit var mRecyclerView: RecyclerView

    @Before
    fun setup() {
        mActivityScenario = launch(MainActivity::class.java)

        mActivityScenario.onActivity {
            mViewIdlingResource = ViewIdlingResource(it.findViewById(R.id.view_recycler))
            mRecyclerView = it.findViewById(R.id.view_recycler)
            IdlingRegistry.getInstance().register(mViewIdlingResource)
        }
    }

    @After
    fun shutdown() {
        mActivityScenario.close()
        IdlingRegistry.getInstance().unregister(mViewIdlingResource)
    }

    /**
     * Test for inputting amounts behavior
     */
    @Test
    fun testInput() {
        onView(withId(R.id.ed_amount)).perform(typeText("10"), closeSoftKeyboard())
        onView(withId(R.id.ed_amount)).check(matches(withText("10")))
    }

    /**
     * Test for recyclerview load data behavior
     */
    @Test
    fun testRecyclerView() {
        onView(withId(R.id.view_recycler)).check(matches(isDisplayed()))
    }

    /**
     * Test for spinner selected
     */
    @Test
    fun testSpinner() {
        onView(withId(R.id.view_recycler)).check(matches(isDisplayed()))

        onView(withId(R.id.spinner_currency)).perform(click())

        onData(allOf(`is`(instanceOf(String::class.java)), `is`("TWD"))).perform(click())

        onView(withId(R.id.spinner_currency))
            .check(matches(withSpinnerText(containsString("TWD"))))
    }

    /**
     * Test for changing currency then inputting desired amounts.
     * Finally checking the exchanged result.
     */
    @Test
    fun testExchange() {
        onView(withId(R.id.view_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.view_recycler))
            .perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(73)
            )

        onView(withId(R.id.spinner_currency)).perform(click())
        onData(allOf(`is`(instanceOf(String::class.java)), `is`("TWD"))).perform(click())

        onView(withId(R.id.view_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.view_recycler))
            .perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(73)
            )

        var beforeExchange = 0f
        mRecyclerView.findViewHolderForAdapterPosition(73)?.itemView?.apply {
            beforeExchange = findViewById<MaterialTextView>(R.id.tv_rate).text.toString().toFloat()
        }

        onView(withId(R.id.ed_amount)).perform(typeText("10"), closeSoftKeyboard())

        onView(withId(R.id.view_recycler)).check(matches(isDisplayed()))
        onView(withId(R.id.view_recycler))
            .perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(73)
            )

        var afterExchange = 0f
        mRecyclerView.findViewHolderForAdapterPosition(73)?.itemView?.apply {
            afterExchange = findViewById<MaterialTextView>(R.id.tv_rate).text.toString().toFloat()
        }

        afterExchange =
            BigDecimal.valueOf(afterExchange.toDouble())
                .setScale(2, RoundingMode.HALF_UP)
                .toFloat()

        assertThat(afterExchange, `is`(beforeExchange * 10))
    }

    inner class ViewIdlingResource(recyclerView: RecyclerView) : IdlingResource {
        private var mResourceCallback: IdlingResource.ResourceCallback? = null
        private val mRecyclerView = recyclerView

        override fun getName(): String = ViewIdlingResource::class.java.simpleName

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            mResourceCallback = callback
        }

        override fun isIdleNow(): Boolean {
            val idle = (mRecyclerView.adapter as CurrencyRateAdapter).itemCount > 0

            if (idle) {
                mResourceCallback?.onTransitionToIdle()
            }

            return idle
        }
    }
}