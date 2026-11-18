package com.strongman.library

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val bg=Color(0xFF100E0C)
private val panel=Color(0xFF1A1714)
private val panel2=Color(0xFF24201C)
private val accent=Color(0xFFE25B2A)
private val fg=Color(0xFFF4EFE6)
private val dim=Color(0xFFA3988C)

data class Ex(val name:String,val type:String,val gear:String,val muscles:String,val cue:String)
data class PE(val name:String,val sets:Int=3,val reps:String="5",val weight:String="",val rest:Int=120)
data class Plan(val id:Long,val name:String,val exercises:List<PE>)
data class SetRow(val reps:String="",val weight:String="",val done:Boolean=false)
data class LiveEx(val name:String,val rest:Int,val sets:List<SetRow>)
data class Live(val id:Long,val name:String,val date:String,val exercises:List<LiveEx>,val note:String="")
data class Log(val date:String,val name:String,val volume:Double,val detail:String,val note:String)

private val catalog=listOf(
 Ex("Приседания со штангой","БАЗА","Штанга, стойка","Квадрицепсы, ягодицы","Напряги корпус, контролируй глубину и дави всей стопой."),
 Ex("Фронтальные приседания","БАЗА","Штанга, стойка","Квадрицепсы, корпус","Держи локти высоко и сохраняй вертикальный корпус."),
 Ex("Становая тяга","БАЗА","Штанга","Спина, ягодицы, бицепс бедра","Зафиксируй корпус и веди гриф близко к ногам."),
 Ex("Румынская тяга","БАЗА","Штанга, гантели","Ягодицы, бицепс бедра","Отводи таз назад, сохраняя спину нейтральной."),
 Ex("Жим лёжа","БАЗА","Скамья, штанга","Грудь, трицепс","Сведи лопатки и контролируй касание груди."),
 Ex("Жим стоя","БАЗА","Штанга","Плечи, трицепс","Напряги ягодицы и пресс, выжимай строго вверх."),
 Ex("Подтягивания","ТЯГА","Турник","Широчайшие, бицепс","Поднимайся без рывка и опускайся подконтрольно."),
 Ex("Тяга штанги в наклоне","ТЯГА","Штанга","Спина, бицепс","Удерживай наклон и тяни гриф к поясу."),
 Ex("Отжимания на брусьях","ЖИМ","Брусья","Грудь, трицепс","Сохраняй контроль в нижней точке."),
 Ex("Фермерская прогулка","STRONGMAN","Фермерские рукояти, гантели","Хват, трапеции, корпус","Поднимись в стойку и пройди заданную дистанцию."),
 Ex("Прогулка с ярмом","STRONGMAN","Yoke","Трапеции, корпус, ноги","Зафиксируй корпус под ярмом и двигайся короткими шагами."),
 Ex("Толчок бревна","STRONGMAN","Log","Плечи, трицепс, ноги","Подними бревно на грудь и выполни мощный толчок."),
 Ex("Подъём атлас-камня","STRONGMAN","Атлас-камень","Спина, бёдра, хват","Обхвати камень, подними на колени и уложи на платформу."),
 Ex("Перенос сэндбэга","STRONGMAN","Мешок","Корпус, спина, ноги","Подними мешок к груди и удерживай плотный захват."),
 Ex("Толкание саней","STRONGMAN","Сани","Ноги, корпус","Наклонись в рукояти и толкай сани равномерно."),
 Ex("Взятие на грудь","МОЩНОСТЬ","Штанга","Ноги, спина, трапеции","Разгони гриф и быстро подсядь под него."),
 Ex("Рывок","МОЩНОСТЬ","Штанга","Всё тело","Разгони гриф и зафиксируй его над головой."),
 Ex("Толчок штанги","МОЩНОСТЬ","Штанга","Ноги, плечи, трицепс","Используй подсед и мощно отправь штангу вверх."),
 Ex("Махи гирей","МОЩНОСТЬ","Гиря","Ягодицы, задняя цепь","Создавай движение разгибанием таза.")
)

