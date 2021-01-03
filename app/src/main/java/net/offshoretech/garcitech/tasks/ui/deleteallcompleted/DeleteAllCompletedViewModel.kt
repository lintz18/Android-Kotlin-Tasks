package net.offshoretech.garcitech.tasks.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.offshoretech.garcitech.di.AppModule
import net.offshoretech.garcitech.tasks.data.TaskDao

class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @AppModule.ApplicationScope private val applicationScope: CoroutineScope
    ): ViewModel() {

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }
}