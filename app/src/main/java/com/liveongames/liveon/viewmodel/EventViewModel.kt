// app/src/main/java/com/liveongames/liveon/ui/viewmodel/EventViewModel.kt
package com.liveongames.liveon.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liveongames.data.repository.EventRepositoryImpl
import com.liveongames.domain.model.Event
import com.liveongames.domain.model.EventChoice
import com.liveongames.domain.model.EventOutcome
import com.liveongames.domain.model.LifeLogEntry
import com.liveongames.domain.usecase.GetRandomEventsUseCase
import com.liveongames.domain.usecase.GetYearlyEventsUseCase
import com.liveongames.domain.usecase.MarkEventAsShownUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val getRandomEventsUseCase: GetRandomEventsUseCase,
    private val getYearlyEventsUseCase: GetYearlyEventsUseCase,
    private val markEventAsShownUseCase: MarkEventAsShownUseCase,
    private val eventRepository: EventRepositoryImpl
) : ViewModel() {

    private val _activeEvents = MutableStateFlow<List<Event>>(emptyList())
    val activeEvents: StateFlow<List<Event>> = _activeEvents.asStateFlow()

    private val _currentEvent = MutableStateFlow<Event?>(null)
    val currentEvent: StateFlow<Event?> = _currentEvent.asStateFlow()

    private val _selectedChoice = MutableStateFlow<EventChoice?>(null)
    val selectedChoice: StateFlow<EventChoice?> = _selectedChoice.asStateFlow()

    private val _eventOutcomes = MutableStateFlow<List<EventOutcome>>(emptyList())
    val eventOutcomes: StateFlow<List<EventOutcome>> = _eventOutcomes.asStateFlow()

    private val _lifeLogEntries = MutableStateFlow<List<LifeLogEntry>>(emptyList())
    val lifeLogEntries: StateFlow<List<LifeLogEntry>> = _lifeLogEntries.asStateFlow()

    private var currentPlayerAge = 18 // Default starting age

    init {
        Log.d("EventViewModel", "Initializing EventViewModel")
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                Log.d("EventViewModel", "Loading events via use case...")
                val events = getRandomEventsUseCase()
                Log.d("EventViewModel", "Got ${events.size} events from use case")
                val unshownEvents = events.filter { !it.isShown }
                Log.d("EventViewModel", "Filtered to ${unshownEvents.size} unshown events")
                _activeEvents.value = unshownEvents
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error loading events", e)
            }
        }
    }

    fun showEvent(event: Event) {
        Log.d("EventViewModel", "Showing event: ${event.title}")
        _currentEvent.value = event
    }

    fun hideEvent() {
        Log.d("EventViewModel", "Hiding current event")
        _currentEvent.value = null
        _selectedChoice.value = null
        _eventOutcomes.value = emptyList()
    }

    fun selectChoice(choice: EventChoice) {
        Log.d("EventViewModel", "Choice selected: ${choice.description}")
        _selectedChoice.value = choice
        _eventOutcomes.value = choice.outcomes

        // Process the choice in life log
        _currentEvent.value?.let { event ->
            choice.outcomes.forEach { outcome ->
                val lifeLogEntry = LifeLogEntry(
                    title = event.title,
                    description = outcome.description,
                    category = event.category,
                    age = currentPlayerAge,
                    statChanges = outcome.statChanges
                )
                addLifeLogEntry(lifeLogEntry)
            }
        }

        // Mark event as shown after choice is made
        viewModelScope.launch {
            _currentEvent.value?.let { event ->
                Log.d("EventViewModel", "Marking event ${event.id} as shown")
                markEventAsShownUseCase(event.id)
                // Remove from active events
                _activeEvents.value = _activeEvents.value.filter { it.id != event.id }
            }
        }
    }

    private fun addLifeLogEntry(entry: LifeLogEntry) {
        val currentEntries = _lifeLogEntries.value.toMutableList()
        currentEntries.add(0, entry) // Add to beginning for recent first
        _lifeLogEntries.value = currentEntries
    }

    fun clearSelectedChoice() {
        _selectedChoice.value = null
        _eventOutcomes.value = emptyList()
    }

    fun processYearlyEvents() {
        viewModelScope.launch {
            try {
                val yearlyEvents = getYearlyEventsUseCase()
                // Process yearly events
                _activeEvents.value = _activeEvents.value + yearlyEvents
            } catch (e: Exception) {
                Log.e("EventViewModel", "Error processing yearly events", e)
            }
        }
    }

    fun reloadEvents() {
        Log.d("EventViewModel", "Reloading events...")
        eventRepository.reloadEvents()
        loadEvents()
    }

    fun setCurrentAge(age: Int) {
        currentPlayerAge = age
    }
}