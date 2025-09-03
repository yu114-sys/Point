package personal.cx.point.data.file

import personal.cx.point.exception.PointRuntimeException
import java.io.File

class fileData(root_name : String){
    data class files (
        val name : String,
        var path : String?,
        val folder: Boolean,
        val size : Long,
        var file: List<files>?
    )
    data class fileString (
        val path: String,
        val str: String
    )

    

    //构造

    init {
    	    var root = files(root_name, "", false, 0, null)
        var realPath = ""
    }

    //ports

    fun addNode(name: String, path: String, folder: Boolean, size: Long, file: List<files>? = null) : files?{
        val faFolder = toPath(path)
        val fileN = newFiles(name, path, folder, size, null)
        if(faFolder != null){
            if(addFile(faFolder, fileN) != null){
                return fileN
            }
        }
        return null
    }

    fun addByFile(fileN: File, node: files? = null){
        var element : files? = null
        if(node == null){
            realPath = fileN.absolutePath
            root = newFiles(fileN, "\\")
            element = root
        }
        else{
            addFile(node, newFiles(fileN, getPath(node)))
        }
        if()
    }
    fun addByPath(path: String, node: files){
        //TODO 把node的files添加至path所在的folder为true的节点的的file子项
    }

    fun toString(path : String, deepth : Int) : String{
    
    }
    fun setByString(str : String, node: files? = null){

    }

    //data manage

    fun setFiles(node: files, elements: List<files>) : files?{
        if(node.folder == true){
            node.file = elements
            setPath(node)
            return node
        }
        return null
    }
    fun addFile(node: files, element: files) : files?{
        if(node.folder == true){
            if(node.file != null){
                val tempList = node.file!!.toMutableList()
                tempList.add(element)
                node.file = tempList.toList()
            }
            else{
                node.file = listOf<files>(element)
            }
            if(setPathOnly(node, element) != null){
                return node
            }
            else{
                throw PointRuntimeException("Error: type/file/fileData - class fileData - fun addFile 逻辑错误")
            }
        }
        return null
    }
    fun newFiles(name: String, path: String?, folder: Boolean, size: Long, file: List<files>? = null) : files{
        return files(name, path, folder, size, file)
    }
    fun newFiles(fileN: File, path: String?, file: List<files>? = null) : files{
        if(fileN.isDirectory == true){
            return newFiles(fileN.name, path, true, 0, file)
        }
        else{
            return newFiles(fileN.name, path, false, fileN.length(), file)
        }
    }
    fun setPath(node: files) : files?{
        if(node.file != null){
            val newPath = getPath(node)
            for(i in node.file!!){
                i.path = newPath
            }
            return node
        }
        return null
    }
    fun setPathOnly(node: files, element: files) : files?{
        if(nodeSearch(node, element.name) != element){
            val newPath = getPath(node)
            element.path = newPath

            return node
        }
        return null
    }

    //tools

    fun nodeSearch(node: files, name: String): files?{
        if(node.file != null){
            for(i in node.file!!){
                if(i.name == name){
                    return i
                }
            }
        }

        return null
    }
    fun pathSearch(node: files, name: String): files?{
        var tempNode = nodeSearch(node, name)
        if(tempNode == null && node.file != null){
            for(i in node.file!!){
                if(i.folder == true){
                    tempNode = pathSearch(i, name)
                    if(tempNode != null){
                        return tempNode
                    }
                }
            }
        }
        return tempNode
    }
    fun toPath(path: String, ifCreateNode: Int = 1) : files?{
        val folders = path.split("\\")
        var tempNode : files? = null
        if(root != null){
            if(folders[0] == root!!.name){
                for(i in folders){
                    if(tempNode == null){
                        tempNode = root
                    }
                    else{
                        tempNode = nodeSearch(tempNode, i)
                        if(tempNode == null){
                            return null
                        }
                    }
                }
            }
            else{
                return null
            }
        }
        else{
            return null
        }
        return tempNode
    }
    fun getPath(node: files): String{
        return node.path + "\\" + node.name
    }
}