@file:OptIn(ExperimentalMaterial3Api::class)

package eu.darken.apl.common.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun ErrorHandler(
    errors: Flow<Throwable>,
    custom: @Composable (Throwable, () -> Unit) -> Unit = { error, onDismiss ->
        DefaultErrorDialog(
            error,
            onDismiss
        )
    },
) {
    var currentError by remember { mutableStateOf<Throwable?>(null) }

    LaunchedEffect(errors) {
        errors.collect { exception ->
            currentError = exception
        }
    }

    currentError?.let { error ->
        custom(error) { currentError = null }
    }
}

@Composable
fun DefaultErrorDialog(
    error: Throwable,
    onDismiss: () -> Unit,
) {
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
                val context = LocalContext.current
                val localizedError = error.localized(context)
                Text(localizedError.label.get(context), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(localizedError.description.get(context), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}

@Preview(name = "Error Handler Preview")
@Composable
fun ErrorHandlerPreview() {
    ErrorHandler(
        flowOf(IllegalStateException("You need to pass an error flow"))
    )
}