package com.whisker.mrr.filterEffects

import android.graphics.*
import io.reactivex.Single

class BitmapFilters {

    companion object {

        fun applyBoxFilter(bitmap: Bitmap) : Single<Bitmap> {
            val scaledBitmap = getResizedBitmap(bitmap)
            val imageBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val pixels = IntArray(imageBitmap.width * imageBitmap.height)
            val newPixels = IntArray(imageBitmap.width * imageBitmap.height)
            scaledBitmap.getPixels(pixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            scaledBitmap.recycle()

            val radius = Math.min(imageBitmap.width, imageBitmap.height) / 150
            val divider = (2 * radius + 1) * (2 * radius + 1)

            for(i in 0 until imageBitmap.height) {
                for(j in 0 until imageBitmap.width) {
                    var sumAlpha = 0
                    var sumRed = 0
                    var sumGreen = 0
                    var sumBlue = 0
                    for(m in -radius..radius) {
                        for(n in -radius..radius) {
                            val pixelX = j + m
                            val pixelY = i + n
                            val pixelIndex = imageBitmap.width * pixelY + pixelX
                            if((pixelIndex in 0 until  pixels.size)) {
                                val pixel = pixels[pixelIndex]
                                sumAlpha += Color.alpha(pixel)
                                sumRed += Color.red(pixel)
                                sumGreen += Color.green(pixel)
                                sumBlue += Color.blue(pixel)
                            }
                        }
                    }
                    newPixels[imageBitmap.width * i + j] = Color.argb((sumAlpha / divider), (sumRed / divider), (sumGreen / divider), (sumBlue / divider))
                }
            }
            imageBitmap.setPixels(newPixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            return Single.just(imageBitmap)
        }

        fun applyMedianFilter(bitmap: Bitmap) : Single<Bitmap> {
            val scaledBitmap = getResizedBitmap(bitmap)
            val imageBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val pixels = IntArray(imageBitmap.width * imageBitmap.height)
            val newPixels = IntArray(imageBitmap.width * imageBitmap.height)
            scaledBitmap.getPixels(pixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            scaledBitmap.recycle()

            val radius = Math.min(imageBitmap.width, imageBitmap.height) / 150
            val maskSize = 2 * radius + 1
            val medianIndex = maskSize / 2

            val medianAlphaArray = IntArray(maskSize * maskSize)
            val medianRedArray = IntArray(maskSize * maskSize)
            val medianGreenArray = IntArray(maskSize * maskSize)
            val medianBlueArray = IntArray(maskSize * maskSize)

            for(i in 0 until imageBitmap.height) {
                for(j in 0 until imageBitmap.width) {

                    for(m in -radius..radius) {
                        for(n in -radius..radius) {
                            val pixelX = j + m
                            val pixelY = i + n
                            val currentIndex = (radius + m) * maskSize + (radius + n)
                            val pixelIndex = imageBitmap.width * pixelY + pixelX
                            val pixel = if(pixelIndex in 0 until  pixels.size) {
                                pixels[pixelIndex]
                            } else {
                                pixels[imageBitmap.width * i + j]
                            }

                            medianAlphaArray[currentIndex] = Color.alpha(pixel)
                            medianRedArray[currentIndex] = Color.red(pixel)
                            medianGreenArray[currentIndex] = Color.green(pixel)
                            medianBlueArray[currentIndex] = Color.blue(pixel)
                        }
                    }
                    medianAlphaArray.sort()
                    medianRedArray.sort()
                    medianGreenArray.sort()
                    medianBlueArray.sort()

                    val alpha = medianAlphaArray[medianIndex]
                    val red = medianRedArray[medianIndex]
                    val green = medianGreenArray[medianIndex]
                    val blue = medianGreenArray[medianIndex]

                    newPixels[imageBitmap.width * i + j] = Color.argb(alpha, red, green, blue)
                }
            }
            imageBitmap.setPixels(newPixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            return Single.just(imageBitmap)
        }

        fun applySobelDetectionFilter(bitmap: Bitmap) : Single<Bitmap> {
            val scaledBitmap = getResizedBitmap(bitmap)
            val grayBitmap = getGrayscaleBitmap(scaledBitmap)
            scaledBitmap.recycle()
            val imageBitmap = grayBitmap.copy(Bitmap.Config.ARGB_8888, true)

            val pixels = IntArray(imageBitmap.width * imageBitmap.height)
            val newPixels = IntArray(imageBitmap.width * imageBitmap.height)
            grayBitmap.getPixels(pixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            grayBitmap.recycle()

            val sobelX = arrayOf(-1, -2, -1, 0, 0, 0, 1, 2, 1)
            val sobelY = arrayOf(-1, 0, 1, -2, 0, 2, -1, 0, 1)
            val directions = arrayOf(
                    arrayOf(-1, -1),
                    arrayOf(0, -1),
                    arrayOf(1, -1),
                    arrayOf(-1, 0),
                    arrayOf(0, 0),
                    arrayOf(1, 0),
                    arrayOf(-1, 1),
                    arrayOf(0, 1),
                    arrayOf(1, 1)
            )
            val width = imageBitmap.width
            val height = imageBitmap.height

            for(i in 1 until height) {
                for(j in 1 until width) {
                    var sumX = 0
                    var sumY = 0
                    for(k in 0 until directions.size) {
                        val index = width * (i + directions[k][0]) + j + directions[k][1]
                        if(index >= pixels.size) break
                        val pixel = pixels[width * (i + directions[k][0]) + j + directions[k][1]]
                        sumX += Color.red(pixel) * sobelX[k]
                        sumY += Color.red(pixel) * sobelY[k]
                    }
                    val newPx = Math.sqrt(Math.pow(sumX.toDouble(), 2.0) + Math.pow(sumY.toDouble(), 2.0)).toInt()
                    newPixels[width * i + j] = Color.rgb(newPx, newPx, newPx)
                }
            }
            imageBitmap.setPixels(newPixels, 0, imageBitmap.width, 0, 0, imageBitmap.width, imageBitmap.height)
            return Single.just(imageBitmap)
        }

        private fun getResizedBitmap(bitmap: Bitmap) : Bitmap {
            val scaleSize = if(bitmap.width > bitmap.height) bitmap.width.toFloat() / 1000 else bitmap.height.toFloat() / 1000

            val scaleWidth = 1f / scaleSize
            val scaleHeight = 1f / scaleSize
            val matrix = Matrix()
            matrix.postScale(scaleWidth, scaleHeight)

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)
        }

        private fun getGrayscaleBitmap(bitmap: Bitmap) : Bitmap {
            val bitmapGrayscale = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmapGrayscale)
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            val colorFilter = ColorMatrixColorFilter(colorMatrix)
            val paint = Paint()
            paint.colorFilter = colorFilter
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            return bitmapGrayscale
        }
    }
}