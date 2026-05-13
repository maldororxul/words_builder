import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.wordsbuilder.R
import com.example.wordsbuilder.SoundManager

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
                    SoundManager.playSound(context, R.raw.click)
                    onDismiss()
                    if (coins >= hintCost) {

                        val statsManager = com.example.wordsbuilder.data.StatsManager(context)
                        statsManager.hintsUsed += 1

                        val newCoins = coins - hintCost
                        saveCoins(context, newCoins)

                        val remaining = targetWords.filter { !solvedWords.contains(it) }
                        if (remaining.isNotEmpty()) {
                            val hintWord = remaining.random()
                            val newSolved = solvedWords + hintWord

                            if (gameMode == "campaign") {
                                saveCurrentLevelProgress(context, newSolved)
                            }
                            SoundManager.playSound(context, R.raw.success)
                            if (newSolved.size == targetWords.size) {
                                SoundManager.playSound(context, R.raw.victory)
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