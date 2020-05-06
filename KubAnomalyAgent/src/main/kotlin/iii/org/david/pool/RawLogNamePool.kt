package iii.org.david.pool

import java.util.concurrent.ArrayBlockingQueue

object RawLogNamePool {
    val rawLogNamePool = ArrayBlockingQueue<String>(1000)

    fun queueSize():Int{
        return rawLogNamePool.size
    }

    @Synchronized
    fun putLogIntoPool(rawlogname:String):Boolean{
        rawLogNamePool.put(rawlogname)
        return false
    }

    @Synchronized
    fun getLogOutPool():String{
       return rawLogNamePool.take()
    }
}