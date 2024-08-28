package com.ierusalem.androchat.features_local

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ierusalem.androchat.R
import com.ierusalem.androchat.databinding.ActivityNavBinding
import com.ierusalem.androchat.features_local.tcp.domain.TcpViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * NavActivity
 *
 * @author A.H.I "andro" on 28/08/2024
 */

@Suppress("unused")
@AndroidEntryPoint
class NavActivity : AppCompatActivity() {

    private val viewModel: TcpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }
        setContentView(
            ComposeView(this@NavActivity).apply {
                consumeWindowInsets = false
                setContent {
                    AndroidViewBinding(ActivityNavBinding::inflate)
                }
            }
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        return navHostFragment.navController
    }

}
