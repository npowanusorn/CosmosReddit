package com.hamburghini.cosmos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.hamburghini.cosmos.model.AuthState
import com.hamburghini.cosmos.model.RedditAccount
import com.hamburghini.cosmos.model.UserInfo
import com.hamburghini.cosmos.ui.theme.RedditOrange
import com.hamburghini.cosmos.core.util.AccountUtils
import com.hamburghini.cosmos.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcherBottomSheet(
    onDismissRequest: () -> Unit,
    onAddAccountClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    val scope = rememberCoroutineScope()

    val authState by viewModel.authState.collectAsState()
    val storedAccounts by viewModel.storedAccounts.collectAsState()
    val isSwitchingAccount by viewModel.isSwitchingAccount.collectAsState()
    val switchError by viewModel.switchError.collectAsState()

    var selectedAccount by remember { mutableStateOf<RedditAccount?>(null) }

    // Handle successful account switch
    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedIn && selectedAccount != null) {
            val currentUsername = (authState as AuthState.LoggedIn).account.username
            if (currentUsername == selectedAccount?.username) {
                // Account switch successful, close sheet after a brief delay
                delay(500)
                sheetState.hide()
                onDismissRequest()
                selectedAccount = null
            }
        }
    }

    // Show error if switch fails
    LaunchedEffect(switchError) {
        if (switchError != null) {
            // Error will be shown in UI, clear selection
            selectedAccount = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!isSwitchingAccount) {
                viewModel.clearSwitchError()
                onDismissRequest()
            }
        },
        sheetState = sheetState
    ) {
        ProfileSwitcherContent(
            authState = authState,
            storedAccounts = storedAccounts,
            isSwitchingAccount = isSwitchingAccount,
            switchError = switchError,
            selectedAccount = selectedAccount,
            onAccountClick = { account ->
                selectedAccount = account
                viewModel.switchAccount(account)
            },
            onAddAccountClick = {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                    onAddAccountClick()
                }
            },
            onLogoutClick = {
                scope.launch {
                    viewModel.logout()
                    delay(300)
                    sheetState.hide()
                    onDismissRequest()
                }
            },
            onRemoveAccount = { account ->
                viewModel.removeAccount(account)
            },
            onDismissError = {
                viewModel.clearSwitchError()
            }
        )
    }
}

@Composable
private fun ProfileSwitcherContent(
    authState: AuthState,
    storedAccounts: List<RedditAccount>,
    isSwitchingAccount: Boolean,
    switchError: String?,
    selectedAccount: RedditAccount?,
    onAccountClick: (RedditAccount) -> Unit,
    onAddAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRemoveAccount: (RedditAccount) -> Unit,
    onDismissError: () -> Unit
) {
    val isLoggedIn = authState is AuthState.LoggedIn
    val hasContent = isLoggedIn || storedAccounts.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // Header
        ProfileSwitcherHeader(
            isSwitchingAccount = isSwitchingAccount,
            onAddAccountClick = onAddAccountClick
        )

        // Loading bar
        AnimatedVisibility(
            visible = isSwitchingAccount,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = RedditOrange
            )
        }

        if (hasContent) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Error Message
        AnimatedVisibility(
            visible = switchError != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            ErrorBanner(
                message = switchError ?: "",
                onDismiss = onDismissError
            )
        }

        // Current Account Section
        if (authState is AuthState.LoggedIn) {
            Text(
                text = "Current Account",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            CurrentAccountCard(
                account = authState.account,
                userInfo = authState.userInfo,
                onLogoutClick = onLogoutClick,
                isLoading = isSwitchingAccount
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Other Accounts Section
        val currentUsername = (authState as? AuthState.LoggedIn)?.account?.username
        val otherAccounts = AccountUtils.sortAccounts(
            storedAccounts.filter { it.username != currentUsername }
        )

        if (otherAccounts.isNotEmpty()) {
            Text(
                text = "Other Accounts",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(otherAccounts) { account ->
                    AccountListItem(
                        account = account,
                        isProcessing = isSwitchingAccount,
                        isSelected = selectedAccount?.username == account.username,
                        onClick = { onAccountClick(account) },
                        onRemove = { onRemoveAccount(account) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileSwitcherHeader(
    isSwitchingAccount: Boolean,
    onAddAccountClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Accounts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Show loading indicator in header when switching
        if (isSwitchingAccount) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = RedditOrange
            )
        } else {
            IconButton(
                onClick = onAddAccountClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun CurrentAccountCard(
    account: RedditAccount,
    userInfo: UserInfo,
    onLogoutClick: () -> Unit,
    isLoading: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue = if (isLoading) 0.6f else 1f,
        animationSpec = tween(300),
        label = "alphaAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (userInfo.iconImg?.isNotBlank() == true) {
                AsyncImage(
                    model = userInfo.iconImg,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(RedditOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AccountUtils.getInitials(account.username),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Account Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AccountUtils.getDisplayUsername(account.username),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active account",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "${AccountUtils.formatKarma(AccountUtils.getTotalKarma(userInfo))} karma",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Logout Button
            IconButton(
                onClick = onLogoutClick,
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Log out",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AccountListItem(
    account: RedditAccount,
    isProcessing: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isSelected && isProcessing) 0.95f else 1f,
        animationSpec = tween(300),
        label = "scaleAnimation"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isProcessing && !isSelected) 0.5f else 1f,
        animationSpec = tween(300),
        label = "alphaAnimation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isProcessing) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        enabled = !isProcessing,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box {
                if (account.iconImg?.isNotBlank() == true) {
                    AsyncImage(
                        model = account.iconImg,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AccountUtils.getInitials(account.username),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Show loading indicator overlay
                if (isSelected && isProcessing) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Account Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = AccountUtils.getDisplayUsername(account.username),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isSelected && isProcessing) "Switching..." else "Tap to switch",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected && isProcessing) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Remove Button
            IconButton(
                onClick = { showRemoveDialog = true },
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove account",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Remove Account Confirmation Dialog
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = {
                Text("Remove Account?")
            },
            text = {
                Text("Are you sure you want to remove ${AccountUtils.getDisplayUsername(account.username)} from this device? You can add it back by logging in again.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove()
                        showRemoveDialog = false
                    }
                ) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview
@Composable
fun PreviewProfileSwitcherHeader() {
    ProfileSwitcherHeader(
        isSwitchingAccount = false,
        onAddAccountClick = {}
    )
}