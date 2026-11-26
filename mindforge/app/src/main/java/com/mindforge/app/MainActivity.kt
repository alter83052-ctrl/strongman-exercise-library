package com.mindforge.app

import android.app.*
import android.content.*
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

private val Bg = Color(0xFF08090C)
private val Surface = Color(0xFF17191E)
private val Surface2 = Color(0xFF242329)
private val Red = Color(0xFFB51225)
private val Ember = Color(0xFFE05A36)
private val TextMain = Color(0xFFF2EEE9)
private val TextMuted = Color(0xFFAAA5A2)
private val Gold = Color(0xFFC6A36A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MindForge(this) }
    }
}
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = "mind_forge"
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channel, "MIND FORGE", NotificationManager.IMPORTANCE_DEFAULT))
        val n = androidx.core.app.NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_mind_forge)
            .setContentTitle("MIND FORGE")
            .setContentText("Время тренировать разум / Zeit, deinen Geist zu trainieren")
            .setAutoCancel(true).build()
        manager.notify(77, n)
    }
}

private data class Lesson(val id: String, val section: String, val titleRu: String, val titleDe: String, val bodyRu: String, val bodyDe: String, val tags: String = "")
private data class Flash(val front: String, val back: String, val due: Long = 0L, val interval: Int = 0, val reps: Int = 0)
private data class UserNote(val title: String, val body: String, val section: String)

