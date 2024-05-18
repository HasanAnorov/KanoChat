package com.ierusalem.androchat.features_tcp.tcp.tcp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.ierusalem.androchat.R
import com.ierusalem.androchat.features_tcp.tcp.tcp.TcpView
import com.ierusalem.androchat.features_tcp.tcp.tcp.presentation.components.rememberAllTabs
import com.ierusalem.androchat.ui.theme.AndroChatTheme
import com.ierusalem.androchat.utils.UiText
import kotlinx.coroutines.launch

class TcpFragment : Fragment() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val scope = rememberCoroutineScope()
                val allTabs = rememberAllTabs()
                val pagerState =
                    rememberPagerState(
                        initialPage = 0,
                        initialPageOffsetFraction = 0F,
                        pageCount = { allTabs.size },
                    )

                val handleTabSelected by rememberUpdatedState { tab: TcpView ->
                    // Click fires the index to update
                    // The index updating is caught by the snapshot flow
                    // Which then triggers the page update function
                    val index = allTabs.indexOf(tab)
                    scope.launch(context = kotlinx.coroutines.Dispatchers.Main) {
                        pagerState.animateScrollToPage(
                            index
                        )
                    }
                }

                AndroChatTheme {
                    TcpScreen(
                        appName = UiText.StringResource(R.string.app_name),
                        allTabs = allTabs,
                        pagerState = pagerState,
                        onTabClick = { index -> handleTabSelected(index) },
                        onNavIconClick = {},
                        onSettingsClick = {}
                    )
                }
            }
        }
    }

}