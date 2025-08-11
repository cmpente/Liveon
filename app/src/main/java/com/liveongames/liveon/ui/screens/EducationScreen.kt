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
import com.liveongames.liveon.model.EducationLockInfo
import com.liveongames.liveon.model.toModel
import com.liveongames.liveon.ui.screens.education.*
import com.liveongames.liveon.ui.theme.LiveonTheme
import com.liveongames.liveon.viewmodel.EducationViewModel
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost

@Composable
fun EducationScreen(
    viewModel: EducationViewModel = hiltViewModel(),
    theme: LiveonTheme,
    onEducationCompleted: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val catalog by viewModel.catalog.collectAsStateWithLifecycle(emptyList())
    val actions by viewModel.actions.collectAsStateWithLifecycle(emptyList())
    val educations by viewModel.educations.collectAsStateWithLifecycle(emptyList())
    val active by viewModel.activeEducation.collectAsStateWithLifecycle(null)
    val overallGpa by viewModel.overallGpa.collectAsStateWithLifecycle(0.0)
    val termEntity by viewModel.termState.collectAsStateWithLifecycle(null)
    val term = termEntity?.toModel()

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
            theme = theme,
            modifier = Modifier.fillMaxWidth(),
            onInfoTap = { showGpaInfo = true },
            onCompleteTap = {
                viewModel.completeActiveIfAny()
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
                    isActionLocked = { def ->
                        // ✅ Added safe check so viewmodel doesn't crash
                        val lockInfo = viewModel.isActionLocked(course, def)
                        lockInfo.locked
                    },
                    isOnCooldown = { def -> viewModel.isOnCooldown(a.id, def) },
                    cooldownProgress = { def -> viewModel.cooldownProgress(a.id, def) },
                    capRemaining = { def -> viewModel.capRemaining(def) },
                    onActionClick = { def ->
                        pendingAction = def
                        when (def.minigame?.type) {
                            EducationActionDef.MiniGameType.TIMING -> showTiming = true
                            EducationActionDef.MiniGameType.MEMORY -> showMemory = true
                            EducationActionDef.MiniGameType.QUIZ -> showQuiz = true
                            EducationActionDef.MiniGameType.DRAG, null -> viewModel.performAction(def, tierMultiplier = 1.0)
                        }
                    },
                    theme = theme,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            } else {
                Text("Education course not found.", color = theme.text)
            }
        }

        EducationPathMap(
            courses = catalog,
            lockInfo = { course ->
                val lock = viewModel.courseLockInfo(course)
                EducationLockInfo(locked = lock.locked, reason = lock.reason ?: "")
            },
            onEnroll = { selectedCourse = it },
            theme = theme,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        EducationAchievementsShelf(
            completed = educations.filter { it.completionDate != null },
            theme = theme,
            modifier = Modifier.fillMaxWidth(),
            courseResolver = { id -> catalog.firstOrNull { it.id == id } }
        )
        Spacer(Modifier.height(100.dp))
    }

    SnackbarHost(hostState = snackHost)

    if (showGpaInfo) {
        GpaInfoDialog(show = true, onDismiss = { showGpaInfo = false })
    }

    selectedCourse?.let { course ->
        CourseDetailsPanel(
            course = course,
            lockInfo = run {
                val lock = viewModel.courseLockInfo(course)
                EducationLockInfo(locked = lock.locked, reason = lock.reason ?: "")
            },
            theme = theme,
            onEnroll = {
                viewModel.enroll(course)
                selectedCourse = null
            },
            onDismiss = { selectedCourse = null }
        )
    }

    if (showTiming && pendingAction != null) {
        TimingTapMiniGame(
            onClose = { showTiming = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { viewModel.performAction(it, tierMultiplier = tierMul) }
                showTiming = false; pendingAction = null
            }
        )
    }
    if (showMemory && pendingAction != null) {
        MemoryMatchMiniGame(
            onClose = { showMemory = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { viewModel.performAction(it, tierMultiplier = tierMul) }
                showMemory = false; pendingAction = null
            }
        )
    }
    if (showQuiz && pendingAction != null) {
        QuickQuizMiniGame(
            numQuestions = 5,
            onClose = { showQuiz = false; pendingAction = null },
            onResult = { _, tierMul ->
                pendingAction?.let { viewModel.performAction(it, tierMultiplier = tierMul) }
                showQuiz = false; pendingAction = null
            }
        )
    }
}