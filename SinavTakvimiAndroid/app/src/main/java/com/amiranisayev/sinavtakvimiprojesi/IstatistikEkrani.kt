package com.amiranisayev.sinavtakvimiprojesi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstatistikEkrani(
    viewModel: SinavViewModel,
    onMenuClick: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.salonDolulugununGetir()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İstatistikler") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.istatistikYukleniyor.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Özet kartlar
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OzetKartlar(viewModel.salonDolulukListesi.value)
                }

                // Bölüm başlığı
                item {
                    Text(
                        text = "Salon Doluluk Oranları",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Her salon için kart
                items(viewModel.salonDolulukListesi.value.sortedByDescending { it.DolulukYuzdesi }) { salon ->
                    SalonDolulukKarti(salon)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun OzetKartlar(salonlar: List<SalonDoluluk>) {
    val toplamSalon = salonlar.size
    val doluSalon = salonlar.count { it.DolulukDurumu == "Dolu" }
    val ortaSalon = salonlar.count { it.DolulukDurumu == "Orta" }
    val bosSalon = salonlar.count { it.DolulukDurumu == "Boş" }
    val ortalamaDoluluk = if (salonlar.isNotEmpty())
        salonlar.map { it.DolulukYuzdesi }.average() else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Genel Özet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OzetKart(
                baslik = "Toplam Salon",
                deger = "$toplamSalon",
                renk = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f)
            )
            OzetKart(
                baslik = "Ort. Doluluk",
                deger = "%${"%.1f".format(ortalamaDoluluk)}",
                renk = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OzetKart(
                baslik = "Dolu",
                deger = "$doluSalon salon",
                renk = Color(0xFFFFCDD2),
                modifier = Modifier.weight(1f)
            )
            OzetKart(
                baslik = "Orta",
                deger = "$ortaSalon salon",
                renk = Color(0xFFFFF9C4),
                modifier = Modifier.weight(1f)
            )
            OzetKart(
                baslik = "Boş",
                deger = "$bosSalon salon",
                renk = Color(0xFFC8E6C9),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun OzetKart(
    baslik: String,
    deger: String,
    renk: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = renk),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = deger,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = baslik,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SalonDolulukKarti(salon: SalonDoluluk) {
    val renkBilgisi = when (salon.DolulukDurumu) {
        "Dolu" -> Triple(Color(0xFFFFCDD2), Color(0xFFD32F2F), "🔴")
        "Orta" -> Triple(Color(0xFFFFF9C4), Color(0xFFF57F17), "🟡")
        else   -> Triple(Color(0xFFC8E6C9), Color(0xFF2E7D32), "🟢")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = renkBilgisi.first),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${renkBilgisi.third} ${salon.SalonAdi}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = salon.DerslikTuru,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "%${"%.1f".format(salon.DolulukYuzdesi)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = renkBilgisi.second
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Doluluk progress bar
            LinearProgressIndicator(
                progress = { (salon.DolulukYuzdesi / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = renkBilgisi.second,
                trackColor = renkBilgisi.second.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Öğrenci: ${salon.ToplamOgrenci}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Kapasite: ${salon.Kapasite}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = salon.DolulukDurumu,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = renkBilgisi.second
                )
            }
        }
    }
}