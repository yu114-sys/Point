package personal.cx.point.data

import personal.cx.point.data.file.shareFileData
import personal.cx.point.data.file.downloadFileData


val libraryDatabase = downloadFileData()
val innerDatabase = shareFileData()

var globalSetting : SettingData = SettingData()