class MainActivity:ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  val prefs=getSharedPreferences("strongman_grif",MODE_PRIVATE)
  setContent{MaterialTheme(colorScheme=darkColorScheme(primary=accent,background=bg,surface=panel,onSurface=fg,onPrimary=bg)){MainApp(prefs)}}
 }
}

@Composable private fun MainApp(p:android.content.SharedPreferences){
 var tab by remember{mutableStateOf("Сегодня")}
 var plans by remember{mutableStateOf(loadPlans(p))}
 var logs by remember{mutableStateOf(loadLogs(p))}
 var customExercises by remember{mutableStateOf(loadCustom(p))}
 val allExercises = catalog + customExercises
 var editor by remember{mutableStateOf<Plan?>(null)}
 var live by remember{mutableStateOf(loadLive(p))}
 var detail by remember{mutableStateOf<Ex?>(null)}
 var weight by remember{mutableStateOf(p.getString("weight","102")?:"102")}
 fun savePlans(v:List<Plan>){plans=v;p.edit().putString("plans",plansJson(v)).apply()}
 fun saveLive(v:Live?){live=v;p.edit().putString("live",if(v==null)"" else liveJson(v)).apply()}
 fun saveLogs(v:List<Log>){logs=v;p.edit().putString("logs",logsJson(v)).apply()}
 fun saveCustom(v:List<Ex>){customExercises=v;p.edit().putString("custom_exercises",customJson(v)).apply()}
 fun start(plan:Plan){saveLive(Live(plan.id,plan.name,SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(Date()),plan.exercises.map{e->LiveEx(e.name,e.rest,List(e.sets.coerceIn(1,20)){SetRow(weight=e.weight)})}))}
 if(detail!=null){ExerciseInfo(detail!!){detail=null};return}
 if(editor!=null){PlanEdit(editor!!,allExercises,{editor=null},{v->savePlans(plans.filterNot{it.id==v.id}+v);editor=null;tab="План"},{id->savePlans(plans.filterNot{it.id==id});editor=null});return}
 if(live!=null){LiveScreen(live!!,{saveLive(it)},{ 
  val s=live!!
  val vol=s.exercises.sumOf{e->e.sets.filter{it.done}.sumOf{(it.weight.toDoubleOrNull()?:0.0)*(it.reps.toDoubleOrNull()?:0.0)}}
  val detailText=s.exercises.joinToString(" · "){it.name+" "+it.sets.count{z->z.done}.toString()+"/"+it.sets.size}
  saveLogs(logs+Log(SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(Date()),s.name,vol,detailText,s.note));saveLive(null);tab="Журнал"
 },{saveLive(null)});return}
 Scaffold(containerColor=bg,bottomBar={
  NavigationBar(containerColor=panel,tonalElevation=0.dp){
   listOf("Сегодня","Журнал","Цифры","План").forEach{t->NavigationBarItem(selected=tab==t,onClick={tab=t},icon={Text(when(t){"Сегодня"->"⌂";"Журнал"->"▤";"Цифры"->"↗";else->"☷"},fontSize=20.sp,color=if(tab==t)accent else dim)},label={Text(t,fontSize=11.sp)},colors=NavigationBarItemDefaults.colors(selectedIconColor=accent,selectedTextColor=accent,indicatorColor=panel2,unselectedTextColor=dim))}
  }
 }){pad->
  Column(Modifier.fillMaxSize().padding(pad)){
   Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically){
    Column(Modifier.weight(1f)){Text("STRONGMAN × ГРИФ",color=fg,fontSize=19.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp);Text("СИЛА И ВЗРЫВ · ТРЕНИРОВОЧНЫЙ ДНЕВНИК",color=dim,fontSize=9.sp,letterSpacing=1.sp)}
    Column(horizontalAlignment=Alignment.End){Text(weight+" кг",color=accent,fontSize=19.sp,fontWeight=FontWeight.Bold);Text("МАССА ТЕЛА",color=dim,fontSize=9.sp)}
   }
   when(tab){
    "Сегодня"->Home(plans,logs,weight,{weight=it;p.edit().putString("weight",it).apply()},{editor=Plan(System.currentTimeMillis(),"",emptyList());tab="План"},{start(it)})
    "План"->PlanList(plans,allExercises,{editor=Plan(System.currentTimeMillis(),"",emptyList())},{editor=it},{start(it)},{detail=it},{saveCustom(customExercises+it)})
    "Журнал"->Journal(logs)
    else->Numbers(logs,plans)
   }
  }
 }
}

