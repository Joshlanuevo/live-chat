package com.ym.base.constant

/**
 * Author:yangcheng
 * Date:2020-10-6
 * Time:16:07
 * 来源kok体育源码定义
 */
class DownErrorCode {
  companion object {
    const val  APK_INVALID = 204
    const val  APK_PATH_ERROR = 202
    const val  APK_VERSION_ERROR = 203
    const val  COPY_EXCEPTION = 215
    const val  COPY_FAIL = 212
    const val  COPY_INSTALL_SUCCESS = 220
    const val  COPY_SRCDIR_ERROR = 213
    const val  COPY_TMPDIR_ERROR = 214
    const val  CREATE_TEMP_CONF_ERROR = 225
    const val  DEXOAT_EXCEPTION = 226
    const val  DEXOPT_EXCEPTION = 209
    const val  DISK_FULL = 105
    const val  DOWNLOAD_FILE_CONTENTLENGTH_NOT_MATCH = 113
    const val  DOWNLOAD_HAS_COPY_TBS_ERROR = 122
    const val  DOWNLOAD_HAS_LOCAL_TBS_ERROR = 120
    const val  DOWNLOAD_INSTALL_SUCCESS = 200
    const val  DOWNLOAD_OVER_FLOW = 112
    const val  DOWNLOAD_REDIRECT_EMPTY = 124
    const val  DOWNLOAD_RETRYTIMES302_EXCEED = 123
    const val  DOWNLOAD_SUCCESS = 100
    const val  DOWNLOAD_THROWABLE = 125
    const val  ERROR_CANLOADVIDEO_RETURN_FALSE = 313
    const val  ERROR_CANLOADVIDEO_RETURN_NULL = 314
    const val  ERROR_CANLOADX5_RETURN_FALSE = 307
    const val  ERROR_CANLOADX5_RETURN_NULL = 308
    const val  ERROR_CODE_DOWNLOAD_BASE = 100
    const val  ERROR_CODE_INSTALL_BASE = 200
    const val  ERROR_CODE_LOAD_BASE = 300
    const val  ERROR_GETSTRINGARRAY_JARFILE = 329
    const val  ERROR_HOST_UNAVAILABLE = 304
    const val  ERROR_QBSDK_INIT_CANLOADX5 = 319
    const val  ERROR_QBSDK_INIT_ERROR_EMPTY_BUNDLE = 331
    const val  ERROR_QBSDK_INIT_ERROR_RET_TYPE_NOT_BUNDLE = 330
    const val  ERROR_QBSDK_INIT_ISSUPPORT = 318
    const val  ERROR_TBSCORE_SHARE_DIR = 312
    const val  ERROR_UNMATCH_TBSCORE_VER = 303
    const val  ERROR_UNMATCH_TBSCORE_VER_THIRDPARTY = 302
    const val  EXCEED_COPY_RETRY_NUM = 211
    const val  EXCEED_DEXOPT_RETRY_NUM = 208
    const val  EXCEED_INCR_UPDATE = 224
    const val  EXCEED_LZMA_RETRY_NUM = 223
    const val  EXCEED_UNZIP_RETRY_NUM = 201
    const val  FILE_DELETED = 106
    const val  FILE_RENAME_ERROR = 109
    const val  HOST_CONTEXT_IS_NULL = 227
    const val  INCRUPDATE_INSTALL_SUCCESS = 221
    const val  INCR_UPDATE_ERROR = 216
    const val  INCR_UPDATE_EXCEPTION = 218
    const val  INCR_UPDATE_FAIL = 217
    const val  INFO_CAN_NOT_DISABLED_BY_CRASH = 408
    const val  INFO_CAN_NOT_LOAD_TBS = 405
    const val  INFO_CAN_NOT_LOAD_X5 = 407
    const val  INFO_CAN_NOT_USE_X5_FINAL_REASON = 411
    const val  INFO_CAN_NOT_USE_X5_TBS_AVAILABLE = 409
    const val  INFO_CAN_NOT_USE_X5_TBS_NOTAVAILABLE = 410
    const val  INFO_CODE_BASE = 400
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_APKFILE = 506
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_COUNTS = 505
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_FILEPATHISNULL = 509
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_MINIQBFAILED = 511
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_MINIQBSUCCESS = 510
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_NOTSUPPORT = 507
    const val  INFO_CODE_FILEREADER_OPENFILEREADER_OPENINQB = 508
    const val  INFO_CODE_MINIQB = 500
    const val  INFO_CODE_MINIQB_STARTMINIQBTOLOADURL_COUNTS = 501
    const val  INFO_CODE_MINIQB_STARTMINIQBTOLOADURL_FAILED = 504
    const val  INFO_CODE_MINIQB_STARTMINIQBTOLOADURL_ISNOTX5CORE = 502
    const val  INFO_CODE_MINIQB_STARTMINIQBTOLOADURL_SUCCESS = 503
    const val  INFO_CORE_EXIST_NOT_LOAD = 418
    const val  INFO_DISABLE_X5 = 404
    const val  INFO_FORCE_SYSTEM_WEBVIEW_INNER = 401
    const val  INFO_FORCE_SYSTEM_WEBVIEW_OUTER = 402
    const val  INFO_INFO_MISS_SDKEXTENSION_JAR_WITHOUT_FUSION_DEX_WITHOUT_CORE = 4122
    const val  INFO_INFO_MISS_SDKEXTENSION_JAR_WITHOUT_FUSION_DEX_WITH_CORE = 4121
    const val  INFO_INITX5_FALSE_DEFAULT = 415
    const val  INFO_MISS_SDKEXTENSION_JAR = 403
    const val  INFO_MISS_SDKEXTENSION_JAR_OLD = 406
    const val  INFO_MISS_SDKEXTENSION_JAR_WITHOUT_FUSION_DEX = 412
    const val  INFO_MISS_SDKEXTENSION_JAR_WITH_FUSION_DEX = 413
    const val  INFO_MISS_SDKEXTENSION_JAR_WITH_FUSION_DEX_WITHOUT_CORE = 4132
    const val  INFO_MISS_SDKEXTENSION_JAR_WITH_FUSION_DEX_WITH_CORE = 4131
    const val  INFO_SDKINIT_IS_SYS_FORCED = 414
    const val  INFO_TEMP_CORE_EXIST_CONF_ERROR = 417
    const val  INFO_USE_BACKUP_FILE_INSTALL_BY_SERVER = 416
    const val  NETWORK_NOT_WIFI_ERROR = 111
    const val  NETWORK_UNAVAILABLE = 101
    const val  NONEEDTODOWN_ERROR = 110
    const val  READ_RESPONSE_ERROR = 103
    const val  RENAME_EXCEPTION = 219
    const val  ROM_NOT_ENOUGH = 210
    const val  SERVER_ERROR = 102
    const val  TEST_THROWABLE_ISNOT_NULL = 327
    const val  TEST_THROWABLE_IS_NULL = 326
    const val  THREAD_INIT_ERROR = 121
    const val  THROWABLE_INITTESRUNTIMEENVIRONMENT = 328
    const val  THROWABLE_INITX5CORE = 325
    const val  THROWABLE_QBSDK_INIT = 306
    const val  UNKNOWN_ERROR = 107
    const val  UNLZMA_FAIURE = 222
    const val  UNZIP_DIR_ERROR = 205
    const val  UNZIP_IO_ERROR = 206
    const val  UNZIP_OTHER_ERROR = 207
    const val  VERIFY_ERROR = 108
    const val  WRITE_DISK_ERROR = 104

  }
}