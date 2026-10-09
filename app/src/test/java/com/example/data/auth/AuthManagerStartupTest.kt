package com.example.data.auth

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ErpViewModel
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AuthManagerStartupTest {

    @Test
    fun authManagerStartsWithoutGoogleServicesJson() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = AuthManager(context)
        assertNull(manager.currentUser)
    }

    @Test
    fun viewModelStartsWithoutGoogleServicesJson() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = ErpViewModel(app)
        assertNull(viewModel.currentUser.value)
    }
}