@Composable private fun Home(plans:List<Plan>,logs:List<Log>,weight:String,onWeight:(String)->Unit,onCreate:()->Unit,onStart:(Plan)->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){
  Text("СЕГОДНЯ",color=accent,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
  Text("Твоя сила строится здесь.",color=fg,fontSize=28.sp,fontWeight=FontWeight.Black)
  Spacer(Modifier.height(18.dp))
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("ТРЕНИРОВОК",logs.size.toString(),Modifier.weight(1f));Metric("ОБЪЁМ",logs.sumOf{it.volume}.toLong().toString()+" кг",Modifier.weight(1f));Metric("ПЛАНОВ",plans.size.toString(),Modifier.weight(1f))}
  Spacer(Modifier.height(20.dp));Text("МАССА ТЕЛА",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)
  OutlinedTextField(weight,onWeight,Modifier.fillMaxWidth(),label={Text("Вес, кг")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),colors=fieldColors())
  Spacer(Modifier.height(22.dp));Text("СЛЕДУЮЩАЯ ТРЕНИРОВКА",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)
  if(plans.isEmpty()){CardPanel{Text("Пока нет тренировочных планов",color=fg,fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Создай собственную программу и добавь упражнения из библиотеки.",color=dim);Spacer(Modifier.height(12.dp));MainButton("СОЗДАТЬ ПЛАН",onCreate)}}else plans.forEach{plan->CardPanel{Text(plan.name,color=fg,fontSize=21.sp,fontWeight=FontWeight.Bold);Text(plan.exercises.size.toString()+" упражнений · "+plan.exercises.sumOf{it.sets}+" подходов",color=dim,fontSize=12.sp);Spacer(Modifier.height(12.dp));MainButton("НАЧАТЬ ТРЕНИРОВКУ"){onStart(plan)}}}
  Spacer(Modifier.height(16.dp));Text("ПОСЛЕДНЯЯ АКТИВНОСТЬ",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp)
  if(logs.isEmpty())Text("Здесь появятся завершённые тренировки.",color=dim,modifier=Modifier.padding(top=10.dp)) else logs.takeLast(2).reversed().forEach{l->CardPanel{Text(l.name,color=fg,fontWeight=FontWeight.Bold);Text(l.date+" · "+l.volume.toLong()+" кг",color=dim,fontSize=12.sp)}}
 }
}

@Composable private fun Metric(label:String,value:String,modifier:Modifier=Modifier){Surface(modifier,shape=RoundedCornerShape(12.dp),color=panel){Column(Modifier.padding(11.dp)){Text(label,color=dim,fontSize=9.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text(value,color=fg,fontSize=18.sp,fontWeight=FontWeight.Bold)}}}
@Composable private fun CardPanel(content:@Composable ColumnScope.()->Unit){Surface(color=panel,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth().padding(vertical=6.dp)){Column(Modifier.padding(16.dp),content=content)}}
@Composable private fun MainButton(label:String,onClick:()->Unit){Button(onClick,Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(9.dp),colors=ButtonDefaults.buttonColors(containerColor=accent,contentColor=bg)){Text(label,fontWeight=FontWeight.Black,letterSpacing=1.sp)}}
@Composable private fun fieldColors()=OutlinedTextFieldDefaults.colors(focusedBorderColor=accent,unfocusedBorderColor=panel2,focusedLabelColor=accent,cursorColor=accent,focusedTextColor=fg,unfocusedTextColor=fg)

@Composable private fun PlanList(plans:List<Plan>,library:List<Ex>,create:()->Unit,edit:(Plan)->Unit,start:(Plan)->Unit,detail:(Ex)->Unit,addCustom:(Ex)->Unit){
 var adding by remember{mutableStateOf(false)}
 var exName by remember{mutableStateOf("")}
 var exType by remember{mutableStateOf("STRONGMAN")}
 var exGear by remember{mutableStateOf("")}
 var exMuscles by remember{mutableStateOf("")}
 var exCue by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(horizontal=18.dp)){
  Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("ПЛАН",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text("Мои программы",color=fg,fontSize=26.sp,fontWeight=FontWeight.Black)};Button(create,colors=ButtonDefaults.buttonColors(containerColor=accent,contentColor=bg)){Text("+ НОВЫЙ")}}
  LazyColumn{items(plans){pl->CardPanel{Text(pl.name,color=fg,fontSize=20.sp,fontWeight=FontWeight.Bold);Text(pl.exercises.size.toString()+" упражнений · "+pl.exercises.sumOf{it.sets}+" подходов",color=dim,fontSize=12.sp);pl.exercises.take(3).forEach{Text("— "+it.name,color=dim,fontSize=12.sp)};Row{TextButton({edit(pl)}){Text("ИЗМЕНИТЬ",color=accent)};TextButton({start(pl)}){Text("НАЧАТЬ",color=accent)}}}}
   item{Spacer(Modifier.height(12.dp));Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Text("БИБЛИОТЕКА УПРАЖНЕНИЙ",color=accent,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.5.sp,modifier=Modifier.weight(1f));TextButton({adding=true}){Text("+ СВОЁ",color=accent)}};library.forEach{e->Row(Modifier.fillMaxWidth().clickable{detail(e)}.padding(vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(e.name,color=fg,fontWeight=FontWeight.SemiBold);Text(e.type+" · "+e.gear,color=dim,fontSize=11.sp)};Text("›",color=accent,fontSize=22.sp)};HorizontalDivider(color=panel2)}}
  }
 }
}

