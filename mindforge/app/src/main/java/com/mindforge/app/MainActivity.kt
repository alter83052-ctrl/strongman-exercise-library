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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

private val Black = Color(0xFF090A0D)
private val Panel = Color(0xFF17191F)
private val Red = Color(0xFFB51225)
private val Muted = Color(0xFF9A9DA6)
private val White = Color(0xFFF2F2F3)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ForgeApp(this) }
    }
}
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "mind_forge_practice"
        if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(NotificationChannel(channel, "MIND FORGE", NotificationManager.IMPORTANCE_DEFAULT))
        val n = androidx.core.app.NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("MIND FORGE")
            .setContentText("Время для практики / Zeit für deine Praxis").setAutoCancel(true).build()
        nm.notify(intent.getIntExtra("notification_id", 71), n)
    }
}
private data class Note(val title: String, val body: String, val module: String)
private data class CardData(val front: String, val back: String, val due: Long = 0L, val interval: Int = 0, val reps: Int = 0)
private fun readNotes(c: Context): List<Note> {
    val a = JSONArray(c.getSharedPreferences("mf", 0).getString("notes", "[]"))
    return List(a.length()) { i -> a.getJSONObject(i).let { Note(it.optString("t"), it.optString("b"), it.optString("m")) } }
}
private fun writeNotes(c: Context, v: List<Note>) {
    val a = JSONArray(); v.forEach { a.put(JSONObject().put("t", it.title).put("b", it.body).put("m", it.module)) }
    c.getSharedPreferences("mf", 0).edit().putString("notes", a.toString()).apply()
}
private fun readCards(c: Context): List<CardData> {
    val a = JSONArray(c.getSharedPreferences("mf", 0).getString("cards", "[]"))
    return List(a.length()) { i -> a.getJSONObject(i).let { CardData(it.optString("f"), it.optString("b"), it.optLong("d"), it.optInt("i"), it.optInt("r")) } }
}
private fun writeCards(c: Context, v: List<CardData>) {
    val a = JSONArray(); v.forEach { a.put(JSONObject().put("f", it.front).put("b", it.back).put("d", it.due).put("i", it.interval).put("r", it.reps)) }
    c.getSharedPreferences("mf", 0).edit().putString("cards", a.toString()).apply()
}

