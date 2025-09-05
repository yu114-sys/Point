package personal.cx.point.data

import personal.cx.point.exception.PointRuntimeException
import java.util.concurrent.locks.ReentrantLock
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class downloadFileData {
    data class libraryDatabase(
        val id: Int,
        
        val name: String,
        val ip: String,
        
        val password: String,

        var files: fileData?
    )

    data class libraryJSON(
        val id: Int,

        val name: String,
        val ip: String,

        val password: String,

        var files: String
    )

    private var lock = ReentrantLock()
    private var database = mutableListOf<libraryDatabase>()
    private var num: Int = 0

    //inner function

    private fun copyLibrary(goalLibrary: libraryDatabase) : libraryDatabase {
        lock.lock()

        val tempLibrary : libraryDatabase = libraryDatabase(
            goalLibrary.id,
            goalLibrary.name,
            goalLibrary.ip,
            "",
            goalLibrary.files
        )

        lock.unlock()

        return tempLibrary
    }

    //端口

    //以下获取元素的函数均会消去password信息
    //The following functions for obtaining elements will eliminate password information
    fun libraryList() : List<libraryDatabase> {
        lock.lock()

        var returnList = mutableListOf<libraryDatabase>()
        for(i in database){
            lock.unlock()

            returnList.add(copyLibrary(i))

            lock.lock()
        }

        lock.unlock()

        return returnList.toList()
    }
    fun library(id : Int) : libraryDatabase?{
        val element = getElementById(id)
        if(element != null){
            return copyLibrary(element)
        }

        return null
    }

    //通过该函数来获取密码
    fun password(id : Int) : String?{
        lock.lock()

        val element = getElementById(id)
        var pw : String? = null
        if(element != null){
            pw = element.password
        }

        lock.unlock()

        return pw
    }
    //添加新的库
    fun add(name : String, ip : String, password : String, libraryType : Int, JSON: String){
        val data = fileData()
        data.setByJSON(JSON)
        lock.lock()
        val element = libraryDatabase(num, name, ip, password, data)
        num += 1
   	    database.add(element)
        lock.unlock()
    }
    fun delete(num: Int){
        lock.lock()
        database.forEach{ i ->
            if(i.id == num){
                i.files = null
                database.remove(i)
            }
        }
        lock.unlock()
    }
    //通过JSON添加libraryDatabase
    fun setByJSON(jsonString: String) {
        lock.lock()

        try {
            val type = object : TypeToken<List<libraryJSON>>() {}.type
            val jsonList = Gson().fromJson<List<libraryJSON>>(jsonString, type)

            database.clear()

            jsonList.forEach { jsonItem ->
                val data = fileData()
                data.setByJSON(jsonItem.files)
                add(jsonItem.name, jsonItem.ip, jsonItem.password, 0, jsonItem.files)
            }
        } catch (e: Exception) {
            throw PointRuntimeException("Failed to parse JSON: ${e.message}")
        } finally {
            lock.unlock()
        }
    }
    fun toJSON():String {
        lock.lock()

        try {
            val jsonList = database.map { dbItem ->
                libraryJSON(
                    dbItem.id,
                    dbItem.name,
                    dbItem.ip,
                    dbItem.password,
                    dbItem.files?.toJSON() ?: ""
                )
            }
            return Gson().toJson(jsonList)
        } finally {
            lock.unlock()
        }
    }

    //tool functions
    fun getElementById(id : Int): libraryDatabase? {

        lock.lock()

        database.forEach{ i ->
            if(i.id == id){
                lock.unlock()
                return i
            }
        }

        lock.unlock()

        return null
    }
    fun deleteElementByName(name: List<String>, ip: String): List<String>{
        lock.lock()

        try {
            // 筛出database中ip一致的项
            val dataName = database.filter { it.ip == ip }.map { it.name }

            // 找出name中没有而dataName有的项（需要删除的项）
            val toDelete = dataName.filter { !name.contains(it) }

            // 找出name有而dataName没有的项（需要返回的不存在项）
            val notFound = name.filter { !dataName.contains(it) }

            // 删除需要删除的项
            toDelete.forEach { deleteName ->
                val itemsToRemove = database.filter { it.ip == ip && it.name == deleteName }
                database.removeAll(itemsToRemove)
            }

            return notFound
        } finally {
            lock.unlock()
        }
    }
}