private val lessons = listOf(
    Lesson("k1","Knowledge Base","Стоицизм: зона контроля","Stoizismus: Kontrollbereich","Эпиктет разделял события на зависящие от нас и не зависящие. В нашей власти находятся суждения, намерения и действия. Практика: перед реакцией спроси себя, что именно зависит от тебя.","Epiktet unterschied zwischen Dingen, die von uns abhängen, und Dingen, die nicht von uns abhängen. In unserer Macht liegen Urteile, Absichten und Handlungen. Übung: Frage vor deiner Reaktion, was tatsächlich in deiner Kontrolle liegt.","Философия"),
    Lesson("k2","Knowledge Base","Дисциплина и привычки","Disziplin und Gewohnheiten","Привычка закрепляется повторением в устойчивом контексте. Выбери небольшое действие, привяжи его к существующему событию и отслеживай выполнение.","Eine Gewohnheit entsteht durch Wiederholung in einem stabilen Kontext. Wähle eine kleine Handlung, verknüpfe sie mit einem bestehenden Ereignis und verfolge die Umsetzung.","Психология"),
    Lesson("k3","Knowledge Base","Стресс и адаптация","Stress und Anpassung","Стрессовая реакция включает нервные и гормональные механизмы. Кратковременная мобилизация помогает действовать; длительная нагрузка влияет на сон, внимание и восстановление.","Die Stressreaktion umfasst nervöse und hormonelle Mechanismen. Kurzfristige Aktivierung unterstützt das Handeln; anhaltende Belastung beeinflusst Schlaf, Aufmerksamkeit und Regeneration.","Психология"),
    Lesson("k4","Knowledge Base","Научный метод","Wissenschaftliche Methode","Наблюдение формирует вопрос, гипотеза даёт проверяемое объяснение, эксперимент собирает данные. Вывод должен соответствовать данным, а не ожиданиям.","Beobachtung führt zu einer Frage, eine Hypothese liefert eine überprüfbare Erklärung, ein Experiment sammelt Daten. Die Schlussfolgerung muss zu den Daten passen, nicht zu den Erwartungen.","Наука"),
    Lesson("k5","Knowledge Base","Принцип прогрессивной нагрузки","Prinzip der progressiven Belastung","Для развития силы нагрузка постепенно увеличивается через вес, повторения, подходы или качество выполнения. Прогресс оценивают по журналу тренировок.","Für Kraftentwicklung wird die Belastung schrittweise über Gewicht, Wiederholungen, Sätze oder Ausführungsqualität erhöht. Fortschritt wird im Trainingsprotokoll dokumentiert.","Спорт"),
    Lesson("k6","Knowledge Base","Восстановление","Regeneration","Адаптация происходит не только во время нагрузки, но и в период восстановления. На неё влияют сон, питание, управление объёмом и регулярность тренировок.","Anpassung findet nicht nur während der Belastung, sondern auch in der Erholungsphase statt. Schlaf, Ernährung, Trainingsumfang und Regelmäßigkeit beeinflussen sie.","Спорт"),
    Lesson("l1","Language Core","Артикли: определённый и неопределённый","Artikel: bestimmt und unbestimmt","der — мужской род; die — женский; das — средний. ein/eine употребляются, когда предмет вводится впервые или не уточняется. Учить существительное лучше сразу с артиклем.","der — maskulin; die — feminin; das — neutrum. ein/eine verwendet man häufig bei erstmaliger oder nicht näher bestimmter Erwähnung. Lerne Substantive am besten immer mit Artikel.","Grammatik • A2"),
    Lesson("l2","Language Core","Perfekt в разговорной речи","Perfekt im Alltag","Perfekt образуется с haben или sein и Partizip II: Ich habe gearbeitet. / Ich bin aufgestanden. Sein часто используется с движением или изменением состояния.","Das Perfekt wird mit haben oder sein und Partizip II gebildet: Ich habe gearbeitet. / Ich bin aufgestanden. Sein steht oft bei Bewegung oder Zustandsänderung.","Grammatik • A2"),
    Lesson("l3","Language Core","Порядок слов: weil","Wortstellung: weil","После weil спрягаемый глагол уходит в конец придаточного: Ich bleibe zu Hause, weil ich krank bin. В главном предложении глагол обычно на втором месте.","Nach weil steht das konjugierte Verb am Ende des Nebensatzes: Ich bleibe zu Hause, weil ich krank bin. Im Hauptsatz steht das Verb normalerweise an zweiter Stelle.","Grammatik • B1"),
    Lesson("l4","Language Core","Konjunktiv II: вежливая просьба","Konjunktiv II: höfliche Bitte","Könnten Sie mir bitte helfen? — Не могли бы Вы мне помочь? Würden Sie das bitte wiederholen? — Не могли бы Вы это повторить? Форма делает просьбу вежливее.","Könnten Sie mir bitte helfen? Würden Sie das bitte wiederholen? Der Konjunktiv II macht eine Bitte höflicher.","Grammatik • B1"),
    Lesson("l5","Language Core","Пассив: Vorgangspassiv","Passiv: Vorgangspassiv","werden + Partizip II: Der Patient wird gewaschen. — Пациента моют. Пассив подчёркивает действие, а не того, кто его выполняет.","werden + Partizip II: Der Patient wird gewaschen. Das Passiv betont die Handlung und nicht die Person, die sie ausführt.","Grammatik • B1"),
    Lesson("l6","Language Core","Профессиональная лексика Pflege","Fachwortschatz Pflege","die Körperpflege — уход за телом; die Mobilisation — мобилизация; die Übergabe — передача смены; die Vitalzeichen — жизненные показатели; die Flüssigkeitsbilanz — водный баланс.","die Körperpflege — уход за телом; die Mobilisation — мобилизация; die Übergabe — передача смены; die Vitalzeichen — жизненные показатели; die Flüssigkeitsbilanz — водный баланс.","Pflege • B1"),
    Lesson("l7","Language Core","Kommunikation mit Bewohnern","Kommunikation mit Bewohnern","Открытые вопросы: Wie fühlen Sie sich heute? Что Вы сегодня чувствуете? Активное слушание: дать человеку договорить, уточнить смысл и подтвердить понимание.","Offene Frage: Wie fühlen Sie sich heute? Aktives Zuhören bedeutet: ausreden lassen, Verständnis klären und das Gehörte bestätigen.","Pflege • B1"),
    Lesson("l8","Language Core","Документирование ухода","Pflegedokumentation","Документируй наблюдаемые факты: время, действие, реакцию и измеримые показатели. Используй ясные формулировки без предположений и оценочных ярлыков.","Dokumentiere beobachtbare Fakten: Zeit, Handlung, Reaktion und messbare Werte. Verwende klare Formulierungen ohne Vermutungen oder wertende Etiketten.","Pflege • B1"),
    Lesson("m1","Medical Mind","Кожа и её функции","Haut und ihre Funktionen","Кожа состоит из эпидермиса, дермы и подкожной клетчатки. Она защищает организм, участвует в терморегуляции, чувствительности и иммунной защите.","Die Haut besteht aus Epidermis, Dermis und Subcutis. Sie schützt den Körper und ist an Thermoregulation, Sensibilität und Immunabwehr beteiligt.","Anatomie"),
    Lesson("m2","Medical Mind","Декубитус","Dekubitus","Декубитус — локальное повреждение кожи и/или подлежащих тканей, обычно над костным выступом, вследствие давления или давления в сочетании со сдвигом. Профилактика включает оценку риска, движение, осмотр кожи и индивидуальное перераспределение давления.","Ein Dekubitus ist eine lokale Schädigung der Haut und/oder des darunterliegenden Gewebes, meist über einem Knochenvorsprung, infolge von Druck oder Druck in Kombination mit Scherkräften. Prophylaxe umfasst Risikoeinschätzung, Bewegung, Hautbeobachtung und individuelle Druckentlastung.","Pflege • Prophylaxe"),
    Lesson("m3","Medical Mind","Sturzprophylaxe","Профилактика падений","Оценивают индивидуальные факторы риска: предыдущие падения, походку, лекарства, зрение, обувь и окружающую среду. Меры подбирают индивидуально и пересматривают при изменении состояния.","Individuelle Risikofaktoren: frühere Stürze, Gangbild, Medikamente, Sehvermögen, Schuhe und Umgebung. Maßnahmen werden individuell geplant und bei Zustandsänderung überprüft.","Pflege • Prophylaxe"),
    Lesson("m4","Medical Mind","Thromboseprophylaxe","Профилактика тромбоза","Профилактика направлена на снижение венозного застоя и других факторов риска. В уходе важны ранняя мобилизация, упражнения для ног и выполнение назначенных медицинских мер.","Die Prophylaxe zielt darauf ab, venöse Stase und weitere Risikofaktoren zu verringern. In der Pflege sind frühe Mobilisation, Beinübungen und die Umsetzung ärztlich angeordneter Maßnahmen wichtig.","Pflege • Prophylaxe"),
    Lesson("m5","Medical Mind","Dehydration","Обезвоживание","Недостаток жидкости может проявляться сухостью слизистых, уменьшением диуреза, слабостью и спутанностью сознания. Важны наблюдение за питьём, баланс жидкости и своевременное сообщение об изменениях.","Flüssigkeitsmangel kann sich durch trockene Schleimhäute, verringerte Urinmenge, Schwäche und Verwirrtheit zeigen. Wichtig sind Trinkbeobachtung, Flüssigkeitsbilanz und rechtzeitige Weitergabe von Veränderungen.","Pflege"),
    Lesson("m6","Medical Mind","Vitalzeichen","Жизненные показатели","К основным Vitalzeichen относятся Blutdruck, Puls, Atmung, Körpertemperatur и je nach Kontext Sauerstoffsättigung. Всегда учитывают индивидуальные исходные значения и состояние человека.","Zu den Vitalzeichen gehören Blutdruck, Puls, Atmung, Körpertemperatur und je nach Kontext die Sauerstoffsättigung. Individuelle Ausgangswerte und der Zustand der Person sind zu berücksichtigen.","Physiologie • Pflege"),
    Lesson("m7","Medical Mind","Сердце и кровообращение","Herz und Kreislauf","Сердце работает как насос. Правая половина направляет кровь в лёгкие, левая — в системный круг кровообращения. Артерии отводят кровь от сердца, вены возвращают её.","Das Herz arbeitet als Pumpe. Die rechte Herzhälfte leitet Blut in die Lunge, die linke in den Körperkreislauf. Arterien führen Blut vom Herzen weg, Venen zum Herzen zurück.","Anatomie"),
    Lesson("m8","Medical Mind","Инсулин и глюкоза","Insulin und Glukose","Инсулин вырабатывается бета-клетками поджелудочной железы и способствует поступлению глюкозы в чувствительные к инсулину ткани. Он также влияет на обмен жиров и белков.","Insulin wird von den Betazellen des Pankreas gebildet und fördert die Glukoseaufnahme in insulinempfindliche Gewebe. Es beeinflusst außerdem den Fett- und Eiweißstoffwechsel.","Endokrinologie"),
    Lesson("m9","Medical Mind","Воспаление","Entzündung","Классические признаки воспаления: покраснение, тепло, отёк, боль и ограничение функции. Воспаление — часть защитной реакции организма.","Klassische Entzündungszeichen sind Rötung, Wärme, Schwellung, Schmerz und Funktionseinschränkung. Entzündung ist Teil der körpereigenen Abwehrreaktion.","Pathologie"),
    Lesson("m10","Medical Mind","Фармакология: пути введения","Pharmakologie: Applikationswege","Лекарства могут вводиться перорально, сублингвально, ректально, ингаляционно, местно или парентерально. Путь введения влияет на скорость и характер поступления вещества.","Arzneimittel können oral, sublingual, rektal, inhalativ, lokal oder parenteral verabreicht werden. Der Applikationsweg beeinflusst Geschwindigkeit und Art der Aufnahme.","Pharmakologie"),
    Lesson("m11","Medical Mind","Пульс","Puls","Пульс оценивают по частоте, ритму и наполнению. В покое у взрослых часто встречается диапазон 60–100 ударов в минуту, но интерпретация зависит от контекста и индивидуальных особенностей.","Der Puls wird nach Frequenz, Rhythmus und Qualität beurteilt. Bei Erwachsenen liegt der Ruhepuls häufig zwischen 60 und 100 Schlägen pro Minute; die Bewertung hängt vom Kontext und individuellen Merkmalen ab.","Physiologie"),
    Lesson("m12","Medical Mind","13 AEDL по Krohwinkel","13 AEDL nach Krohwinkel","Структура AEDL: kommunizieren; sich bewegen; vitale Funktionen aufrechterhalten; sich pflegen; essen und trinken; ausscheiden; sich kleiden; ruhen und schlafen; sich beschäftigen; sich als Mann/Frau fühlen; für Sicherheit sorgen; soziale Bereiche sichern; mit existenziellen Erfahrungen umgehen.","Die AEDL strukturieren zentrale Lebensaktivitäten: kommunizieren; sich bewegen; vitale Funktionen aufrechterhalten; sich pflegen; essen und trinken; ausscheiden; sich kleiden; ruhen und schlafen; sich beschäftigen; sich als Mann/Frau fühlen; für Sicherheit sorgen; soziale Bereiche sichern; mit existenziellen Erfahrungen umgehen.","Pflegemodell"),
    Lesson("m13","Medical Mind","Модель Dorothea Orem","Modell nach Dorothea Orem","Модель дефицита самоухода рассматривает способность человека заботиться о себе. Pflegebedarf возникает, когда потребность в самоуходе превышает способность человека её удовлетворить.","Die Selbstpflegedefizit-Theorie betrachtet die Fähigkeit eines Menschen zur Selbstpflege. Pflegebedarf entsteht, wenn der Selbstpflegebedarf die Selbstpflegefähigkeit übersteigt.","Pflegemodell"),
    Lesson("s1","Stoic Mind","Утреннее намерение","Morgendliche Absicht","Определи одну главную задачу дня. Сформулируй конкретное действие, которое зависит от тебя. Вечером оцени не обстоятельства, а качество собственного решения.","Bestimme eine zentrale Aufgabe des Tages. Formuliere eine konkrete Handlung, die in deiner Kontrolle liegt. Bewerte abends nicht die Umstände, sondern die Qualität deiner Entscheidung.","Praxis"),
    Lesson("s2","Stoic Mind","Вечерний разбор","Abendliche Rückschau","Три вопроса: что сделал хорошо? Где потерял самообладание? Что завтра сделаю иначе? Цель — обучение и корректировка поведения.","Drei Fragen: Was habe ich gut gemacht? Wo habe ich die Selbstbeherrschung verloren? Was mache ich morgen anders? Ziel ist Lernen und Verhaltensanpassung.","Praxis"),
    Lesson("s3","Stoic Mind","Пауза перед реакцией","Pause vor der Reaktion","Между стимулом и ответом можно создать паузу. Назови эмоцию, отдели факт от интерпретации и выбери действие, соответствующее цели.","Zwischen Reiz und Reaktion lässt sich eine Pause schaffen. Benenne das Gefühl, trenne Tatsache und Interpretation und wähle eine Handlung, die deinem Ziel entspricht.","Selbstführung"),
    Lesson("t1","Tactical Mind","Числовая последовательность","Zahlenfolge","2, 4, 8, 16, 32. Правило: каждое следующее число в два раза больше предыдущего. Попробуй найти правило, а не угадывать ответ.","2, 4, 8, 16, 32. Regel: Jede Zahl ist doppelt so groß wie die vorherige. Suche die Regel, statt die Antwort zu erraten.","Logik"),
    Lesson("t2","Tactical Mind","Задача о семье","Familienrätsel","У отца четыре сына. У каждого сына одна сестра. Всего пять детей: четыре сына и одна общая сестра.","Ein Vater hat vier Söhne. Jeder Sohn hat eine Schwester. Insgesamt sind es fünf Kinder: vier Söhne und eine gemeinsame Schwester.","Logik"),
    Lesson("t3","Tactical Mind","Рабочая память","Arbeitsgedächtnis","Запомни последовательность: 7 – 2 – 9 – 4 – 1. Затем воспроизведи её в обратном порядке: 1 – 4 – 9 – 2 – 7.","Merke dir die Folge: 7 – 2 – 9 – 4 – 1. Gib sie anschließend rückwärts wieder: 1 – 4 – 9 – 2 – 7.","Gedächtnis"),
    Lesson("tr1","Training","Силовая база: день A","Kraftbasis: Tag A","Приседания 5×5; жим лёжа 5×5; тяга штанги в наклоне 4×6; подтягивания 3×максимум технически чистых повторений; переноска тяжестей 4×20–30 м. Отдых 2–4 минуты в базовых упражнениях.","Kniebeugen 5×5; Bankdrücken 5×5; Langhantelrudern 4×6; Klimmzüge 3×saubere Wiederholungen; Loaded Carries 4×20–30 m. Pause 2–4 Minuten bei Grundübungen.","Powerlifting • Strongman"),
    Lesson("tr2","Training","Мощность и Strongman: день B","Power und Strongman: Tag B","Толчковая или взрывная техника 5×2–3; становая тяга 4×3–5; жим стоя 4×5; фермерская прогулка 5×20 м; толкание саней 4×20 м. Каждое повторение выполняй мощно и технически.","Explosive Technik 5×2–3; Kreuzheben 4×3–5; Schulterdrücken 4×5; Farmer’s Walk 5×20 m; Schlitten schieben 4×20 m. Jede Wiederholung kraftvoll und technisch sauber ausführen.","Strongman"),
    Lesson("tr3","Training","Принципы взрывной силы","Prinzipien der Explosivkraft","Взрывная работа строится на высокой скорости намерения, малом числе повторений и достаточном отдыхе. Останавливай подход, когда скорость заметно падает.","Explosivtraining basiert auf maximaler Bewegungsabsicht, wenigen Wiederholungen und ausreichender Pause. Beende den Satz, wenn die Geschwindigkeit deutlich sinkt.","Athletik"),
    Lesson("tr4","Training","Фермерская прогулка","Farmer’s Walk","Возьми две тяжёлые гантели или специальные рукояти. Сохраняй высокий корпус, нейтральное положение позвоночника, сильный хват и ровный шаг. Отмечай дистанцию и вес.","Nimm zwei schwere Kurzhanteln oder Farmer-Griffe. Halte den Oberkörper aufrecht, die Wirbelsäule neutral, den Griff fest und die Schritte gleichmäßig. Dokumentiere Strecke und Gewicht.","Strongman")
)

