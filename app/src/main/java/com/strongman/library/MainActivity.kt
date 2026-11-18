package com.strongman.library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Exercise(val ru:String,val de:String,val en:String,val type:String,val gear:String,val muscles:String,val how:String)
val catalog = listOf(
Exercise("Становая тяга","Kreuzheben","Deadlift","Сила","Штанга","Ягодицы, бицепс бедра, спина","Таз назад, напряги корпус, встань разгибанием ног и таза."),
Exercise("Присед со штангой","Kniebeuge","Back Squat","Сила","Штанга, стойка","Квадрицепсы, ягодицы, корпус","Зафиксируй гриф, присядь между ног, встань через всю стопу."),
Exercise("Жим стоя","Schulterdrücken","Overhead Press","Сила","Штанга","Плечи, трицепс, корпус","Гриф на ключицах, напряги корпус, выжми над головой."),
Exercise("Взятие на грудь","Umsetzen","Power Clean","Взрывная сила","Штанга","Ноги, трапеции, спина","Разгони гриф ногами, резко разогни таз, быстро подсядь под гриф."),
Exercise("Толчок штанги","Stoßen","Jerk","Взрывная сила","Штанга","Ноги, плечи, трицепс","Короткий подсед, мощный толчок и фиксация штанги над головой."),
Exercise("Рывок","Reißen","Snatch","Взрывная сила","Штанга","Всё тело, плечевой пояс","Разгони гриф ногами, быстро уйди под него и зафиксируй над головой."),
Exercise("Тяга в наклоне","Langhantelrudern","Barbell Row","Сила","Штанга","Широчайшие, ромбовидные, бицепс","Наклони корпус, зафиксируй спину, тяни гриф к поясу."),
Exercise("Подтягивания","Klimmzüge","Pull-Up","Сила","Турник","Широчайшие, бицепс","Опусти плечи, подтяни грудь к перекладине, опустись подконтрольно."),
Exercise("Отжимания на брусьях","Dips","Dip","Сила","Брусья","Грудь, трицепс, плечи","Опустись сгибанием локтей и выжми себя вверх."),
Exercise("Фермерская прогулка","Bauerngehen","Farmer's Walk","Strongman","Гантели, рукояти","Хват, трапеции, корпус, ноги","Подними снаряды, выпрямись и пройди дистанцию уверенными шагами."),
Exercise("Прогулка с осью","Achsen tragen","Axle Carry","Strongman","Ось","Хват, трапеции, корпус","Возьми ось, зафиксируй корпус и пройди заданную дистанцию."),
Exercise("Прогулка с мешком","Sandsack tragen","Sandbag Carry","Strongman","Сэндбэг","Корпус, спина, ноги","Подними мешок к груди, плотно обними и пройди дистанцию."),
Exercise("Подъём атлас-камня","Atlasstein heben","Atlas Stone Lift","Strongman","Атлас-камень","Спина, бёдра, бицепс","Обхвати камень, подними на колени, перехвати и уложи на платформу."),
Exercise("Толчок бревна","Baumstamm-Drücken","Log Press","Strongman","Log","Плечи, трицепс, корпус","Подними бревно на грудь, выполни подсед и толчок."),
Exercise("Тяга саней","Schlittenziehen","Sled Drag","Functional","Сани, ремень","Квадрицепсы, ягодицы, икры","Закрепи ремень, наклони корпус и двигай сани ногами."),
Exercise("Толкание саней","Schlittenschieben","Sled Push","Functional","Сани","Ноги, ягодицы, корпус","Возьмись за рукояти, зафиксируй корпус и толкай сани."),
Exercise("Тяга трэп-грифа","Trap-Bar-Kreuzheben","Trap Bar Deadlift","Сила","Trap Bar","Ноги, ягодицы, спина","Встань по центру, напряги корпус и подними снаряд."),
Exercise("Румынская тяга","Rumänisches Kreuzheben","Romanian Deadlift","Сила","Штанга, гантели","Бицепс бедра, ягодицы","Мягкие колени, таз назад, снаряд скользит вдоль ног."),
Exercise("Выпады","Ausfallschritte","Lunges","Functional","Гантели, штанга","Квадрицепсы, ягодицы","Сделай шаг, опустись контролируемо и оттолкнись передней ногой."),
Exercise("Жим лёжа","Bankdrücken","Bench Press","Сила","Скамья, штанга","Грудь, трицепс, плечи","Сведи лопатки, опусти гриф к груди и выжми вверх."),
Exercise("Жим гантелей","Kurzhantel-Bankdrücken","Dumbbell Press","Сила","Гантели, скамья","Грудь, трицепс","Установи гантели у груди, выжми вверх и опусти подконтрольно."),
Exercise("Махи гирей","Kettlebell Swing","Kettlebell Swing","Взрывная сила","Гиря","Ягодицы, бицепс бедра","Отведи гирю назад и резко разогни таз."),
Exercise("Подъём гири к груди","Kettlebell High Pull","Kettlebell High Pull","Взрывная сила","Гиря","Трапеции, плечи, ягодицы","Начни как мах, разогни таз и направь гирю вверх локтем."),
Exercise("Подъём на тумбу","Step-up","Step-up","Functional","Тумба, гантели","Квадрицепсы, ягодицы","Поставь всю стопу на тумбу, поднимись и спустись под контролем."),
Exercise("Гиперэкстензия","Hyperextension","Back Extension","Functional","Римский стул","Разгибатели спины, ягодицы","Опусти корпус и подними до прямой линии тела."),
Exercise("Шраги","Schulterheben","Shrug","Сила","Гантели, штанга","Трапеции, хват","Подними плечи вверх и опусти подконтрольно."),
Exercise("Подъём на носки","Wadenheben","Calf Raise","Functional","Тренажёр, гантели","Икроножные","Опусти пятки, поднимись на носки и сделай паузу."),
Exercise("Удержание дисков","Hantelscheiben halten","Plate Pinch Hold","Strongman","Диски","Пальцы, предплечья, хват","Сожми гладкие стороны дисков и удерживай заданное время."),
Exercise("Прогулка с ярмом","Joch tragen","Yoke Walk","Strongman","Yoke","Трапеции, корпус, ноги","Подсядь под ярмо, встань и пройди дистанцию."),
Exercise("Переноска на плече","Schultertragen","Shoulder Carry","Strongman","Мешок, бревно","Корпус, трапеции, ноги","Подними снаряд на плечо, зафиксируй и пройди дистанцию.")
)

