package com.example.counterapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import kotlinx.coroutines.launch

// === ДАННЫЕ ===
data class Group(val groupId: Int, val groupName: String)
data class Counter(
    val counterId: Int,
    val counterName: String,
    val value: Int,
    val group: Group?
)

// === API ===
interface Api {
    @GET("api/counters")
    suspend fun getCounters(): List<Counter>

    @POST("api/counters/{id}/inc")
    suspend fun inc(@Path("id") id: Int, @Query("value") value: Int): Counter
}

// === ОСНОВНОЙ ЭКРАН ===
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

// === НАВИГАЦИЯ ===
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val api = retrofit.create(Api::class.java)

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CounterListScreen(navController = navController, api = api)
        }
        composable("counter/{counterId}") { backStackEntry ->
            val counterId = backStackEntry.arguments?.getString("counterId")?.toIntOrNull()
            CounterDetailScreen(navController = navController, api = api, initialCounterId = counterId)
        }
    }
}

// === ЭКРАН СПИСКА СЧЁТЧИКОВ ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterListScreen(navController: NavController, api: Api) {
    var counters by remember { mutableStateOf(listOf<Counter>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        counters = api.getCounters()
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF00A86B))) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Счётчики", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Text("+", fontSize = 24.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Text("⚙", fontSize = 24.sp)
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(counters) { counter ->
                    CounterListItem(
                        counter = counter,
                        onClick = {
                            navController.navigate("counter/${counter.counterId}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CounterListItem(counter: Counter, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF26A69A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                counter.counterName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Text(
                counter.value.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}

// === ЭКРАН СЧЁТЧИКА ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CounterDetailScreen(navController: NavController, api: Api, initialCounterId: Int?) {
    var counters by remember { mutableStateOf(listOf<Counter>()) }
    var selectedCounter by remember { mutableStateOf<Counter?>(null) }
    var inputValue by remember { mutableStateOf("5") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        counters = api.getCounters()
        selectedCounter = counters.find { it.counterId == initialCounterId } ?: counters.firstOrNull()
    }

    fun refresh() {
        scope.launch {
            counters = api.getCounters()
            selectedCounter = counters.find { it.counterId == selectedCounter?.counterId }
        }
    }

    fun changeValue(delta: Int) {
        selectedCounter?.let { counter ->
            scope.launch {
                api.inc(counter.counterId, delta)
                refresh()
            }
        }
    }

    if (selectedCounter == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF00A86B))) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(selectedCounter!!.counterName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Text("←", fontSize = 24.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Text("⚙", fontSize = 24.sp)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFF00A86B))
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(40.dp))

                Text(
                    selectedCounter!!.value.toString(),
                    fontSize = 80.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(50.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { text ->
                        if (text.isEmpty() || text.all { it.isDigit() }) {
                            inputValue = text
                        }
                    },
                    label = { Text("Значение", color = Color.White) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(Modifier.height(24.dp))

                // Кнопки + и -
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 1
                            changeValue(-value)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("-", fontSize = 32.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 1
                            changeValue(value)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("+", fontSize = 32.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(60.dp))

                // Большая кнопка ↑ (оставляем)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { changeValue(1) },
                        modifier = Modifier.size(100.dp)
                    ) {
                        Text("+1", color = Color(0xFF00A86B), fontSize = 60.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}