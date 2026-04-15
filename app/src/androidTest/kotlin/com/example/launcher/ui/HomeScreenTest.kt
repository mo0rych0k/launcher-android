package com.example.launcher.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.launcher.LauncherActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LauncherActivity>()

    @Test
    fun launcherRoot_isDisplayed() {
        composeTestRule.onNodeWithTag("launcher_root").assertIsDisplayed()
    }
}
