import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.wordsbuilder.R

@Composable
fun HintConfirmationDialog(
    visible: Boolean,
    hintCost: Int,
    coins: Int,
    targetWords: List<String>,
    solvedWords: Set<String>,
    gameMode: String,
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (newCoins: Int, newSolved: Set<String>) -> Unit
) {
    if (visible) {
        val notEnoughCoinsText = stringResource(id = R.string.not_enough_coins)

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.hint_confirm_title)) },
            text = { Text(stringResource(R.string.hint_confirm_msg, hintCost)) },
            confirmButton = {
                Button(onClick = {
                    onDismiss()
                    if (coins >= hintCost) {
                        val newCoins = coins - hintCost
                        saveCoins(context, newCoins)

                        val remaining = targetWords.filter { !solvedWords.contains(it) }
                        if (remaining.isNotEmpty()) {
                            val hintWord = remaining.random()
                            val newSolved = solvedWords + hintWord

                            if (gameMode == "campaign") {
                                saveCurrentLevelProgress(context, newSolved)
                            }

                            SoundManager.playSound("success")
                            if (newSolved.size == targetWords.size) {
                                SoundManager.playSound("victory")
                            }

                            onConfirm(newCoins, newSolved)
                        }
                    } else {
                        Toast.makeText(context, notEnoughCoinsText, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismiss) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }
}