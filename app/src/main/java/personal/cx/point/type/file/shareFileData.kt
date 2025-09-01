package personal.cx.point.type.file

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.math.BigInteger
import java.time.Instant

class shareFileData {
    data class fileDatabase(
        val id : Int,
        val password: String?,

        val name : String,
        val path : String,
        val files : fileData
    )
    private var database : MutableList<fileDatabase> = mutableListOf<fileDatabase>()

    private var num : Int = 0

    fun add(path : String, name: String, password: String? = null) : Int{
        val newDatabase : fileDatabase
        val file = File(path)

        if(file.exists()){
            val files = loadFile(path)
            newDatabase = fileDatabase(
                num,
                password,
                name,
                path,
                files)

            num+=1
            database.add(newDatabase)

            return newDatabase.id
        }
        else{
            return 0
        }
    }
    fun remove(id : Int){

    }
    fun loadFile(path: String): fileData{
        val file = File(path)
        val sonFiles : MutableList<fileData> = mutableListOf<fileData>()
        if(file.isDirectory == true){
            file.listFiles()?.forEach { file ->
                sonFiles.add(loadFile(file.path))
            }
            return fileData(file.name, file.path, true, 0, sonFiles)
        }
        else{
            return fileData(file.name, file.path, false, file.length(), null)
        }
    }
}