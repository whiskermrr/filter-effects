package com.whisker.mrr.filter_effects

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val PICK_IMAGE_CODE = 1001
    }

    private val disposables: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bPickImage.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_CODE)
        }

        ibBlur.setOnClickListener { boxBlur() }
        ibMedian.setOnClickListener { medianFilter() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when(requestCode) {
                PICK_IMAGE_CODE -> {
                    val imageUri = data.data!!
                    val imageStream = contentResolver.openInputStream(imageUri)
                    val imageBitmap = BitmapFactory.decodeStream(imageStream)
                    ivSelectedImage.setImageBitmap(imageBitmap)
                }
            }
        }
    }

    private fun boxBlur() {
        val imageBitmap = (ivSelectedImage.drawable as BitmapDrawable).bitmap
        disposables.add(
                BitmapFilters.boxBlur(imageBitmap)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            ivSelectedImage.setImageBitmap(it)
                        }
        )
    }

    private fun medianFilter() {
        val imageBitmap = (ivSelectedImage.drawable as BitmapDrawable).bitmap
        disposables.add(
                BitmapFilters.medianFilter(imageBitmap)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            ivSelectedImage.setImageBitmap(it)
                        }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }
}