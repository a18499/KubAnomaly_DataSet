package iii.org.david.inspects


import com.zaxxer.nuprocess.NuAbstractProcessHandler
import com.zaxxer.nuprocess.NuProcess
import iii.org.david.inspects.interfaces.LoggerReceiver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import java.nio.ByteBuffer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream

import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule



/**
 * Created by jimmyhuang on 2018/7/20.
 */
class BehaviorMonitor(private val receiver: LoggerReceiver): NuAbstractProcessHandler() {
    private var nuProcess: NuProcess? = null
    var logger : Logger = LogManager.getLogger(BehaviorMonitor::class.java)
    //val jsonWorker = JsonTokenParser(receiver)
    var pipeOutput:PipedOutputStream = PipedOutputStream()
    var pipeInput:PipedInputStream = PipedInputStream(pipeOutput)
    var byteReader = BufferedReader(InputStreamReader(pipeInput))
    private val list = ArrayList<String>()
    init {
        GlobalScope.launch {

            while (true) {
                if (byteReader.ready()) {
                    val line = byteReader.readLine()
                    // logger.info("-> $line")
                    synchronized(list) {
                        list.add(line)
                    }
                }
                //Why
                /*while (list.size >= 500) {
                /*    synchronized(list) {
                        receiver.processNewLines(ArrayList(list))
                        list.clear()
                    }*/
                    Thread.sleep(250)
                }*/
                //Thread.sleep(5)
            }

        }
        Timer().schedule(0, 2000){
            synchronized(list) {
                logger.info("listSize: ${list.size}")
                receiver.processNewLines(ArrayList(list))
                list.clear()
            }
        }
    }
    private fun writeToBuffer(byteArray: ByteArray){
        pipeOutput.write(byteArray)
    }

    override fun onStdout(buffer: ByteBuffer, closed: Boolean) {
        try {
            if(!closed){
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                writeToBuffer(bytes)
                //jsonWorker.feedData(bytes)
            }
        }catch (e: Exception){
            logger.error("BehaviorMonitor -> $e")
            e.stackTrace.forEach { logger.error("\t $it") }
        }
    }

    override fun onStart(nuProcess: NuProcess?) {
       this.nuProcess = nuProcess
    }


}