private val seedCards = listOf(
    Flash("Was bedeutet die Körperpflege?","Уход за телом / Körperpflege — Maßnahmen zur Erhaltung der Körperhygiene."),
    Flash("Was ist ein Dekubitus?","Локальное повреждение кожи и/или подлежащих тканей из-за давления или давления со сдвигом."),
    Flash("Nenne drei Vitalzeichen.","Blutdruck, Puls, Atmung. Также Körpertemperatur und Sauerstoffsättigung."),
    Flash("Was bedeutet Mobilisation?","Мобилизация — Maßnahmen zur Förderung von Bewegung und Beweglichkeit."),
    Flash("Was ist die Thromboseprophylaxe?","Меры по снижению риска образования тромба."),
    Flash("Was bedeutet Sturzprophylaxe?","Профилактика падений."),
    Flash("Was bedeutet die Übergabe?","Передача смены / strukturierte Weitergabe pflegerelevanter Informationen."),
    Flash("Was ist Dehydration?","Обезвоживание / Flüssigkeitsmangel im Körper."),
    Flash("Wie lautet der Plural von der Bewohner?","die Bewohner"),
    Flash("Wie lautet der Plural von das Bett?","die Betten"),
    Flash("Was bedeutet ausscheiden?","Выделять / Urin und Stuhl ausscheiden."),
    Flash("Was ist eine Anamnese?","Сбор сведений о состоянии здоровья и истории пациента."),
    Flash("Was ist die Primärprävention?","Предупреждение возникновения заболевания."),
    Flash("Was ist die Sekundärprävention?","Раннее выявление заболевания."),
    Flash("Was ist die Tertiärprävention?","Предупреждение осложнений и последствий уже имеющегося заболевания.")
)

