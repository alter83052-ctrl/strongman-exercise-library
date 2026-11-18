package com.strongman.library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val bg = Color(0xFF101310)
private val panel = Color(0xFF1B211B)
private val lime = Color(0xFFB7F34A)
private val white = Color(0xFFF2F5EF)
private val muted = Color(0xFFABB5A7)

data class Exercise(val name: String, val category: String, val equipment: String, val muscles: String, val technique: String)
data class PlanExercise(val name: String, val sets: String = "3", val reps: String = "5", val weight: String = "0", val rest: String = "120")
data class TrainingPlan(val id: Long, val name: String, val exercises: List<PlanExercise>)
data class Session(val date: String, val plan: String, val summary: String, val volume: Double)

private val library = listOf(
    Exercise("Присед со штангой","Сила","Штанга, стойка","Квадрицепсы, ягодицы","Сохраняй корпус напряжённым, опускайся подконтрольно и вставай через всю стопу."),
    Exercise("Становая тяга","Сила","Штанга","Ягодицы, задняя поверхность бедра, спина","Напряги корпус, удерживай гриф близко к ногам и разогни таз."),
    Exercise("Жим стоя","Сила","Штанга","Плечи, трицепс","Напряги ягодицы и пресс, выжми гриф вертикально над головой."),
    Exercise("Жим лёжа","Сила","Скамья, штанга","Грудь, трицепс","Сведи лопатки, опусти гриф к нижней части груди и выжми вверх."),
    Exercise("Подтягивания","Сила","Турник","Широчайшие, бицепс","Подтянись без раскачки и опустись под контролем."),
    Exercise("Тяга штанги в наклоне","Сила","Штанга","Спина, бицепс","Наклони корпус, удерживай спину ровной и тяни гриф к поясу."),
    Exercise("Отжимания на брусьях","Сила","Брусья","Грудь, трицепс","Опускайся контролируемо и выжимай себя вверх."),
    Exercise("Фермерская прогулка","Strongman","Гантели, фермерские рукояти","Хват, трапеции, корпус","Подними снаряды, выпрямись и пройди заданную дистанцию."),
    Exercise("Прогулка с ярмом","Strongman","Yoke","Трапеции, корпус, ноги","Подсядь под ярмо, зафиксируй корпус и пройди дистанцию."),
    Exercise("Толчок бревна","Strongman","Log","Плечи, трицепс, ноги","Подними бревно на грудь, выполни подсед и зафиксируй над головой."),
    Exercise("Подъём атлас-камня","Strongman","Атлас-камень","Спина, бёдра, хват","Обхвати камень, подними на колени, перехвати и уложи на платформу."),
    Exercise("Перенос сэндбэга","Strongman","Мешок","Корпус, спина, ноги","Подними мешок к груди, плотно удерживай и пройди дистанцию."),
    Exercise("Взятие на грудь","Взрывная сила","Штанга","Ноги, спина, трапеции","Разгони гриф ногами, разогни таз и быстро подсядь под него."),
    Exercise("Рывок","Взрывная сила","Штанга","Всё тело","Разгони гриф и быстро уйди под него, зафиксировав над головой."),
    Exercise("Толчок штанги","Взрывная сила","Штанга","Ноги, плечи, трицепс","Выполни короткий подсед, мощный толчок и фиксацию."),
    Exercise("Махи гирей","Взрывная сила","Гиря","Ягодицы, задняя поверхность бедра","Отведи гирю назад и резко разогни таз."),
    Exercise("Толкание саней","Strongman","Сани","Ноги, корпус","Возьмись за рукояти, зафиксируй корпус и толкай сани."),
    Exercise("Румынская тяга","Сила","Штанга, гантели","Ягодицы, задняя поверхность бедра","Отведи таз назад, сохраняя мягкие колени и ровную спину.")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("strongman_grif", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = lime, background = bg, surface = panel, onSurface = white, onPrimary = bg)) {
                StrongmanApp(prefs)
            }
        }
    }
}

