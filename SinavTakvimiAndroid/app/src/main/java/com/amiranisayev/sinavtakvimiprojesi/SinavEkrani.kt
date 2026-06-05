package com.amiranisayev.sinavtakvimiprojesi

import android.R
import android.R.attr.id
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@Composable
fun SinavEkranı(
    viewModel: SinavViewModel,
    userRole:String,
    onGozetmenAtamaClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Log.d("DEBUG_ROL", "Gelen Rol: $userRole")
    var showDialog = remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    var takvimMesaji = remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Sınav Takvimi") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü")
                    }
                },
                actions = {
                    if (userRole.equals("Admin", ignoreCase = true)) {
                        IconButton(onClick = { viewModel.yedekAl() }) {
                            Icon(Icons.Default.Save, contentDescription = "Yedek Al")
                        }
                        IconButton(onClick = onGozetmenAtamaClick) {
                            Icon(Icons.Default.Person, contentDescription = "Gözetmen Atama")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Sadece Admin ise butonu gösteriyoruz
            if (userRole.equals("Admin",ignoreCase=true)) {
                FloatingActionButton(
                    onClick = { showDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add,"Sınav Ekle")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            //yedekleme mesajı
            viewModel.yedekMesaji.value?.let { mesaj ->
                Snackbar(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(mesaj)
                }
            }
            //ekleme mesajı
            viewModel.eklemeMesaji.value?.let { mesaj ->
                Snackbar(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(mesaj)
                }
            }
            if (viewModel.yukleniyorMu.value) {
                // Veri yüklenirken dönen çember
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.hataMesaji.value != null) {
                // Hata varsa mesajı ve tekrar dene butonunu gösterir
                Text(text = viewModel.hataMesaji.value!!,modifier=Modifier, color = MaterialTheme.colorScheme.error)
                Button(onClick = { viewModel.verileriGetir() }) {
                    Text("Tekrar Dene")
                }
            } else {
                takvimMesaji.value?.let { mesaj ->
                    Snackbar(modifier = Modifier.padding(8.dp)) {
                        Text(mesaj)
                    }
                }
                // Veri geldiyse listeler
                LazyColumn {
                    items(viewModel.sinavListesi.value) { sinav ->
                        SinavKarti(
                            exam = sinav,
                            userRole = userRole,
                            onDeleteClick = { id ->
                                val etkinlikId = TercihYoneticisi.takvimEtkinlikIdGetir(context, id)
                                android.util.Log.d("TakvimSil", "SinavID: $id, EtkinlikID: $etkinlikId")
                                if (etkinlikId != -1L) {
                                    android.util.Log.d("TakvimSil", "Takvimden siliniyor: $etkinlikId")
                                    TakvimYoneticisi.sinaviTakvimdenSil(context, etkinlikId)
                                    TercihYoneticisi.takvimEtkinlikIdSil(context, id)
                                } else {
                                    android.util.Log.d("TakvimSil", "Takvim ID bulunamadı, silinemedi")
                                }
                                viewModel.sinavSil(id)
                            },
                            onTakvimeEkleClick = { sinavDetay ->
                                val etkinlikId = TakvimYoneticisi.sinaviTakvimaEkle(context, sinavDetay)
                                if (etkinlikId != null) {
                                    TercihYoneticisi.takvimEtkinlikIdKaydet(context, sinavDetay.SinavID!!, etkinlikId)
                                    takvimMesaji.value = "Sınav takvime eklendi! 📅"
                                } else {
                                    takvimMesaji.value = "Takvime eklendi (ID kaydedilemedi)"
                                }
                                kotlinx.coroutines.MainScope().launch {
                                    kotlinx.coroutines.delay(3000)
                                    takvimMesaji.value = null
                                }
                            }
                        )
                    }
                }
            }
        }
        if (showDialog.value) {
            SinavEklemeDialog(
                onDismiss = { showDialog.value = false },
                onConfirm = { yeniSinav ->
                    viewModel.sinavEkle(yeniSinav)
                    showDialog.value = false
                },
                oturumListesi = viewModel.oturumListesi.value,
                bolumListesi = viewModel.bolumListesi.value,
                dersListesi = viewModel.dersListesi.value,
                onTarihSecildi = { tarih ->
                    viewModel.oturumlariGetir(tarih)
                },
                onBolumSecildi = { bolumId, yariyil ->
                    viewModel.dersleriGetir(bolumId, yariyil)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SinavEklemeDialog(
    onDismiss: () -> Unit,
    onConfirm: (SinavTakvimi) -> Unit,
    oturumListesi: List<Oturum>,
    bolumListesi: List<Bolum>,
    //derslikListesi: List<Derslik>,
    dersListesi: List<Ders>,
    onTarihSecildi: (String) -> Unit,
    onBolumSecildi: (Int, Int?) -> Unit
) {
    val dersKodu = remember { mutableStateOf("") }
    val dersAdi = remember { mutableStateOf("") }
    val tarih = remember { mutableStateOf("") }
    val secilenBolum = remember { mutableStateOf<Bolum?>(null) }
    val secilenDonem = remember { mutableStateOf<Int?>(null) }
    val secilenDers = remember { mutableStateOf<Ders?>(null) }
    val secilenOturum = remember { mutableStateOf<Oturum?>(null) }
    //val secilenDerslik = remember { mutableStateOf<Derslik?>(null) }

    val bolumDropdownAcik = remember { mutableStateOf(false) }
    val donemDropdownAcik = remember { mutableStateOf(false) }
    val dersDropdownAcik = remember { mutableStateOf(false) }
    val oturumDropdownAcik = remember { mutableStateOf(false) }
    //val derslikDropdownAcik = remember { mutableStateOf(false) }
    val tarihPickerAcik = remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val donemler = listOf(1, 2, 3, 4, 5, 6, 7, 8)

    if (tarihPickerAcik.value) {
        DatePickerDialog(
            onDismissRequest = { tarihPickerAcik.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val secilenTarih = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                            .toString()
                        tarih.value = secilenTarih
                        onTarihSecildi(secilenTarih)
                        secilenOturum.value = null
                    }
                    tarihPickerAcik.value = false
                }) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { tarihPickerAcik.value = false }) {
                    Text("İptal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Sınav Ekle") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Bölüm Dropdown
                ExposedDropdownMenuBox(
                    expanded = bolumDropdownAcik.value,
                    onExpandedChange = { bolumDropdownAcik.value = !bolumDropdownAcik.value }
                ) {
                    OutlinedTextField(
                        value = secilenBolum.value?.BolumAdi ?: "Bölüm Seç",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bölüm") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bolumDropdownAcik.value) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = bolumDropdownAcik.value,
                        onDismissRequest = { bolumDropdownAcik.value = false }
                    ) {
                        bolumListesi.forEach { bolum ->
                            DropdownMenuItem(
                                text = { Text(bolum.BolumAdi) },
                                onClick = {
                                    secilenBolum.value = bolum
                                    secilenDonem.value = null
                                    secilenDers.value = null
                                    bolumDropdownAcik.value = false
                                }
                            )
                        }
                    }
                }

                // 2. Dönem Dropdown
                ExposedDropdownMenuBox(
                    expanded = donemDropdownAcik.value,
                    onExpandedChange = {
                        if (secilenBolum.value != null) {
                            donemDropdownAcik.value = !donemDropdownAcik.value
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = secilenDonem.value?.let { "${it}. Dönem" }
                            ?: if (secilenBolum.value == null) "Önce bölüm seçin" else "Dönem Seç",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dönem") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = donemDropdownAcik.value) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = donemDropdownAcik.value,
                        onDismissRequest = { donemDropdownAcik.value = false }
                    ) {
                        donemler.forEach { donem ->
                            DropdownMenuItem(
                                text = { Text("${donem}. Dönem") },
                                onClick = {
                                    secilenDonem.value = donem
                                    secilenDers.value = null
                                    donemDropdownAcik.value = false
                                    secilenBolum.value?.let {
                                        onBolumSecildi(it.BolumID, donem)
                                    }
                                }
                            )
                        }
                    }
                }

                // 3. Ders Dropdown
                ExposedDropdownMenuBox(
                    expanded = dersDropdownAcik.value,
                    onExpandedChange = {
                        if (secilenDonem.value != null) {
                            dersDropdownAcik.value = !dersDropdownAcik.value
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = secilenDers.value?.let { "${it.DersKodu} - ${it.Ad}" }
                            ?: if (secilenDonem.value == null) "Önce dönem seçin" else "Ders Seç",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ders") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dersDropdownAcik.value) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dersDropdownAcik.value,
                        onDismissRequest = { dersDropdownAcik.value = false },
                        modifier = Modifier.heightIn(max = 300.dp) // kaydırılabilir
                    ) {
                        dersListesi.forEach { ders ->
                            DropdownMenuItem(
                                text = { Text("${ders.DersKodu} - ${ders.Ad}") },
                                onClick = {
                                    secilenDers.value = ders
                                    dersKodu.value = ders.DersKodu
                                    dersAdi.value = ders.Ad
                                    dersDropdownAcik.value = false
                                }
                            )
                        }
                    }
                }

                // 4. Tarih Seçici
                OutlinedTextField(
                    value = tarih.value,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tarih") },
                    trailingIcon = {
                        IconButton(onClick = { tarihPickerAcik.value = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Tarih Seç")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Oturum Dropdown
                ExposedDropdownMenuBox(
                    expanded = oturumDropdownAcik.value,
                    onExpandedChange = {
                        if (tarih.value.isEmpty()) {
                        } else {
                            oturumDropdownAcik.value = !oturumDropdownAcik.value
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = secilenOturum.value?.let {
                            "${it.Tanim} (${it.BaslangicSaat} - ${it.BitisSaat})"
                        } ?: if (tarih.value.isEmpty()) "Önce tarih seçin" else "Oturum Seç",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Oturum") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = oturumDropdownAcik.value) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = oturumDropdownAcik.value,
                        onDismissRequest = { oturumDropdownAcik.value = false }
                    ) {
                        oturumListesi.forEach { oturum ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${oturum.Tanim} (${oturum.BaslangicSaat} - ${oturum.BitisSaat})" +
                                                if (!oturum.bosMu) " — Dolu" else "",
                                        color = if (!oturum.bosMu)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    if (oturum.bosMu) {
                                        secilenOturum.value = oturum
                                        oturumDropdownAcik.value = false
                                    }
                                },
                                enabled = oturum.bosMu
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (dersKodu.value.isNotEmpty() && dersAdi.value.isNotEmpty() &&
                    secilenBolum.value != null && tarih.value.isNotEmpty() &&
                    secilenOturum.value != null
                ) {
                    val yeniSinav = SinavTakvimi(
                        SinavID = 0,
                        DersKodu = dersKodu.value,
                        DersAdi = dersAdi.value,
                        BolumAdi = secilenBolum.value!!.BolumAdi,
                        Tarih = tarih.value,
                        Oturum = secilenOturum.value!!.OturumID.toString(),
                        BaslangicSaat = "",
                        BitisSaat = "",
                        SalonAdi = "",
                        KatBilgisi = 0
                    )
                    onConfirm(yeniSinav)
                }
            }) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}

@Composable
fun SinavKarti(
    exam: SinavDetay,
    userRole: String,
    onDeleteClick: (Int) -> Unit,
    onTakvimeEkleClick: (SinavDetay) -> Unit // yeni parametre
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f)
        ),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exam.DersAdi ?: "Bilinmeyen Ders",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = exam.Tarih ?: "", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${exam.BaslangicSaat} - ${exam.BitisSaat}", style = MaterialTheme.typography.bodyMedium)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Salon: ${exam.SalonAdi}", style = MaterialTheme.typography.bodySmall)
                }

                if (exam.Gozetmenler.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Person, contentDescription = null,
                            modifier = Modifier.size(16.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Gözetmenler:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            exam.Gozetmenler.forEach { gozetmen ->
                                Text(
                                    text = "• $gozetmen",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            // Sağ taraf butonlar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Takvime ekle butonu
                IconButton(
                    onClick = { onTakvimeEkleClick(exam) },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Takvime Ekle")
                }

                // Silme butonu - sadece Admin
                if (userRole.equals("Admin", ignoreCase = true)) {
                    IconButton(
                        onClick = { onDeleteClick(exam.SinavID!!) },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GozetmenAtamaEkrani(
    viewModel: SinavViewModel,
    onGeriClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val secilenSinav = remember { mutableStateOf<SinavDetay?>(null) }
    val sinavDropdownAcik = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.sinavlariGetir()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gözetmen Atama") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sınav Seçimi Dropdown
            ExposedDropdownMenuBox(
                expanded = sinavDropdownAcik.value,
                onExpandedChange = { sinavDropdownAcik.value = !sinavDropdownAcik.value }
            ) {
                OutlinedTextField(
                    value = secilenSinav.value?.let {
                        "${it.DersKodu} - ${it.DersAdi} (${it.Tarih}) - Salon: ${it.SalonAdi ?: "Atanmadı"}"
                    } ?: "Sınav Seç",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sınav") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = sinavDropdownAcik.value
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = sinavDropdownAcik.value,
                    onDismissRequest = { sinavDropdownAcik.value = false }
                ) {
                    viewModel.sinavlarListesi.value.forEach { sinav ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${sinav.DersKodu} - ${sinav.DersAdi} (${sinav.Tarih}) - Salon: ${sinav.SalonAdi ?: "Atanmadı"}" +
                                            if (sinav.gozetmenAtandi) " — Atandı" else "",
                                    color = if (sinav.gozetmenAtandi)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                if (!sinav.gozetmenAtandi) {
                                    secilenSinav.value = sinav
                                    sinavDropdownAcik.value = false
                                    sinav.SinavID?.let { viewModel.gozetmenleriGetir(it) }
                                }
                            },
                            enabled = !sinav.gozetmenAtandi
                        )
                    }
                }
            }

            // Gözetmen Ata Butonu
            Button(
                onClick = {
                    secilenSinav.value?.SinavID?.let { viewModel.gozetmenAta(it) }
                },
                enabled = secilenSinav.value != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gözetmen Ata")
            }

            // Sonuç mesajı
            viewModel.gozetmenAtamaMesaji.value?.let { mesaj ->
                Text(
                    text = mesaj,
                    color = if (mesaj.contains("başarıyla"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            // Atanan gözetmenler listesi
            if (viewModel.gozetmenListesi.value.isNotEmpty()) {
                Text(
                    text = "Atanan Gözetmenler:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyColumn {
                    items(viewModel.gozetmenListesi.value) { personel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${personel.Unvan} ${personel.Ad} ${personel.Soyad}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}