package net.offshoretech.garcitech.tasks.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.offshoretech.garcitech.di.AppModule
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase: RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @AppModule.ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback(){

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            //Operaciones DB
            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Hacer algo de deporte", false, false))
                dao.insert(Task("Subir el salario a Joel", true, false))
                dao.insert(Task("Cantar el Mani gay", false, true))
                dao.insert(Task("Eruptar 100 veces al día", true, true))
                dao.insert(Task("Task", false, false))
//                for (i in 1..100) {
//                    dao.insert(Task("Task", false, false))
//                }
            }



        }
    }
}