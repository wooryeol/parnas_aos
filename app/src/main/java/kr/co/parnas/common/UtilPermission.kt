package kr.co.parnas.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object UtilPermission {
    /**
     * 파일 읽기, 쓰기, 카메라 권한 체크
     * @param context
     * @return
     */
    fun isAllPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if ((ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {    // 권한 없음
                return false
            } else {
                return true
            }
        }else{
            return true
        }
    }

    /**
     * 위치값 권한 체크
     * @param context
     * @return
     */
    fun isLocatinoPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {    // 권한 없음
            return  false
        } else {
            return  true
        }
    }

    /**
     * 파일 접근 권한 체크
     * @param context
     * @return
     */
    fun isStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {    // 권한 없음
            return false
        } else {
            return true
        }
    }

    /**
     * 카메라사용 권한 체크
     * @param context
     * @return
     */
    fun isCameraPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {    // 권한 없음
            return false
        } else {
            return true
        }
    }
}