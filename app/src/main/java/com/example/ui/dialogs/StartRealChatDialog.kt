package com.example.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.ChatRepository
import kotlinx.coroutines.launch

/**
 * Real, working "start a new chat with an actual person" flow.
 *
 * Unlike the seeded demo contacts (Sarah, Alex, Priya), this looks up a REAL user
 * document in Firestore by phone number — meaning it only finds someone if they've
 * also opened this app at least once on a real device. This is the genuine
 * equivalent of adding a contact in WhatsApp.
 */
@Composable
fun StartRealChatDialog(
  repository: ChatRepository,
  onDismiss: () -> Unit,
  onChatReady: (chatId: String) -> Unit
) {
  var phoneNumber by remember { mutableStateOf("") }
  var isSearching by remember { mutableStateOf(false) }
  val lookupError by repository.realChatLookupError.collectAsState()
  val scope = rememberCoroutineScope()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Start a real chat") },
    text = {
      Column {
        Text("Enter the exact phone number of someone who has also opened Global Stream on their own device. This finds a real person — not a demo contact.")
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = phoneNumber,
          onValueChange = { phoneNumber = it },
          label = { Text("Phone number (e.g. +1 555-0123)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        if (isSearching) {
          Spacer(modifier = Modifier.height(12.dp))
          CircularProgressIndicator(modifier = Modifier.height(24.dp))
        }
        if (lookupError != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(lookupError ?: "", color = Color.Red)
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (phoneNumber.isBlank() || isSearching) return@TextButton
          isSearching = true
          scope.launch {
            val chatId = repository.startRealChatByPhoneNumber(phoneNumber.trim())
            isSearching = false
            if (chatId != null) {
              onChatReady(chatId)
            }
          }
        }
      ) { Text("Find & Chat") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
