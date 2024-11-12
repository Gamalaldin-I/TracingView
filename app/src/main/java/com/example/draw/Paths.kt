package com.example.draw

import android.graphics.Path

object Paths {
    private lateinit var paths: ArrayList<Path>
     fun getA():ArrayList<Path>{
        paths = arrayListOf()
        addPath(setPath(Path(), 300f, 100f, 100f, 500f))
        addPath(setPath(Path(), 300f, 100f,500f, 500f))
        addPath(setPath(Path(), 200f, 300f, 400f, 300f))
        return paths
    }
    // you can add more paths here

    private fun addPath(path:Path){
        paths.add(path)
    }
    private fun setPath(path:Path, mx:Float, my:Float, ex:Float, ey:Float):Path{
        path.apply {
            moveTo(mx, my)
            lineTo(ex, ey)
        }
        return path
    }

}