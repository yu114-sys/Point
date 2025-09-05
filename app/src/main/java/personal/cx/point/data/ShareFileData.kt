package personal.cx.point.data

import java.io.File
import java.util.concurrent.locks.ReentrantLock
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class shareFileData {
    data class fileDatabase(
        val id : Int,
        val password: String?,

        val name : String,
        val path : String,
        val files : fileData
    )
    data class fileJSON(
        val name : String,
        val path : String,
        val files : String
    )
    data class saveJSON(
        val password: String?,

        val name : String,
        val path : String,
    )
    private var database : MutableList<fileDatabase> = mutableListOf<fileDatabase>()
    private var num : Int = 0
    private var lock = ReentrantLock()
    private var JSON:String = ""
    private var JSONChange: Boolean = false


    fun add(path : String, name: String, password: String? = null) : Int{
        val newDatabase : fileDatabase

        if(File(path).exists()){
            val file = fileData()
            file.setByFile(File(path))
            lock.lock()
            newDatabase = fileDatabase(num, password, name, path, file)
            num+=1
            database.add(newDatabase)
            JSONChange = true
            lock.unlock()

            return newDatabase.id
        }
        else{
            return 0
        }
    }
    fun remove(id : Int){
        lock.lock()
        database.forEach{ i ->
            if(i.id == id){
                database.remove(i)
            }
        }
        lock.unlock()
    }
    fun setByJSON(JSONString: String){
        lock.lock()

        try {
            val type = object : TypeToken<List<saveJSON>>() {}.type
            val jsonList = Gson().fromJson<List<saveJSON>>(JSONString, type)

            // 清空当前数据库
            database.clear()
            num = 0

            // 重新添加所有项目
            jsonList.forEach { jsonItem ->
                if(File(jsonItem.path).exists()){
                    val file = fileData()
                    file.setByFile(File(jsonItem.path))
                    val newItem = fileDatabase(num, jsonItem.password, jsonItem.name, jsonItem.path, file)
                    database.add(newItem)
                    num += 1
                }
            }

            JSONChange = true
        } finally {
            lock.unlock()
        }
    }
    fun toFileJSON(): String{
        lock.lock()

        try {
            // 如果JSON没有改变且JSON不为空，直接返回缓存的JSON
            if(!JSONChange && JSON.isNotEmpty()){
                return JSON
            }

            // 创建fileJSON列表
            val jsonList = database.map { dbItem ->
                fileJSON(
                    dbItem.name,
                    dbItem.path,
                    dbItem.files.toJSON() ?: ""
                )
            }

            // 转换为JSON字符串
            JSON = Gson().toJson(jsonList)
            JSONChange = false

            return JSON
        } finally {
            lock.unlock()
        }
    }
    fun toSaveJSON(): String{
        lock.lock()

        try {
            // 创建saveJSON列表
            val jsonList = database.map { dbItem ->
                saveJSON(
                    dbItem.password,
                    dbItem.name,
                    dbItem.path
                )
            }

            // 转换为JSON字符串并返回
            return Gson().toJson(jsonList)
        } finally {
            lock.unlock()
        }
    }
}