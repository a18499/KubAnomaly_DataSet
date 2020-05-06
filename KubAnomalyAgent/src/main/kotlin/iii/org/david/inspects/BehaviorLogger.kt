package iii.org.david.inspects

import com.zaxxer.nuprocess.NuProcessBuilder
import iii.org.david.inspects.interfaces.LoggerReceiver
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.zip.CheckedOutputStream

/**
 * Created by jimmyhuang on 2018/7/19.
 */
class BehaviorLogger(private val receiver: (List<String>) -> Unit): Runnable{
    var logger : Logger = LogManager.getLogger(BehaviorLogger::class.java)

    var falcoPath =  if(Files.exists(Paths.get("/home/falco"))) "/home/falco" else "/usr/bin/falco"
    val cmd =  arrayOf(falcoPath, "--snaplen=2048", "-A")

    private val nuProcessBuilder = NuProcessBuilder(cmd.toMutableList())
    override fun run() {
        try {
            logger.info("Start BehaviorLogger ")
            nuProcessBuilder.setProcessListener(
                BehaviorMonitor(
                    object : LoggerReceiver{
                        override fun processNewLines(lines: ArrayList<String>) {
                            receiver(lines)
                        }
                    }
                )
            )
            val process = nuProcessBuilder.start()
            process.waitFor(0, TimeUnit.SECONDS)
            logger.info("Process completely")
        }catch (e:Exception){
            logger.error("BehaviorLogger -> $e")
            e.stackTrace.forEach { logger.error("\t $it") }
        }
    }
}