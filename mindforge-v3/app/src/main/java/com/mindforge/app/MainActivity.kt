package com.mindforge.app
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import org.json.JSONArray
import org.json.JSONObject

private val Coal=Color(0xFF08090B); private val Panel=Color(0xFF17191D); private val Panel2=Color(0xFF24262A)
private val Red=Color(0xFFB51F20); private val Ember=Color(0xFFE04B2F); private val Gold=Color(0xFFC9A36B)
private val Bone=Color(0xFFF0E8D9); private val Ash=Color(0xFFAAA49A); private val Iron=Color(0xFF494449)
data class ForgeModule(val id:String,val ru:String,val en:String,val desc:String,val art:Int,val cats:List<String>)
data class Lesson(val id:String,val module:String,val cat:String,val ru:String,val de:String,val body:String,val bodyDe:String)
data class CardData(val q:String,val a:String,val days:Int=0,val reps:Int=0)
private val modules=listOf(
 ForgeModule("knowledge","База знаний","Knowledge Base","Философия • психология • наука",0,listOf("Философия","Психология","Наука","Спорт и тренировки","Питание","История и культура")),
 ForgeModule("memory","Двигатель памяти","Memory Engine","Карточки • интервальное повторение",1,listOf("Мои карточки","Повторение сегодня","Колоды","Методики памяти")),
 ForgeModule("language","Немецкий язык","Language Core • Deutsch","A2 • B1 • B2 • Pflege",2,listOf("Лексика RU ↔ DE","Грамматика","Pflege — медицина","Диалоги и ситуации","Упражнения","Мини-тесты")),
 ForgeModule("medical","Медицина","Medical Mind • Pflege","Анатомия • физиология • патология",3,listOf("Анатомия","Физиология","Патология","Заболевания","Фармакология","Уход (Pflege)")),
 ForgeModule("stoic","Стоицизм","Stoic Mind","Практика • журнал • самоконтроль",4,listOf("Теория стоицизма","Ежедневная практика","Журнал размышлений","Контроль эмоций","Устойчивость к стрессу")),
 ForgeModule("tactical","Тактика и мышление","Tactical Mind","Логика • концентрация • аналитика",5,listOf("Логика","Концентрация","Аналитика","Когнитивные тренировки","Решение задач")),
 ForgeModule("progress","Прогресс","Progress & Goals","Статистика • цели • привычки",6,listOf("Статистика обучения","Достижения","Цели SMART","Привычки","Отчёты")),
 ForgeModule("training","Тренировки и сила","Training Forge","Powerlifting • Strongman • мощность",7,listOf("Силовая база","Strongman","Взрывная сила","Периодизация","Тренировочный журнал"))
)
private val lessons=listOf(
 Lesson("k1","knowledge","Философия","Стоицизм: зона контроля","Stoizismus: Kontrollbereich","Эпиктет разделял события на зависящие от нас и не зависящие. В нашей власти находятся суждения, намерения и действия. Перед реакцией определи, что зависит от тебя.","Epiktet unterschied zwischen Dingen, die von uns abhängen, und Dingen, die nicht von uns abhängen."),
 Lesson("k2","knowledge","Психология","Стресс и адаптация","Stress und Anpassung","Стрессовая реакция включает нервные и гормональные механизмы. Кратковременная мобилизация помогает действовать; длительная нагрузка влияет на сон, внимание и восстановление.","Die Stressreaktion umfasst nervöse und hormonelle Mechanismen. Anhaltender Stress beeinflusst Schlaf, Aufmerksamkeit und Regeneration."),
 Lesson("k3","knowledge","Наука","Научный метод","Wissenschaftliche Methode","Наблюдение формирует вопрос, гипотеза даёт проверяемое объяснение, эксперимент собирает данные. Вывод должен соответствовать данным.","Beobachtung führt zu einer Frage. Eine Hypothese liefert eine überprüfbare Erklärung."),
 Lesson("k4","knowledge","Спорт и тренировки","Прогрессивная нагрузка","Progressive Belastung","Для развития силы нагрузка постепенно увеличивается через вес, повторения, подходы или качество выполнения. Прогресс оценивают по журналу тренировок.","Für Kraftentwicklung wird die Belastung schrittweise über Gewicht, Wiederholungen und Sätze erhöht."),
 Lesson("l1","language","Грамматика","Perfekt в разговорной речи","Perfekt im Alltag","Perfekt образуется с haben или sein и Partizip II: Ich habe gearbeitet. / Ich bin aufgestanden.","Das Perfekt wird mit haben oder sein und Partizip II gebildet."),
 Lesson("l2","language","Грамматика","Порядок слов: weil","Wortstellung: weil","После weil спрягаемый глагол уходит в конец придаточного: Ich bleibe zu Hause, weil ich krank bin.","Nach weil steht das konjugierte Verb am Ende des Nebensatzes."),
 Lesson("l3","language","Pflege — медицина","Профессиональная лексика Pflege","Fachwortschatz Pflege","die Körperpflege — уход за телом; die Mobilisation — мобилизация; die Übergabe — передача смены; die Vitalzeichen — жизненные показатели; die Flüssigkeitsbilanz — водный баланс.","die Körperpflege; die Mobilisation; die Übergabe; die Vitalzeichen; die Flüssigkeitsbilanz."),
 Lesson("l4","language","Диалоги и ситуации","Вежливая просьба","Höfliche Bitte","Könnten Sie mir bitte helfen? — Не могли бы Вы мне помочь? Würden Sie das bitte wiederholen? — Не могли бы Вы это повторить?","Könnten Sie mir bitte helfen? Würden Sie das bitte wiederholen?"),
 Lesson("m1","medical","Анатомия","Кожа и её функции","Haut und ihre Funktionen","Кожа состоит из эпидермиса, дермы и подкожной клетчатки. Она защищает организм, участвует в терморегуляции и чувствительности.","Die Haut besteht aus Epidermis, Dermis und Subcutis. Sie schützt den Körper und ist an Thermoregulation und Sensibilität beteiligt."),
 Lesson("m2","medical","Уход (Pflege)","Декубитус","Dekubitus","Декубитус — локальное повреждение кожи и/или подлежащих тканей вследствие давления или давления в сочетании со сдвигом. Профилактика включает оценку риска, движение и наблюдение за кожей.","Ein Dekubitus ist eine lokale Schädigung der Haut und/oder des darunterliegenden Gewebes infolge von Druck oder Druck in Kombination mit Scherkräften."),
 Lesson("m3","medical","Уход (Pflege)","Профилактика падений","Sturzprophylaxe","Оценивают предыдущие падения, походку, лекарства, зрение, обувь и окружающую среду. Меры подбирают индивидуально.","Risikofaktoren sind frühere Stürze, Gangbild, Medikamente, Sehvermögen, Schuhe und Umgebung."),
 Lesson("m4","medical","Уход (Pflege)","Профилактика тромбоза","Thromboseprophylaxe","Профилактика направлена на снижение венозного застоя. Важны ранняя мобилизация, упражнения для ног и выполнение назначенных мер.","Die Prophylaxe zielt darauf ab, venöse Stase zu verringern. Wichtig sind Mobilisation und Beinübungen."),
 Lesson("m5","medical","Физиология","Жизненные показатели","Vitalzeichen","К основным Vitalzeichen относятся Blutdruck, Puls, Atmung, Körpertemperatur и Sauerstoffsättigung.","Zu den Vitalzeichen gehören Blutdruck, Puls, Atmung, Körpertemperatur und Sauerstoffsättigung."),
 Lesson("m6","medical","Фармакология","Пути введения лекарств","Applikationswege","Лекарства могут вводиться перорально, сублингвально, ректально, ингаляционно, местно или парентерально.","Arzneimittel können oral, sublingual, rektal, inhalativ, lokal oder parenteral verabreicht werden."),
 Lesson("s1","stoic","Ежедневная практика","Утреннее намерение","Morgendliche Absicht","Определи одну главную задачу дня. Сформулируй конкретное действие, которое зависит от тебя.","Bestimme eine zentrale Aufgabe des Tages und formuliere eine konkrete Handlung."),
 Lesson("s2","stoic","Журнал размышлений","Вечерний разбор","Abendliche Rückschau","Три вопроса: что сделал хорошо? Где потерял самообладание? Что завтра сделаю иначе?","Drei Fragen: Was habe ich gut gemacht? Wo verlor ich die Selbstbeherrschung? Was mache ich morgen anders?"),
 Lesson("t1","tactical","Логика","Числовая последовательность","Zahlenfolge","2, 4, 8, 16, 32. Каждое следующее число в два раза больше предыдущего.","2, 4, 8, 16, 32. Jede Zahl ist doppelt so groß wie die vorherige."),
 Lesson("t2","tactical","Решение задач","Задача о семье","Familienrätsel","У отца четыре сына. У каждого сына одна сестра. Всего пять детей: четыре сына и одна общая сестра.","Ein Vater hat vier Söhne. Jeder Sohn hat eine Schwester. Insgesamt sind es fünf Kinder."),
 Lesson("tr1","training","Силовая база","Силовая база: день A","Kraftbasis: Tag A","Приседания 5×5; жим лёжа 5×5; тяга штанги 4×6; подтягивания 3 подхода; переноска тяжестей 4×20–30 м.","Kniebeugen 5×5; Bankdrücken 5×5; Langhantelrudern 4×6; Klimmzüge 3 Sätze; Loaded Carries 4×20–30 m."),
 Lesson("tr2","training","Strongman","Мощность и Strongman: день B","Power und Strongman: Tag B","Взрывная техника 5×2–3; становая тяга 4×3–5; жим стоя 4×5; фермерская прогулка 5×20 м.","Explosive Technik 5×2–3; Kreuzheben 4×3–5; Schulterdrücken 4×5; Farmer’s Walk 5×20 m."),
 Lesson("k5","knowledge","Питание","Белки, жиры и углеводы","Proteine, Fette und Kohlenhydrate","Белки обеспечивают аминокислоты для тканей и восстановления. Жиры участвуют в построении клеточных мембран и синтезе гормонов. Углеводы служат важным источником энергии.","Proteine liefern Aminosäuren für Gewebe und Regeneration. Fette sind Bestandteile der Zellmembranen. Kohlenhydrate sind eine wichtige Energiequelle."),
 Lesson("k6","knowledge","Питание","Энергетический баланс","Energiebilanz","Изменение массы тела в долгосрочной перспективе связано с соотношением поступающей и расходуемой энергии. Для оценки рациона учитывают динамику массы, активность и размер порций.","Die langfristige Gewichtsveränderung hängt von der Energiebilanz ab. Berücksichtigt werden Gewichtsentwicklung, Aktivität und Portionsgrößen."),
 Lesson("k7","knowledge","История и культура","Скандинавская мифология: Иггдрасиль","Nordische Mythologie: Yggdrasil","Иггдрасиль — мировое дерево в скандинавской мифологии, связывающее различные миры. Образы дерева, воронов Одина и рун стали важными символами северной культурной традиции.","Yggdrasil ist der Weltenbaum der nordischen Mythologie und verbindet verschiedene Welten."),
 Lesson("l5","language","Лексика RU ↔ DE","Тело человека","Der menschliche Körper","der Kopf — голова; die Haut — кожа; der Knochen — кость; das Gelenk — сустав; der Muskel — мышца; die Wunde — рана.","der Kopf; die Haut; der Knochen; das Gelenk; der Muskel; die Wunde."),
 Lesson("l6","language","Лексика RU ↔ DE","Жизненные показатели","Vitalzeichen","der Blutdruck — артериальное давление; der Puls — пульс; die Atmung — дыхание; die Temperatur — температура; die Sauerstoffsättigung — сатурация.","der Blutdruck, der Puls, die Atmung, die Temperatur, die Sauerstoffsättigung."),
 Lesson("l7","language","Упражнения","Perfekt: упражнение","Perfekt: Übung","Поставь глаголы в Perfekt: Ich ___ heute gearbeitet. (haben); Sie ___ früh aufgestanden. (sein). Ответ: habe; ist.","Setze ins Perfekt: Ich habe heute gearbeitet. Sie ist früh aufgestanden."),
 Lesson("l8","language","Мини-тесты","Проверка Pflege-лексики","Pflege-Wortschatz: Mini-Test","Как переводится die Übergabe? Правильный ответ: передача смены. Как переводится die Körperpflege? Уход за телом.","Was bedeutet die Übergabe? Schichtübergabe. Was bedeutet die Körperpflege? Körperpflege."),
 Lesson("m7","medical","Патология","Воспаление","Entzündung","Классические признаки воспаления: покраснение (Rubor), местное повышение температуры (Calor), отёк (Tumor), боль (Dolor) и нарушение функции (Functio laesa).","Klassische Entzündungszeichen: Rubor, Calor, Tumor, Dolor und Functio laesa."),
 Lesson("m8","medical","Заболевания","Сахарный диабет","Diabetes mellitus","При сахарном диабете нарушается регуляция уровня глюкозы в крови. В Pflege важны наблюдение за самочувствием, питание, движение и выполнение назначенного плана ухода.","Bei Diabetes mellitus ist die Regulation des Blutzuckers gestört. In der Pflege sind Beobachtung, Ernährung, Bewegung und der Pflegeplan wichtig."),
 Lesson("m9","medical","Заболевания","Обезвоживание","Dehydration","Возможные признаки недостатка жидкости: сухость слизистых, жажда, уменьшение количества мочи, слабость и спутанность сознания. Наблюдение и документирование входят в задачи Pflege.","Mögliche Zeichen eines Flüssigkeitsmangels sind trockene Schleimhäute, Durst, geringe Urinmenge, Schwäche und Verwirrtheit."),
 Lesson("m10","medical","Физиология","Сердечно-сосудистая система","Herz-Kreislauf-System","Сердце перекачивает кровь по большому и малому кругам кровообращения. Артерии несут кровь от сердца, вены возвращают её к сердцу.","Das Herz pumpt Blut durch Körper- und Lungenkreislauf. Arterien führen Blut vom Herzen weg, Venen zum Herzen zurück."),
 Lesson("s3","stoic","Теория стоицизма","Дихотомия контроля","Dichotomie der Kontrolle","Разделяй то, что зависит от тебя, и то, что не зависит. Направляй усилия на собственные решения, суждения и поступки.","Unterscheide zwischen dem, was in deiner Macht liegt, und dem, was nicht von dir abhängt."),
 Lesson("s4","stoic","Контроль эмоций","Пауза перед реакцией","Pause vor der Reaktion","Назови эмоцию, определи событие, которое её вызвало, и выбери действие вместо автоматической реакции.","Benenne das Gefühl, erkenne den Auslöser und wähle eine Handlung statt einer automatischen Reaktion."),
 Lesson("s5","stoic","Устойчивость к стрессу","Предварительное обдумывание","Premeditatio malorum","Заранее представь возможные препятствия и подготовь спокойный план действий. Цель — не тревога, а готовность.","Stelle dir mögliche Hindernisse vor und bereite einen ruhigen Handlungsplan vor."),
 Lesson("t3","tactical","Концентрация","Один фокус","Ein Fokus","Выбери одну задачу, убери отвлекающие факторы и работай заданный промежуток времени. После завершения оцени качество концентрации.","Wähle eine Aufgabe, entferne Ablenkungen und arbeite für einen festgelegten Zeitraum konzentriert."),
 Lesson("t4","tactical","Аналитика","Факт, гипотеза, вывод","Fakt, Hypothese, Schlussfolgerung","Разделяй наблюдаемые факты, возможные объяснения и выводы. Не представляй гипотезу как установленный факт.","Unterscheide beobachtbare Fakten, mögliche Erklärungen und Schlussfolgerungen."),
 Lesson("t5","tactical","Когнитивные тренировки","Обратный счёт","Rückwärts zählen","Начни со 100 и вычитай по семь: 100, 93, 86, 79… Упражнение тренирует удержание правила и концентрацию.","Beginne bei 100 und ziehe jeweils sieben ab: 100, 93, 86, 79…"),
 Lesson("tr3","training","Взрывная сила","Прыжковая подготовка","Sprungkraft","Прыжковые упражнения выполняют с акцентом на качество и скорость движения. Между интенсивными подходами оставляют достаточный отдых.","Sprungübungen werden mit Fokus auf Bewegungsqualität und Geschwindigkeit ausgeführt. Zwischen intensiven Sätzen ist ausreichend Pause wichtig."),
 Lesson("tr4","training","Периодизация","Линейное увеличение нагрузки","Lineare Periodisierung","В линейной модели сначала постепенно повышают рабочую нагрузку, затем снижают объём и повышают интенсивность в зависимости от цели цикла.","Bei der linearen Periodisierung wird die Belastung schrittweise angepasst; Volumen und Intensität verändern sich je nach Trainingsphase."),
 Lesson("tr5","training","Тренировочный журнал","Учёт прогресса","Trainingsprotokoll","Записывай упражнение, вес, подходы, повторения и субъективную тяжесть. Журнал помогает оценивать динамику и планировать следующую тренировку.","Notiere Übung, Gewicht, Sätze, Wiederholungen und subjektive Anstrengung. Das Trainingsprotokoll hilft bei der Planung.")

)
private fun getCards(c:Context):MutableList<CardData>{
 val s=c.getSharedPreferences("forge3",0).getString("cards",null) ?: return mutableListOf(CardData("Was bedeutet Körperpflege?","Уход за телом / Maßnahmen zur Erhaltung der Körperhygiene."),CardData("Was ist ein Dekubitus?","Локальное повреждение кожи и/или подлежащих тканей из-за давления."),CardData("Nenne drei Vitalzeichen.","Blutdruck, Puls, Atmung."),CardData("Was ist Primärprävention?","Предупреждение возникновения заболевания."),CardData("Was ist Sekundärprävention?","Раннее выявление заболевания."))
 return try{val a=JSONArray(s);MutableList(a.length()){i->val o=a.getJSONObject(i);CardData(o.getString("q"),o.getString("a"),o.optInt("d"),o.optInt("r"))}}catch(e:Exception){mutableListOf()}
}
private fun saveCards(c:Context,list:List<CardData>){val a=JSONArray();list.forEach{a.put(JSONObject().put("q",it.q).put("a",it.a).put("d",it.days).put("r",it.reps))};c.getSharedPreferences("forge3",0).edit().putString("cards",a.toString()).apply()}

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(8,9,11);window.navigationBarColor=android.graphics.Color.rgb(8,9,11);setContent{ForgeRoot(this)}}}

