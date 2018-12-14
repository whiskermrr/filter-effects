package com.whisker.mrr.filter_effects

import android.graphics.Bitmap
import android.graphics.Color
import io.reactivex.Single

class BitmapFilters {

    companion object {

        fun smoothingFilter(bitmap: Bitmap) : Single<Bitmap> {
            val scaledBitmap = scaleBitmap(bitmap)
            val imageBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
            scaledBitmap.recycle()
            val imageWidth = imageBitmap.width
            val imageHeight = imageBitmap.height
            val pixels = IntArray(imageWidth * imageHeight)
            val newPixels = IntArray(imageWidth * imageHeight)
            bitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)
            bitmap.recycle()
            val radius = Math.min(imageWidth, imageHeight) / 150
            val divider = (2 * radius + 1) * (2 * radius + 1)

            for(i in 0 until imageHeight) {
                for(j in 0 until imageWidth) {
                    var sumAlpha = 0
                    var sumRed = 0
                    var sumGreen = 0
                    var sumBlue = 0
                    for(m in -radius..radius) {
                        for(n in -radius..radius) {
                            val pixelX = j + m
                            val pixelY = i + n
                            if(pixelX in 0 until (imageWidth - 1) && pixelY in 0 until (imageHeight - 1)) {
                                val pixel = pixels[imageWidth * pixelY + pixelX]
                                sumAlpha += Color.alpha(pixel)
                                sumRed += Color.red(pixel)
                                sumGreen += Color.green(pixel)
                                sumBlue += Color.blue(pixel)
                            }
                        }
                    }
                    val newPixel = Color.argb((sumAlpha / divider), (sumRed / divider), (sumGreen / divider), (sumBlue / divider))
                    newPixels[imageWidth * i + j] = newPixel
                }
            }
            imageBitmap.setPixels(newPixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)
            return Single.just(imageBitmap)
        }

        private fun scaleBitmap(bitmap: Bitmap) : Bitmap {
            val ratio = Math.min(bitmap.width, bitmap.height)
            val newWidth = bitmap.width / ratio * 1000
            val newHeight = bitmap.height / ratio * 1000
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
        }
    }
}