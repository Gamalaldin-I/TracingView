package com.example.draw

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class TracingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private lateinit var currentPath :Path //the current path
    private var drawing=false             // drawing state
    private var counter=0
    private var completedPaths=ArrayList<Path>() //completed paths
    private val completedPaint=Paint()
    private var pathArray=ArrayList<Path>() //all paths
    private val activePaint=Paint()             //active paint
    private val tracePaint=Paint()
    private val tracedPaint=Paint()
    private val pathMeasure=PathMeasure() //path measure
    private val borderPaint=Paint()
    private val thumbPaint=Paint()
    private val thumbRadius = 60f           //the radius of the thumb
    private var thumbPos = FloatArray(2)    //the position of the thumb
    private var pathProgress = 0f          //the progress of the path
    init {

        tracePaint.apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 80f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        borderPaint.apply {
            color = Color.BLACK // Border color (solid black)
            style = Paint.Style.STROKE
            strokeWidth = 5f // Border thickness
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
         //active paint
        activePaint.apply {
            color = Color.YELLOW // Set the desired color (Yellow in this case)
            style = Paint.Style.STROKE
            strokeWidth = 20f // Adjust the thickness of the dots
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND

            // Apply DashPathEffect for dotted line
            val intervals = floatArrayOf(0.1f, 50f) // (dot size, gap size)
            pathEffect = DashPathEffect(intervals, 0f)
        }
        //traced paint
        tracedPaint.apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 80f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        //completed paint
        completedPaint.apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 80f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
         //ball paint
        thumbPaint.apply {
            color = Color.RED
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //draw all paths
        for(path in pathArray){
            canvas.drawPath(path, tracePaint)
        }

        //draw the traced path
        canvas.drawPath(getTracedPath(),tracedPaint)
        //draw completed paths
        for (path in completedPaths){
            canvas.drawPath(path, completedPaint)
            if(checkAllPathsCompleted()){
                drawing=false
                invalidate()
            }
        }

        // draw the thumb
        if(drawing){
            canvas.drawPath(getActivePath(),borderPaint)
            canvas.drawPath(getActivePath(), activePaint)
            canvas.drawCircle(thumbPos[0], thumbPos[1], thumbRadius, thumbPaint)
        }
        invalidate()
    }

    private fun getActivePath(): Path {
        return currentPath
    }
    private fun getTracedPath(): Path {
        val tracedPath = Path()
        pathMeasure.getSegment(0f, pathProgress * pathMeasure.length, tracedPath, true)
        return tracedPath
    }

    private fun changePath(index:Int){
        currentPath=pathArray[index]
        pathMeasure.setPath(currentPath, false)
        pathMeasure.getPosTan(0f, thumbPos, null)
        pathProgress = 0f
        invalidate()
    }
    private var isDragging = false  //variable to know if the thumb is being dragged
    private fun addCompletedPath(path: Path) {
        completedPaths.add(path)
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        //check if the current path is fully traced
        if(isFullyTraced()){
            //add the current path to the completed paths array
            isDragging=false
            if(counter<pathArray.size){
                addCompletedPath(currentPath)
                counter++
            }
            //check if there are more paths to trace
            if(counter<pathArray.size){
                changePath(counter)
                isDragging=true
            }
        }


        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // check if the touch is inside the thumb
                val dx = touchX - thumbPos[0]
                val dy = touchY - thumbPos[1]
                val distance = sqrt((dx * dx + dy * dy).toDouble())

                if (distance <= thumbRadius) {
                    isDragging = true
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val (nearestProgress, nearestPos) = getNearestPointOnPath(touchX, touchY)

                    // check if the nearest point is within the thumb radius
                    if (nearestProgress > pathProgress && distanceToPath(touchX, touchY) <= thumbRadius) {
                        pathProgress = nearestProgress
                        thumbPos = nearestPos
                        invalidate()  //refresh the view
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // is the thumb released
                isDragging = false
            }
        }
        return true
    }

    //fun to calculate the distance between the touch and the path
    private fun distanceToPath(touchX: Float, touchY: Float): Float {
        var closestDistance = Float.MAX_VALUE

        // loop through the path to find the closest point
        for (i in 0..1000) {
            val progress = i / 1000f * pathMeasure.length
            val pos = FloatArray(2)
            pathMeasure.getPosTan(progress, pos, null)

            val dx = touchX - pos[0]
            val dy = touchY - pos[1]
            val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            if (distance < closestDistance) {
                closestDistance = distance
            }
        }
        return closestDistance
    }

    private fun getNearestPointOnPath(touchX: Float, touchY: Float): Pair<Float, FloatArray> {
        val pathLength = pathMeasure.length
        var closestDistance = Float.MAX_VALUE
        var closestPos = FloatArray(2)
        var closestProgress = 0f

        for (i in 0..1000) {
            val progress = i / 1000f * pathLength
            val pos = FloatArray(2)
            pathMeasure.getPosTan(progress, pos, null)

            val dx = touchX - pos[0]
            val dy = touchY - pos[1]
            val distToTouch = dx * dx + dy * dy

            if (distToTouch < closestDistance) {
                closestDistance = distToTouch
                closestPos = pos
                closestProgress = progress / pathLength
            }
        }
        return Pair(closestProgress, closestPos)
    }
    fun checkAllPathsCompleted(): Boolean {

        return (completedPaths.size == pathArray.size)
    }
    private fun isFullyTraced(): Boolean {
        val progress = pathProgress * pathMeasure.length
        val pathLength = pathMeasure.length
        return (pathLength - progress)<=1f
    }
    fun setNewPathArray(paths: ArrayList<Path>) {
        pathArray= arrayListOf()
        completedPaths= arrayListOf()
        pathArray = paths
        if (pathArray.isNotEmpty()) {
            currentPath = pathArray[0]
            pathMeasure.setPath(currentPath,false)
            pathMeasure.getPosTan(0f, thumbPos, null)
            pathProgress = 0f
            counter = 0
            drawing = true
            invalidate()
        }
    }
    fun reset(){
        pathArray= arrayListOf()
        pathArray=Paths.getA()
        completedPaths= arrayListOf()
        currentPath=pathArray[0]
        pathMeasure.setPath(currentPath,false)
        pathMeasure.getPosTan(0f, thumbPos, null)
        pathProgress = 0f
        counter = 0
        drawing = true
        invalidate()
    }


}