private fun loadCards(c: Context): MutableList<Flash> {
    val p = c.getSharedPreferences("mf_v2", 0)
    if (!p.getBoolean("seeded", false)) {
        val a = JSONArray()
        seedCards.forEach { a.put(JSONObject().put("f", it.front).put("b", it.back).put("d", 0L).put("i", 0).put("r", 0)) }
        p.edit().putString("cards", a.toString()).putBoolean("seeded", true).apply()
    }
    val a = JSONArray(p.getString("cards", "[]"))
    return MutableList(a.length()) { i -> a.getJSONObject(i).let { Flash(it.optString("f"), it.optString("b"), it.optLong("d"), it.optInt("i"), it.optInt("r")) } }
}
private fun saveCards(c: Context, cards: List<Flash>) {
    val a = JSONArray()
    cards.forEach { a.put(JSONObject().put("f", it.front).put("b", it.back).put("d", it.due).put("i", it.interval).put("r", it.reps)) }
    c.getSharedPreferences("mf_v2", 0).edit().putString("cards", a.toString()).apply()
}
private fun loadNotes(c: Context): List<UserNote> {
    val a = JSONArray(c.getSharedPreferences("mf_v2", 0).getString("notes", "[]"))
    return List(a.length()) { i -> a.getJSONObject(i).let { UserNote(it.optString("t"), it.optString("b"), it.optString("s")) } }
}
private fun saveNotes(c: Context, notes: List<UserNote>) {
    val a = JSONArray(); notes.forEach { a.put(JSONObject().put("t", it.title).put("b", it.body).put("s", it.section)) }
    c.getSharedPreferences("mf_v2", 0).edit().putString("notes", a.toString()).apply()
}

