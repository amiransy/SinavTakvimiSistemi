package com.amiranisayev.sinavtakvimiprojesi

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.amiranisayev.sinavtakvimiprojesi.ui.theme.SinavTakvimiProjesiTheme
import kotlinx.coroutines.launch
import android.Manifest

class MainActivity : ComponentActivity() {
    private val bildirimIzniLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { izinVerildi ->
        if (izinVerildi) {
            android.util.Log.d("BILDIRIM", "Bildirim izni verildi!")
        }
    }

    private val takvimIzniLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { izinler ->
        val yazmaIzni = izinler[Manifest.permission.WRITE_CALENDAR] ?: false
        if (yazmaIzni) {
            android.util.Log.d("TAKVIM", "Takvim izni verildi!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        takvimIzniLauncher.launch(arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        ))
        // Android versiyonu için bildirim izni isteği
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bildirimIzniLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            SinavTakvimiProjesiTheme {
                val sinavViewModel: SinavViewModel = viewModel()
                val context = androidx.compose.ui.platform.LocalContext.current

                // Kayıtlı kullanıcıyı kontrol et
                var currentUser = remember {
                    mutableStateOf(TercihYoneticisi.kullaniciyiGetir(context))
                }

                // Kayıtlı kullanıcı varsa verileri getir
                LaunchedEffect(currentUser.value) {
                    if (currentUser.value != null) {
                        sinavViewModel.verileriGetir()
                    }
                }

                Surface(color = MaterialTheme.colorScheme.background) {
                    if (currentUser.value == null) {
                        LoginEkrani(
                            onLoginSuccess = { user ->
                                Log.d("DEBUG_DETAY", "Gelen Obje: $user")
                                Log.d("DEBUG_DETAY", "Gelen Rol: ${user.rol}")
                                TercihYoneticisi.kullaniciyiKaydet(context, user) // kaydet
                                currentUser.value = user
                                sinavViewModel.verileriGetir()
                            }
                        )
                    } else {
                        val rol = currentUser.value?.rol ?: "Gozetmen"
                        AnaEkran(
                            viewModel = sinavViewModel,
                            userRole = rol,
                            kullanici = currentUser.value!!,
                            onCikis = {
                                TercihYoneticisi.cikisYap(context) // çıkışta temizle
                                currentUser.value = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(
    viewModel: SinavViewModel,
    userRole: String,
    kullanici: Kullanici,
    onCikis: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen = remember { mutableStateOf("sinav_takvimi") }
    val context= LocalContext.current

    LaunchedEffect(Unit) {
        BildirimYoneticisi.kanalOlustur(context)
        //viewModel.setContext(context)
    }

    LaunchedEffect(Unit) {
        BildirimYoneticisi.kanalOlustur(context)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = kullanici.kullaniciAdi,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = kullanici.rol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    label = { Text("Sınav Takvimi") },
                    selected = currentScreen.value == "sinav_takvimi",
                    onClick = {
                        currentScreen.value = "sinav_takvimi"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("İstatistikler") },
                    selected = currentScreen.value == "istatistik",
                    onClick = {
                        currentScreen.value = "istatistik"
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                if (userRole.equals("Admin", ignoreCase = true)) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Gözetmen Atama") },
                        selected = currentScreen.value == "gozetmen_atama",
                        onClick = {
                            currentScreen.value = "gozetmen_atama"
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                    label = { Text("Çıkış") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCikis()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        when (currentScreen.value) {
            "sinav_takvimi" -> SinavEkranı(
                viewModel = viewModel,
                userRole = userRole,
                onMenuClick = { scope.launch { drawerState.open() } },
                onGozetmenAtamaClick = { currentScreen.value = "gozetmen_atama" }
            )
            "gozetmen_atama" -> GozetmenAtamaEkrani(
                viewModel = viewModel,
                onMenuClick = { scope.launch { drawerState.open() } },
                onGeriClick = {
                    currentScreen.value = "sinav_takvimi"
                    viewModel.verileriGetir()
                }
            )
            "istatistik" -> IstatistikEkrani(
                viewModel = viewModel,
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@Composable
fun LoginEkrani(onLoginSuccess: (Kullanici) -> Unit) {
    var username = remember { mutableStateOf("") }
    var password = remember { mutableStateOf("") }
    var errorMessage = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val apiService = ApiService()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sınav Sistemi Giriş", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Kullanıcı Adı") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    val user = apiService.login(username.value, password.value)
                    if (user != null) {
                        onLoginSuccess(user)
                    } else {
                        errorMessage.value = "Hatalı kullanıcı adı veya şifre!"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Giriş Yap")
        }
        if (errorMessage.value.isNotEmpty()) {
            Text(
                errorMessage.value,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}