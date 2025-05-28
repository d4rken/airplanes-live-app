package eu.darken.apl.feeder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import eu.darken.apl.R
import eu.darken.apl.common.compose.CombinedPreview
import eu.darken.apl.common.compose.MyAppTheme
import eu.darken.apl.common.error.ErrorHandler
import eu.darken.apl.common.uix.Fragment3
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.core.config.FeederConfig
import eu.darken.apl.main.core.ThemeState
import eu.darken.apl.main.ui.MainActivity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@AndroidEntryPoint
class FeederListFragment : Fragment3() {

    override val vm: FeederListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            val themeState by vm.themeState.collectAsState(ThemeState())
            MyAppTheme(state = themeState) {
                val vmState by vm.state.collectAsState(FeederListViewModel.State())
                ErrorHandler(vm.errorEvents)
                var showAddFeeder by remember { mutableStateOf(false) }

                if (showAddFeeder) {
                    UuidInputDialog(
                        onDismiss = { showAddFeeder = false },
                        onConfirm = { uuid ->
                            showAddFeeder = false
                            vm.addFeeders(uuid)
                        }
                    )
                }

                Screen(
                    state = vmState,
                    onRefresh = { vm.refresh() },
                    onStartFeeding = { vm.startFeeding() },
                    onFeederClick = {},
                    onNewFeederDialog = { showAddFeeder = true },
                    onSettings = { (requireActivity() as MainActivity).goToSettings() }
                )
            }
        }
    }

    @CombinedPreview
    @Composable
    private fun FeederStateScreenPreview() {
        MyAppTheme {
            Screen(
                state = FeederListViewModel.State(
                    items = (1..10).map {
                        Feeder(
                            config = FeederConfig(
                                receiverId = "id $it",
                                user = "user $it",
                            ),
                        )
                    },
                    isRefreshing = true,
                )
            )
        }
    }

    @CombinedPreview
    @Composable
    private fun EmptyStateScreenPreview() {
        MyAppTheme { Screen() }
    }

    @Composable
    private fun Screen(
        state: FeederListViewModel.State = FeederListViewModel.State(),
        onRefresh: () -> Unit = {},
        onNewFeederDialog: () -> Unit = {},
        onStartFeeding: () -> Unit = {},
        onFeederClick: (Feeder) -> Unit = {},
        onSettings: () -> Unit = {},
    ) {
        val pullRefreshState = rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = onRefresh
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.feeder_page_label),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = pluralStringResource(
                                    id = R.plurals.feeder_yours_x_active_msg,
                                    count = state.items.size
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                    },
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = onNewFeederDialog) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = stringResource(R.string.feeder_list_add_your_feeder_action),
                            )
                        }
                        IconButton(onClick = onSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.label_settings),
                            )
                        }
                    },
                )
            }
        ) { innerPadding ->
            Box(
                Modifier
                    .padding(innerPadding)
                    .pullRefresh(pullRefreshState)
            ) {
                if (state.items.isEmpty()) {
                    EmptyStateScreen(
                        onNewFeeder = onNewFeederDialog,
                        onStartFeeding = onStartFeeding,
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.items) { item ->
                            FeederItem(feeder = item, onClick = onFeederClick)
                        }
                    }
                }

                PullRefreshIndicator(
                    state.isRefreshing,
                    pullRefreshState,
                    Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }


    @Composable
    private fun EmptyStateScreen(
        onNewFeeder: () -> Unit = {},
        onStartFeeding: () -> Unit = {},
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.feeder_list_add_yours_now_msg),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onNewFeeder, modifier = Modifier.padding(top = 8.dp)) {
                Icon(ImageVector.vectorResource(R.drawable.ic_plus_network_24), contentDescription = null)
                Text(stringResource(R.string.feeder_list_add_your_feeder_action), Modifier.padding(start = 8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.feeder_startfeeding_msg),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onStartFeeding, modifier = Modifier.padding(top = 8.dp)) {
                Icon(ImageVector.vectorResource(R.drawable.ic_heart_24), contentDescription = null)
                Text(stringResource(R.string.common_start_feeding_action), Modifier.padding(start = 8.dp))
            }
        }
    }

    @CombinedPreview
    @Composable
    private fun UuidInputDialogPreview() {
        MyAppTheme { UuidInputDialog {} }
    }

    @Composable
    fun UuidInputDialog(
        onDismiss: () -> Unit = {},
        onConfirm: (String) -> Unit = {}
    ) {
        var uuidInput by remember { mutableStateOf("") }

        BasicAlertDialog(
            onDismissRequest = onDismiss
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(stringResource(R.string.feeder_list_add_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.feeder_list_add_message), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uuidInput,
                        onValueChange = { uuidInput = it },
                        label = { Text("Feeder ID(s)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Ascii
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        TextButton(onClick = { onConfirm(uuidInput) }) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}
