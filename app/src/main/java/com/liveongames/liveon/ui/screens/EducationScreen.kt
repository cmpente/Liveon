// app/src/main/java/com/liveongames/liveon/ui/screens/EducationScreen.kt
package com.liveongames.liveon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liveongames.liveon.R
import com.liveongames.liveon.model.EducationActionDef
import com.liveongames.liveon.model.EducationCourse
import com.liveongames.liveon.ui.screens.education.ActiveEducationActionBar
import com.liveongames.liveon.ui.screens.education.CourseDetailsPanel
import com.liveongames.liveon.ui.screens.education.EducationAchievementsShelf
import com.liveongames.liveon.ui.screens.education.EducationPathMap
import com.liveongames.liveon.ui.screens.education.EducationProfileCard
import com.liveongames.liveon.ui.screens.education.GpaInfoDialog
import com.liveongames.liveon.ui.screens.minigame.MemoryMatchMiniGame
import com.liveongames.liveon.ui.screens.minigame.QuickQuizMiniGame
import com.liveongames.liveon.ui.screens.minigame.TimingTapMiniGame
import com.liveongames.liveon.viewmodel.EducationViewModel
import com.liveongames.liveon.viewmodel.SettingsViewModel

@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onEducationCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val vm = viewModel

    val catalog by vm.catalog.collectAsStateWithLifecycle(emptyList())
    val actions by vm.actions.collectAsStateWithLifecycle(emptyList())
    val educations by vm.educations.collectAsStateWithLifecycle(emptyList())
    val active by vm.activeEducation.collectAsStateWithLifecycle(null)
    val overallGpa by vm.overallGpa.collectAsStateWithLifecycle(0.0)
    val term by vm.termState.collectAsStateWithLifecycle(null)

    val bg = colorResource(id = R.color.slate_950)
    val snackHost = remember { SnackbarHostState() }

    var showGpaInfo by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<EducationCourse?>(null) }

    var pendingAction by remember { mutableStateOf<EducationActionDef?>(null) }
    var showTiming by remember { mutableStateOf(false) }
    var showMemory by remember { mutableStateOf(false) }
    var showQuiz by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        EducationProfileCard(
            overallGpa = overallGpa,
            activeEducation = active,
            termState = term,
            modifier = Modifier.fillMaxWidth(),
            onInfoTap = { showGpaInfo = true },
            onCompleteTap = {
                vm.completeActiveIfAny()
                onEducationCompleted()
            }
        )
        Spacer(Modifier.height(12.dp))

        active?.let { a ->
            val course = catalog.firstOrNull { it.id == a.id }
            if (course != null) {
                ActiveEducationActionBar(
                    actions = actions,
                    course = course,
                    isActionLocked = { def -> vm.isActionLocked(course, def) },
                    isOnCooldown = { def -> vm.isOnCooldown(a.id, def) },
                    cooldownProgress = { def -> vm.cooldownProgress(a.id, def) },
                    capRemaining = { _ -> -1 },
                    onActionClick = { def ->
                        pendingAction = def
                        when (def.minigame?.type) {
                            EducationActionDef.MiniGameType.TIMING -> showTiming = true
                            EducationActionDef.MiniGameType.MEMORY -> showMemory = true
                            EducationActionDef.MiniGameType.QUIZ -> showQuiz = true
                            EducationActionDef.MiniGameType.DRAG, null -> vm.performAction(def, tierMultiplier = 1.0)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        EducationPathMap(
            courses = catalog,
            lockInfo = { vm.courseLockInfo(it) },
            onEnroll = { selectedCourse = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        EducationAchievementsShelf(
            completed = educations.filter { it.completionDate != null },
            modifier = Modifier.fillMaxWidth(),
            courseResolver = { id -> catalog.firstOrNull { it.id == id } }
        )
        Spacer(Modifier.height(100.dp))
    }

    SnackbarHost(hostState = snackHost)
    GpaInfoDialog(show = showGpaInfo, onDismiss = { showGpaInfo = false })

    selectedCourse?.let { course ->
        CourseDetailsPanel(
            course = course,
            lockInfo = vm.courseLockInfo(course),
            onEnroll = {
                vm.enroll(course)
                selectedCourse = null
            },
            onDismiss = { selectedCourse = null }
        )
    }

    if (showTiming && pendingAction != null) {
        TimingTapMiniGame(
            onClose = { showTiming = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { vm.performAction(it, tierMultiplier = tierMul) }
                showTiming = false; pendingAction = null
            }
        )
    }
    if (showMemory && pendingAction != null) {
        MemoryMatchMiniGame(
            onClose = { showMemory = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { vm.performAction(it, tierMultiplier = tierMul) }
                showMemory = false; pendingAction = null
            }
        )
    }
    if (showQuiz && pendingAction != null) {
        QuickQuizMiniGame(
            numQuestions = 5,
            onClose = { showQuiz = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { vm.performAction(it, tierMultiplier = tierMul) }
                showQuiz = false; pendingAction = null
            }
        )
    }
}