@Composable fun ForgeRoot(ctx:Context){
 var page by remember{mutableStateOf("home")};var module by remember{mutableStateOf(modules[0])};var category by remember{mutableStateOf("")};var topic by remember{mutableStateOf<Lesson?>(null)}
 var search by remember{mutableStateOf("")};var lang by remember{mutableStateOf("RU")}
 var cards by remember{mutableStateOf(getCards(ctx))};var cardPos by remember{mutableIntStateOf(0)};var flipped by remember{mutableStateOf(false)}
 var notes by remember{mutableStateOf(ctx.getSharedPreferences("forge3",0).getString("notes","") ?: "")}
 var done by remember{mutableStateOf(ctx.getSharedPreferences("forge3",0).getStringSet("done",emptySet()) ?: emptySet())}
 MaterialTheme(colorScheme=darkColorScheme(primary=Ember,secondary=Gold,background=Coal,surface=Panel,onBackground=Bone,onSurface=Bone)){
  Column(Modifier.fillMaxSize().background(Coal)){
   if(page!="home") TopBar(when(page){"module"->module.ru;"topic"->topic?.ru ?: "Материал";"memory"->"Двигатель памяти";"search"->"Поиск";"notes"->"Заметки";"profile"->"Профиль";"progress"->"Прогресс";else->"MIND FORGE"}){page=if(page=="topic")"module" else "home"}
   AnimatedContent(page,label="page",modifier=Modifier.weight(1f)){p->when(p){
    "home"->HomeForge(cards.count{it.days==0},cards.count{it.days>0},cards.size,{m->module=m;category="";page=if(m.id=="memory")"memory" else if(m.id=="progress")"progress" else "module"},{page="search"})
    "module"->ModuleForge(module,category,{category=it},{t->topic=t;page="topic"},done)
    "topic"->TopicForge(topic,done.contains(topic?.id),{topic?.id?.let { id -> done=done+id;ctx.getSharedPreferences("forge3",0).edit().putStringSet("done",done).apply() }})
    "memory"->MemoryForge(cards,cardPos,flipped,{flipped=it},{rate->if(cards.isNotEmpty()){val old=cards[cardPos.coerceIn(cards.indices)];val days=when(rate){0->0;1->1;else->if(old.days==0)2 else (old.days*2).coerceAtMost(60)};cards=cards.toMutableList().also{it[cardPos]=old.copy(days=days,reps=old.reps+1)};saveCards(ctx,cards);cardPos=(cardPos+1)%cards.size;flipped=false}},{q,a->cards=(cards+CardData(q,a)).toMutableList();saveCards(ctx,cards)})
    "search"->SearchForge(search,{search=it},{t->topic=t;page="topic"})
    "notes"->NotesForge(notes,{notes=it;ctx.getSharedPreferences("forge3",0).edit().putString("notes",notes).apply()})
    "profile"->ProfileForge(lang,{lang=it},cards.size,done.size)
    "progress"->ProgressForge(cards.size,cards.count{it.days>0},done.size)
    else->HomeForge(0,0,cards.size,{module=it;page="module"},{page="search"})
   }}
   BottomForge(page){page=it}
  }
 }
}

