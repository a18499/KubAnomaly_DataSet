package iii.org.david.core
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException


import com.github.dockerjava.api.model.Container
import iii.org.david.config.Configure
import iii.org.david.interfaces.ContainerBehavierCollector
import iii.org.david.interfaces.structure.ContainerLog


import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.ExternalProcessHandleError
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeoutException
import kotlin.streams.toList

open class DockerBehavierCollector :ContainerBehavierCollector{

    var falco = ExternalProcessHandleError()
    var logger: Logger = LogManager.getLogger(DockerBehavierCollector::class.java.getName())
    lateinit var logName: String
    var normalizeLog = ArrayList<ContainerLog>()
    var dockerclient = DockerClient()
    var filterLog = ArrayList<ContainerLog>()
    var agentIDsHash = ArrayList<Int>()
    var rawLogList= ArrayList<String>()
    /*
    * var AgentIDsHash = ArrayList<Int>()

            Configure.AgentID.forEach {
                AgentIDsHash.add(it.hashCode())
            }

            val monitorLogsWithContianerID = ArrayList<ContainerLog>()
            val filterMonitorLog = ArrayList<ContainerLog>()
            monitorLogs.forEach{
                val ContainerID :String= it.output.substring(it.output.indexOf("ID=") + 3, it.output.indexOf("ID=") + 16)

                it.containerid = ContainerID.trim()
                val contianerIDHash = it.containerid.hashCode()
                val eachContainerLog = it
                var AgentFlag = false
                run breaking@ {
                    AgentIDsHash.forEach {
                        if (contianerIDHash.equals(it)){
                            AgentFlag=true
                            return@breaking
                        }
                    }
                }

                if(!AgentFlag){
                    filterMonitorLog.add(eachContainerLog)
                }

    * */
    inline fun <reified T> List<T>.foreach(crossinline invoke:(T) -> Unit):
            Unit{
        val size =size
        var i=0
        while (i<size){
            invoke(get(i))
            i++
        }
    }

    override fun init(logname:String): Boolean{
        logName = "events.json"
        Configure.AgentID.forEach {
            agentIDsHash.add(it.hashCode())
        }
        normalizeLog.clear()
        filterLog.clear()
        logger.info("DockerBehavierCollector init complete")
        return true
    }



    override fun collect(): Boolean{
        logger.info("Begin Collect")
        logger.info("Looger will start record 10 sec")
        logger.info("LogName $logName")
        //clear normalize log array
        normalizeLog = ArrayList<ContainerLog>()
        try {
            falco.exec("timeout 11 falco --snaplen=2048 -A ",true,true,11.toLong()*1000)
        }catch (e: TimeoutException){
            logger.error("TimeoutException: "+e.toString())
        }

        logger.info("Error Message "+falco.errorMsg)
        logger.info("OutPut Message "+falco.outputMsg)
        logNormalizer()
        //rawLogTransfer()
        logger.info("Collect Complete")
        logger.info("Normalize log size: ${this.normalizeLog.size}")
        logger.info("Delete Log File")
        deleteLogFile()

        return true
    }

