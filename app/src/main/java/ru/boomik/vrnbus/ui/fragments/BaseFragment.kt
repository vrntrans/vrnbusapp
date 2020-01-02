package ru.boomik.vrnbus.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.coroutines.*
import ru.boomik.vrnbus.ui.fragments.utils.navigation.MainHostFragment


open class BaseFragment : Fragment() {

    lateinit var navController: NavController
    /**
     * This is the job for all coroutines started by this ViewModel.
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     * Since we pass viewModelJob, you can cancel all coroutines
     * launched by uiScope by calling viewModelJob.cancel()
     */
    val vmScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun <T : View> View?.setNavigationClick(@IdRes id: Int, @IdRes navigationId: Int) {
        this?.findViewById<T>(id)?.setOnClickListener(Navigation.createNavigateOnClickListener(navigationId))
    }

    fun <T : View> View?.setNavigationClick(@IdRes id: Int, @IdRes navigationId: Int, args: () -> Bundle) {
        this?.findViewById<T>(id)?.setOnClickListener { Navigation.findNavController(this).navigate(navigationId, args()) }
    }

    fun <T : View> View?.setNavigationClick(@IdRes id: Int, @IdRes navigationId: Int, args: () -> Bundle, canNavigate: () -> Boolean) {
        this?.findViewById<T>(id)?.setOnClickListener {
            if (!canNavigate()) return@setOnClickListener
            Navigation.findNavController(this).navigate(navigationId, args())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navController = MainHostFragment.findNavController(this)

        // This callback will only be called when MyFragment is at least Started.
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() { // Handle the back button event
                vmScope.launch {  backPressed() }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }

    private suspend fun backPressed() {
        if (onBackPressed()) return
        onClosed()
        vmScope.cancel("Fragment closed")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vmScope.launch { loadDataAsync() }
    }

    open suspend fun loadDataAsync() {

    }

    open suspend fun onBackPressed() : Boolean {
        return false
    }


    open suspend fun onClosed() {
    }

}