@Composable fun TopBar(title:String,back:()->Unit){Row(Modifier.fillMaxWidth().height(58.dp).background(Brush.verticalGradient(listOf(Color(0xFF24191A),Coal))).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=back){Icon(Icons.Default.ArrowBack,null,tint=Gold)};Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text(title.uppercase(),color=Bone,fontSize=14.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text("ᚠ ᚢ ᚦ ᚨ ᚱ ᚲ  •  MIND FORGE",color=Ember,fontSize=8.sp,letterSpacing=2.sp)};Icon(Icons.Default.MoreVert,null,tint=Gold)}}

@Composable fun HomeForge(due:Int,learned:Int,total:Int,open:(ForgeModule)->Unit,search:()->Unit){
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Box(Modifier.fillMaxWidth().height(290.dp).clip(RoundedCornerShape(18.dp)).border(1.dp,Gold,RoundedCornerShape(18.dp))){Image(painterResource(R.drawable.raven_emblem),null,Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop);Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22000000),Color(0xDD08090B)))));Column(Modifier.align(Alignment.BottomStart).padding(16.dp)){Text("ᚠ ᚢ ᚦ ᚨ ᚱ ᚲ ᚷ",color=Gold,fontSize=11.sp,letterSpacing=3.sp);Text("MIND FORGE",color=Bone,fontSize=30.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp);Text("ЗНАНИЯ • СИЛА • РАЗВИТИЕ",color=Ember,fontSize=10.sp,letterSpacing=1.5.sp);Text("WISSEN • KRAFT • DISZIPLIN",color=Gold,fontSize=9.sp)};IconButton(onClick=search,modifier=Modifier.align(Alignment.TopEnd)){Icon(Icons.Default.Search,null,tint=Gold)}}
  }
  item{Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("ВОСЕМЬ ПУТЕЙ / ACHT WEGE",color=Gold,fontSize=10.sp,letterSpacing=2.sp);Text("Арсенал развития",color=Bone,fontSize=21.sp,fontWeight=FontWeight.Bold)};Text("ᚱ ᚢ ᚾ ᛖ",color=Ember,fontSize=17.sp)}}
  items(modules.chunked(2)){row->Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){row.forEach{m->ForgeTile(m,Modifier.weight(1f)){open(m)}};if(row.size==1)Spacer(Modifier.weight(1f))}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){SmallMetric("К ПОВТОРЕНИЮ",due.toString(),Modifier.weight(1f));SmallMetric("ВЫУЧЕНО",learned.toString(),Modifier.weight(1f));SmallMetric("КАРТОЧЕК",total.toString(),Modifier.weight(1f))}}
 }
}