@Composable
private fun StrongmanApp(prefs: android.content.SharedPreferences) {
    var tab by remember { mutableStateOf("Сегодня") }
    var plans by remember { mutableStateOf(loadPlans(prefs)) }
    var sessions by remember { mutableStateOf(loadSessions(prefs)) }
    var editing by remember { mutableStateOf<TrainingPlan?>(null) }
    var selectedExercise by remember { mutableStateOf<Exercise?>(null) }
    var activePlan by remember { mutableStateOf<TrainingPlan?>(null) }
    var completed by remember { mutableStateOf(setOf<String>()) }
    var search by remember { mutableStateOf("") }

    fun savePlans(value: List<TrainingPlan>) {
        plans = value
        prefs.edit().putString("plans", plansToJson(value)).apply()
    }
    fun saveSessions(value: List<Session>) {
        sessions = value
        prefs.edit().putString("sessions", sessionsToJson(value)).apply()
    }

    if (selectedExercise != null) {
        Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp)) {
            TextButton(onClick = { selectedExercise = null }) { Text("← Назад") }
            Text(selectedExercise!!.category.uppercase(), color = lime, fontWeight = FontWeight.Bold)
            Text(selectedExercise!!.name, color = white, fontSize = 28.sp, fontWeight = FontWeight.Black)
            InfoBlock("ОБОРУДОВАНИЕ", selectedExercise!!.equipment)
            InfoBlock("РАБОТАЮЩИЕ МЫШЦЫ", selectedExercise!!.muscles)
            InfoBlock("ТЕХНИКА ВЫПОЛНЕНИЯ", selectedExercise!!.technique)
        }
        return
    }

    if (editing != null) {
        PlanEditor(
            initial = editing!!,
            onCancel = { editing = null },
            onSave = {
                val updated = plans.filterNot { p -> p.id == it.id } + it
                savePlans(updated)
                editing = null
                tab = "План"
            },
            onDelete = { id ->
                savePlans(plans.filterNot { it.id == id })
                editing = null
            }
        )
        return
    }

    if (activePlan != null) {
        WorkoutScreen(
            plan = activePlan!!,
            completed = completed,
            onToggle = { name -> completed = if (name in completed) completed - name else completed + name },
            onFinish = {
                val summary = activePlan!!.exercises.joinToString(" • ") { it.name + " " + it.sets + "×" + it.reps + " @ " + it.weight + " кг" }
                val volume = activePlan!!.exercises.sumOf { (it.sets.toDoubleOrNull() ?: 0.0) * (it.reps.toDoubleOrNull() ?: 0.0) * (it.weight.toDoubleOrNull() ?: 0.0) }
                saveSessions(sessions + Session(java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()), activePlan!!.name, summary, volume))
                activePlan = null
                completed = emptySet()
                tab = "Журнал"
            },
            onExit = { activePlan = null }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(bg)) {
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                "Сегодня" -> TodayScreen(plans, onCreate = { editing = TrainingPlan(System.currentTimeMillis(), "", emptyList()) }, onStart = { activePlan = it; completed = emptySet() }, onPlans = { tab = "План" })
                "План" -> PlansScreen(plans, onCreate = { editing = TrainingPlan(System.currentTimeMillis(), "", emptyList()) }, onEdit = { editing = it }, onStart = { activePlan = it; completed = emptySet() })
                "Журнал" -> JournalScreen(sessions)
                "Цифры" -> StatsScreen(sessions)
                "Библиотека" -> {
                    Column(Modifier.padding(18.dp)) {
                        Text("Библиотека упражнений", color = white, fontSize = 25.sp, fontWeight = FontWeight.Black)
                        OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Поиск упражнения") }, singleLine = true)
                        LazyColumn {
                            items(library.filter { it.name.contains(search, true) || it.category.contains(search, true) }) { exercise ->
                                Surface(color = panel, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { selectedExercise = exercise }, shape = MaterialTheme.shapes.medium) {
                                    Column(Modifier.padding(15.dp)) {
                                        Text(exercise.name, color = white, fontWeight = FontWeight.Bold)
                                        Text(exercise.category + " · " + exercise.equipment, color = lime, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        NavigationBar(containerColor = panel, contentColor = white) {
            listOf("Сегодня", "Журнал", "Цифры", "План", "Библиотека").forEach { item ->
                NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Text(when (item) { "Сегодня" -> "●"; "Журнал" -> "▤"; "Цифры" -> "↗"; "План" -> "☷"; else -> "＋" }, color = if (tab == item) lime else muted) }, label = { Text(item, fontSize = 10.sp) })
            }
        }
    }
}

@Composable
private fun TodayScreen(plans: List<TrainingPlan>, onCreate: () -> Unit, onStart: (TrainingPlan) -> Unit, onPlans: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("STRONGMAN × ГРИФ", color = lime, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Text("Сегодня", color = white, fontSize = 34.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        if (plans.isEmpty()) {
            Text("Пока нет тренировочных планов", color = white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Создай свою программу. Здесь не будет заранее заполненных тренировок.", color = muted)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onCreate) { Text("＋ Создать первый план") }
        } else {
            Text("ТВОИ ПЛАНЫ", color = lime, fontWeight = FontWeight.Bold)
            plans.forEach { plan ->
                Surface(color = panel, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(plan.name, color = white, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        Text(plan.exercises.size.toString() + " упражнений", color = muted)
                        Button(onClick = { onStart(plan) }, modifier = Modifier.padding(top = 8.dp)) { Text("Начать тренировку") }
                    }
                }
            }
            OutlinedButton(onClick = onPlans) { Text("Управление планами") }
        }
    }
}

@Composable
private fun PlansScreen(plans: List<TrainingPlan>, onCreate: () -> Unit, onEdit: (TrainingPlan) -> Unit, onStart: (TrainingPlan) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Мои планы", color = white, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Button(onClick = onCreate, modifier = Modifier.padding(vertical = 12.dp)) { Text("＋ Новый план") }
        if (plans.isEmpty()) Text("Здесь появятся программы, которые ты создашь.", color = muted)
        LazyColumn {
            items(plans) { plan ->
                Surface(color = panel, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(plan.name, color = white, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(plan.exercises.size.toString() + " упражнений", color = muted)
                        Row {
                            TextButton(onClick = { onEdit(plan) }) { Text("Изменить") }
                            TextButton(onClick = { onStart(plan) }) { Text("Начать") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanEditor(initial: TrainingPlan, onCancel: () -> Unit, onSave: (TrainingPlan) -> Unit, onDelete: (Long) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var entries by remember { mutableStateOf(initial.exercises) }
    var choosing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(bg).padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCancel) { Text("Отмена") }
            Text("Редактор плана", color = white, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onSave(initial.copy(name = name.trim(), exercises = entries)) }, enabled = name.isNotBlank() && entries.isNotEmpty()) { Text("Сохранить") }
        }
        OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Название плана") }, singleLine = true)
        Spacer(Modifier.height(12.dp))
        Text("УПРАЖНЕНИЯ", color = lime, fontWeight = FontWeight.Bold)
        Button(onClick = { choosing = true }, modifier = Modifier.padding(vertical = 8.dp)) { Text("＋ Добавить упражнение") }
        LazyColumn {
            items(entries) { entry ->
                Surface(color = panel, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.name, color = white, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            TextButton(onClick = { entries = entries - entry }) { Text("Убрать") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallField("Подходы", entry.sets, { v -> entries = entries.map { if (it == entry) it.copy(sets = v) else it } }, Modifier.weight(1f))
                            SmallField("Повторы", entry.reps, { v -> entries = entries.map { if (it == entry) it.copy(reps = v) else it } }, Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallField("Вес, кг", entry.weight, { v -> entries = entries.map { if (it == entry) it.copy(weight = v) else it } }, Modifier.weight(1f))
                            SmallField("Отдых, сек", entry.rest, { v -> entries = entries.map { if (it == entry) it.copy(rest = v) else it } }, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
    if (choosing) AlertDialog(
        onDismissRequest = { choosing = false },
        title = { Text("Выбери упражнение") },
        text = { LazyColumn { items(library) { ex -> Text(ex.name + " · " + ex.category, modifier = Modifier.fillMaxWidth().clickable { if (entries.none { it.name == ex.name }) entries = entries + PlanExercise(ex.name); choosing = false }.padding(10.dp)) } } },
        confirmButton = { TextButton(onClick = { choosing = false }) { Text("Закрыть") } }
    )
}

@Composable
private fun SmallField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(value = value, onValueChange = onChange, modifier = modifier, label = { Text(label, fontSize = 10.sp) }, singleLine = true)
}

@Composable
private fun WorkoutScreen(plan: TrainingPlan, completed: Set<String>, onToggle: (String) -> Unit, onFinish: () -> Unit, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize().background(bg).padding(18.dp)) {
        TextButton(onClick = onExit) { Text("← Завершить позже") }
        Text("ТРЕНИРОВКА", color = lime, fontWeight = FontWeight.Bold)
        Text(plan.name, color = white, fontSize = 27.sp, fontWeight = FontWeight.Black)
        LazyColumn(Modifier.weight(1f)) {
            items(plan.exercises) { ex ->
                Surface(color = panel, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(checked = ex.name in completed, onCheckedChange = { onToggle(ex.name) })
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, color = white, fontWeight = FontWeight.Bold)
                            Text(ex.sets + " × " + ex.reps + " · " + ex.weight + " кг · отдых " + ex.rest + " сек", color = muted)
                        }
                    }
                }
            }
        }
        Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text("Завершить и сохранить") }
    }
}

@Composable
private fun JournalScreen(sessions: List<Session>) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Журнал", color = white, fontSize = 28.sp, fontWeight = FontWeight.Black)
        if (sessions.isEmpty()) Text("Здесь будут сохранённые тренировки.", color = muted, modifier = Modifier.padding(top = 12.dp))
        LazyColumn {
            items(sessions.reversed()) { session ->
                Surface(color = panel, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(session.plan, color = white, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(session.date, color = lime, fontSize = 12.sp)
                        Text(session.summary, color = muted)
                        Text("Объём: " + session.volume.toLong() + " кг", color = white, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsScreen(sessions: List<Session>) {
    val volume = sessions.sumOf { it.volume }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("Цифры", color = white, fontSize = 30.sp, fontWeight = FontWeight.Black)
        InfoBlock("ТРЕНИРОВОК", sessions.size.toString())
        InfoBlock("ОБЩИЙ ТОННАЖ", volume.toLong().toString() + " кг")
        InfoBlock("ПОСЛЕДНЯЯ ТРЕНИРОВКА", sessions.lastOrNull()?.date ?: "Пока нет данных")
    }
}

@Composable
private fun InfoBlock(title: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, color = lime, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = white, fontSize = 17.sp)
        HorizontalDivider(color = panel, modifier = Modifier.padding(top = 10.dp))
    }
}

private fun loadPlans(prefs: android.content.SharedPreferences): List<TrainingPlan> = try {
    val array = JSONArray(prefs.getString("plans", "[]"))
    (0 until array.length()).map { i ->
        val obj = array.getJSONObject(i)
        val exArray = obj.getJSONArray("exercises")
        val ex = (0 until exArray.length()).map { j ->
            val e = exArray.getJSONObject(j)
            PlanExercise(e.getString("name"), e.getString("sets"), e.getString("reps"), e.getString("weight"), e.getString("rest"))
        }
        TrainingPlan(obj.getLong("id"), obj.getString("name"), ex)
    }
} catch (_: Exception) { emptyList() }

private fun plansToJson(plans: List<TrainingPlan>): String {
    val array = JSONArray()
    plans.forEach { plan ->
        val obj = JSONObject().put("id", plan.id).put("name", plan.name)
        val exArray = JSONArray()
        plan.exercises.forEach { ex -> exArray.put(JSONObject().put("name", ex.name).put("sets", ex.sets).put("reps", ex.reps).put("weight", ex.weight).put("rest", ex.rest)) }
        obj.put("exercises", exArray)
        array.put(obj)
    }
    return array.toString()
}

private fun loadSessions(prefs: android.content.SharedPreferences): List<Session> = try {
    val array = JSONArray(prefs.getString("sessions", "[]"))
    (0 until array.length()).map { i ->
        val o = array.getJSONObject(i)
        Session(o.getString("date"), o.getString("plan"), o.getString("summary"), o.getDouble("volume"))
    }
} catch (_: Exception) { emptyList() }

private fun sessionsToJson(sessions: List<Session>): String {
    val array = JSONArray()
    sessions.forEach { s -> array.put(JSONObject().put("date", s.date).put("plan", s.plan).put("summary", s.summary).put("volume", s.volume)) }
    return array.toString()
}
