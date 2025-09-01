package personal.cx.point.type

import personal.cx.point.type.file.shareFileData
import personal.cx.point.type.file.downloadFileData


val libraryDatabase = downloadFileData()
val innerDatabase = shareFileData()

var globalSetting : SettingData = SettingData()