@Composable private fun PlanEdit(initial:Plan,cancel:()->Unit,save:(Plan)->Unit,delete:(Long)->Unit){
 var name by remember(initial.id){mutableStateOf(initial.name)}
 var entries by remember(initial.id){mutableStateOf(initial.exercises)}
 var choose by remember{mutableStateOf(false)}
 var query by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().background(bg).padding(16.dp)){
  Row(verticalAlignment=Alignment.CenterVertically){TextButton(cancel){Text("НАЗАД",color=dim)};Text("КОНСТРУКТОР ПЛАНА",color=accent,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));TextButton({save(initial.copy(name=name.trim(),exercises=entries))},enabled=name.isNotBlank()&&entries.isNotEmpty()){Text("СОХРАНИТЬ",color=accent)}}
  OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Название тренировочного дня")},singleLine=true,colors=fieldColors())
  Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Text("УПРАЖНЕНИЯ",color=fg,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));TextButton({choose=true}){Text("+ ДОБАВИТЬ",color=accent)}}
  LazyColumn{items(entries){e->CardPanel{Text(e.name,color=fg,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){EditField("Подходы",e.sets.toString(),Modifier.weight(1f)){v->entries=entries.map{if(it===e)it.copy(sets=(v.toIntOrNull()?:it.sets).coerceIn(1,20))else it}};EditField("Повторы",e.reps,Modifier.weight(1f)){v->entries=entries.map{if(it===e)it.copy(reps=v)else it}}};Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){EditField("Вес, кг",e.weight,Modifier.weight(1f)){v->entries=entries.map{if(it===e)it.copy(weight=v)else it}};EditField("Отдых, сек",e.rest.toString(),Modifier.weight(1f)){v->entries=entries.map{if(it===e)it.copy(rest=(v.toIntOrNull()?:it.rest).coerceIn(0,3600))else it}}};TextButton({entries=entries.filterNot{it===e}}){Text("УБРАТЬ УПРАЖНЕНИЕ",color=accent)}}}}
  if(initial.name.isNotBlank())TextButton({delete(initial.id)}){Text("УДАЛИТЬ ПЛАН",color=accent)}
 }
 if(choose)AlertDialog(onDismissRequest={choose=false},containerColor=panel,title={Text("Библиотека упражнений",color=fg)},text={Column{OutlinedTextField(query,{query=it},label={Text("Поиск")},singleLine=true,colors=fieldColors());LazyColumn(Modifier.heightIn(max=380.dp)){items(library.filter{it.name.contains(query,true)||it.type.contains(query,true)||it.gear.contains(query,true)}){e->Text(e.name+" · "+e.type,color=fg,modifier=Modifier.fillMaxWidth().clickable{entries=entries+PE(e.name);choose=false;query=""}.padding(12.dp))}}}},confirmButton={TextButton({choose=false}){Text("ЗАКРЫТЬ",color=accent)}})
}
@Composable private fun EditField(label:String,value:String,modifier:Modifier=Modifier,onValue:(String)->Unit){OutlinedTextField(value,onValue,modifier,label={Text(label,fontSize=10.sp)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),colors=fieldColors())}

