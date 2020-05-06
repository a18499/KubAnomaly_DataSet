package iii.org.david.inspects

import iii.org.david.inspects.interfaces.LoggerReceiver
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.nio.charset.Charset


/**
 * Created by jimmyhuang on 2018/7/19.
 */
class ProcessOutputHandler(private val loggerStream: InputStream,
                           private val receiver: LoggerReceiver){
    var logger : Logger = LogManager.getLogger(BehaviorLogger::class.java)
    init{
        val byteArray = ByteArray(16384)
        while (true){
            val length = this.loggerStream.read(byteArray)
            if(length < 0){
                break
            }else if(length == 0){
                Thread.sleep(1000)
            }else {
                val lines = byteArray.toString(Charset.defaultCharset())
                        .split(Regex("\n"))
                logger.info("ProcessOutputHandler -> ${lines.size}")
                receiver.processNewLines(ArrayList(lines))
            }
        }
    }
}