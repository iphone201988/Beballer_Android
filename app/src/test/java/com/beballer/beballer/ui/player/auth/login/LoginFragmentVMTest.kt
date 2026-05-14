package com.beballer.beballer.ui.player.auth.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.beballer.beballer.data.api.ApiHelper
import com.beballer.beballer.utils.Status
import com.google.gson.JsonObject
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.*
import org.junit.Assert.assertEquals
import retrofit2.Response

@ExperimentalCoroutinesApi
class LoginFragmentVMTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: LoginFragmentVM
    private val apiHelper: ApiHelper = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginFragmentVM(apiHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun commonLoginAPi_success_updatesObserver() = runTest {
        val request = hashMapOf<String, Any>("id" to "uid123")
        val url = "test/login"
        val jsonResponse = JsonObject().apply { addProperty("success", true) }
        coEvery { apiHelper.apiForRawBody(request, url) } returns Response.success(jsonResponse)

        viewModel.commonLoginAPi(request, url)
        advanceUntilIdle()

        val result = viewModel.commonObserver.value
        assertEquals(Status.SUCCESS, result?.status)
        assertEquals(jsonResponse, result?.data)
    }

    @Test
    fun commonLoginAPi_error_updatesObserver() = runTest {
        val request = hashMapOf<String, Any>("id" to "uid123")
        val url = "test/login"
        val errorBody = "{\"message\":\"Unauthorized\"}".toResponseBody(null)
        coEvery { apiHelper.apiForRawBody(request, url) } returns Response.error(401, errorBody)

        viewModel.commonLoginAPi(request, url)
        advanceUntilIdle()

        val result = viewModel.commonObserver.value
        assertEquals(Status.ERROR, result?.status)
        assertEquals("Unauthorized", result?.message)
    }
}
