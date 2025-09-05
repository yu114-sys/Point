package personal.cx.point.data

import personal.cx.point.exception.PointRuntimeException
import java.io.File
import java.sql.Time
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
//fileData 管理文件树目录
//添加：1.内部添加（接口函数中的addByFile）：通过文件或文件夹添加；一次添加；再次运行会遍历与更新
//     2.网络添加（接口函数中的addByJSON）：通过文本字段添加；当文件多时，多次添加，通过字段识别
//工具函数：1.toPath：移动到输入的path对应的目录，如果没有就创建目录
//        2.addByFile：递归获取文件获取文件的相关信息
//        3.createJSON：递归创建各个文件的JSON
//接口函数：1.setByFile：需要相同的根目录，在root初始化后会更新文件列表
//        2.setByJSON：需要相同的根目录，支持多次添加
//        3.toJSON：输出JSON，在JSON为null或JSONChange=true时都会重新生成JSON文件，最后返回JSON的String
//        4.check：检查文件，返回信息完整性，如果文件信息被改变则设置JSONChange=true
//        5.path：返回对应的files的realPath，或对应path的realPath
//        6.search：搜索文件名
//JSON格式：（即JSONMain,包括了多个JSONfiles）
//{
//"type" : "files"//JSON标识
//"num" : "2",  //num的值为接下来files中项的数量
//"files" :
//    { "0" : { "name" : file1.name,
//              "path" : file1.path,
//              "folder" : file1.folder,
//              "size" : file1.size,
//             },
//      "1" : { "name" : file2.name,
//              "path" : file2.path,
//              "folder" : file2.folder,
//              "size" : file2.size,
//             }
//     }
//}

class fileData(){
    data class files (
        val name: String,      //储存文件/文件夹名
        var path: String?,     //path为相对位置，realPath(在使用文件或文件夹初始化时）+ path才是绝对位置（作为公共数据保护用户信息）
        val folder: Boolean,    //储存是否为文件夹
        val size: Long,        //储存文件大小，如果是文件夹则为0
        var file: List<files>?  //当该节点为文件夹时（folder==true）可以为file添加子节点，即该文件夹下目录中的东西
    )

    data class JSONMain(
        val type : String,
        val num : Int,
        val files : List<JSONfiles>
    )

    data class JSONfiles(
        val name : String,
        val path: String,
        val folder: Boolean,
        val size: Long
    )

    //构造

    private var root : files? = null
    private var realPath : String? = null
    private var JSON : String? = null
    private var JSONChange : Boolean = false
    private var multipleFiles : Boolean = false
    private var changeTime : Time? = null

    //工具

    //toPath：移动到输入的path对应的目录，如果没有就创建目录
    fun toPath(path: String) : files?{
        if (root == null) return null

        // 标准化路径分隔符
        val normalizedPath = path.replace("/", "\\")
        val folders = normalizedPath.split("\\").filter { it.isNotEmpty() }

        // 如果路径为空，直接返回根节点
        if (folders.isEmpty()) return root

        // 检查第一个文件夹是否与根节点名称匹配
        if (folders[0] != root!!.name) return null

        var currentNode: files? = root

        // 从第二个文件夹开始遍历路径
        for (i in 1 until folders.size) {
            val folderName = folders[i]
            var found = false

            // 在当前节点的子节点中查找
            currentNode?.file?.forEach { child ->
                if (child.name == folderName) {
                    currentNode = child
                    found = true
                    return@forEach // 跳出内部循环
                }
            }

            // 如果没找到，创建新的文件夹节点
            if (!found) {
                val newFolder = files(
                    name = folderName,
                    path = "${currentNode?.path ?: ""}\\$folderName",
                    folder = true,
                    size = 0L,
                    file = mutableListOf()
                )

                // 将新文件夹添加到当前节点的子节点列表中
                val currentChildren = currentNode?.file?.toMutableList() ?: mutableListOf()
                currentChildren.add(newFolder)
                currentNode?.file = currentChildren

                currentNode = newFolder
            }
        }

        return currentNode
    }

    fun addByFile(fileN: File) : files{
        val isFolder = fileN.isDirectory
        val fileSize = if (isFolder) 0L else fileN.length()

        val children = if (isFolder) {
            fileN.listFiles()?.map { addByFile(it) } ?: emptyList()
        } else {
            null
        }

        return files(
            name = fileN.name,
            path = fileN.absolutePath.removePrefix(realPath ?: ""),
            folder = isFolder,
            size = fileSize,
            file = children
        )
    }
    fun createJSON(node: files): List<JSONfiles> {
        val result = mutableListOf<JSONfiles>()

        // 添加当前节点
        result.add(JSONfiles(
            name = node.name,
            path = node.path ?: "",
            folder = node.folder,
            size = node.size
        ))

        // 递归添加子节点
        node.file?.forEach { child ->
            result.addAll(createJSON(child))
        }

        return result
    }

