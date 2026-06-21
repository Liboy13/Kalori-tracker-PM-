package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.AppDatabase
import com.example.data.model.DailyProgress
import com.example.data.model.FoodLog
import com.example.data.model.WeightRecord
import com.example.data.remote.RetrofitClient
import com.example.data.remote.model.Content
import com.example.data.remote.model.GenerateContentRequest
import com.example.data.remote.model.Part
import com.example.data.repository.FuelTrackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String
)

class FuelTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FuelTrackRepository(AppDatabase.getDatabase(application).fuelTrackDao())
    
    // Available dates to switch in Indonesian
    val dates = listOf(
        "SELASA, 24 OKT",
        "RABU, 25 OKT",
        "KAMIS, 26 OKT"
    )
    
    private val _selectedDate = MutableStateFlow("SELASA, 24 OKT")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Database Flows
    val currentFoodLogs: StateFlow<List<FoodLog>> = _selectedDate
        .flatMapLatest { date -> repository.getFoodLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentDailyProgress: StateFlow<DailyProgress> = _selectedDate
        .flatMapLatest { date ->
            repository.getDailyProgressForDate(date).map { progress ->
                progress ?: DailyProgress(dateString = date, stepsLogged = 0, waterLogged = 0f, weight = 74.5)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyProgress(dateString = _selectedDate.value))

    val weightHistory: StateFlow<List<WeightRecord>> = repository.getAllWeightRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini AI States
    private val _systemAnalysis = MutableStateFlow<String>("Sedang memuat analisis sistem...")
    val systemAnalysis: StateFlow<String> = _systemAnalysis.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("AI", "Halo! Saya Gemini. Tanyakan apa saja tentang nutrisi Anda!")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        // Seed initial mockup data if DB is empty
        viewModelScope.launch {
            repository.getFoodLogsForDate("SELASA, 24 OKT").first().let { logs ->
                if (logs.isEmpty()) {
                    seedData()
                }
            }
            // Trigger initial AI analysis
            triggerSystemAnalysis()
        }
    }

    private suspend fun seedData() {
        // Tuesday logs
        repository.insertFoodLog(FoodLog(dateString = "SELASA, 24 OKT", mealType = "BREAKFAST", foodName = "Oatmeal dengan Blueberry", portionInfo = "1 mangkuk (350g)", calories = 320, protein = 24, carbs = 45, fat = 8))
        repository.insertFoodLog(FoodLog(dateString = "SELASA, 24 OKT", mealType = "BREAKFAST", foodName = "Kopi Hitam", portionInfo = "1 cangkir (250ml)", calories = 2, protein = 0, carbs = 1, fat = 0))
        repository.insertFoodLog(FoodLog(dateString = "SELASA, 24 OKT", mealType = "LUNCH", foodName = "Salad Ayam Panggang", portionInfo = "450g porsi sajian", calories = 480, protein = 45, carbs = 20, fat = 18))
        repository.insertFoodLog(FoodLog(dateString = "SELASA, 24 OKT", mealType = "LUNCH", foodName = "Saus Minyak Zaitun", portionInfo = "1 sdm", calories = 120, protein = 0, carbs = 0, fat = 14))
        repository.insertFoodLog(FoodLog(dateString = "SELASA, 24 OKT", mealType = "SNACKS", foodName = "Protein Bar Cokelat", portionInfo = "1 bar (60g)", calories = 210, protein = 20, carbs = 15, fat = 7))

        // Wednesday logs
        repository.insertFoodLog(FoodLog(dateString = "RABU, 25 OKT", mealType = "BREAKFAST", foodName = "Roti Alpukat & Telur", portionInfo = "2 iris sourdough", calories = 450, protein = 22, carbs = 38, fat = 15))
        repository.insertFoodLog(FoodLog(dateString = "RABU, 25 OKT", mealType = "LUNCH", foodName = "Quinoa Power Bowl", portionInfo = "1 porsi besar", calories = 620, protein = 45, carbs = 55, fat = 22))

        // Daily Progress seeds
        repository.insertDailyProgress(DailyProgress("SELASA, 24 OKT", stepsLogged = 8420, waterLogged = 1.5f, weight = 74.5))
        repository.insertDailyProgress(DailyProgress("RABU, 25 OKT", stepsLogged = 9150, waterLogged = 1.8f, weight = 74.3))
        repository.insertDailyProgress(DailyProgress("KAMIS, 26 OKT", stepsLogged = 4210, waterLogged = 0.8f, weight = 74.6))

        // Weight records seeds for linear trajectory chart
        val now = System.currentTimeMillis()
        repository.insertWeightRecord(WeightRecord(dateLabel = "01 Okt", weight = 78.7, timestamp = now - 24 * 3600 * 1000 * 24))
        repository.insertWeightRecord(WeightRecord(dateLabel = "05 Okt", weight = 77.5, timestamp = now - 24 * 3600 * 1000 * 20))
        repository.insertWeightRecord(WeightRecord(dateLabel = "10 Okt", weight = 76.8, timestamp = now - 24 * 3600 * 1000 * 15))
        repository.insertWeightRecord(WeightRecord(dateLabel = "15 Okt", weight = 76.0, timestamp = now - 24 * 3600 * 1000 * 10))
        repository.insertWeightRecord(WeightRecord(dateLabel = "20 Okt", weight = 75.2, timestamp = now - 24 * 3600 * 1000 * 5))
        repository.insertWeightRecord(WeightRecord(dateLabel = "24 Okt", weight = 74.5, timestamp = now - 24 * 3600 * 1000 * 1))
        repository.insertWeightRecord(WeightRecord(dateLabel = "25 Okt", weight = 74.3, timestamp = now))
    }

    fun selectDate(date: String) {
        if (date in dates) {
            _selectedDate.value = date
            viewModelScope.launch {
                triggerSystemAnalysis()
            }
        }
    }

    fun nextDate() {
        val currentIndex = dates.indexOf(_selectedDate.value)
        if (currentIndex < dates.lastIndex) {
            selectDate(dates[currentIndex + 1])
        }
    }

    fun previousDate() {
        val currentIndex = dates.indexOf(_selectedDate.value)
        if (currentIndex > 0) {
            selectDate(dates[currentIndex - 1])
        }
    }

    // --- CONTROLLER ACTIONS ---
    fun addFoodLog(mealType: String, name: String, portion: String, calories: Int, p: Int, c: Int, f: Int) {
        viewModelScope.launch {
            repository.insertFoodLog(
                FoodLog(
                    dateString = _selectedDate.value,
                    mealType = mealType,
                    foodName = name,
                    portionInfo = portion,
                    calories = calories,
                    protein = p,
                    carbs = c,
                    fat = f
                )
            )
            triggerSystemAnalysis()
        }
    }

    fun deleteFoodLog(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodLog(id)
            triggerSystemAnalysis()
        }
    }

    fun addWater(amount: Float) {
        viewModelScope.launch {
            val progress = currentDailyProgress.value
            val nextWater = (progress.waterLogged + amount).coerceAtLeast(0f)
            repository.insertDailyProgress(progress.copy(waterLogged = nextWater))
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            val progress = currentDailyProgress.value
            val nextSteps = (progress.stepsLogged + steps).coerceAtLeast(0)
            repository.insertDailyProgress(progress.copy(stepsLogged = nextSteps))
        }
    }

    fun addWeightRecord(weightVal: Double, label: String) {
        viewModelScope.launch {
            repository.insertWeightRecord(
                WeightRecord(
                    dateLabel = label,
                    weight = weightVal,
                    timestamp = System.currentTimeMillis()
                )
            )
            // also update current progress weight
            val progress = currentDailyProgress.value
            repository.insertDailyProgress(progress.copy(weight = weightVal))
        }
    }

    fun updateDailyWeight(weightVal: Double) {
        viewModelScope.launch {
            val progress = currentDailyProgress.value
            repository.insertDailyProgress(progress.copy(weight = weightVal))
        }
    }

    // --- GEMINI REST API INTEGRATION ---
    fun triggerSystemAnalysis() {
        viewModelScope.launch {
            _systemAnalysis.value = "Menganalisis nutrisi Anda dengan Gemini AI..."
            val logs = currentFoodLogs.value
            val progress = currentDailyProgress.value
            
            val totalCalories = logs.sumOf { it.calories }
            val totalProtein = logs.sumOf { it.protein }
            val totalCarbs = logs.sumOf { it.carbs }
            val totalFat = logs.sumOf { it.fat }

            val prompt = """
                Anda adalah asisten nutrisi berbasis AI yang cerdas untuk aplikasi FUEL_TRACK.
                Berikan ringkasan taktis singkat harian dalam Bahasa Indonesia tentang parameter nutrisi pengguna.
                Gunakan estetika Brutalist, tegas, pragmatis, efektif (tidak menggunakan perkenalan basi, langsung analisis).
                
                Data Hari Ini (${_selectedDate.value}):
                - Log Kalori Terkumpul: $totalCalories Kcal
                - Protein: ${totalProtein}g
                - Karbohidrat: ${totalCarbs}g
                - Lemak: ${totalFat}g
                - Langkah Kaki: ${progress.stepsLogged} langkah (Target: ${progress.stepsGoal})
                - Air Minum: ${progress.waterLogged}L (Target: ${progress.waterGoal})
                - Berat Badan Saat Ini: ${progress.weight} kg
                
                Tugas:
                Berikan 1 kalimat analisis sistem secara langsung, dan tambahkan 1 rekomendasi strategi instan berdaya impact tinggi (misal: 'Strategi: Jalan kaki 20 menit untuk menjaga ritme glukosa' atau 'Strategi: Tambahkan 25g protein dari dada ayam pada makan malam').
            """.trimIndent()

            val responseText = callGeminiApi(prompt)
            _systemAnalysis.value = responseText ?: "Analisis Sistem optimal saat ini."
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        
        val newMessages = _chatMessages.value + ChatMessage("USER", userText)
        _chatMessages.value = newMessages
        _isChatLoading.value = true

        viewModelScope.launch {
            val systemContext = """
                Anda adalah Gemini, pendamping cerdas nutrisi taktis Indonesia dalam aplikasi FUEL_TRACK.
                Gunakan nada bicara yang brutalist, padat pesan, informatif, tangguh, memotivasi tanpa basa-basi.
                Semua jawaban Anda harus dalam Bahasa Indonesia.
                Nutrisi hari ini: ${currentFoodLogs.value.sumOf { it.calories }} Kcal, ${currentFoodLogs.value.sumOf { it.protein }}g protein, berat body ${currentDailyProgress.value.weight}kg.
            """.trimIndent()

            // Map chat messages to Gemini's expected Content format:
            val contents = newMessages.map { msg ->
                val prefix = if (msg.sender == "USER") "User: " else "Gemini: "
                Content(parts = listOf(Part(text = prefix + msg.text)))
            }

            val request = GenerateContentRequest(
                contents = contents,
                systemInstruction = Content(parts = listOf(Part(text = systemContext)))
            )

            val apiResponse = try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    "Kunci Gemini API belum diatur di AI Studio. Silakan masukkan kunci API Anda di bawah panel Secrets."
                } else {
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: "Gagal memproses respons AI."
                }
            } catch (e: Exception) {
                "Kesalahan jaringan: ${e.localizedMessage ?: "Tidak dapat menghubungi server Gemini."}"
            }

            _chatMessages.value = _chatMessages.value + ChatMessage("AI", apiResponse)
            _isChatLoading.value = false
        }
    }

    private suspend fun callGeminiApi(prompt: String): String? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Kunci Gemini API belum diatur. Harap gunakan Secrets panel di AI Studio."
        }
        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        } catch (e: Exception) {
            "Kesalahan Analisis AI: Jalankan koneksi internet atau periksa apiKey."
        }
    }
}
