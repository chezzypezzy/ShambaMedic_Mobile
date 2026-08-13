package com.example.shambamedic.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.shambamedic.R
import com.example.shambamedic.presentation.auth.LanguageToggle
import com.example.shambamedic.presentation.common.getAppLocale
import com.example.shambamedic.presentation.common.setAppLocale
import com.example.shambamedic.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryGreen = Color(0xFF2E7D32)
    val secondaryGreen = Color(0xFF66BB6A)
    val backgroundGrey = Color(0xFFF5F5F5)

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            navController.navigate(Screen.Auth.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_profile), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_desc_back), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryGreen)
            )
        },
        containerColor = backgroundGrey
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryGreen)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(secondaryGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = uiState.phoneNumber,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                // Reflects the actual live app locale (set via the toggle on the auth
                // screen), not uiState.languagePreference - that DB field is currently
                // always "en" at account creation and is never updated by the toggle, so
                // it would show stale/wrong data here.
                val context = LocalContext.current
                val isSwahili = getAppLocale(context) == "sw"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.profile_language_label),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .background(primaryGreen, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        LanguageToggle("EN", !isSwahili) {
                            setAppLocale(context, "en")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        LanguageToggle("SW", isSwahili) {
                            setAppLocale(context, "sw")
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C))
                ) {
                    Text(stringResource(R.string.action_logout))
                }
            }
        }
    }
}