@Composable fun ForgeTile(m:ForgeModule,modifier:Modifier=Modifier,onClick:()->Unit){
 Column(modifier.height(175.dp).clip(RoundedCornerShape(12.dp)).background(Panel).border(1.dp,Color(0xFF67433A),RoundedCornerShape(12.dp)).clickable(onClick=onClick)){
  Box(Modifier.weight(1f).fillMaxWidth()){AtlasArt(m.art,Modifier.fillMaxSize());Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22000000),Color(0xE608090B)))));Column(Modifier.align(Alignment.BottomStart).padding(9.dp)){Text("0"+(modules.indexOf(m)+1)+"  /  ᚱᚢᚾ",color=Gold,fontSize=8.sp);Text(m.ru.uppercase(),color=Bone,fontSize=11.sp,fontWeight=FontWeight.Black,maxLines=1,overflow=TextOverflow.Ellipsis);Text(m.en,color=Gold,fontSize=8.sp,maxLines=1,overflow=TextOverflow.Ellipsis)}}
  Row(Modifier.fillMaxWidth().background(Color(0xFF101114)).padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){Text(m.desc,color=Ash,fontSize=8.sp,maxLines=1,modifier=Modifier.weight(1f));Icon(Icons.Default.ChevronRight,null,tint=Ember)}
 }
}
@Composable fun AtlasArt(index:Int,modifier:Modifier=Modifier){val atlas=androidx.compose.ui.graphics.ImageBitmap.imageResource(R.drawable.module_atlas);Canvas(modifier){drawImage(atlas,srcOffset=IntOffset((index%4)*80,(index/4)*60),srcSize=IntSize(80,60),dstSize=IntSize(size.width.toInt(),size.height.toInt()))}}

