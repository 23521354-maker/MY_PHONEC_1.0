package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiBuildRecommendation(
    val cpu: String = "",
    val gpu: String = "",
    val motherboard: String = "",
    val ram: String = "",
    val psu: String = "",
    val reason: String = ""
)

class PCBuilderViewModel(
    private val repository: FirebaseRepository,
    private val aiRepository: BuildAiRepository = BuildAiRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(PCBuildState())
    val uiState: StateFlow<PCBuildState> = _uiState.asStateFlow()

    private val _cpus = MutableStateFlow<List<CPU>>(emptyList())
    val cpus: StateFlow<List<CPU>> = _cpus.asStateFlow()

    private val _gpus = MutableStateFlow<List<GPU>>(emptyList())
    val gpus: StateFlow<List<GPU>> = _gpus.asStateFlow()

    private val _motherboards = MutableStateFlow<List<Motherboard>>(emptyList())
    val motherboards: StateFlow<List<Motherboard>> = _motherboards.asStateFlow()

    private val _rams = MutableStateFlow<List<RAM>>(emptyList())
    val rams: StateFlow<List<RAM>> = _rams.asStateFlow()

    private val _psus = MutableStateFlow<List<PSU>>(emptyList())
    val psus: StateFlow<List<PSU>> = _psus.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiRecommendation = MutableStateFlow<AiBuildRecommendation?>(null)
    val aiRecommendation: StateFlow<AiBuildRecommendation?> = _aiRecommendation.asStateFlow()

    // Chat states
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I'm your PC Build Assistant. How can I help you today?", false)
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    init {
        loadComponents()
    }

    private fun loadComponents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                _cpus.value = repository.getCPUs()
                _gpus.value = repository.getGPUs()
                _motherboards.value = repository.getMotherboards()
                _rams.value = repository.getRAM()
                _psus.value = repository.getPSUs()
                
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Unable to load components") }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _isChatLoading.value) return

        val userMessage = ChatMessage(text, true)
        _chatMessages.update { it + userMessage }
        
        viewModelScope.launch {
            _isChatLoading.value = true
            val result = aiRepository.chatWithAi(_chatMessages.value.dropLast(1), text)
            
            result.onSuccess { response ->
                _chatMessages.update { it + ChatMessage(response, false) }
            }.onFailure { exception ->
                val errorMessage = exception.message ?: "Connection Error"
                _chatMessages.update { it + ChatMessage(errorMessage, false) }
            }

            _isChatLoading.value = false
        }
    }

    fun suggestBuildWithAi(
        budget: String,
        usage: String,
        resolution: String,
        brandPreference: String,
        futureUpgrade: String
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiRecommendation.value = null
            
            val prompt = buildAiPrompt(budget, usage, resolution, brandPreference, futureUpgrade)
            val result = aiRepository.suggestBuild(prompt)
            
            result.onSuccess { response ->
                try {
                    // Xử lý chuỗi JSON từ phản hồi của AI
                    val jsonStart = response.indexOf("{")
                    val jsonEnd = response.lastIndexOf("}")
                    if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                        val cleanedJson = response.substring(jsonStart, jsonEnd + 1)
                        val recommendation = Gson().fromJson(cleanedJson, AiBuildRecommendation::class.java)
                        _aiRecommendation.value = recommendation
                        autoFillFromRecommendation(recommendation)
                    } else {
                        _uiState.update { it.copy(error = "Định dạng phản hồi AI không hợp lệ.") }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Lỗi xử lý dữ liệu AI.") }
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(error = exception.message ?: "Lỗi kết nối AI.") }
            }

            _isAiLoading.value = false
        }
    }

    private fun autoFillFromRecommendation(rec: AiBuildRecommendation) {
        val cpu = _cpus.value.find { it.name.contains(rec.cpu, ignoreCase = true) || rec.cpu.contains(it.name, ignoreCase = true) }
        val gpu = _gpus.value.find { it.name.contains(rec.gpu, ignoreCase = true) || rec.gpu.contains(it.name, ignoreCase = true) }
        val mobo = _motherboards.value.find { it.name.contains(rec.motherboard, ignoreCase = true) || rec.motherboard.contains(it.name, ignoreCase = true) }
        val ram = _rams.value.find { it.name.contains(rec.ram, ignoreCase = true) || rec.ram.contains(it.name, ignoreCase = true) }
        val psu = _psus.value.find { it.name.contains(rec.psu, ignoreCase = true) || rec.psu.contains(it.name, ignoreCase = true) }

        _uiState.update { state ->
            state.copy(
                cpu = cpu ?: state.cpu,
                gpu = gpu ?: state.gpu,
                motherboard = mobo ?: state.motherboard,
                ram = ram ?: state.ram,
                psu = psu ?: state.psu
            )
        }
        runCompatibilityEngine()
    }

    private fun buildAiPrompt(
        budget: String,
        usage: String,
        resolution: String,
        brandPreference: String,
        futureUpgrade: String
    ): String {
        return """
            You are a professional PC builder. Use ONLY the parts listed below from the provided Firestore database.
            
            User Requirements:
            - Budget: ${'$'}$budget
            - Usage: $usage
            - Resolution: $resolution
            - Brand Preference: $brandPreference
            - Future Upgrade: $futureUpgrade
            
            Available CPUs:
            ${_cpus.value.joinToString("\n") { "- ${it.name} (Socket: ${it.socket}, Score: ${it.score})" }}
            
            Available GPUs:
            ${_gpus.value.joinToString("\n") { "- ${it.name} (Score: ${it.score})" }}
            
            Available Motherboards:
            ${_motherboards.value.joinToString("\n") { "- ${it.name} (Socket: ${it.socket}, RAM: ${it.ramType})" }}
            
            Available RAM:
            ${_rams.value.joinToString("\n") { "- ${it.name} (Type: ${it.type})" }}
            
            Available PSU:
            ${_psus.value.joinToString("\n") { "- ${it.name} (Wattage: ${it.watt}W)" }}
            
            Return ONLY a valid JSON object in this format (no markdown code blocks):
            {
              "cpu": "Name of CPU",
              "gpu": "Name of GPU",
              "motherboard": "Name of Motherboard",
              "ram": "Name of RAM",
              "psu": "Name of PSU",
              "reason": "Short explanation of why this build was chosen"
            }
        """.trimIndent()
    }

    fun selectCPU(cpu: CPU) {
        _uiState.update { it.copy(cpu = cpu) }
        runCompatibilityEngine()
    }

    fun selectGPU(gpu: GPU) {
        _uiState.update { it.copy(gpu = gpu) }
        runCompatibilityEngine()
    }

    fun selectMotherboard(motherboard: Motherboard) {
        _uiState.update { it.copy(motherboard = motherboard) }
        runCompatibilityEngine()
    }

    fun selectRAM(ram: RAM) {
        _uiState.update { it.copy(ram = ram) }
        runCompatibilityEngine()
    }

    fun selectPSU(psu: PSU) {
        _uiState.update { it.copy(psu = psu) }
        runCompatibilityEngine()
    }

    private fun runCompatibilityEngine() {
        val s = _uiState.value
        
        val socketRes = when {
            s.cpu == null || s.motherboard == null -> CompatibilityResult(true, "Select CPU & Motherboard")
            s.cpu.socket == s.motherboard.socket -> CompatibilityResult(true, "Compatible", CompatibilityResult.Status.OK)
            else -> CompatibilityResult(false, "Socket mismatch", CompatibilityResult.Status.ERROR)
        }

        val ramRes = when {
            s.motherboard == null || s.ram == null -> CompatibilityResult(true, "Select RAM & Motherboard")
            s.motherboard.ramType == s.ram.type -> CompatibilityResult(true, "RAM Supported", CompatibilityResult.Status.OK)
            else -> CompatibilityResult(false, "Wrong RAM Type", CompatibilityResult.Status.ERROR)
        }

        val requiredPower = (s.cpu?.tdp ?: 0) + (s.gpu?.tdp ?: 0) + 100
        val psuRes = when {
            s.psu == null -> CompatibilityResult(true, "Select Power Supply")
            s.psu.watt >= requiredPower -> {
                if (s.psu.watt - requiredPower < 50) 
                    CompatibilityResult(true, "Near Limit", CompatibilityResult.Status.WARNING)
                else 
                    CompatibilityResult(true, "Power Stable", CompatibilityResult.Status.OK)
            }
            else -> CompatibilityResult(false, "Insufficient PSU", CompatibilityResult.Status.ERROR)
        }

        val allSelected = s.cpu != null && s.gpu != null && s.motherboard != null && s.ram != null && s.psu != null
        val hasError = socketRes.status == CompatibilityResult.Status.ERROR || 
                       ramRes.status == CompatibilityResult.Status.ERROR || 
                       psuRes.status == CompatibilityResult.Status.ERROR

        val (finalScore, stability) = when {
            hasError -> 0 to "INCOMPATIBLE"
            !allSelected -> 0 to "INCOMPLETE"
            else -> 100 to "COMPATIBLE"
        }

        _uiState.update {
            it.copy(
                socketCompatible = socketRes,
                ramCompatible = ramRes,
                psuStatus = psuRes,
                requiredPower = requiredPower,
                buildScore = finalScore,
                buildStability = stability,
                totalTdp = requiredPower - 100
            )
        }
    }
}