@Composable private fun MindForge(context: Context) {
    val prefs = remember { context.getSharedPreferences("mf_v2", 0) }
    var page by remember { mutableStateOf("home") }
    var section by remember { mutableStateOf("Knowledge Base") }
    var selected by remember { mutableStateOf<Lesson?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Все / Alle") }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()) }
    var cards by remember { mutableStateOf(loadCards(context)) }
    var notes by remember { mutableStateOf(loadNotes(context)) }
    var cardIndex by remember { mutableIntStateOf(0) }
    var reveal by remember { mutableStateOf(false) }
    var newFront by remember { mutableStateOf("") }; var newBack by remember { mutableStateOf("") }
    var noteTitle by remember { mutableStateOf("") }; var noteBody by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(prefs.getInt("hour", 19)) }
    var minute by remember { mutableIntStateOf(prefs.getInt("minute", 0)) }
    var reminderOn by remember { mutableStateOf(prefs.getBoolean("reminder", false)) }
    var days by remember { mutableStateOf((1..7).map { prefs.getBoolean("day$it", it <= 5) }) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val modules = listOf(
        Triple("Knowledge Base","База знаний / Wissensdatenbank","Философия • психология • наука • спорт"),
        Triple("Memory Engine","Тренировка памяти / Gedächtnistraining","Готовые карточки • интервальное повторение"),
        Triple("Language Core","Немецкий язык / Deutsch lernen","A2–B2 • грамматика • Pflege • лексика"),
        Triple("Medical Mind","Медицина и Pflege / Medizin & Pflege","Анатомия • физиология • патология"),
        Triple("Stoic Mind","Стоическая практика / Stoische Praxis","Рефлексия • самодисциплина"),
        Triple("Tactical Mind","Тактическое мышление / Taktisches Denken","Логика • внимание • память"),
        Triple("Training Forge","Сила и Strongman / Kraft & Strongman","Силовая база • мощность • программы"),
        Triple("Progress","Прогресс / Fortschritt","Статистика • повторения • активность")
    )
    val due = cards.filter { it.due <= System.currentTimeMillis() }
    val sectionLessons = lessons.filter { it.section == section && (filter == "Все / Alle" || it.tags.contains(filter, true)) && (query.isBlank() || (it.titleRu + it.titleDe + it.bodyRu + it.bodyDe + it.tags).contains(query, true)) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Red, background = Bg, surface = Surface, onSurface = TextMain)) {
        Column(Modifier.fillMaxSize().background(Bg)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("MIND FORGE", color = TextMain, fontSize = 23.sp, fontWeight = FontWeight.Black, letterSpacing = 2.2.sp)
                    Text("ЗНАНИЯ  •  СИЛА  •  ДИСЦИПЛИНА", color = Red, fontSize = 9.sp, letterSpacing = 1.0.sp)
                }
                IconButton(onClick = { page = "reminders" }) { Icon(Icons.Default.Notifications, null, tint = Gold) }
            }
            when {
                page == "home" -> {
                    LazyColumn(
                        Modifier.weight(1f),
                        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 22.dp),
                        verticalArrangement = Arrangement.spacedBy(17.dp)
                    ) {
                        item {
                            ForgeHero(
                                due = due.size,
                                cards = cards.size,
                                notes = notes.size,
                                onStudy = { section = "Memory Engine"; page = "memory" }
                            )
                        }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("ТВОЯ АРХИТЕКТУРА", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.8.sp)
                                    Text("Семь направлений развития", color = TextMain, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("01 — 07", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        items(modules.filter { it.first != "Progress" }.chunked(2)) { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                pair.forEach { m ->
                                    val target = if (m.first == "Memory Engine") "memory" else "library"
                                    ForgeModuleCard(
                                        modifier = Modifier.weight(1f),
                                        index = modules.indexOf(m) + 1,
                                        title = m.first,
                                        subtitle = m.second,
                                        description = m.third,
                                        glyph = moduleGlyph(m.first),
                                        onClick = { section = m.first; page = target; query = ""; filter = "Все / Alle" }
                                    )
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                        item {
                            ForgeProgressPanel(
                                total = lessons.size,
                                favorites = favorites.size,
                                onClick = { page = "progress" }
                            )
                        }
                    }
                }
                page == "library" -> {
                    Header(section) { page = "home" }
                    Column(Modifier.weight(1f).padding(horizontal = 15.dp)) {
                        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text("Поиск / Suche") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
                        Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            val filters = listOf("Все / Alle") + lessons.filter { it.section == section }.map { it.tags }.distinct()
                            filters.forEach { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) }) }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 15.dp)) {
                            items(sectionLessons) { l ->
                                Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(12.dp)).border(1.dp, Color(0xFF3A3033), RoundedCornerShape(12.dp)).clickable { selected = l; page = "detail" }.padding(13.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(l.tags.uppercase(), color = Ember, fontSize = 9.sp, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { favorites = if (favorites.contains(l.id)) favorites - l.id else favorites + l.id; prefs.edit().putStringSet("favorites", favorites).apply() }) {
                                            Icon(if (favorites.contains(l.id)) Icons.Default.Star else Icons.Default.StarBorder, null, tint = Gold)
                                        }
                                    }
                                    Text(l.titleRu, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(l.titleDe, color = Gold, fontSize = 13.sp)
                                    Text(l.bodyRu.take(125) + if (l.bodyRu.length > 125) "…" else "", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                page == "detail" && selected != null -> {
                    Header(selected!!.titleRu) { page = "library" }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(17.dp)) {
                        Text(selected!!.tags.uppercase(), color = Ember, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(selected!!.titleDe, color = Gold, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Text("РУССКИЙ", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(selected!!.bodyRu, color = TextMain, fontSize = 16.sp, lineHeight = 24.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("DEUTSCH", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(selected!!.bodyDe, color = TextMain, fontSize = 15.sp, lineHeight = 23.sp)
                        Spacer(Modifier.height(20.dp))
                        OutlinedButton(onClick = { favorites = favorites + selected!!.id; prefs.edit().putStringSet("favorites", favorites).apply() }) { Icon(Icons.Default.Star, null); Text(" В избранное / Favorit") }
                    }
                }
                page == "memory" -> {
                    Header("Memory Engine / Gedächtnis") { page = "home" }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp)) {
                        Text("ГОТОВАЯ КОЛОДА PFLEGE + DEUTSCH", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("К повторению: ${due.size} из ${cards.size}", color = TextMuted)
                        Spacer(Modifier.height(12.dp))
                        if (due.isNotEmpty()) {
                            val current = due[cardIndex.coerceIn(0, due.lastIndex)]
                            Column(Modifier.fillMaxWidth().background(Surface2, RoundedCornerShape(18.dp)).border(1.dp, Red, RoundedCornerShape(18.dp)).padding(20.dp)) {
                                Text("KARTEIKARTE / КАРТОЧКА", color = Ember, fontSize = 10.sp)
                                Spacer(Modifier.height(16.dp))
                                Text(if (reveal) current.back else current.front, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp)
                                Spacer(Modifier.height(20.dp))
                                Button(onClick = { reveal = !reveal }, colors = ButtonDefaults.buttonColors(containerColor = Red), modifier = Modifier.fillMaxWidth()) { Text(if (reveal) "СКРЫТЬ ОТВЕТ / ANTWORT VERBERGEN" else "ПОКАЗАТЬ ОТВЕТ / ANTWORT ZEIGEN") }
                                if (reveal) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                        OutlinedButton(onClick = { updateCard(context, cards, current, 0); cards = loadCards(context); reveal = false; cardIndex = 0 }, modifier = Modifier.weight(1f)) { Text("ЕЩЁ РАЗ") }
                                        Button(onClick = { updateCard(context, cards, current, 1); cards = loadCards(context); reveal = false; cardIndex = 0 }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF315B46))) { Text("ЗНАЮ / WEISS") }
                                    }
                                }
                            }
                        } else Text("На сегодня всё. / Für heute ist alles erledigt.", color = Gold, fontSize = 18.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("ДОБАВИТЬ СВОЮ КАРТОЧКУ / EIGENE KARTE", color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        OutlinedTextField(newFront, { newFront = it }, Modifier.fillMaxWidth(), label = { Text("Вопрос / Vorderseite") })
                        OutlinedTextField(newBack, { newBack = it }, Modifier.fillMaxWidth(), label = { Text("Ответ / Rückseite") })
                        Button(onClick = { if (newFront.isNotBlank() && newBack.isNotBlank()) { cards = (cards + Flash(newFront.trim(), newBack.trim())).toMutableList(); saveCards(context, cards); newFront = ""; newBack = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("ДОБАВИТЬ / HINZUFÜGEN") }
                    }
                }
                page == "progress" -> {
                    Header("Progress / Fortschritt") { page = "home" }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp)) {
                        Text("ТВОЯ СТАТИСТИКА / DEINE STATISTIK", color = Gold, fontWeight = FontWeight.Bold)
                        MetricRow("Материалы в библиотеке / Lerninhalte", lessons.size.toString())
                        MetricRow("Карточки / Karteikarten", cards.size.toString())
                        MetricRow("Повторения / Wiederholungen", cards.sumOf { it.reps }.toString())
                        MetricRow("Выучено / Gelernt", cards.count { it.interval > 0 }.toString())
                        MetricRow("Избранное / Favoriten", favorites.size.toString())
                        MetricRow("Личные заметки / Eigene Notizen", notes.size.toString())
                        Text("АКТИВНОСТЬ ПО РАЗДЕЛАМ / AKTIVITÄT", color = Red, modifier = Modifier.padding(top = 18.dp))
                        modules.forEach { m -> MetricRow(m.first, lessons.count { it.section == m.first }.toString()) }
                    }
                }
                page == "notes" -> {
                    Header("Заметки / Notizen") { page = "home" }
                    Column(Modifier.weight(1f).padding(16.dp)) {
                        OutlinedTextField(noteTitle, { noteTitle = it }, Modifier.fillMaxWidth(), label = { Text("Заголовок / Titel") })
                        OutlinedTextField(noteBody, { noteBody = it }, Modifier.fillMaxWidth().height(130.dp), label = { Text("Текст / Inhalt") })
                        Button(onClick = { if (noteBody.isNotBlank()) { notes = notes + UserNote(noteTitle.ifBlank { "Notiz" }, noteBody, section); saveNotes(context, notes); noteTitle = ""; noteBody = "" } }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("СОХРАНИТЬ / SPEICHERN") }
                        LazyColumn { items(notes.reversed()) { n -> Column(Modifier.padding(vertical = 8.dp)) { Text(n.title, color = Gold, fontWeight = FontWeight.Bold); Text(n.body, color = TextMain); TextButton(onClick = { notes = notes - n; saveNotes(context, notes) }) { Text("Удалить / Löschen", color = Red) }; Divider(color = Surface2) } } }
                    }
                }
                page == "favorites" -> {
                    Header("Избранное / Favoriten") { page = "home" }
                    LazyColumn(Modifier.weight(1f).padding(horizontal = 15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(lessons.filter { favorites.contains(it.id) }) { l ->
                            Column(Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(12.dp)).clickable { selected = l; page = "detail" }.padding(13.dp)) { Text(l.titleRu, color = TextMain, fontWeight = FontWeight.Bold); Text(l.titleDe, color = Gold) }
                        }
                    }
                }
                page == "reminders" -> {
                    Header("Напоминания / Erinnerungen") { page = "home" }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Text("Включить / Aktivieren", color = TextMain, modifier = Modifier.weight(1f)); Switch(reminderOn, { reminderOn = it }) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { hour = (hour + 1) % 24 }) { Text("ЧАС %02d".format(hour)) }
                            OutlinedButton(onClick = { minute = (minute + 5) % 60 }) { Text("МИН %02d".format(minute)) }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("ДНИ НЕДЕЛИ / WOCHENTAGE", color = Gold, fontWeight = FontWeight.Bold)
                        listOf("Понедельник / Montag","Вторник / Dienstag","Среда / Mittwoch","Четверг / Donnerstag","Пятница / Freitag","Суббота / Samstag","Воскресенье / Sonntag").forEachIndexed { i, label ->
                            Row(verticalAlignment = Alignment.CenterVertically) { Text(label, color = TextMain, modifier = Modifier.weight(1f)); Checkbox(days[i], { v -> days = days.toMutableList().also { it[i] = v } }, colors = CheckboxDefaults.colors(checkedColor = Red)) }
                        }
                        Button(onClick = {
                            prefs.edit().putInt("hour", hour).putInt("minute", minute).putBoolean("reminder", reminderOn).apply()
                            days.forEachIndexed { i, v -> prefs.edit().putBoolean("day${i + 1}", v).apply() }
                            if (Build.VERSION.SDK_INT >= 33) permission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            for (d in 1..7) {
                                val pi = PendingIntent.getBroadcast(context, 700 + d, Intent(context, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                if (reminderOn && days[d - 1]) {
                                    val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, if (d == 7) Calendar.SUNDAY else d + 1); set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1) }
                                    am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pi)
                                } else am.cancel(pi)
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("СОХРАНИТЬ / SPEICHERN") }
                    }
                }
            }
            if (page != "detail") BottomBar(page, { page = it })
        }
    }
}
private fun updateCard(context: Context, all: List<Flash>, current: Flash, known: Int) {
    val nextInterval = if (known == 0) 0 else if (current.interval == 0) 1 else (current.interval * 2).coerceAtMost(60)
    val due = if (known == 0) System.currentTimeMillis() + 10 * 60 * 1000L else System.currentTimeMillis() + nextInterval * 86400000L
    saveCards(context, all.map { if (it == current) it.copy(due = due, interval = nextInterval, reps = it.reps + 1) else it })
}
private fun moduleGlyph(s: String) = when(s) {
    "Knowledge Base" -> "K"; "Memory Engine" -> "M"; "Language Core" -> "L"; "Medical Mind" -> "+" 
    "Stoic Mind" -> "S"; "Tactical Mind" -> "T"; "Training Forge" -> "F"; else -> "P"
}
@Composable private fun ForgeHero(due: Int, cards: Int, notes: Int, onStudy: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "forgeGlow")
    val glow by pulse.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF272027), Color(0xFF111216), Color(0xFF08090C))))
            .border(1.dp, Gold.copy(alpha = .34f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PERSONAL INTELLIGENCE SYSTEM", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(13.dp))
                Text("MIND", color = TextMain, fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("FORGE", color = Red, fontSize = 31.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Spacer(Modifier.height(5.dp))
                Text("КУЙ СВОЙ РАЗУМ  /  SCHMIEDE DEINEN GEIST", color = Gold, fontSize = 8.sp, letterSpacing = .7.sp)
            }
            Box(
                Modifier.size(94.dp).clip(RoundedCornerShape(26.dp))
                    .background(Brush.radialGradient(listOf(Red.copy(alpha = glow), Color(0xFF171116), Bg)))
                    .border(1.dp, Red.copy(alpha = glow), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                ForgeEmblem(Modifier.size(77.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ForgeMetric("К ПОВТОРЕНИЮ", due.toString())
            ForgeMetric("КАРТОЧКИ", cards.toString())
            ForgeMetric("ЗАМЕТКИ", notes.toString())
        }
        Spacer(Modifier.height(17.dp))
        Button(
            onClick = onStudy,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Red)
        ) {
            Text("НАЧАТЬ ТРЕНИРОВКУ ПАМЯТИ", fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable private fun ForgeEmblem(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(Color(0xFF8F1022).copy(alpha = .22f), radius = w * .46f, center = center)
        drawCircle(Color(0xFFC6A36A).copy(alpha = .55f), radius = w * .43f, center = center, style = Stroke(width = 1.2.dp.toPx()))
        val raven = Path().apply {
            moveTo(w*.12f,h*.56f); cubicTo(w*.18f,h*.18f,w*.55f,h*.10f,w*.72f,h*.34f)
            lineTo(w*.87f,h*.43f); lineTo(w*.70f,h*.48f); lineTo(w*.61f,h*.64f)
            lineTo(w*.46f,h*.69f); lineTo(w*.34f,h*.88f); lineTo(w*.24f,h*.72f)
            lineTo(w*.12f,h*.70f); close()
        }
        drawPath(raven, Color(0xFF090A0D))
        drawCircle(Color(0xFFFF263E), radius = w*.035f, center = androidx.compose.ui.geometry.Offset(w*.67f,h*.43f))
        drawLine(Color(0xFFC6A36A), androidx.compose.ui.geometry.Offset(w*.12f,h*.80f), androidx.compose.ui.geometry.Offset(w*.88f,h*.80f), strokeWidth = 3.dp.toPx())
        drawRect(Color(0xFF393239), topLeft = androidx.compose.ui.geometry.Offset(w*.27f,h*.80f), size = androidx.compose.ui.geometry.Size(w*.46f,h*.10f))
    }
}

@Composable private fun ForgeMetric(label: String, value: String) {
    Column {
        Text(value, color = TextMain, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text(label, color = TextMuted, fontSize = 8.sp, letterSpacing = .6.sp)
    }
}

@Composable private fun ForgeModuleCard(
    modifier: Modifier,
    index: Int,
    title: String,
    subtitle: String,
    description: String,
    glyph: String,
    onClick: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (entered) 1f else .96f, tween(520, easing = FastOutSlowInEasing), label = "tileScale")
    LaunchedEffect(Unit) { entered = true }
    Column(
        modifier.scale(scale).heightIn(min = 170.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF202027), Color(0xFF111216))))
            .border(1.dp, Color(0xFF49353B), RoundedCornerShape(19.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(Red.copy(alpha=.17f)), contentAlignment = Alignment.Center) {
                Text(glyph, color = Ember, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.weight(1f))
            Text("%02d".format(index), color = Gold.copy(alpha=.7f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Text(title.uppercase(), color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = .45.sp, maxLines = 2)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, color = Gold, fontSize = 10.sp, lineHeight = 14.sp, maxLines = 2)
        Spacer(Modifier.weight(1f))
        Text(description, color = TextMuted, fontSize = 9.sp, lineHeight = 13.sp, maxLines = 2)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ВОЙТИ В МОДУЛЬ", color = Red, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ArrowOutward, null, tint = Gold, modifier = Modifier.size(15.dp))
        }
    }
}

@Composable private fun ForgeProgressPanel(total: Int, favorites: Int, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF241A20), Color(0xFF17181D))))
            .border(1.dp, Gold.copy(alpha=.25f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick).padding(17.dp)
    ) {
        Text("ПРОГРЕСС / FORTSCHRITT", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(8.dp))
        Text("Твой интеллектуальный арсенал", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text("Dein persönliches Wissensarchiv", color = TextMuted, fontSize = 11.sp)
        Spacer(Modifier.height(13.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ForgeMetric("МАТЕРИАЛЫ", total.toString())
            ForgeMetric("ИЗБРАННОЕ", favorites.toString())
            Text("ОТКРЫТЬ  →", color = Red, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable private fun Metric(label: String, value: String) {
    Column { Text(value, color = TextMain, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(label, color = TextMuted, fontSize = 9.sp) }
}
@Composable private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Text(label, color = TextMuted, modifier = Modifier.weight(1f)); Text(value, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
    Divider(color = Surface2)
}
@Composable private fun Header(title: String, back: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.Default.ArrowBack, null, tint = Red) }
        Text(title, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable private fun BottomBar(page: String, go: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Surface).padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf(Triple("home", Icons.Default.Home, "Главная"), Triple("favorites", Icons.Default.Star, "Избранное"), Triple("notes", Icons.Default.EditNote, "Заметки"), Triple("reminders", Icons.Default.Alarm, "План")).forEach { (p, icon, label) ->
            TextButton(onClick = { go(p) }) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = if (page == p) Red else TextMuted); Text(label, color = if (page == p) Red else TextMuted, fontSize = 9.sp) } }
        }
    }
}
