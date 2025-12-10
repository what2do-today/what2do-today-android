package com.example.what2do_today.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.what2do_today.R
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.route.RouteLineOptions
import com.kakao.vectormap.route.RouteLineSegment
import com.kakao.vectormap.route.RouteLineStyle
import com.kakao.vectormap.route.RouteLineStyles
import com.kakao.vectormap.route.RouteLineStylesSet


private const val TAG = "MainMapScreen"
fun addColoredMarkers(
    context: Context,
    kakaoMap: KakaoMap,
    points: List<LatLng>
) {
    val layer = kakaoMap.labelManager?.layer ?: return

    val startBitmap = vectorToBitmapTinted(context, R.drawable.red_pin, 14f, 14f) // 초록
    val viaBitmap   = vectorToBitmapTinted(context, R.drawable.red_pin, 14f, 14f) // 파랑
    val endBitmap   = vectorToBitmapTinted(context, R.drawable.red_pin, 14f, 14f) // 빨강

    if (startBitmap == null || viaBitmap == null || endBitmap == null) return

    val startStyle = LabelStyles.from(LabelStyle.from(startBitmap))
    val viaStyle   = LabelStyles.from(LabelStyle.from(viaBitmap))
    val endStyle   = LabelStyles.from(LabelStyle.from(endBitmap))

    points.forEachIndexed { index, latLng ->

        val (style, tag) = when (index) {
            0 -> startStyle to "start"
            points.lastIndex -> endStyle to "end"
            else -> viaStyle to "via_$index"
        }

        layer.addLabel(
            LabelOptions.from(latLng)
                .setStyles(style)
                .setClickable(true)  // 🔥 클릭 활성화
                .setTag(tag)         // 🔥 어떤 마커인지 구분
                .setRank(index.toLong())
        )
    }
}


// 📌 경로 그리기 함수
fun drawRoute(context: Context, kakaoMap: KakaoMap, points: List<LatLng>) {
    val routeManager = kakaoMap.routeLineManager
    val routeLayer = routeManager?.layer

    if (routeLayer == null) {
        Log.e(TAG, "RouteLayer를 가져올 수 없습니다.")
        return
    }

    // 스타일 설정 (파란색, 두께 16)
    val style = RouteLineStyle.from(
        16f,
        ContextCompat.getColor(context, android.R.color.holo_blue_dark)
    )
    val stylesSet = RouteLineStylesSet.from(RouteLineStyles.from(style))

    val segment = RouteLineSegment.from(points, style)

    // 기존 경로 삭제 후 새로 그리기 (선택 사항)
    // routeLayer.removeAll()

    routeLayer.addRouteLine(
        RouteLineOptions.from(segment).setStylesSet(stylesSet)
    )
    Log.d(TAG, "🖌️ 경로 그리기 완료 (Point: ${points.size}개)")
}
// 📌 수정된 벡터 -> 비트맵 변환 함수 (원하는 크기 지정 가능)
fun vectorToBitmapTinted(
    context: Context,
    drawableId: Int,
    widthDp: Float,
    heightDp: Float
): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null

    val wrapped = DrawableCompat.wrap(drawable).mutate()


    val widthPx = dpToPx(context, widthDp)
    val heightPx = dpToPx(context, heightDp)

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    wrapped.setBounds(0, 0, canvas.width, canvas.height)
    wrapped.draw(canvas)

    return bitmap
}



// 📏 (보조 함수) dp를 픽셀로 변환해주는 함수
fun dpToPx(context: Context, dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density + 0.5f).toInt() // 반올림하여 정수로 변환
}