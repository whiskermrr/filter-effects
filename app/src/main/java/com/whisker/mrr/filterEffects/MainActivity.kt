package com.whisker.mrr.filterEffects

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide
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
        ibSobel.setOnClickListener { sobelEdgeDetection() }
        ibDilatation.setOnClickListener { dilatation() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when(requestCode) {
                PICK_IMAGE_CODE -> {
                    val imageUri = data.data!!
                    Glide.with(this).load(imageUri).into(ivSelectedImage)
                }
            }
        }
    }

    private fun boxBlur() {
        val imageBitmap = (ivSelectedImage.drawable as BitmapDrawable).bitmap
        disposables.add(
                BitmapFilters.applyBoxFilter(imageBitmap)
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
                BitmapFilters.applyMedianFilter(imageBitmap)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            ivSelectedImage.setImageBitmap(it)
                        }
        )
    }

    private fun sobelEdgeDetection() {
        val imageBitmap = (ivSelectedImage.drawable as BitmapDrawable).bitmap
        disposables.add(
                BitmapFilters.applySobelDetectionFilter(imageBitmap)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { it ->
                            ivSelectedImage.setImageBitmap(it)
                        }
        )
    }

    private fun dilatation() {
        val imageBitmap = (ivSelectedImage.drawable as BitmapDrawable).bitmap
        disposables.add(
                BitmapFilters.applyDilatation(imageBitmap)
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