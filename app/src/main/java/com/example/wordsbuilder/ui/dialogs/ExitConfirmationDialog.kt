import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.wordsbuilder.R
import com.example.wordsbuilder.ui.components.GameDialog

@Composable
fun ExitConfirmationDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    GameDialog(
        visible = visible,
        title = stringResource(id = R.string.exit_title),
        description = stringResource(id = R.string.exit_message),
        confirmButtonText = stringResource(id = R.string.yes),
        dismissButtonText = stringResource(id = R.string.no),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}