@Composable private fun LiveScreen(session:Live,change:(Live)->Unit,finish:()->Unit,pause:()->Unit){
 var seconds by remember{mutableIntStateOf(0)}
 var running by remember{mutableStateOf(false)}
 LaunchedEffect(running,seconds){if(running&&seconds>0){delay(1000);seconds--;if(seconds==0)running=false}}
 Column(Modifier.fillMaxSize().background(bg).padding(horizontal=14.dp)){
  Row(verticalAlignment=Alignment.CenterVertically){TextButton(pause){Text("СВЕРНУТЬ",color=dim)};Spacer(Modifier.weight(1f));Text("ТРЕНИРОВКА",color=accent,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));TextButton(finish){Text("ГОТОВО",color=accent)}}
  Text(session.name,color=fg,fontSize=26.sp,fontWeight=FontWeight.Black);Text("НАЧАЛО · "+session.date,color=dim,fontSize=11.sp)
  if(seconds>0)CardPanel{Text("ОТДЫХ",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(String.format(Locale.getDefault(),"%02d:%02d",seconds/60,seconds%60),color=accent,fontSize=34.sp,fontWeight=FontWeight.Black);Row{TextButton({seconds+=30}){Text("+30 СЕК",color=accent)};TextButton({running=!running}){Text(if(running)"ПАУЗА" else "ПРОДОЛЖИТЬ",color=accent)};TextButton({seconds=0;running=false}){Text("СБРОС",color=dim)}}}
  LazyColumn(Modifier.weight(1f)){
   items(session.exercises){ex->
    Column(Modifier.fillMaxWidth().padding(vertical=7.dp)){
     Row(verticalAlignment=Alignment.CenterVertically){Text(ex.name,color=fg,fontSize=17.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));Text(ex.sets.count{it.done}.toString()+"/"+ex.sets.size,color=accent,fontWeight=FontWeight.Bold)}
     ex.sets.forEachIndexed{idx,s->
      Surface(color=if(s.done)Color(0xFF302019) else panel,shape=RoundedCornerShape(9.dp),modifier=Modifier.fillMaxWidth().padding(vertical=3.dp)){
       Row(Modifier.padding(7.dp),verticalAlignment=Alignment.CenterVertically){Text((idx+1).toString(),color=accent,fontWeight=FontWeight.Bold,modifier=Modifier.width(22.dp));OutlinedTextField(s.weight,{v->val list=ex.sets.toMutableList();list[idx]=s.copy(weight=v);change(session.copy(exercises=session.exercises.map{if(it===ex)it.copy(sets=list)else it}))},Modifier.weight(1f).padding(end=5.dp),label={Text("кг",fontSize=10.sp)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),colors=fieldColors());OutlinedTextField(s.reps,{v->val list=ex.sets.toMutableList();list[idx]=s.copy(reps=v);change(session.copy(exercises=session.exercises.map{if(it===ex)it.copy(sets=list)else it}))},Modifier.weight(1f).padding(end=5.dp),label={Text("повт.",fontSize=10.sp)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),colors=fieldColors());Checkbox(s.done,{v->val list=ex.sets.toMutableList();list[idx]=s.copy(done=v);change(session.copy(exercises=session.exercises.map{if(it===ex)it.copy(sets=list)else it}));if(v){seconds=ex.rest;running=seconds>0}},colors=CheckboxDefaults.colors(checkedColor=accent,uncheckedColor=dim))}
      }
     }
     Row{TextButton({change(session.copy(exercises=session.exercises.map{if(it===ex)it.copy(sets=it.sets+SetRow(weight=it.sets.lastOrNull()?.weight?:""))else it}))}){Text("+ ПОДХОД",color=accent)};TextButton({change(session.copy(exercises=session.exercises.map{if(it===ex)it.copy(sets=it.sets.dropLast(1))else it}))},enabled=ex.sets.isNotEmpty()){Text("УБРАТЬ ПОСЛЕДНИЙ",color=dim)};TextButton({seconds=ex.rest;running=seconds>0}){Text("ОТДЫХ",color=accent)}}
     HorizontalDivider(color=panel2)
    }
   }
   item{Text("ЗАМЕТКИ",color=accent,fontWeight=FontWeight.Bold);OutlinedTextField(session.note,{change(session.copy(note=it))},Modifier.fillMaxWidth().padding(vertical=8.dp),placeholder={Text("Самочувствие, техника, комментарии…")},colors=fieldColors())}
  }
  MainButton("ЗАВЕРШИТЬ И СОХРАНИТЬ",finish);Spacer(Modifier.height(8.dp))
 }
}

