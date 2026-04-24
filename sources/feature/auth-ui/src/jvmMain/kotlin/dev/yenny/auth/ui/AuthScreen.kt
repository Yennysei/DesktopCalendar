package dev.yenny.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import desktopcalendar.sources.feature.auth_ui.generated.resources.Res
import desktopcalendar.sources.feature.auth_ui.generated.resources.continue_with_google
import desktopcalendar.sources.feature.auth_ui.generated.resources.google_logo
import desktopcalendar.sources.feature.auth_ui.generated.resources.sign_in_message
import desktopcalendar.sources.feature.auth_ui.generated.resources.welcome
import dev.yenny.core.ui.CalendarTheme
import dev.yenny.core.ui.IconDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ButtonText = Color(0xFF374151)

@Composable
internal fun AuthScreen(
    viewModel: AuthViewModel = viewModel(factory = AuthUiComponent.instance.viewModelFactory),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    AuthScreenContent(state = state, onContinueWithGoogle = viewModel::logInGoogle)
}

@Composable
internal fun AuthScreenContent(
    state: AuthScreenState,
    onContinueWithGoogle: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center,
    ) {
        LoginCard(state = state, onContinueWithGoogle = onContinueWithGoogle)
    }
}

@Composable
private fun LoginCard(
    state: AuthScreenState,
    onContinueWithGoogle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            AppIcon()

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.welcome),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A2E),
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.sign_in_message),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            GoogleSignInButton(isDisabled = state.isGoogleButtonBlocked, onClick = onContinueWithGoogle)
        }
    }
}

@Composable
private fun AppIcon() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF3D5AFE)),
        contentAlignment = Alignment.Center
    ) {
        LockIcon()
    }
}

@Composable
private fun LockIcon() {
    Image(
        painter = painterResource(IconDrawable.lock24),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
    )
}

@Composable
private fun GoogleSignInButton(isDisabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        enabled = !isDisabled,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFE5E7EB),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = ButtonText,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            GoogleLogo()

            Spacer(Modifier.width(10.dp))

            Text(
                text = stringResource(Res.string.continue_with_google),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ButtonText
                )
            )
        }
    }
}

@Composable
private fun GoogleLogo() {
    Image(
        painter = painterResource(Res.drawable.google_logo),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Preview
@Composable
fun AuthScreenPreview() {
    CalendarTheme {
        AuthScreenContent(
            state = AuthScreenState(),
            onContinueWithGoogle = {},
        )
    }
}