@Composable private fun ForgeApp(context: Context) {
    val prefs = remember { context.getSharedPreferences("mf", 0) }
    var page by remember { mutableStateOf("home") }
    var module by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(readNotes(context)) }
    var cards by remember { mutableStateOf(readCards(context)) }
    var title by remember { mutableStateOf("") }; var body by remember { mutableStateOf("") }
    var front by remember { mutableStateOf("") }; var back by remember { mutableStateOf("") }
    var reveal by remember { mutableStateOf(false) }; var search by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 19)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 0)) }
    var enabled by remember { mutableStateOf(prefs.getBoolean("enabled", false)) }
    var days by remember { mutableStateOf((1..7).map { prefs.getBoolean("day$it", it <= 5) }) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val modules = listOf(
        Triple("Knowledge Base", "База знаний / Wissensdatenbank", "Философия • психология • наука"),
        Triple("Memory Engine", "Тренировка памяти / Gedächtnistraining", "Карточки • интервальное повторение"),
        Triple("Language Core", "Немецкий язык / Sprachentwicklung", "Deutsch A2–B2 • Pflege • Fachbegriffe"),
        Triple("Medical Mind", "Медицинские знания / Medizinisches Wissen", "Анатомия • физиология • патология"),
        Triple("Stoic Mind", "Стоическая практика / Stoische Praxis", "Рефлексия • решения • дневник"),
        Triple("Tactical Mind", "Мышление / Kognitives Training", "Логика • внимание • анализ"),
        Triple("Progress", "Прогресс / Persönlicher Fortschritt", "Статистика • освоенные темы")
    )
    val due = cards.filter { it.due <= System.currentTimeMillis() }
    MaterialTheme(colorScheme = darkColorScheme(primary = Red, background = Black, surface = Panel, onSurface = White)) {
        Column(Modifier.fillMaxSize().background(Black).padding(horizontal = 17.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MIND FORGE", color = White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("КУЙ СВОЙ РАЗУМ  /  FORGE YOUR MIND", color = Red, fontSize = 10.sp, letterSpacing = 1.sp)
                }
                IconButton(onClick = { page = "reminders" }) { Icon(Icons.Default.Notifications, null, tint = Red) }
            }
            when (page) {
                "home" -> LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                    item {
                        Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).padding(18.dp)) {
                            Text("DISCIPLINA • SCIENTIA • MENS", color = Red, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Куй знания. Тренируй память. Развивай мышление.", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Wissen schmieden. Gedächtnis trainieren. Denken entwickeln.", color = Muted, fontSize = 13.sp)
                            Spacer(Modifier.height(10.dp))
                            Text("Карточки к повторению: ${due.size}  •  Записи: ${notes.size}", color = White, fontSize = 12.sp)
                        }
                        Text("МОДУЛИ / MODULE", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 13.dp))
                    }
                    items(modules) { item ->
                        Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).clickable { module = item.first; page = "module" }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.width(4.dp).height(43.dp).background(Red, RoundedCornerShape(3.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(item.first, color = White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(item.second, color = Muted, fontSize = 12.sp); Text(item.third, color = Color.LightGray, fontSize = 11.sp)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Red)
                        }
                    }
                }
                "module" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { page = "home" }) { Icon(Icons.Default.ArrowBack, null, tint = Red) }
                        Text(module, color = White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    }
                    when (module) {
                        "Memory Engine", "Language Core" -> {
                            Text(if (module == "Memory Engine") "КАРТОЧКИ / LERNKARTEN • ${due.size} К ПОВТОРЕНИЮ" else "DEUTSCH A2–B2 • PFLEGE & FACHBEGRIFFE", color = Red, fontSize = 11.sp)
                            OutlinedTextField(front, { front = it }, Modifier.fillMaxWidth(), label = { Text(if (module == "Language Core") "Слово с артиклем / Wort mit Artikel" else "Вопрос / Vorderseite") })
                            OutlinedTextField(back, { back = it }, Modifier.fillMaxWidth(), label = { Text("Перевод и пример / Antwort & Beispiel") })
                            Button(onClick = { if (front.isNotBlank() && back.isNotBlank()) { cards = cards + CardData(front.trim(), back.trim()); writeCards(context, cards); front = ""; back = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("ДОБАВИТЬ / HINZUFÜGEN") }
                            if (module == "Memory Engine") {
                                val c = due.firstOrNull()
                                if (c != null) {
                                    Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(15.dp)).padding(16.dp)) {
                                        Text(if (reveal) c.back else c.front, color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        OutlinedButton(onClick = { reveal = !reveal }) { Text(if (reveal) "СКРЫТЬ / VERBERGEN" else "ОТВЕТ / ANTWORT") }
                                        if (reveal) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = { cards = cards.map { if (it == c) it.copy(due = System.currentTimeMillis() + 600000L, reps = it.reps + 1) else it }; writeCards(context, cards); reveal = false }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("ЕЩЁ РАЗ") }
                                            Button(onClick = { val n = if (c.interval == 0) 1 else c.interval * 2; cards = cards.map { if (it == c) it.copy(interval = n, due = System.currentTimeMillis() + n * 86400000L, reps = it.reps + 1) else it }; writeCards(context, cards); reveal = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF315B46))) { Text("ЗНАЮ / GEWUSST") }
                                        }
                                    }
                                } else Text("На сегодня всё! / Für heute alles erledigt.", color = Muted)
                            }
                            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Поиск / Suche") })
                            LazyColumn { items(cards.filter { (it.front + it.back).contains(search, true) }) { c -> Column(Modifier.padding(vertical = 6.dp)) { Text(c.front, color = White, fontWeight = FontWeight.Bold); Text(c.back, color = Muted, fontSize = 13.sp); Divider(color = Panel) } } }
                        }
                        "Tactical Mind" -> {
                            var index by remember { mutableIntStateOf(0) }; var answer by remember { mutableStateOf("") }; var result by remember { mutableStateOf("") }
                            val qs = listOf("Продолжи ряд: 2, 4, 8, 16, …", "У отца 4 сына. У каждого сына одна сестра. Сколько детей?")
                            val solutions = listOf("32", "5")
                            Text("МИКРОТРЕНИРОВКА / DENKTRAINING", color = Red)
                            Text(qs[index], color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            OutlinedTextField(answer, { answer = it }, Modifier.fillMaxWidth(), label = { Text("Ответ / Antwort") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                            Button(onClick = { result = if (answer.trim() == solutions[index]) "Верно! / Richtig!" else "Попробуй ещё раз / Versuch es erneut" }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("ПРОВЕРИТЬ / PRÜFEN") }
                            Text(result, color = Red)
                            OutlinedButton(onClick = { index = (index + 1) % qs.size; answer = ""; result = "" }) { Text("СЛЕДУЮЩЕЕ / WEITER") }
                        }
                        "Progress" -> {
                            Text("ТВОЯ СТАТИСТИКА / DEINE STATISTIK", color = Red)
                            Stat("Записи / Notizen", notes.size.toString()); Stat("Карточки / Lernkarten", cards.size.toString())
                            Stat("Повторения / Wiederholungen", cards.sumOf { it.reps }.toString())
                            Stat("Карточки с интервалом / Gelernt", cards.count { it.interval > 0 }.toString())
                            Stat("Активные модули / Aktive Module", notes.map { it.module }.distinct().size.toString())
                        }
                        else -> {
                            Text(if (module == "Medical Mind") "АНАТОМИЯ • ФИЗИОЛОГИЯ • ПАТОЛОГИЯ • PFLEGE" else "ЛИЧНАЯ БАЗА ЗНАНИЙ / DEINE WISSENSDATENBANK", color = Red, fontSize = 11.sp)
                            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Заголовок / Titel") })
                            OutlinedTextField(body, { body = it }, Modifier.fillMaxWidth().height(140.dp), label = { Text("Запись / Notiz") })
                            Button(onClick = { if (body.isNotBlank()) { notes = notes + Note(title.ifBlank { module }, body, module); writeNotes(context, notes); title = ""; body = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("СОХРАНИТЬ / SPEICHERN") }
                            OutlinedTextField(search, { search = it }, Modifier.fillMaxWidth(), label = { Text("Поиск / Suche") })
                            LazyColumn { items(notes.filter { it.module == module && (it.title + it.body).contains(search, true) }.reversed()) { n ->
                                Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(12.dp)).padding(12.dp)) {
                                    Text(n.title, color = White, fontWeight = FontWeight.Bold); Text(n.body, color = Muted, fontSize = 13.sp)
                                    TextButton(onClick = { notes = notes.filterNot { it == n }; writeNotes(context, notes) }) { Text("УДАЛИТЬ / LÖSCHEN", color = Red) }
                                }; Spacer(Modifier.height(7.dp))
                            } }
                        }
                    }
                }
                "reminders" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { page = "home" }) { Icon(Icons.Default.ArrowBack, null, tint = Red) }
                        Text("НАПОМИНАНИЯ / ERINNERUNGEN", color = White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Включить / Aktivieren", color = White, modifier = Modifier.weight(1f))
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { hour = (hour + 1) % 24 }) { Text("ЧАС %02d".format(hour)) }
                        OutlinedButton(onClick = { minute = (minute + 5) % 60 }) { Text("МИН %02d".format(minute)) }
                    }
                    Text("Дни недели / Wochentage", color = White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                    listOf("Пн / Mo", "Вт / Di", "Ср / Mi", "Чт / Do", "Пт / Fr", "Сб / Sa", "Вс / So").forEachIndexed { i, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, color = White, modifier = Modifier.weight(1f))
                            Checkbox(checked = days[i], onCheckedChange = { v -> days = days.toMutableList().also { it[i] = v } }, colors = CheckboxDefaults.colors(checkedColor = Red))
                        }
                    }
                    Button(onClick = {
                        prefs.edit().putInt("hour", hour).putInt("minute", minute).putBoolean("enabled", enabled).apply()
                        days.forEachIndexed { i, v -> prefs.edit().putBoolean("day${i + 1}", v).apply() }
                        if (Build.VERSION.SDK_INT >= 33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        for (d in 1..7) {
                            val pi = PendingIntent.getBroadcast(context, 710 + d, Intent(context, ReminderReceiver::class.java).putExtra("notification_id", 710 + d), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            if (enabled && days[d - 1]) {
                                val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, d); set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1) }
                                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pi)
                            } else am.cancel(pi)
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("СОХРАНИТЬ / SPEICHERN") }
                    Text("Повтор по выбранным дням / Wiederholung an ausgewählten Tagen", color = Muted, fontSize = 12.sp)
                }
            }
            if (page == "home") Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { page = "home" }) { Icon(Icons.Default.Home, null, tint = Red); Text(" Главная / Start", color = White) }
                TextButton(onClick = { page = "reminders" }) { Icon(Icons.Default.Alarm, null, tint = Red); Text(" Напоминания / Reminder", color = White) }
            }
        }
    }
}
@Composable private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Muted, modifier = Modifier.weight(1f)); Text(value, color = White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }; Divider(color = Panel)
}
