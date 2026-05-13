import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.wordsbuilder.R

@Composable
fun ExitConfirmationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.exit_title)) },
            text = { Text(stringResource(R.string.exit_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        SoundManager.playSound(context, R.raw.click)
                        onConfirm()
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        SoundManager.playSound(context, R.raw.click)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}