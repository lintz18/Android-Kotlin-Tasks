package net.offshoretech.garcitech.tasks.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.offshoretech.garcitech.tasks.data.Task
import net.offshoretech.garcitech.tasks.data.TaskDao
import net.offshoretech.garcitech.tasks.ui.ADD_TASK_RESULT_OK
import net.offshoretech.garcitech.tasks.ui.EDIT_TASK_RESULT_OK

class AddEditTaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    //Mismo nombre que hay en el navGraph arguments del fragment de edición para mantener los datos en casa de poner la app en 2o plano
    val task = state.get<Task>("task")

    //Recuperamos el texto mediante el savedInstance y comprobamos en caso de que sea nulo
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        //Gaurdamos en el savedInstance el valor
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("El nom no pot estar buit")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updatedTask(updatedTask)
        } else {
            val newTask = Task(taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updatedTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidMessage(text))
    }

    sealed class AddEditTaskEvent {
        data class ShowInvalidMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}