val bg = Color(0xFF101311)
val lime = Color(0xFFC4F05A)
val panel = Color(0xFF1C241F)

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val prefs = getSharedPreferences("strongman", MODE_PRIVATE)
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(primary=lime, background=bg, surface=panel, onBackground=Color.White, onSurface=Color.White)) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Все") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var favorites by remember { mutableStateOf(prefs.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()) }
    var selected by remember { mutableStateOf<Exercise?>(null) }
    val categories = listOf("Все","Сила","Взрывная сила","Strongman","Functional")
    val filtered = catalog.filter { e ->
     (category=="Все" || e.type==category) &&
     (!favoritesOnly || favorites.contains(e.ru)) &&
     listOf(e.ru,e.de,e.en,e.gear,e.muscles,e.type).any { it.contains(query,true) }
    }
    if (selected != null) {
     val e = selected!!
     Column(Modifier.fillMaxSize().background(bg).statusBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
      TextButton(onClick={selected=null}) { Text("← Назад",color=lime) }
      Text(e.type.uppercase(),color=lime,fontSize=12.sp,fontWeight=FontWeight.Bold)
      Text(e.ru,fontSize=28.sp,fontWeight=FontWeight.Black)
      Text(e.de + " · " + e.en,color=Color.LightGray)
      Spacer(Modifier.height(18.dp))
      Detail("ОБОРУДОВАНИЕ",e.gear)
      Detail("РАБОТАЮЩИЕ МЫШЦЫ",e.muscles)
      Detail("ТЕХНИКА ВЫПОЛНЕНИЯ",e.how)
      TextButton(onClick={
       favorites = if(favorites.contains(e.ru)) favorites-e.ru else favorites+e.ru
       prefs.edit().putStringSet("favorites",favorites).apply()
      }) { Text(if(favorites.contains(e.ru)) "★ В избранном" else "☆ В избранное",color=lime) }
     }
    } else {
     Column(Modifier.fillMaxSize().background(bg).statusBarsPadding()) {
      Column(Modifier.padding(18.dp)) {
       Text("STRONGMAN",color=lime,fontSize=12.sp,fontWeight=FontWeight.Bold,letterSpacing=3.sp)
       Text("EXERCISE LIBRARY",fontSize=25.sp,fontWeight=FontWeight.Black)
       Spacer(Modifier.height(14.dp))
       OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Поиск RU · DE · EN")},singleLine=true)
       Spacer(Modifier.height(8.dp))
       Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
        categories.forEach { c -> FilterChip(selected=category==c,onClick={category=c},label={Text(c)}) }
       }
       Row(verticalAlignment=Alignment.CenterVertically) {
        Text(filtered.size.toString()+" УПРАЖНЕНИЙ",modifier=Modifier.weight(1f),color=Color.LightGray,fontSize=11.sp)
        FilterChip(selected=favoritesOnly,onClick={favoritesOnly=!favoritesOnly},label={Text("Избранное")})
       }
      }
      LazyColumn(contentPadding=PaddingValues(horizontal=16.dp,vertical=4.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
       items(filtered) { e ->
        Row(Modifier.fillMaxWidth().background(panel,RoundedCornerShape(14.dp)).clickable { selected=e }.padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
         Column(Modifier.weight(1f)) {
          Text(e.ru,fontWeight=FontWeight.Bold,fontSize=16.sp)
          Text(e.de,color=lime,fontSize=13.sp)
          Text(e.en,color=Color.LightGray,fontSize=11.sp)
          Text(e.type+" · "+e.gear,color=Color.LightGray,fontSize=10.sp)
         }
         Text(if(favorites.contains(e.ru)) "★" else "›",color=lime,fontSize=22.sp)
        }
       }
      }
     }
    }
   }
  }
 }
}

@Composable fun Detail(title:String,body:String) {
 Column(Modifier.fillMaxWidth().padding(vertical=10.dp)) {
  Text(title,color=lime,fontSize=11.sp,fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(5.dp))
  Text(body,fontSize=15.sp,lineHeight=22.sp)
  Spacer(Modifier.height(8.dp))
  HorizontalDivider(color=Color.DarkGray)
 }
}