@Composable fun ModuleForge(m:ForgeModule,selected:String,onCategory:(String)->Unit,onTopic:(Lesson)->Unit,done:Set<String>){
 var cat by remember(m.id){mutableStateOf(selected)}
 val entries=lessons.filter{it.module==m.id && (cat.isBlank()||it.cat==cat)}
 LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(14.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
  item{Box(Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(15.dp)).border(1.dp,Gold,RoundedCornerShape(15.dp))){AtlasArt(m.art,Modifier.fillMaxSize());Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x22000000),Color(0xE608090B)))));Column(Modifier.align(Alignment.BottomStart).padding(14.dp)){Text(m.en,color=Gold,fontSize=10.sp,letterSpacing=1.sp);Text(m.ru.uppercase(),color=Bone,fontSize=22.sp,fontWeight=FontWeight.Black);Text(m.desc,color=Ash,fontSize=10.sp)}}
  }
  item{Text("РАЗДЕЛЫ / KAPITEL",color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)}
  item{Column(verticalArrangement=Arrangement.spacedBy(5.dp)){m.cats.forEach{c->Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(cat==c)Color(0xFF3B1B1B) else Panel).border(1.dp,if(cat==c)Ember else Iron,RoundedCornerShape(8.dp)).clickable{cat=if(cat==c)"" else c;onCategory(cat)}.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Text(c,color=Bone,fontSize=12.sp,modifier=Modifier.weight(1f));Icon(Icons.Default.ChevronRight,null,tint=Ember)}}}}
  item{Text("МАТЕРИАЛЫ / LERNINHALTE",color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)}
  if(entries.isEmpty())item{EmptyForge("Материалы этого раздела пополняются.","Weitere Inhalte folgen.")}
  items(entries){t->LessonTile(t,m.art,done.contains(t.id)){onTopic(t)}}
 }
}

