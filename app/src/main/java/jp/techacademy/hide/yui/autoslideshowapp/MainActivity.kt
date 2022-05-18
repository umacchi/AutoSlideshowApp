package jp.techacademy.hide.yui.autoslideshowapp

import android.Manifest
import android.R.attr.button
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null

    private var slideshowStatus = false

    private var mHandler = Handler(Looper.getMainLooper())

    private var cursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            isGrantedStoragePermission()

            // Android 5系以下の場合
        } else {
            cursor = createCursor()
            getFirstContentsInfo(cursor)
        }


        start_stop_button.setOnClickListener {

            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                if(!slideshowStatus) {

                    Log.d("ANDROID_UI", "再生")

                    start_stop_button.text = "一時停止"

                    mTimer = Timer()

                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                slideshowStatus = true

                                if(cursor == null) {
                                    cursor = createCursor()
                                }

                                startSlideshow(cursor)
                            }
                        }
                    }, 1000, 2000)

                } else if(slideshowStatus) {
                    Log.d("ANDROID_UI", "一時停止")
                    start_stop_button.text = "再生"
                    mTimer!!.cancel()
                    slideshowStatus = false

                }

            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }

        next_button.setOnClickListener(View.OnClickListener {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                if(cursor == null) {
                    Log.d("ANDROID_UI", "cursorを生成")
                    cursor = createCursor()
                }
                getNextContentsInfo(cursor)
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
        })

        previous_button.setOnClickListener {

            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                if(cursor == null) {
                    cursor = createCursor()
                }
                getPreviousContentsInfo(cursor)
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    cursor = createCursor()
                    getFirstContentsInfo(cursor)
                }
        }
    }

    private fun createCursor(): Cursor? {
        val resolver = contentResolver

        return resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
    }


    private fun getFirstContentsInfo(cursor: Cursor?){

        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )

            imageView.setImageURI(imageUri)
        }
    }

    private fun startSlideshow(cursor: Cursor?): Boolean {

        if (cursor!!.moveToNext()) true else cursor!!.moveToFirst()

        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        Log.d("ANDROID_UI_TEST", imageView.setImageURI(imageUri).toString())

        return true
    }

    private fun getNextContentsInfo(cursor: Cursor?): Boolean {

        if(slideshowStatus) {
            Toast.makeText(applicationContext, "再生中は進むボタンは無効です", Toast.LENGTH_LONG).show()
            return false
        }

        if (cursor!!.moveToNext()) true else cursor!!.moveToFirst()

        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        return true
    }

    private fun getPreviousContentsInfo(cursor: Cursor?): Boolean {

        if(slideshowStatus) {
            Toast.makeText(applicationContext, "再生中は戻るボタンは無効です", Toast.LENGTH_LONG).show()
            return false
        }

        if (cursor!!.moveToPrevious()) true else cursor!!.moveToLast()

        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)

        return true
    }

    private fun isGrantedStoragePermission() {
        // パーミッションの許可状態を確認する
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            cursor = createCursor()
            getFirstContentsInfo(cursor)
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

}