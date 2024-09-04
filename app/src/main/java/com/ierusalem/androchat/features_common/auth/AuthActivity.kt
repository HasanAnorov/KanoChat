package com.ierusalem.androchat.features_common.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ierusalem.androchat.R
import com.ierusalem.androchat.core.data.DataStorePreferenceRepository
import com.ierusalem.androchat.core.utils.log
import com.ierusalem.androchat.databinding.ActivityAuthBinding
import com.ierusalem.androchat.features_local.LocalActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject
    lateinit var dataStorePreferenceRepository: DataStorePreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
        setContentView(
            ComposeView(this@AuthActivity).apply {
                consumeWindowInsets = false
                setContent {
                    AndroidViewBinding(ActivityAuthBinding::inflate)
                }
            }
        )
        checkUniqueDeviceId()
    }

    private fun checkUniqueDeviceId() {
        // Launch a coroutine scope tied to the activity lifecycle
        lifecycleScope.launch(Dispatchers.IO) {
            val uniqueDeviceId = dataStorePreferenceRepository.getUniqueDeviceId.first()
            if (uniqueDeviceId.isNotEmpty()) {
                log("unique device id found: $uniqueDeviceId")

                // Switch to Main thread to start activity
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@AuthActivity, LocalActivity::class.java)
                    startActivity(intent)
                    finish() // Optional: Call finish() to close AuthActivity
                }
            } else{
                log("unique device id not found")
                val uniqueID = UUID.randomUUID().toString()
                dataStorePreferenceRepository.setUniqueDeviceId(uniqueID)
                log("unique device id generated: $uniqueID")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.login_nav_host_fragment_container) as NavHostFragment
        return navHostFragment.navController
    }
}