package personal.cx.point.app

import personal.cx.point.setting.SettingData
import personal.cx.point.data.shareFileData
import personal.cx.point.data.downloadFileData


val libraryDatabase = downloadFileData()
val innerDatabase = shareFileData()

var globalSetting : SettingData = SettingData()
