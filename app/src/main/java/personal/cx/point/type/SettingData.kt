package personal.cx.point.type

import android.content.SharedPreferences
import personal.cx.point.exception.PointRuntimeException

class SettingData () {
    private var setting : SharedPreferences? = null
    private val dir = mapOf(
        "initial" to 1,

        "connect_key_en" to 1,
        "connect_key" to 3,

        "library_listen_port" to 2,
        "library_listen_delay" to 2,

        "UDP_listen_port" to 2
    )

    fun isSet() : Boolean{
        if(setting != null){
            return true
        }
        return false

    }
    fun set(settingFile : SharedPreferences){
        setting = settingFile
        if(!setting!!.contains("initial")){
            initial()
        }
    }
    fun write(key : String, value : Any){
        if(setting != null){
            when(keyType(key)){
                1 -> when(value){
                    is Boolean -> setting!!.edit().putBoolean(key, value as Boolean).apply()
                    else -> throw PointRuntimeException("Error: setting " + key + " by " + value.toString() + " need Boolean")
                }
                2 -> when(value){
                    is Int -> setting!!.edit().putInt(key, value as Int).apply()
                    else -> throw PointRuntimeException("Error: setting " + key + " by " + value.toString() + " need Int")
                }
                3 -> when(value){
                    is String -> setting!!.edit().putString(key, value as String).apply()
                    else -> throw PointRuntimeException("Error: setting " + key + " by " + value.toString() + " need String")
                }
                else -> throw PointRuntimeException("Error: Unknown key" + key)
            }
        }
        else{
            throw PointRuntimeException("Error: Undefine SharedPreferences")
        }
    }
    fun read(key : String) : Any? {
        if(setting != null){
            when(keyType(key)){
                1 -> return setting!!.getBoolean(key, false)
                2 -> return setting!!.getInt(key, 0)
                3 -> return setting!!.getString(key, "")
                else -> throw PointRuntimeException("Error: Unknown key" + key)
            }
        }
        else{
            return null
        }
    }
    private fun keyType(value : String) : Int?{
        if(dir.containsKey(value)){
            return dir[value]
        }
        else{
            return 0
        }
    }
    fun initial(){
        write("initial", true)

        write("connect_key_en", false)
        write("connect_key", "")

        initialSenior()
    }
    fun initialSenior(){
        write("library_listen_port", 2345)
        write("library_listen_delay", 500)

        write("UDP_listen_port", 2344)
    }
}