@Composable fun LessonTile(t:Lesson,art:Int,done:Boolean,click:()->Unit){Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Panel).border(1.dp,Iron,RoundedCornerShape(11.dp)).clickable(onClick=click).padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)){Box(Modifier.size(68.dp).clip(RoundedCornerShape(8.dp))){AtlasArt(art,Modifier.fillMaxSize())};Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){Text(t.cat.uppercase(),color=Ember,fontSize=8.sp,letterSpacing=1.sp);Text(t.ru,color=Bone,fontSize=13.sp,fontWeight=FontWeight.Bold);Text(t.de,color=Gold,fontSize=10.sp);Text(t.body.take(75)+"…",color=Ash,fontSize=9.sp,maxLines=2)};Icon(if(done)Icons.Default.CheckCircle else Icons.Default.ChevronRight,null,tint=if(done)Gold else Ember)}}

@Composable fun TopicForge(t:Lesson?,done:Boolean,mark:()->Unit){
 if(t==null){EmptyForge("Материал не выбран","Kein Inhalt ausgewählt");return}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){
  Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp)).border(1.dp,Gold,RoundedCornerShape(14.dp))){AtlasArt(modules.first{it.id==t.module}.art,Modifier.fillMaxSize());Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xE808090B)))));Column(Modifier.align(Alignment.BottomStart).padding(14.dp)){Text(t.cat.uppercase(),color=Gold,fontSize=9.sp);Text(t.ru,color=Bone,fontSize=22.sp,fontWeight=FontWeight.Black);Text(t.de,color=Gold,fontSize=12.sp)}}
  InfoForge("МАТЕРИАЛ / LERNINHALT",t.body);InfoForge("DEUTSCH",t.bodyDe)
  Button(onClick=mark,modifier=Modifier.fillMaxWidth().height(48.dp),colors=ButtonDefaults.buttonColors(containerColor=if(done)Panel2 else Red)){Icon(Icons.Default.Check,null);Text(if(done)" ИЗУЧЕНО / ABGESCHLOSSEN" else " ОТМЕТИТЬ ИЗУЧЕННЫМ")}
 }
}
@Composable fun InfoForge(title:String,body:String){Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp)).background(Panel).border(1.dp,Iron,RoundedCornerShape(11.dp)).padding(14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text(title,color=Gold,fontSize=9.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp);Text(body,color=Bone,fontSize=14.sp,lineHeight=21.sp)}}

