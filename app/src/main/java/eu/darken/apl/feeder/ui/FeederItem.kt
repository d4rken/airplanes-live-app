package eu.darken.apl.feeder.ui

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.darken.apl.R
import eu.darken.apl.common.compose.CombinedPreview
import eu.darken.apl.common.compose.MyAppTheme
import eu.darken.apl.feeder.core.Feeder
import eu.darken.apl.feeder.core.config.FeederConfig
import java.time.Instant


@CombinedPreview
@Composable
private fun FeederItemPreview() {
    MyAppTheme {
        FeederItem(
            feeder = Feeder(
                config = FeederConfig(
                    receiverId = "id 1",
                    user = "user 1",
                ),
            )
        )
    }
}

@Composable
internal fun FeederItem(
    feeder: Feeder,
    onClick: (Feeder) -> Unit = {},
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feeder.label,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (feeder.config.offlineCheckTimeout != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier.size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(

                            painter = painterResource(R.drawable.ic_alarm_bell_24),
                            contentDescription = "Offline monitoring state"
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = feeder.lastSeen?.let {
                        DateUtils.getRelativeTimeSpanString(
                            it.toEpochMilli(),
                            Instant.now().toEpochMilli(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                    } ?: "",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.feeder_beast_stats_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = feeder.beastStats?.messageRate?.let { "$it MSG/s" } ?: "? MSG/s unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feeder.beastStats?.bandwidth?.let { "$it KBit/s" } ?: "Bandwith unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = stringResource(id = R.string.feeder_mlat_stats_label),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = feeder.mlatStats?.messageRate?.let { "$it MSG/s" } ?: "MSG/s unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feeder.mlatStats?.outlierPercent?.let { "$it% outliers" } ?: "Outliers unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}