@Composable private fun Journal(logs:List<Log>){
 Column(Modifier.fillMaxSize().padding(18.dp)){Text("ЖУРНАЛ",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text("История тренировок",color=fg,fontSize=26.sp,fontWeight=FontWeight.Black)
 if(logs.isEmpty())Text("Завершённые тренировки появятся здесь.",color=dim,modifier=Modifier.padding(top=16.dp))
 LazyColumn{items(logs.reversed()){l->CardPanel{Text(l.name,color=fg,fontSize=19.sp,fontWeight=FontWeight.Bold);Text(l.date,color=accent,fontSize=11.sp);Text(l.detail,color=dim,fontSize=12.sp);Text("Объём · "+l.volume.toLong()+" кг",color=fg,fontWeight=FontWeight.Bold);if(l.note.isNotBlank())Text(l.note,color=dim)}}}
 }
}
@Composable private fun Numbers(logs:List<Log>,plans:List<Plan>){
 val volume=logs.sumOf{it.volume}
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)){Text("ЦИФРЫ",color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text("Твоя динамика",color=fg,fontSize=27.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(16.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Metric("ТРЕНИРОВОК",logs.size.toString(),Modifier.weight(1f));Metric("ПЛАНОВ",plans.size.toString(),Modifier.weight(1f))};CardPanel{Text("ОБЩИЙ ТОННАЖ",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(volume.toLong().toString()+" кг",color=accent,fontSize=32.sp,fontWeight=FontWeight.Black)};CardPanel{Text("СРЕДНИЙ ОБЪЁМ",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(if(logs.isEmpty())"—" else (volume/logs.size).toLong().toString()+" кг",color=fg,fontSize=24.sp,fontWeight=FontWeight.Bold)};CardPanel{Text("ПОСЛЕДНЯЯ ТРЕНИРОВКА",color=dim,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(logs.lastOrNull()?.date?:"Пока нет данных",color=fg,fontSize=18.sp)}}
}
@Composable private fun ExerciseInfo(e:Ex,back:()->Unit){Column(Modifier.fillMaxSize().background(bg).verticalScroll(rememberScrollState()).padding(20.dp)){TextButton(back){Text("← НАЗАД",color=accent)};Text(e.type,color=accent,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Text(e.name,color=fg,fontSize=30.sp,fontWeight=FontWeight.Black);Info("ИНВЕНТАРЬ",e.gear);Info("РАБОТАЮЩИЕ МЫШЦЫ",e.muscles);Info("ТЕХНИКА",e.cue)}}
@Composable private fun Info(title:String,value:String){Column(Modifier.fillMaxWidth().padding(vertical=12.dp)){Text(title,color=accent,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp);Text(value,color=fg,fontSize=17.sp);HorizontalDivider(color=panel2,modifier=Modifier.padding(top=10.dp))}}

private fun plansJson(v:List<Plan>):String{val a=JSONArray();v.forEach{p->val o=JSONObject().put("id",p.id).put("name",p.name);val x=JSONArray();p.exercises.forEach{x.put(JSONObject().put("name",it.name).put("sets",it.sets).put("reps",it.reps).put("weight",it.weight).put("rest",it.rest))};o.put("exercises",x);a.put(o)};return a.toString()}
private fun loadPlans(p:android.content.SharedPreferences):List<Plan> = try{val a=JSONArray(p.getString("plans","[]"));(0 until a.length()).map{i->val o=a.getJSONObject(i);val x=o.getJSONArray("exercises");Plan(o.getLong("id"),o.getString("name"),(0 until x.length()).map{j->val e=x.getJSONObject(j);PE(e.getString("name"),e.getInt("sets"),e.getString("reps"),e.getString("weight"),e.getInt("rest"))})}}catch(_:Exception){emptyList()}
private fun liveJson(s:Live):String{val o=JSONObject().put("id",s.id).put("name",s.name).put("date",s.date).put("note",s.note);val a=JSONArray();s.exercises.forEach{e->val x=JSONArray();e.sets.forEach{x.put(JSONObject().put("reps",it.reps).put("weight",it.weight).put("done",it.done))};a.put(JSONObject().put("name",e.name).put("rest",e.rest).put("sets",x))};o.put("exercises",a);return o.toString()}
private fun loadLive(p:android.content.SharedPreferences):Live?=try{val raw=p.getString("live","")?:"";if(raw.isBlank())null else{val o=JSONObject(raw);val a=o.getJSONArray("exercises");Live(o.getLong("id"),o.getString("name"),o.getString("date"),(0 until a.length()).map{i->val e=a.getJSONObject(i);val x=e.getJSONArray("sets");LiveEx(e.getString("name"),e.getInt("rest"),(0 until x.length()).map{j->val z=x.getJSONObject(j);SetRow(z.getString("reps"),z.getString("weight"),z.getBoolean("done"))})},o.getString("note"))}}catch(_:Exception){null}
private fun logsJson(v:List<Log>):String{val a=JSONArray();v.forEach{a.put(JSONObject().put("date",it.date).put("name",it.name).put("volume",it.volume).put("detail",it.detail).put("note",it.note))};return a.toString()}
private fun loadLogs(p:android.content.SharedPreferences):List<Log> = try{val a=JSONArray(p.getString("logs","[]"));(0 until a.length()).map{i->val o=a.getJSONObject(i);Log(o.getString("date"),o.getString("name"),o.getDouble("volume"),o.getString("detail"),o.getString("note"))}}catch(_:Exception){emptyList()}

private fun customJson(v:List<Ex>):String{val a=JSONArray();v.forEach{a.put(JSONObject().put("name",it.name).put("type",it.type).put("gear",it.gear).put("muscles",it.muscles).put("cue",it.cue))};return a.toString()}
private fun loadCustom(p:android.content.SharedPreferences):List<Ex> = try{val a=JSONArray(p.getString("custom_exercises","[]"));(0 until a.length()).map{i->val o=a.getJSONObject(i);Ex(o.getString("name"),o.getString("type"),o.getString("gear"),o.getString("muscles"),o.getString("cue"))}}catch(_:Exception){emptyList()}