@Composable fun MemoryForge(cards:List<CardData>,pos:Int,flip:Boolean,onFlip:(Boolean)->Unit,rate:(Int)->Unit,add:(String,String)->Unit){
 var q by remember{mutableStateOf("")};var a by remember{mutableStateOf("")};var adding by remember{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
  Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("SPACED REPETITION",color=Gold,fontSize=9.sp,letterSpacing=1.5.sp);Text("Карточки / Karteikarten",color=Bone,fontSize=20.sp,fontWeight=FontWeight.Bold)};Text((if(cards.isEmpty())0 else pos+1).toString()+" / "+cards.size,color=Ember)}
  LinearProgressIndicator(progress={if(cards.isEmpty())0f else (pos+1).toFloat()/cards.size},modifier=Modifier.fillMaxWidth().height(5.dp),color=Ember,trackColor=Panel2)
  if(cards.isNotEmpty()){val card=cards[pos.coerceIn(cards.indices)];Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(17.dp)).background(Brush.verticalGradient(listOf(Color(0xFFF2E8D6),Color(0xFFD4C4A8)))).border(2.dp,Gold,RoundedCornerShape(17.dp)).clickable{onFlip(!flip)}.padding(20.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(15.dp)){Text(if(flip)"ANTWORT / ОТВЕТ" else "FRAGE / ВОПРОС",color=Color(0xFF7B352B),fontSize=10.sp,fontWeight=FontWeight.Bold);Text(if(flip)card.a else card.q,color=Color(0xFF201A17),fontSize=22.sp,fontWeight=FontWeight.Bold,lineHeight=29.sp);Text("Нажми на карточку",color=Color(0xFF715C4B),fontSize=10.sp)}};if(flip)Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){RateForge("НЕ ЗНАЮ",Red,Modifier.weight(1f)){rate(0)};RateForge("ЕЩЁ РАЗ",Color(0xFF8A5A27),Modifier.weight(1f)){rate(1)};RateForge("ЗНАЮ",Color(0xFF356C42),Modifier.weight(1f)){rate(2)}}else Button(onClick={onFlip(true)},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Red)){Text("ПОКАЗАТЬ ОТВЕТ / ANTWORT")}}else EmptyForge("Колода пуста","Der Stapel ist leer")
  OutlinedButton(onClick={adding=!adding},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Add,null);Text(" ДОБАВИТЬ КАРТОЧКУ")}
  if(adding){OutlinedTextField(q,{q=it},Modifier.fillMaxWidth(),label={Text("Вопрос")});OutlinedTextField(a,{a=it},Modifier.fillMaxWidth(),label={Text("Ответ")});Button(onClick={if(q.isNotBlank()&&a.isNotBlank()){add(q,a);q="";a="";adding=false}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Red)){Text("СОХРАНИТЬ")}}
 }
}
@Composable fun RateForge(label:String,color:Color,modifier:Modifier,click:()->Unit){Button(onClick=click,modifier=modifier.height(44.dp),colors=ButtonDefaults.buttonColors(containerColor=color),contentPadding=PaddingValues(2.dp)){Text(label,fontSize=9.sp,fontWeight=FontWeight.Bold)}}

