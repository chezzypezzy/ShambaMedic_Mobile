package com.example.shambamedic.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.shambamedic.R
import com.example.shambamedic.presentation.navigation.Screen
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pinVisible by remember { mutableStateOf(false) }
    var confirmPinVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val coroutineScope = rememberCoroutineScope()
    val googleWebClientId = stringResource(R.string.google_web_client_id)
    val errorUnexpectedCredential = stringResource(R.string.error_unexpected_credential)
    val errorGoogleSignInCancelledTemplate = stringResource(R.string.error_google_signin_cancelled)
    val fallbackGoogleUserName = stringResource(R.string.fallback_google_user_name)

    fun triggerGoogleSignIn() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(googleWebClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.onGoogleSignInResult(
                        googleId = googleIdTokenCredential.id,
                        email = googleIdTokenCredential.id,
                        displayName = googleIdTokenCredential.displayName
                            ?: fallbackGoogleUserName
                    )
                } else {
                    viewModel.onGoogleSignInError(errorUnexpectedCredential)
                }
            } catch (e: GetCredentialException) {
                viewModel.onGoogleSignInError(
                    errorGoogleSignInCancelledTemplate.format(e.message)
                )
            }
        }
    }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    val primaryGreen = Color(0xFF2E7D32)
    val secondaryGreen = Color(0xFF66BB6A)
    val backgroundGrey = Color(0xFFF5F5F5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGrey)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // TOP SECTION
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(primaryGreen)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            // Language toggle
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                LanguageToggle("EN", uiState.selectedLanguage == "en") {
                    viewModel.onLanguageChange("en")
                }
                Spacer(modifier = Modifier.width(8.dp))
                LanguageToggle("SW", uiState.selectedLanguage == "sw") {
                    viewModel.onLanguageChange("sw")
                }
            }
        }

        TabRow(
            selectedTabIndex = if (uiState.isLoginMode) 0 else 1,
            containerColor = Color.White,
            contentColor = primaryGreen,
            indicator = { tabPositions ->
                if (uiState.isLoginMode) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[0]),
                        color = primaryGreen
                    )
                } else {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[1]),
                        color = primaryGreen
                    )
                }
            }
        ) {
            Tab(
                selected = uiState.isLoginMode,
                onClick = { if (!uiState.isLoginMode) viewModel.toggleMode() },
                text = { Text(stringResource(R.string.tab_login)) }
            )
            Tab(
                selected = !uiState.isLoginMode,
                onClick = { if (uiState.isLoginMode) viewModel.toggleMode() },
                text = { Text(stringResource(R.string.tab_register)) }
            )
        }

        // FORM CARD
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val fieldTextStyle = LocalTextStyle.current.copy(
                    color = Color(0xFF1B1B1B),
                    fontSize = 16.sp
                )
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1B1B1B),
                    unfocusedTextColor = Color(0xFF1B1B1B)
                )

                if (!uiState.isLoginMode) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.label_full_name)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        textStyle = fieldTextStyle,
                        colors = fieldColors
                    )
                }

                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text(stringResource(R.string.label_phone_number)) },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    prefix = { Text("+254 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    textStyle = fieldTextStyle,
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = uiState.pin,
                    onValueChange = viewModel::onPinChange,
                    label = { Text(stringResource(R.string.label_pin)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = if (pinVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                imageVector = if (pinVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.pinError != null,
                    supportingText = uiState.pinError?.let { { Text(it) } },
                    textStyle = fieldTextStyle,
                    colors = fieldColors
                )

                if (!uiState.isLoginMode) {
                    OutlinedTextField(
                        value = uiState.confirmPin,
                        onValueChange = viewModel::onConfirmPinChange,
                        label = { Text(stringResource(R.string.label_confirm_pin)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = if (confirmPinVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmPinVisible = !confirmPinVisible }) {
                                Icon(
                                    imageVector = if (confirmPinVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.confirmPinError != null,
                        supportingText = uiState.confirmPinError?.let { { Text(it) } },
                        textStyle = fieldTextStyle,
                        colors = fieldColors
                    )
                }

                if (uiState.generalError != null) {
                    Text(
                        text = uiState.generalError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = viewModel::authenticate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isLoginMode) stringResource(R.string.tab_login) else stringResource(R.string.action_create_account),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isLoginMode) stringResource(R.string.prompt_no_account) else stringResource(R.string.prompt_has_account),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = viewModel::toggleMode) {
                        Text(
                            text = if (uiState.isLoginMode) stringResource(R.string.tab_register) else stringResource(R.string.tab_login),
                            color = primaryGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.divider_or),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = { triggerGoogleSignIn() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    enabled = !uiState.isGoogleSignInLoading
                ) {
                    if (uiState.isGoogleSignInLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = primaryGreen
                        )
                    } else {
                        // NOTE: placeholder icon -- replace with a proper Google "G" logo
                        // asset before production release.
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.action_continue_google),
                            fontSize = 15.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageToggle(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isActive) Color.White else Color.Transparent,
            contentColor = if (isActive) Color(0xFF2E7D32) else Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(navController = rememberNavController())
}
