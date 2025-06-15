package com.ideiassertiva.FypMatch.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
// import androidx.compose.material.icons.filled.Visibility
// import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.ideiassertiva.FypMatch.data.repository.NavigationState
import com.ideiassertiva.FypMatch.data.repository.PhoneVerificationState
import com.ideiassertiva.FypMatch.model.isProfileComplete
import com.ideiassertiva.FypMatch.ui.theme.FypMatchTheme
import com.ideiassertiva.FypMatch.ui.viewmodel.LoginViewModel
import com.ideiassertiva.FypMatch.ui.viewmodel.LoginMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToPhotoUpload: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDiscovery: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle()
    val needsInteractiveSignIn by viewModel.needsInteractiveSignIn.collectAsStateWithLifecycle()
    val phoneVerificationState by viewModel.phoneVerificationState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Estados para campos de input
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailVerificationSent by remember { mutableStateOf(false) }
    
    // Sincronizar com o estado do ViewModel
    LaunchedEffect(uiState.emailVerificationSent) {
        emailVerificationSent = uiState.emailVerificationSent
    }
    
    // Launcher para fallback quando Credential Manager falha
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        println("🔍 DEBUG - Resultado do launcher fallback: resultCode=${result.resultCode}, data=${result.data != null}")
        viewModel.handleGoogleSignInResult(result.data)
    }
    
    // Navegação automática baseada no estado
    LaunchedEffect(navigationState) {
        when (navigationState) {
            is NavigationState.ToPhotoUpload -> {
                onNavigateToPhotoUpload()
                viewModel.clearNavigationState()
            }
            is NavigationState.ToProfile -> {
                onNavigateToProfile()
                viewModel.clearNavigationState()
            }
            is NavigationState.ToDiscovery -> {
                onNavigateToDiscovery()
                viewModel.clearNavigationState()
            }
            else -> {} // Não navegar
        }
    }
    
    // Fallback para quando Credential Manager falha
    LaunchedEffect(needsInteractiveSignIn) {
        if (needsInteractiveSignIn) {
            println("🔍 DEBUG - Iniciando fallback do Google Sign-In")
            try {
                val signInIntent = viewModel.getGoogleSignInIntent()
                googleSignInLauncher.launch(signInIntent)
            } catch (e: Exception) {
                println("🔍 DEBUG - Erro ao obter Intent fallback: ${e.message}")
                viewModel.handleSignInError("Erro ao iniciar login: ${e.message}")
            }
        }
    }
    
    // Mostrar mensagens de erro
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo e título
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "FypMatch",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Encontre conexões autênticas",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Seletor de modo de login
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Como você quer entrar?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botões de seleção
                Row(
            modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LoginModeButton(
                        text = "Google",
                        icon = "🔐",
                        isSelected = uiState.loginMode == LoginMode.GOOGLE,
                        onClick = { viewModel.setLoginMode(LoginMode.GOOGLE) }
                    )
                    
                    LoginModeButton(
                        text = "Email",
                        icon = "📧",
                        isSelected = uiState.loginMode == LoginMode.EMAIL,
                        onClick = { viewModel.setLoginMode(LoginMode.EMAIL) }
                    )
                    
                    LoginModeButton(
                        text = "Telefone",
                        icon = "📱",
                        isSelected = uiState.loginMode == LoginMode.PHONE,
                        onClick = { viewModel.setLoginMode(LoginMode.PHONE) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Conteúdo baseado no modo selecionado
        when (uiState.loginMode) {
            LoginMode.GOOGLE -> {
                GoogleLoginSection(
                    uiState = uiState,
                    needsInteractiveSignIn = needsInteractiveSignIn,
                    onSignIn = { viewModel.signInWithGoogle() }
                )
            }
            LoginMode.EMAIL -> {
                EmailLoginSection(
                    email = email,
                    password = password,
                    isSignUp = isSignUp,
                    passwordVisible = passwordVisible,
                    isLoading = uiState.isLoading,
                    emailVerificationSent = emailVerificationSent,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onModeToggle = { isSignUp = !isSignUp },
                    onSignIn = { viewModel.signInWithEmail(email, password) },
                    onSignUp = { viewModel.signUpWithEmail(email, password) },
                                         onCheckVerification = { viewModel.checkEmailVerification() },
                     onResendVerification = { viewModel.resendEmailVerification() }
                )
            }
            LoginMode.PHONE -> {
                PhoneLoginSection(
                    phoneNumber = phoneNumber,
                    verificationCode = verificationCode,
                    isLoading = uiState.isLoading,
                    phoneVerificationStarted = uiState.phoneVerificationStarted,
                    phoneVerificationState = phoneVerificationState,
                    onPhoneNumberChange = { phoneNumber = it },
                    onVerificationCodeChange = { verificationCode = it },
                    onStartVerification = { 
                        viewModel.startPhoneVerification(phoneNumber, context as Activity)
                    },
                    onVerifyCode = { viewModel.verifyPhoneCode(verificationCode) },
                    onResendCode = { viewModel.resendPhoneCode(context as Activity) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mensagens de erro
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // Mensagens de sucesso
        uiState.successMessage?.let { message ->
            Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        uiState.cancelledMessage?.let { message ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LoginModeButton(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.surface
        )
    ) {
        Text(icon)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                   else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun GoogleLoginSection(
    uiState: com.ideiassertiva.FypMatch.ui.viewmodel.LoginUiState,
    needsInteractiveSignIn: Boolean,
    onSignIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "✨ Por que escolher o FypMatch?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("🤖 IA para compatibilidade real")
                Text("🌈 Assistente para neurodiversidade")
                Text("🏆 Sistema de verificação inclusivo")
                Text("💕 Relacionamentos autênticos")
                Text("🔒 Privacidade e segurança")
            }
        }
        
    Spacer(modifier = Modifier.height(24.dp))
        
    Button(
        onClick = onSignIn,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !uiState.isLoading && !needsInteractiveSignIn
    ) {
        when {
            needsInteractiveSignIn -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escolha sua conta Google...")
                }
            }
            uiState.isLoading -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fazendo login...")
                }
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔐")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entrar com Google")
                }
            }
        }
    }
}

@Composable
private fun EmailLoginSection(
    email: String,
    password: String,
    isSignUp: Boolean,
    passwordVisible: Boolean,
    isLoading: Boolean,
    emailVerificationSent: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onModeToggle: () -> Unit,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onCheckVerification: () -> Unit,
    onResendVerification: () -> Unit
) {
    Column {
        if (emailVerificationSent) {
            // Tela de verificação de email
            EmailVerificationSection(
                email = email,
                isLoading = isLoading,
                onCheckVerification = onCheckVerification,
                onResendVerification = onResendVerification,
                onBackToSignUp = onModeToggle
            )
        } else {
            // Tela normal de login/cadastro
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                visualTransformation = if (passwordVisible) VisualTransformation.None 
                                     else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    TextButton(onClick = onPasswordVisibilityToggle) {
                        Text(
                            text = if (passwordVisible) "👁️" else "🙈",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (isSignUp) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Mínimo 6 caracteres\n• Você receberá um email de verificação",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = if (isSignUp) onSignUp else onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSignUp) "Criando conta..." else "Entrando...")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isSignUp) "📧" else "🔑")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSignUp) "Criar conta" else "Entrar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onModeToggle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isSignUp) "Já tem conta? Entrar" 
                    else "Não tem conta? Criar"
                )
            }
        }
    }
}