@Composable fun SearchForge(value:String,onValue:(String)->Unit,select:(Lesson)->Unit){val found=lessons.filter{value.isBlank()||listOf(it.ru,it.de,it.body,it.cat).any{x->x.contains(value,true)}};Column(Modifier.fillMaxSize().padding(13.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){OutlinedTextField(value,onValue,Modifier.fillMaxWidth(),placeholder={Text("Поиск по материалам / Suche")},leadingIcon={Icon(Icons.Default.Search,null)},singleLine=true);Text("НАЙДЕНО: "+found.size,color=Gold,fontSize=10.sp);LazyColumn(verticalArrangement=Arrangement.spacedBy(7.dp)){items(found){t->LessonTile(t,modules.first{it.id==t.module}.art,false){select(t)}}}}}

@Composable fun NotesForge(value:String,change:(String)->Unit){Column(Modifier.fillMaxSize().padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("ЛИЧНЫЙ ЖУРНАЛ / NOTIZEN",color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp);OutlinedTextField(value,change,Modifier.fillMaxSize().weight(1f),placeholder={Text("Мысли, наблюдения, планы…")},colors=OutlinedTextFieldDefaults.colors(focusedTextColor=Bone,unfocusedTextColor=Bone,focusedBorderColor=Ember,unfocusedBorderColor=Iron));Text("Изменения сохраняются автоматически.",color=Ash,fontSize=10.sp)}}

@Composable fun ProfileForge(lang:String,setLang:(String)->Unit,total:Int,done:Int){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(15.dp)).border(1.dp,Gold,RoundedCornerShape(15.dp))){Image(painterResource(R.drawable.raven_emblem),null,Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop);Box(Modifier.fillMaxSize().background(Color(0x9908090B)));Text("MIND FORGE",Modifier.align(Alignment.BottomStart).padding(15.dp),color=Bone,fontSize=24.sp,fontWeight=FontWeight.Black)};InfoForge("ЯЗЫК ИНТЕРФЕЙСА / SPRACHE","Выбранный язык: "+lang);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("RU","DE").forEach{FilterChip(selected=lang==it,onClick={setLang(it)},label={Text(it)})}};InfoForge("ЛИЧНАЯ СТАТИСТИКА","Карточек: "+total+"\nИзучено материалов: "+done);InfoForge("ХРАНЕНИЕ","Данные хранятся локально на устройстве.")}}

@Composable fun ProgressForge(total:Int,learned:Int,done:Int){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),verticalArrangement=Arrangement.spacedBy(11.dp)){Box(Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(15.dp)).border(1.dp,Gold,RoundedCornerShape(15.dp))){AtlasArt(6,Modifier.fillMaxSize());Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xDD08090B)))));Text("ПРОГРЕСС / FORTSCHRITT",Modifier.align(Alignment.BottomStart).padding(15.dp),color=Bone,fontSize=20.sp,fontWeight=FontWeight.Black)};Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){SmallMetric("КАРТОЧЕК",total.toString(),Modifier.weight(1f));SmallMetric("ВЫУЧЕНО",learned.toString(),Modifier.weight(1f));SmallMetric("ТЕМЫ",done.toString(),Modifier.weight(1f))};InfoForge("ПАМЯТЬ / GEDÄCHTNIS","Освоено карточек: "+learned+" из "+total);InfoForge("ИЗУЧЕННЫЕ МАТЕРИАЛЫ","Отмечено завершёнными: "+done)}}

@Composable fun BottomForge(page:String,go:(String)->Unit){val tabs=listOf(Triple("home",Icons.Default.Home,"Главная"),Triple("search",Icons.Default.Search,"Поиск"),Triple("memory",Icons.Default.Style,"Память"),Triple("notes",Icons.Default.EditNote,"Заметки"),Triple("profile",Icons.Default.Person,"Профиль"));Row(Modifier.fillMaxWidth().background(Color(0xFF101114)).border(BorderStroke(1.dp,Iron)).navigationBarsPadding().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceEvenly){tabs.forEach{(id,ic,label)->Column(Modifier.weight(1f).clickable{go(id)}.padding(vertical=3.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(ic,null,tint=if(page==id)Ember else Ash);Text(label,color=if(page==id)Ember else Ash,fontSize=9.sp)}}}}
@Composable fun SmallMetric(label:String,value:String,modifier:Modifier=Modifier){Column(modifier.clip(RoundedCornerShape(9.dp)).background(Panel).border(1.dp,Iron,RoundedCornerShape(9.dp)).padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(label,color=Gold,fontSize=8.sp);Text(value,color=Bone,fontSize=19.sp,fontWeight=FontWeight.Bold)}}
@Composable fun EmptyForge(ru:String,de:String){Column(Modifier.fillMaxWidth().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("ᚱ ᚢ ᚾ ᛖ",color=Ember,fontSize=23.sp);Text(ru,color=Bone,fontWeight=FontWeight.Bold);Text(de,color=Ash,fontSize=10.sp)}}
