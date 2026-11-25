package com.mindforge.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val Black = Color(0xFF090A0D)
private val Card = Color(0xFF17191F)
private val Crimson = Color(0xFFB51225)
private val Grey = Color(0xFF9A9DA6)
private val Ivory = Color(0xFFF2F2F3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ForgeApp(this) }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "forge_daily"
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channel, "MIND FORGE", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = androidx.core.app.NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("MIND FORGE")
            .setContentText("Время для практики / Zeit für deine Praxis")
            .setAutoCancel(true).build()
        manager.notify(71, notification)
    }
}

@Composable
private fun ForgeApp(context: Context) {
    var screen by remember { mutableStateOf("home") }
    var activeModule by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(context.getSharedPreferences("forge", 0).getString("notes", "") ?: "") }
    var hour by remember { mutableIntStateOf(19) }
    var minute by remember { mutableIntStateOf(0) }
    var enabled by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val modules = listOf(
        listOf("Knowledge Base", "База знаний / Wissensdatenbank", "Философия • психология • наука"),
        listOf("Memory Engine", "Тренировка памяти / Gedächtnistraining", "Карточки • интервальное повторение"),
        listOf("Language Core", "Немецкий язык / Sprachentwicklung", "Deutsch A2–B2 • Pflege • Fachbegriffe"),
        listOf("Medical Mind", "Медицинские знания / Medizinisches Wissen", "Анатомия • физиология • патология"),
        listOf("Stoic Mind", "Стоическая практика / Stoische Praxis", "Рефлексия • решения • дневник"),
        listOf("Tactical Mind", "Мышление / Kognitives Training", "Логика • внимание • анализ"),
        listOf("Progress", "Прогресс / Persönlicher Fortschritt", "Статистика • освоенные темы")
    )
    MaterialTheme(colorScheme = darkColorScheme(primary = Crimson, background = Black, surface = Card, onSurface = Ivory)) {
        Column(Modifier.fillMaxSize().background(Black).padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MIND FORGE", color = Ivory, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("КУЙ СВОЙ РАЗУМ  /  FORGE YOUR MIND", color = Crimson, fontSize = 10.sp, letterSpacing = 1.2.sp)
                }
                IconButton(onClick = { screen = "reminders" }) { Icon(Icons.Default.Notifications, null, tint = Crimson) }
            }
            when (screen) {
                "home" -> {
                    Text("ТВОЯ СИСТЕМА РАЗВИТИЯ / DEIN SYSTEM", color = Grey, fontSize = 11.sp, letterSpacing = 1.sp)
                    Spacer(Modifier.height(12.dp))
                    Column(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(18.dp)).padding(18.dp)) {
                        Text("DISCIPLINA • SCIENTIA • MENS", color = Crimson, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.3.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Куй знания. Тренируй память. Развивай мышление.", color = Ivory, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text("Wissen schmieden. Gedächtnis trainieren. Denken entwickeln.", color = Grey, fontSize = 13.sp)
                        Spacer(Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, null, tint = Crimson)
                            Text("  Ежедневная практика / Tägliche Praxis", color = Ivory, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("МОДУЛИ / MODULE", color = Ivory, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(Modifier.height(9.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 18.dp)) {
                        items(modules) { item ->
                            Row(Modifier.fillMaxWidth().background(Card, RoundedCornerShape(14.dp)).clickable { activeModule = item[0]; screen = "module" }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.width(4.dp).height(42.dp).background(Crimson, RoundedCornerShape(3.dp)))
                                Spacer(Modifier.width(13.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(item[0], color = Ivory, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(item[1], color = Grey, fontSize = 12.sp)
                                    Text(item[2], color = Color(0xFFD0D0D3), fontSize = 11.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Crimson)
                            }
                        }
                    }
                }
                "module" -> {
                    Text(activeModule, color = Ivory, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    Text("ЛИЧНОЕ ПРОСТРАНСТВО / DEIN BEREICH", color = Crimson, fontSize = 11.sp)
                    Spacer(Modifier.height(18.dp))
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth().height(150.dp), label = { Text("Новая запись / Neue Notiz") })
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        notes = (notes + "\n\n[" + activeModule + "] " + note).trim()
                        context.getSharedPreferences("forge", 0).edit().putString("notes", notes).apply()
                        note = ""
                    }, colors = ButtonDefaults.buttonColors(containerColor = Crimson)) { Text("СОХРАНИТЬ / SPEICHERN") }
                    Spacer(Modifier.height(16.dp))
                    Text(notes.ifBlank { "Твои записи появятся здесь / Deine Notizen erscheinen hier" }, color = Grey, fontSize = 13.sp)
                }
                "reminders" -> {
                    Text("НАПОМИНАНИЯ / ERINNERUNGEN", color = Ivory, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("Ежедневная практика / Tägliche Praxis", color = Grey)
                    Spacer(Modifier.height(20.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Включить / Aktivieren", color = Ivory, modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it }, colors = SwitchDefaults.colors(checkedTrackColor = Crimson))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { hour = (hour + 1) % 24 }) { Text("ЧАС / STD  %02d".format(hour)) }
                        OutlinedButton(onClick = { minute = (minute + 15) % 60 }) { Text("МИН / MIN  %02d".format(minute)) }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= 33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val pi = PendingIntent.getBroadcast(context, 71, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                        if (enabled) {
                            val c = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1) }
                            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.timeInMillis, AlarmManager.INTERVAL_DAY, pi)
                        } else alarm.cancel(pi)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Crimson)) { Text("СОХРАНИТЬ / SPEICHERN") }
                    Spacer(Modifier.height(10.dp))
                    Text("Повтор каждый день / Tägliche Wiederholung", color = Grey, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { screen = "home" }) { Icon(Icons.Default.Home, null, tint = Crimson); Text(" Главная / Start", color = Ivory) }
                TextButton(onClick = { screen = "reminders" }) { Icon(Icons.Default.Alarm, null, tint = Crimson); Text(" Напоминания / Reminder", color = Ivory) }
            }
        }
    }
}
