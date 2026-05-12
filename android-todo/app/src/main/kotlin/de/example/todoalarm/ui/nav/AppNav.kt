package de.example.todoalarm.ui.nav

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.example.todoalarm.ui.screens.PermissionsScreen
import de.example.todoalarm.ui.screens.TaskEditScreen
import de.example.todoalarm.ui.screens.TaskListScreen
import de.example.todoalarm.viewmodel.TaskEditViewModel
import de.example.todoalarm.viewmodel.TaskListViewModel
import de.example.todoalarm.viewmodel.ViewModelFactory

object Routes {
    const val LIST = "list"
    const val EDIT = "edit"
    const val EDIT_ARG = "taskId"
    const val PERMISSIONS = "permissions"

    fun edit(taskId: Long?): String = "$EDIT?$EDIT_ARG=${taskId ?: 0L}"
}

@Composable
fun AppNav(
    factory: ViewModelFactory,
    startDestination: String,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onContinue = {
                navController.navigate(Routes.LIST) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            })
        }
        composable(Routes.LIST) {
            val vm: TaskListViewModel = viewModel(factory = factory)
            TaskListScreen(
                vm = vm,
                onAdd = { navController.navigate(Routes.edit(null)) },
                onEdit = { id -> navController.navigate(Routes.edit(id)) },
            )
        }
        composable("${Routes.EDIT}?${Routes.EDIT_ARG}={${Routes.EDIT_ARG}}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString(Routes.EDIT_ARG)?.toLongOrNull()
            val vm: TaskEditViewModel = viewModel(factory = factory)
            TaskEditScreen(
                vm = vm,
                taskId = id?.takeIf { it > 0L },
                onDone = { navController.popBackStack() },
            )
        }
    }
}
