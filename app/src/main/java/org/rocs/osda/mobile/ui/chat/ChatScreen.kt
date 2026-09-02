package org.rocs.osda.mobile.ui.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.rocs.osda.mobile.data.model.ChatMessage
import org.rocs.osda.mobile.ui.common.BackHeader
import org.rocs.osda.mobile.ui.theme.OsdaTokens

@Composable
fun ChatScreen(viewModel: ChatViewModel, onBack: () -> Unit, onViewAppeals: () -> Unit, onViewOffenses: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader("Ask the Chatbot", onBack)

        if (state.messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ask about your own offense records, appeal status, or how to file an appeal.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.messages) { message -> MessageBubble(message) }
                if (state.isSending) {
                    item { TypingIndicator(isFirstMessage = state.messages.size <= 1) }
                }
            }
        }

        if (state.quickReplies.isNotEmpty() && !state.isSending) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(state.quickReplies) { reply ->
                    AssistChip(
                        // "View My Appeals"/"View My Offenses" leave the chat
                        // screen entirely, so they're handled here as real
                        // navigation instead of going through the ViewModel
                        // like the other chips.
                        onClick = {
                            when (reply.id) {
                                "view_appeals" -> onViewAppeals()
                                "view_offenses", "appeal_go_manual" -> onViewOffenses()
                                else -> viewModel.onQuickReplySelected(reply)
                            }
                        },
                        label = { Text(reply.label) }
                    )
                }
            }
        }

        state.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = state.input,
                onValueChange = viewModel::onInputChange,
                placeholder = { Text("Type a message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(OsdaTokens.inputRadius),
                singleLine = true
            )
            IconButton(
                onClick = viewModel::send,
                enabled = !state.isSending && state.input.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (!state.isSending && state.input.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val bubbleWidth = if (isUser) Modifier.widthIn(max = 260.dp) else Modifier.fillMaxWidth(0.92f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = bubbleWidth
                .then(
                    if (isUser) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = 14.dp,
                                bottomEnd = 2.dp
                            )
                        )
                    } else {
                        val shape = RoundedCornerShape(
                            topStart = 14.dp,
                            topEnd = 14.dp,
                            bottomStart = 2.dp,
                            bottomEnd = 14.dp
                        )
                        Modifier
                            .background(color = MaterialTheme.colorScheme.surface, shape = shape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                    }
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            MarkdownText(
                text = message.content,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TypingIndicator(isFirstMessage: Boolean) {
    val transition = rememberInfiniteTransition(label = "thinking-pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking-alpha"
    )
    val label = if (isFirstMessage) "Waking up the assistant..." else "Thinking..."

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                label,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