    override fun deinit(): Boolean{

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun killMonitorProcess(): Boolean{
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMonitorLogs(): ArrayList<ContainerLog>{
        return normalizeLog
    }

    fun getFilterLogs(): ArrayList<ContainerLog>{
        return filterLog
    }

    override fun discoveryLiveContainer(): ArrayList<Container>{
        val result = dockerclient.listContainerID(false)
        val filterdresult = ArrayList<Container>()
        logger.info("discoveryLiveContainer list containers ${result.size}")
        try {
            result.forEach{
                var flag = true
                it.names.forEach {
                    if(it.hashCode()=="composedasbydockerfile_scheduler_1".hashCode()
                            ||it.hashCode().equals("composedasbydockerfile_activemq_1".hashCode())
                            ||it.hashCode().equals("composedasbydockerfile_analyzer_1".hashCode())
                            ||it.hashCode().equals("composedasbydockerfile_containermonitor_1".hashCode())
                            ||it.hashCode().equals("composedasbydockerfile_dbagent_1".hashCode())
                            ||it.hashCode().equals("composedasbydockerfile_mongodb_1".hashCode())
                            ||it.hashCode().equals("composedasbydockerfile_restservice_1".hashCode())
                            ||it.hashCode().equals("davidui_webservice_1".hashCode())
                            ||it.hashCode().equals("davidui_mongodb_1".hashCode())
                            ||it.hashCode().equals("davidwithgui_agentui_1".hashCode())
                            ||it.hashCode().equals("davidonlycli_davidagent_1".hashCode())
                            ||it.hashCode().equals("davidwithgui_davidagent_1".hashCode())){

                        flag = false
                    }
                }
                if (flag){
                    filterdresult.add(it)

                }
            }
        }catch (e:Exception){
        logger.error("discoveryLiveContainer error $e")
        }
        return filterdresult
    }

    fun deleteLogFile(): Boolean{
        if (Files.exists(Paths.get(logName))){
            Files.delete(Paths.get(logName))
            logger.info("Delete Complete")
        }
        return true
    }

    fun rawLogTransfer(): Boolean{
        if(Files.exists(Paths.get(logName))){
            val inputStream: InputStream = File(logName).inputStream()
            val lineList = mutableListOf<String>()
            rawLogList = inputStream.bufferedReader().lines().toList().toTypedArray().toCollection(ArrayList())
            //rawLogList.addAll(inputStream.bufferedReader().lines().toList().toTypedArray())
            //inputStream.bufferedReader().useLines { lines -> rawLogList.addAll(lines) }
            logger.info("Total have ${rawLogList.size} raw data ready to Transfer ")

        }
        return true
    }

    fun logNormalizer():Boolean{
        if(Files.exists(Paths.get(logName))){
            try {
                val inputStream: InputStream = File(logName).inputStream()
                val lineList = mutableListOf<String>()

                inputStream.bufferedReader().useLines { lines -> lines.forEach {
                    try {
                        if(it.contains("Events")){
                            logger.info("End of Log")
                            return true
                        }else{
                            var result = Klaxon().parse<ContainerLog>(it)
                            var ContainerID :String= result!!.output.substring(result.output.indexOf("ID=") + 3, result.output.indexOf("ID=") + 16)
                            result.containerid = ContainerID.trim()
                            normalizeLog.add(result)
                            //Filter Log
                            val contianerIDHash = ContainerID.trim().hashCode()
                            val eachContainerLog = result
                            var AgentFlag = false
                            agentIDsHash.forEach {
                                if (contianerIDHash.equals(it)){
                                    AgentFlag=true
                                }
                            }

                            if(!AgentFlag){
                                filterLog.add(eachContainerLog)
                            }
                        }

                    }catch (e: KlaxonException){
                        logger.error("logNormalizer Error "+e.toString())

                    }

                } }
                logger.info("Total have ${lineList.size} raw data")
               /* lineList.forEach {
                    try {
                        if(it.contains("Events")){
                            logger.info("End of Log")
                            return true
                        }else{
                            var result = Klaxon().parse<ContainerLog>(it)
                            var ContainerID :String= result!!.output.substring(result.output.indexOf("ID=") + 3, result.output.indexOf("ID=") + 16)
                            result.containerid = ContainerID.trim()
                            normalizeLog.add(result)
                            //Filter Log
                            val contianerIDHash = ContainerID.trim().hashCode()
                            val eachContainerLog = result
                            var AgentFlag = false
                             agentIDsHash.forEach {
                                 if (contianerIDHash.equals(it)){
                                      AgentFlag=true
                                 }
                               }

                            if(!AgentFlag){
                                filterLog.add(eachContainerLog)
                            }
                        }

                    }catch (e: KlaxonException){
                        logger.error("logNormalizer Error "+e.toString())

                    }
                }*/
                    //logger.info("Not JSON Data Pass")
                /*var rawdata = Files.readAllLines(Paths.get(logName), Charset.forName("UTF-8"))
                rawdata.forEach {
                    try {
                        if(it.contains("Events")){
                            logger.info("End of Log")
                            return true
                        }else{
                            var result = Klaxon().parse<ContainerLog>(it)
                            var ContainerID :String= result!!.output.substring(result.output.indexOf("ID=") + 3, result.output.indexOf("ID=") + 16)
                            result.containerid = ContainerID
                            normalizeLog.add(result)
                        }

                    }catch (e: KlaxonException){
                        logger.error("logNormalizer Error "+e.toString())
                        println(e.toString())
                    }
                }*/
            }catch (e:Exception){
                logger.error("Unknow Exception $e")
                println(e.toString())
            }
        }else{
            logger.info("Log File doesn't exist")
        }
        return true
    }


}