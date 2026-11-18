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
import kotlinx.coroutines.delay

data class Ex(val name:String,val group:String,val gear:String,val muscles:String,val technique:String)
val exercises=listOf(
Ex("Становая тяга","Сила","Штанга","Ягодицы, бицепс бедра, спина","Таз назад, напряги корпус, подними штангу разгибанием ног и таза."),
Ex("Присед со штангой","Сила","Штанга, стойка","Квадрицепсы, ягодицы","Присядь между ног, сохраняя корпус собранным, встань через всю стопу."),
Ex("Жим стоя","Сила","Штанга","Плечи, трицепс","Гриф у ключиц, напряги корпус и выжми над головой."),
Ex("Взятие на грудь","Взрывная сила","Штанга","Ноги, трапеции, спина","Разгони гриф ногами, резко разогни таз и быстро подсядь под гриф."),
Ex("Толчок штанги","Взрывная сила","Штанга","Ноги, плечи, трицепс","Короткий подсед, мощный толчок и фиксация над головой."),
Ex("Рывок","Взрывная сила","Штанга","Всё тело","Разгони гриф ногами, быстро уйди под него и зафиксируй над головой."),
Ex("Подтягивания","Сила","Турник","Широчайшие, бицепс","Подтяни грудь к перекладине и опустись подконтрольно."),
Ex("Жим лёжа","Сила","Скамья, штанга","Грудь, трицепс","Сведи лопатки, опусти гриф к груди и выжми вверх."),
Ex("Фермерская прогулка","Strongman","Гантели, рукояти","Хват, трапеции, корпус","Подними снаряды, выпрямись и пройди дистанцию."),
Ex("Прогулка с мешком","Strongman","Сэндбэг","Корпус, спина, ноги","Подними мешок к груди, плотно обними и пройди дистанцию."),
Ex("Подъём атлас-камня","Strongman","Атлас-камень","Спина, бёдра, хват","Обхвати камень, подними на колени, перехвати и уложи на платформу."),
Ex("Толчок бревна","Strongman","Log","Плечи, трицепс","Подними бревно на грудь, выполни подсед и толчок."),
Ex("Тяга саней","Functional","Сани, ремень","Ноги, ягодицы","Закрепи ремень, наклони корпус и двигай сани ногами."),
Ex("Толкание саней","Functional","Сани","Ноги, корпус","Возьмись за рукояти, зафиксируй корпус и толкай сани."),
Ex("Румынская тяга","Сила","Штанга, гантели","Бицепс бедра, ягодицы","Мягкие колени, таз назад, снаряд скользит вдоль ног."),
Ex("Выпады","Functional","Гантели","Квадрицепсы, ягодицы","Сделай шаг, опустись контролируемо и оттолкнись передней ногой."),
Ex("Махи гирей","Взрывная сила","Гиря","Ягодицы, бицепс бедра","Отведи гирю назад и резко разогни таз."),
Ex("Прогулка с ярмом","Strongman","Yoke","Трапеции, корпус, ноги","Подсядь под ярмо, встань и пройди дистанцию.")
)
val green=Color(0xFF356B24); val ink=Color(0xFF172019); val bg=Color(0xFFF2F5F0)
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
val p=getSharedPreferences("grif",MODE_PRIVATE)
setContent{MaterialTheme(colorScheme=lightColorScheme(primary=green,background=bg,surface=Color.White,onSurface=ink)){
var tab by remember{mutableStateOf("Главная")};var query by remember{mutableStateOf("")};var detail by remember{mutableStateOf<Ex?>(null)}
var plan by remember{mutableStateOf(p.getString("plan","Силовая база + Strongman\nДень A: присед, жим стоя, тяга в наклоне, фермерская прогулка\nДень B: становая, жим лёжа, подтягивания, толчок")!!)}
var journal by remember{mutableStateOf(p.getString("journal","")!!)};var profile by remember{mutableStateOf(p.getString("profile","")!!)}
var custom by remember{mutableStateOf(p.getStringSet("custom",emptySet())!!.toList())};var newName by remember{mutableStateOf("")};var newGear by remember{mutableStateOf("")}
var timer by remember{mutableIntStateOf(90)};var running by remember{mutableStateOf(false)}
LaunchedEffect(running,timer){if(running&&timer>0){delay(1000);timer--;if(timer==0)running=false}}
if(detail!=null){Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(18.dp)){
TextButton2("← Назад"){detail=null};Text(detail!!.group.uppercase(),color=green,fontWeight=FontWeight.Bold);Text(detail!!.name,fontSize=28.sp,fontWeight=FontWeight.Black,color=ink)
Info("ОБОРУДОВАНИЕ",detail!!.gear);Info("МЫШЦЫ",detail!!.muscles);Info("ТЕХНИКА",detail!!.technique)
}}
else Column(Modifier.fillMaxSize().background(bg)){
Column(Modifier.weight(1f).fillMaxWidth().padding(16.dp)){
when(tab){
"Главная"->{Text("STRONGMAN × ГРИФ",color=green,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text("Тренируй силу.",fontSize=30.sp,fontWeight=FontWeight.Black,color=ink);Text("Твоя силовая система: упражнения, планы и дневник.",color=ink);Spacer(Modifier.height(16.dp));Info("БИБЛИОТЕКА","18 упражнений • Сила • Взрывная мощность • Strongman");Info("ПРОГРАММА",plan.lines().first());Button(onClick={tab="Тренировка"}){Text("Открыть тренировку")};Button(onClick={tab="Библиотека"}){Text("Каталог упражнений")}}
"Библиотека"->{Text("Упражнения",fontSize=25.sp,fontWeight=FontWeight.Black,color=ink);OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),placeholder={Text("Поиск упражнения")});LazyColumn{items(exercises.filter{it.name.contains(query,true)}+custom.filter{it.contains(query,true)}.map{Ex(it,"Моё упражнение",newGear,"—","Добавлено пользователем.")}){e->Card(Modifier.fillMaxWidth().padding(vertical=4.dp).clickable{detail=e}){Column(Modifier.padding(13.dp)){Text(e.name,fontWeight=FontWeight.Bold,color=ink);Text(e.group+" · "+e.gear,color=green,fontSize=12.sp)}}}}}
"Тренировка"->{Text("Тренировочный план",fontSize=24.sp,fontWeight=FontWeight.Black,color=ink);OutlinedTextField(plan,{plan=it;p.edit().putString("plan",it).apply()},Modifier.fillMaxWidth().heightIn(min=170.dp));Spacer(Modifier.height(15.dp));Text("Таймер отдыха",fontWeight=FontWeight.Bold,color=ink);Text(String.format("%02d:%02d",timer/60,timer%60),fontSize=42.sp,fontWeight=FontWeight.Black,color=green);Row{Button(onClick={running=true}){Text("Старт")};TextButton2("Пауза"){running=false};TextButton2("90 сек"){timer=90;running=false};TextButton2("Сброс"){timer=0;running=false}}}
"Дневник"->{Text("Дневник тренировок",fontSize=24.sp,fontWeight=FontWeight.Black,color=ink);Text("Дата, упражнения, подходы, вес и повторения.",color=ink);OutlinedTextField(journal,{journal=it;p.edit().putString("journal",it).apply()},Modifier.fillMaxWidth().heightIn(min=300.dp),placeholder={Text("Присед: 5×5 × 80 кг\nЖим стоя: 4×6 × 50 кг")})}
"Профиль"->{Text("Профиль",fontSize=24.sp,fontWeight=FontWeight.Black,color=ink);OutlinedTextField(profile,{profile=it;p.edit().putString("profile",it).apply()},Modifier.fillMaxWidth(),label={Text("Рост, вес, цели, заметки")});Spacer(Modifier.height(15.dp));Text("Добавить своё упражнение",fontWeight=FontWeight.Bold,color=ink);OutlinedTextField(newName,{newName=it},Modifier.fillMaxWidth(),label={Text("Название")});OutlinedTextField(newGear,{newGear=it},Modifier.fillMaxWidth(),label={Text("Оборудование")});Button(onClick={if(newName.isNotBlank()){custom=custom+newName;p.edit().putStringSet("custom",custom.toSet()).apply();newName=""}}){Text("Сохранить")}}
}
}
NavigationBar(containerColor=Color.White){listOf("Главная","Библиотека","Тренировка","Дневник","Профиль").forEach{NavigationBarItem(selected=tab==it,onClick={tab=it},icon={Text(when(it){"Главная"->"⌂";"Библиотека"->"▤";"Тренировка"->"◆";"Дневник"->"▣";else->"●"})},label={Text(it,fontSize=9.sp)})}}
}
}}}}
@Composable fun Info(title:String,value:String){Column(Modifier.fillMaxWidth().padding(vertical=9.dp)){Text(title,color=green,fontSize=11.sp,fontWeight=FontWeight.Bold);Text(value,color=ink,fontSize=15.sp);HorizontalDivider(Modifier.padding(top=7.dp))}}
@Composable fun TextButton2(label:String,onClick:()->Unit){Text(label,color=green,modifier=Modifier.clickable{onClick()}.padding(8.dp))}
