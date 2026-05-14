package com.beballer.beballer.ui.player.auth.login

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beballer.beballer.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1], manifest = Config.NONE)
class LoginFragmentTest {

    @Test
    fun mobileNumber_textWatcher_updatesButtonCheck() {
        // Use a theme that exists in the project
        val scenario = launchFragmentInContainer<LoginFragment>(themeResId = R.style.Theme_MyApplication)
        
        scenario.onFragment { fragment ->
            val binding = fragment.binding
            val etMobile = binding.etMobile
            
            // Case 1: Empty input
            etMobile.setText("")
            assertFalse("Button check should be false for empty mobile", binding.buttonCheck == true)
            
            // Case 2: 9 digits
            etMobile.setText("123456789")
            assertFalse("Button check should be false for 9 digits", binding.buttonCheck == true)
            
            // Case 3: 10 digits
            etMobile.setText("1234567890")
            assertTrue("Button check should be true for 10 digits", binding.buttonCheck == true)
        }
    }
}