@Composable
private fun EmailVerificationSection(
    email: String,
    isLoading: Boolean,
    onCheckVerification: () -> Unit,
    onResendVerification: () -> Unit,
    onBackToSignUp: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ícone de email
        Text(
            text = "📧",
            style = MaterialTheme.typography.displayMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Verifique seu email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Enviamos um email de verificação para:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📋 Instruções:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "1. Abra seu email\n2. Procure por um email do FypMatch\n3. Clique no link de verificação\n4. Volte aqui e clique em 'Verificar'",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Botão principal de verificação
        Button(
            onClick = onCheckVerification,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificando...")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✅")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verificar email")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Botões secundários
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = onResendVerification,
                enabled = !isLoading
            ) {
                Text("🔄 Reenviar email")
            }
            
            TextButton(
                onClick = onBackToSignUp,
                enabled = !isLoading
            ) {
                Text("⬅️ Voltar")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Não recebeu o email? Verifique sua pasta de spam ou lixo eletrônico.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PhoneLoginSection(
    phoneNumber: String,
    verificationCode: String,
    isLoading: Boolean,
    phoneVerificationStarted: Boolean,
    phoneVerificationState: PhoneVerificationState,
    onPhoneNumberChange: (String) -> Unit,
    onVerificationCodeChange: (String) -> Unit,
    onStartVerification: () -> Unit,
    onVerifyCode: () -> Unit,
    onResendCode: () -> Unit
) {
    Column {
        if (!phoneVerificationStarted) {
            // Primeira etapa: inserir número de telefone
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Número de telefone") },
                placeholder = { Text("+55 11 99999-9999") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Você receberá um código de verificação via SMS",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onStartVerification,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && phoneNumber.isNotBlank()
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando código...")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📱")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar código")
            }
        }
            }
        } else {
            // Segunda etapa: inserir código de verificação
            Text(
                text = "Código enviado para $phoneNumber",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = verificationCode,
                onValueChange = onVerificationCodeChange,
                label = { Text("Código de verificação") },
                placeholder = { Text("123456") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onVerifyCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && verificationCode.isNotBlank()
            ) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificando...")
                    }
                } else {
                    Text("Verificar código")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = onResendCode,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Reenviar código")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FypMatchTheme {
        LoginScreen(
            onNavigateToPhotoUpload = {},
            onNavigateToProfile = {},
            onNavigateToDiscovery = {}
        )
    }
} 