    fun checkFiles(node: files): Boolean{
        var changed = false

        // 获取节点对应的实际文件
        val realFile = File(path(node))

        // 检查文件是否存在
        if (!realFile.exists()) {
            // 文件不存在，需要删除节点
            return true
        }

        // 检查修改时间
        if (changeTime != null && realFile.lastModified() > changeTime!!.time) {
            // 文件已被修改，需要更新信息
            if (node.folder != realFile.isDirectory) {
                // 文件类型发生变化，需要重新加载
                return true
            }

            if (!node.folder) {
                // 更新文件大小
                if (node.size != realFile.length()) {
                    return true
                }
            }
        }

        // 如果是文件夹，检查子节点
        if (node.folder) {
            // 获取实际文件夹中的文件列表
            val realFiles = realFile.listFiles()?.map { it.name }?.toSet() ?: emptySet()

            // 获取当前节点的子节点名称列表
            val nodeFiles = node.file?.map { it.name }?.toSet() ?: emptySet()

            // 检查是否有新增文件
            val newFiles = realFiles - nodeFiles
            if (newFiles.isNotEmpty()) {
                changed = true
                // 添加新增文件
                val mutableList = node.file?.toMutableList() ?: mutableListOf()
                newFiles.forEach { fileName ->
                    val newFile = File(realFile, fileName)
                    mutableList.add(addByFile(newFile))
                }
                node.file = mutableList
            }

            // 检查是否有删除的文件
            val deletedFiles = nodeFiles - realFiles
            if (deletedFiles.isNotEmpty()) {
                changed = true
                // 删除不存在的文件
                val mutableList = node.file?.toMutableList() ?: mutableListOf()
                mutableList.removeAll { it.name in deletedFiles }
                node.file = mutableList
            }

            // 递归检查子节点
            node.file?.forEach { child ->
                if (checkFiles(child)) {
                    changed = true
                }
            }
        }

        return changed
    }
    fun deleteNode(node: files){
        node.file?.forEach { child ->
            if(child.folder == true){
                deleteNode(child)
            }
        }

        // TODO 释放内存？
        node.file = null
    }

    //接口

    //setByFile：通过输入的fileN与addByFile创建文件树，如果root为空使用addByFile创建后保存到root节点，否则运行check()
    fun setByFile(fileN: File){
        if (root == null) {
            realPath = fileN.parent
            root = addByFile(fileN)
        } else {
            check()
        }
        changeTime = Time(System.currentTimeMillis())
        JSONChange = true
    }
    fun setByJSON(input: String){
        val gson = Gson()
        val type = object : TypeToken<JSONMain>() {}.type
        val jsonMain = gson.fromJson<JSONMain>(input, type)

        if (jsonMain.type != "files") {
            throw PointRuntimeException("Invalid JSON type")
        }

        // 清空当前文件树
        root = null
        realPath = null

        // 重建文件树
        jsonMain.files.forEach { jsonFile ->
            val filePath = (realPath ?: "") + jsonFile.path
            val file = File(filePath)

            if (root == null && jsonFile.path.isEmpty()) {
                // 这是根节点
                realPath = file.parent
                root = files(
                    name = jsonFile.name,
                    path = jsonFile.path,
                    folder = jsonFile.folder,
                    size = jsonFile.size,
                    file = mutableListOf()
                )
            } else if (root != null) {
                // 添加到现有文件树
                val parentPath = file.parent?.removePrefix(realPath ?: "") ?: ""
                val parentNode = toPath(parentPath)

                if (parentNode != null && parentNode.folder) {
                    val newFile = files(
                        name = jsonFile.name,
                        path = jsonFile.path,
                        folder = jsonFile.folder,
                        size = jsonFile.size,
                        file = if (jsonFile.folder) mutableListOf() else null
                    )

                    if (parentNode.file == null) {
                        parentNode.file = mutableListOf()
                    }

                    val mutableList = parentNode.file!!.toMutableList()
                    mutableList.add(newFile)
                    parentNode.file = mutableList
                }
            }
        }

        //TODO check()使用新线程加载
        check()
    }
    fun toJSON() : String?{
        if (root == null) return null

        if (JSON == null || JSONChange) {
            val jsonFiles = createJSON(root!!)
            val jsonMain = JSONMain(
                type = "files",
                num = jsonFiles.size,
                files = jsonFiles
            )

            val gson = Gson()
            JSON = gson.toJson(jsonMain)
            JSONChange = false
        }

        return JSON
    }
    fun check(): Boolean {
        if (root == null) return false

        val changed = checkFiles(root!!)
        if (changed) {
            JSONChange = true
            changeTime = Time(System.currentTimeMillis())
        }
        return changed
    }
    fun path(input: files): String?{
        if(realPath == ""){
            return null
        }
        return (realPath ?: "") + (input.path ?: "")
    }
    fun search(node: files, name: String): files?{
        if(node.file != null){
            for(i in node.file!!){
                if(i.name == name){
                    return i
                }
                // 递归搜索子目录
                if(i.folder){
                    val result = search(i, name)
                    if(result != null){
                        return result
                    }
                }
            }
        }

        return null
    }
    fun getRoot(): files? = root
}