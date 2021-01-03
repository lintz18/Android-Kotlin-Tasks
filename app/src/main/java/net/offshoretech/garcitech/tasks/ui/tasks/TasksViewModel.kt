package net.offshoretech.garcitech.tasks.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.offshoretech.garcitech.tasks.data.PreferencesManager
import net.offshoretech.garcitech.tasks.data.SortOrder
import net.offshoretech.garcitech.tasks.data.Task
import net.offshoretech.garcitech.tasks.data.TaskDao
import net.offshoretech.garcitech.tasks.ui.ADD_TASK_RESULT_OK
import net.offshoretech.garcitech.tasks.ui.EDIT_TASK_RESULT_OK

class TasksViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
): ViewModel() {

//    val searchQuery = MutableStateFlow("")
    val searchQuery = state.getLiveData("searchQuery", "")

//    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
//    val hideCompleted = MutableStateFlow(false)

    val preferenceFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferenceFlow
//        sortOrder,
//        hideCompleted
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (searchQuery, filterPreferences) ->
        taskDao.getTasks(searchQuery, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun onTaskSelected(task: Task) = viewModelScope.launch{
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskCheckedChange(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int){
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmatedMessage("Tasca afegida")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmatedMessage("Tasca actualitzada")
        }
    }

    private fun showTaskSavedConfirmatedMessage(text: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String): TaskEvent()
        object NavigateToDeleteAllCompletedScreen: TaskEvent()
    }
}

