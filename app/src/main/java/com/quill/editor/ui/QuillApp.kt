package com.quill.editor.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quill.editor.data.repository.QuillSettings
import com.quill.editor.data.repository.ThemeMode
import com.quill.editor.ui.drawer.QuillDrawer
import com.quill.editor.ui.editor.EditorScreen
import com.quill.editor.ui.editor.EditorViewModel
import com.quill.editor.ui.settings.SettingsScreen
import com.quill.editor.ui.theme.QuillTheme
import com.quill.editor.ui.version.DiffViewerScreen
import com.quill.editor.ui.version.VersionHistoryScreen
import kotlinx.coroutines.launch

private object Routes {
    const val EDITOR = "editor"
    const val SETTINGS = "settings"
    fun versions(fileId: Long) = "versions/$fileId"
    fun diff(fileId: Long, vOld: Int, vNew: Int) = "diff/$fileId/$vOld/$vNew"
}

@Composable
fun QuillApp() {
    val container = appContainer()
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle(initialValue = QuillSettings())

    val darkTheme = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Keep the status-/nav-bar icon contrast in sync with the app theme.
    val view = LocalView.current
    LaunchedEffect(darkTheme) {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    QuillTheme(darkTheme = darkTheme, dynamicColor = settings.dynamicColor) {
        val navController = rememberNavController()
        val editorViewModel: EditorViewModel = viewModel(factory = EditorViewModel.factory(container))
        val editorState by editorViewModel.uiState.collectAsStateWithLifecycle()
        val files by container.fileRepository.observeFiles().collectAsStateWithLifecycle(initialValue = emptyList())
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        fun goToEditor() {
            navController.popBackStack(Routes.EDITOR, inclusive = false)
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                QuillDrawer(
                    files = files,
                    currentFileId = editorState.fileId,
                    onNewFile = {
                        scope.launch { drawerState.close() }
                        editorViewModel.newFile()
                        goToEditor()
                    },
                    onOpenFile = { id ->
                        scope.launch { drawerState.close() }
                        editorViewModel.openFile(id)
                        goToEditor()
                    },
                    onDeleteFile = { file -> scope.launch { container.fileRepository.deleteFile(file) } },
                    onVersions = {
                        scope.launch { drawerState.close() }
                        editorState.fileId?.let { navController.navigate(Routes.versions(it)) }
                    },
                    onSettings = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.SETTINGS)
                    },
                )
            },
        ) {
            NavHost(navController = navController, startDestination = Routes.EDITOR) {
                composable(Routes.EDITOR) {
                    EditorScreen(
                        viewModel = editorViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateVersions = { id -> navController.navigate(Routes.versions(id)) },
                        onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                        darkTheme = darkTheme,
                    )
                }

                composable(
                    route = "versions/{fileId}",
                    arguments = listOf(navArgument("fileId") { type = NavType.LongType }),
                ) { backStackEntry ->
                    val fileId = backStackEntry.arguments?.getLong("fileId") ?: return@composable
                    VersionHistoryScreen(
                        fileId = fileId,
                        onBack = { navController.popBackStack() },
                        onViewDiff = { vOld, vNew -> navController.navigate(Routes.diff(fileId, vOld, vNew)) },
                        onRestore = { versionNumber ->
                            editorViewModel.restoreVersion(fileId, versionNumber)
                            navController.popBackStack(Routes.EDITOR, inclusive = false)
                        },
                    )
                }

                composable(
                    route = "diff/{fileId}/{vOld}/{vNew}",
                    arguments = listOf(
                        navArgument("fileId") { type = NavType.LongType },
                        navArgument("vOld") { type = NavType.IntType },
                        navArgument("vNew") { type = NavType.IntType },
                    ),
                ) { backStackEntry ->
                    val args = backStackEntry.arguments ?: return@composable
                    val fileId = args.getLong("fileId")
                    val vOld = args.getInt("vOld")
                    val vNew = args.getInt("vNew")
                    DiffViewerScreen(
                        fileId = fileId,
                        versionOld = vOld,
                        versionNew = vNew,
                        onBack = { navController.popBackStack() },
                        onRestore = { versionNumber ->
                            editorViewModel.restoreVersion(fileId, versionNumber)
                            navController.popBackStack(Routes.EDITOR, inclusive = false)
                        },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
