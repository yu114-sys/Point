package personal.cx.point

import personal.cx.point.data.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.*

class DataClassesTest {

    // 创建临时目录用于测试
    private val testDir = "test_temp_dir"

    @BeforeTest
    fun setUp() {
        // 创建测试目录和一些测试文件
        Files.createDirectories(Paths.get(testDir))
        File("$testDir/test1.txt").writeText("Hello World")
        File("$testDir/test2.txt").writeText("Another test file")
        Files.createDirectories(Paths.get("$testDir/subdir"))
        File("$testDir/subdir/test3.txt").writeText("File in subdirectory")
    }

    @AfterTest
    fun tearDown() {
        // 清理测试目录
        File(testDir).deleteRecursively()
    }

    @Test
    fun testDownloadFileData() {
        val downloadData = downloadFileData()

        // 测试添加库
        downloadData.add("Test Library", "192.168.1.1", "password123", 0, "{}")
        downloadData.add("Another Library", "192.168.1.2", "password456", 0, "{}")

        // 测试获取库列表
        val libraries = downloadData.libraryList()
        assertEquals(2, libraries.size)
        assertEquals("Test Library", libraries[0].name)
        assertEquals("192.168.1.1", libraries[0].ip)
        assertTrue(libraries[0].password.isEmpty()) // 密码应该被移除

        // 测试获取特定库
        val library = downloadData.library(0)
        assertNotNull(library)
        assertEquals("Test Library", library?.name)

        // 测试获取密码
        val password = downloadData.password(0)
        assertEquals("password123", password)

        // 测试删除
        downloadData.delete(0)
        assertEquals(1, downloadData.libraryList().size)

        // 测试JSON序列化和反序列化
        val json = downloadData.toJSON()
        assertNotNull(json)
        assertTrue(json.isNotEmpty())

        // 重新添加一个库用于JSON测试
        downloadData.add("JSON Test", "192.168.1.3", "password789", 0, "{}")

        // 测试从JSON设置数据
        downloadData.setByJSON(json)
        assertEquals(1, downloadData.libraryList().size)
    }

    @Test
    fun testShareFileData() {
        val shareData = shareFileData()

        // 测试添加文件
        val id1 = shareData.add("$testDir/test1.txt", "Test File 1", "sharepassword")
        assertNotEquals(0, id1)

        val id2 = shareData.add("$testDir/test2.txt", "Test File 2")
        assertNotEquals(0, id2)

        // 测试toFileJSON
        val fileJson = shareData.toFileJSON()
        assertTrue(fileJson.isNotEmpty())
        assertTrue(fileJson.contains("Test File 1"))

        // 测试toSaveJSON
        val saveJson = shareData.toSaveJSON()
        assertTrue(saveJson.isNotEmpty())
        assertTrue(saveJson.contains("Test File 1"))
        assertTrue(saveJson.contains("sharepassword"))

        // 测试从JSON设置数据
        shareData.setByJSON(saveJson)
        val newSaveJson = shareData.toSaveJSON()
        assertEquals(saveJson, newSaveJson)

        // 测试删除
        shareData.remove(id1)
        val finalSaveJson = shareData.toSaveJSON()
        assertFalse(finalSaveJson.contains("Test File 1"))
    }

    @Test
    fun testFileData() {
        val fileData = fileData()

        // 测试setByFile
        fileData.setByFile(File(testDir))
        assertNotNull(fileData.toJSON())

        // 测试toJSON
        val json = fileData.toJSON()
        assertNotNull(json)
        assertTrue(json!!.contains("test1.txt"))
        assertTrue(json.contains("test2.txt"))
        assertTrue(json.contains("subdir"))

        // 测试path函数
        val root = fileData.search(fileData.getRoot()!!, "test1.txt")
        assertNotNull(root)
        val filePath = fileData.path(root!!)
        assertNotNull(filePath)
        assertTrue(filePath!!.endsWith("test1.txt"))

        // 测试search函数
        val foundFile = fileData.search(fileData.getRoot()!!, "test2.txt")
        assertNotNull(foundFile)
        assertEquals("test2.txt", foundFile!!.name)

        // 测试check函数
        val changed = fileData.check()
        assertFalse(changed) // 文件应该没有变化

        // 测试setByJSON
        val newFileData = fileData()
        newFileData.setByJSON(json)
        val newJson = newFileData.toJSON()
        assertEquals(json, newJson)
    }

}