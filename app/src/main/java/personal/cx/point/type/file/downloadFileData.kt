package personal.cx.point.type.file

import personal.cx.point.exception.PointRuntimeException
import personal.cx.point.type.file.downloadFileData.libraryDatabase
import java.util.concurrent.locks.ReentrantLock

class downloadFileData {
    data class libraryDatabase(
        val id: Int,
        val key: String,
        val password: String,

        val name: String,
        val ip: String,

        var webState: Int,
        var libraryState: Int,

        var files: fileData?
    )

    private var lock = ReentrantLock()
    private var database = mutableListOf<libraryDatabase>()

    //inner function

    private fun copyLibrary(goalLibrary: libraryDatabase) : libraryDatabase{
        lock.lock()

        val tempLibrary : libraryDatabase = libraryDatabase(
            goalLibrary.id,
            "",
            "",
            goalLibrary.name,
            goalLibrary.ip,
            goalLibrary.webState,
            goalLibrary.libraryState,
            goalLibrary.files
        )

        lock.unlock()

        return tempLibrary
    }

    //ports

    //以下获取元素的函数均会消去key与password信息
    //The following functions for obtaining elements will eliminate key and password information
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
        val element = findById(id)
        if(element != null){
            return copyLibrary(element)
        }

        return null
    }

    fun size(): Int {
        lock.lock()

        val size = database.size

        lock.unlock()

        return size
    }
    fun password(id : Int) : String?{
        lock.lock()

        val element = findById(id)
        var pw : String? = null
        if(element != null){
            pw = element.password
        }

        lock.unlock()

        return pw
    }
    fun key(id : Int) : String?{
        lock.lock()

        val element = findById(id)
        var key : String? = null
        if(element != null){
            key = element.key
        }

        lock.unlock()

        return key
    }

    fun setByLibrary(elements : List<libraryDatabase>){
        var ifExit = 0
        var element : libraryDatabase? = null

        lock.lock()

        for(i in elements){
            for(j in database){
                if(i.ip == j.ip && i.name == j.name){
                    ifExit = 1
                    element = j
                }
            }
            if(ifExit == 1){
                if(element != null){
                    element.webState = 3
                }
                else{
                    throw PointRuntimeException("Error: type/file/shareFileData - class downloadFileData - fun reSet 逻辑错误")
                }
            }
            else{
                database.add(i)
            }
            ifExit = 0
        }

        lock.unlock()
    }
    fun setByString(data : String, ip : String, key: String, password: String){
        //TODO
    }
    fun setFiles(id : Int, fileData: fileData){
        val element = findById(id)

        lock.lock()

        if(element != null){
            element.files = fileData
        }

        lock.unlock()
    }
    //basic data manage
    fun setWebState(id: Int, num: Int) {
        val element = findById(id)

        lock.lock()

        if(element != null){
            element.webState = num
        }

        lock.unlock()
    }
    fun setLibraryState(id: Int, num: Int) {
        val element = findById(id)

        lock.lock()

        if(element != null){
            element.libraryState = num
        }

        lock.unlock()
    }
    fun change(data: List<libraryDatabase>) {
        lock.lock()

        database = data.toMutableList()

        lock.unlock()
    }

    //tool functions
    fun findById(id : Int): libraryDatabase? {

        lock.lock()

        for(i in database){
            if(i.id == id){
                lock.unlock()
                return i
            }
        }

        lock.unlock()

        return null
    }
    fun findByName(name: String, area: Int = 0): List<libraryDatabase> {
        var listReturn = mutableListOf<libraryDatabase>()

        lock.lock()

        for(i in database){
            if(i.name == name){
                listReturn.add(i)
            }
        }

        lock.unlock()

        return listReturn.toList()
    }
}
