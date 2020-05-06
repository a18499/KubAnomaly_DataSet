package iii.org.david.core


import com.fasterxml.jackson.databind.ObjectMapper
import iii.org.david.config.Configure



import iii.org.david.interfaces.core.DockerInformationCollector
import iii.org.david.interfaces.structure.ContainerLog
import interfaces.*

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger


class DAVIDLogCollector :Runnable{
    var logger: Logger = LogManager.getLogger(DAVIDLogCollector::class.java.getName())
    var masterPort = ""
    var agentIP = ""
    var dockerBehavierCollector = DockerBehavierCollector() //Log Data
    var dockerInformationCollector = DockerInformationCollector()//HD Information
    lateinit var channelForDAVIDMaster: ManagedChannel
    lateinit var davidCommunicatorServiceGrpcStub: DavidCommunicatorServiceGrpc.DavidCommunicatorServiceBlockingStub
   // lateinit var rawLogSendingPool: ExecutorService
    fun init(port:String,agentip:String):Boolean{
        masterPort = port
        agentIP = agentip
        dockerInformationCollector.init()
        //rawLogSendingPool =  Executors.newFixedThreadPool(3)
        logger.info("DAVIDLogCollector init complete")
        return true
    }
    //High performance foreach
    inline fun <reified T> List<T>.foreach(crossinline invoke:(T) -> Unit):
            Unit{
        val size =size
        var i=0
        while (i<size){
            invoke(get(i))
            i++
        }
    }

    //Connect to David Center
    fun connectToMaster():Boolean{
        logger.info("Master IP ${iii.org.david.config.Configure.getDAVIDMasterIP()}")
        logger.info("Port=${Configure.davidMasterPort}")
        //ContainerBehaviorCollectServiceBlockingStub ManagedChannelBuilder.forAddress(hostname,port).usePlaintext(true).maxInboundMessageSize(1024*1024*1024).build()
        try {
            channelForDAVIDMaster = ManagedChannelBuilder.forAddress(iii.org.david.config.Configure.getDAVIDMasterIP(),Configure.davidMasterPort.toInt()).usePlaintext(true) .maxInboundMessageSize(1024*1024*1024).build()

            davidCommunicatorServiceGrpcStub = DavidCommunicatorServiceGrpc.newBlockingStub(channelForDAVIDMaster)

        }catch (e:Exception){
            logger.error("Unknown Exception $e")
            return false
        }
        logger.info("connectToMaster complete")
        return true
    }

    //This Function used to chunk Raw log data 2500  Raw log data per chunk
    fun processRawLogData(originLogData:ArrayList<String>):ArrayList<ArrayList<String>>{
        val result = ArrayList<ArrayList<String>>()
        val eachChunkOfLogData = ArrayList<String>()
        var messagechunk =0
        originLogData.forEach {
            if(messagechunk%2500==0){
                result.add(eachChunkOfLogData)
                eachChunkOfLogData.clear()
                messagechunk =0
            }else{
                eachChunkOfLogData.add(it)
            }
            messagechunk++
        }

        return result
    }
    //This Function used to chunk log data 1000 log data per chunk
    fun processLogData(originLogData:ArrayList<ContainerLog>):ArrayList<ArrayList<ContainerLog>>{
        val result = ArrayList<ArrayList<ContainerLog>>()
        val eachChunkOfLogData = ArrayList<ContainerLog>()
        var messagechunk =0
        originLogData.forEach {
            if(messagechunk%1000==0){
                result.add(eachChunkOfLogData)
                eachChunkOfLogData.clear()
                messagechunk =0
            }else{
                eachChunkOfLogData.add(it)
            }
            messagechunk++
        }

        return result
    }

    override fun run() {
        connectToMaster()
        logger.info("connectToMaster complete ")
        //Send agent ip to master
        val agentipStruct = StringStruct.newBuilder().setResult(agentIP).build()
        davidCommunicatorServiceGrpcStub.init(agentipStruct)


        do {
            //Checking Connection
            if(channelForDAVIDMaster.isTerminated||channelForDAVIDMaster.isShutdown){
                logger.info("Reconnect to Master ")
                connectToMaster()
            }
            logger.info("Begin to find valid container")
            val ContainerStruct = ContainerListWithIp.newBuilder()
            val containers = dockerBehavierCollector.discoveryLiveContainer()
            logger.info("ContainerSize ${containers.size}")
            if (containers.size!=0){
                containers.forEach {
                    val objectMapper = ObjectMapper()
                    val eachcontainer = objectMapper.writeValueAsString(it)
                    ContainerStruct.addContainer(eachcontainer)
                }
            }
            ContainerStruct.ip =this.agentIP

            val listContainer =  ContainerStruct.build()
            davidCommunicatorServiceGrpcStub.discoveryLiveContainer(listContainer)

            logger.info("Begin to collect HDResourceRsultStruct")
            val HDResourceRsultStruct = ListContainerHDInformationWithIp.newBuilder().clear().clearContainerHDInformation()


            val HDResourceRsult=   dockerInformationCollector.getResource()
            logger.info("HDResourceRsult Size ${HDResourceRsult.size}")
            HDResourceRsult.forEach {
                val objectmapper = ObjectMapper()
                val hdinformation = objectmapper.writeValueAsString(it)
                HDResourceRsultStruct.addContainerHDInformation(hdinformation)
            }
            HDResourceRsultStruct.ip=this.agentIP
            val HDDStructList =  HDResourceRsultStruct.build()

            davidCommunicatorServiceGrpcStub.sendHDInformationToMaster(HDDStructList)
            logger.info("Send HDInformationToMaster Complete")


            logger.info("Begin to collect behavior data")
            val logDatagName = System.currentTimeMillis().toString()+".json"

            dockerBehavierCollector.init(logDatagName)
            dockerBehavierCollector.collect()
            //val rawLogData = dockerBehavierCollector.rawLogList
            val monitorLogs = dockerBehavierCollector.getMonitorLogs()
            //val filterMonitorLog = dockerBehavierCollector.getFilterLogs()

            //preprocessed  monitorLogs
            var AgentIDsHash = ArrayList<Int>()

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

                monitorLogsWithContianerID.add(it)
            }

            /*val filterMonitorLog = monitorLogsWithContianerID.filter {
               it.containerid !in Configure.AgentID
            }*/

            logger.info("Total Collect ${monitorLogs.size}  log data on This agent")
            logger.info("Total Collect ${filterMonitorLog.size} valid log data on This agent")
            val chunkdataOfLogs = processLogData(filterMonitorLog)
           // logger.info("Total Raw Log Size: ${rawLogData.size}")
           /// val chunkdataOfLogs = processRawLogData(rawLogData)
           // logger.info("Total chunk ${chunkdataOfLogs.size} need to send ")

            //var rawLogSender = RawLogSender()
           // rawLogSender.init(agentIP,chunkdataOfLogs,davidCommunicatorServiceGrpcStub)
            //rawLogSendingPool.submit(rawLogSender)

            //val amountOfInterval = (chunkdataOfLogs.size/6)+1
           // logger.info("amountOfInterval $amountOfInterval")
            //var count=0
            //Send Raw Log Chunk By Chunk
            /*chunkdataOfLogs.forEach {
                val rawLogStruct = ListRawLogStringWithIp.newBuilder()
                it.forEach {
                    val objectmapper = ObjectMapper()
                    val rawLogs = objectmapper.writeValueAsString(it)
                    rawLogStruct.addRawlogstring(rawLogs)
                }
                rawLogStruct.ip = agentIP
                val listRawLog = rawLogStruct.build()
                davidCommunicatorServiceGrpcStub.sendRawLogDataToMaster(listRawLog)
                /*count++
                if(amountOfInterval%count==0){
                    logger.info("Delay 1 Sec")
                    Thread.sleep(1000)
                }*/

            }
            logger.info("Send LogDataToMaster Complete")
            dockerBehavierCollector.rawLogList.clear()
            logger.info("Clear RawLogList")
            chunkdataOfLogs.clear()*/

           /* var job =launch {
                chunkdataOfLogs.forEach {
                    val rawLogStruct = ListRawLogStringWithIp.newBuilder()
                    it.forEach {
                        val objectmapper = ObjectMapper()
                        val rawLogs = objectmapper.writeValueAsString(it)
                        rawLogStruct.addRawlogstring(rawLogs)
                    }
                    rawLogStruct.ip = agentIP
                    val listRawLog = rawLogStruct.build()
                    davidCommunicatorServiceGrpcStub.sendRawLogDataToMaster(listRawLog)
                    count++
                    if(count%amountOfInterval==0){
                        logger.info("Delay 1 Sec")
                        Thread.sleep(1000)
                    }
                }
                chunkdataOfLogs.clear()
                logger.info("Send LogDataToMaster Complete")
            }*/


            //job.
            chunkdataOfLogs.forEach {
                val ContainerLogStruct = ListContianerLogWithIp.newBuilder()
                it.forEach {
                    val objectmapper = ObjectMapper()
                    val monitorlogs = objectmapper.writeValueAsString(it)
                    ContainerLogStruct.addContainerLog(monitorlogs)
                }
                ContainerLogStruct.ip=this.agentIP
                val listContianerLog = ContainerLogStruct.build()
                davidCommunicatorServiceGrpcStub.sendLogDataToMaster(listContianerLog)
                //davidCommunicatorServiceGrpcStub
            }
            logger.info("Send LogDataToMaster Complete")

            //checking Status
            if(!Configure.agentSwitch){
                logger.info("DAVIDCollector is goning to shutdown")
                break
            }
        }while (true)
    }

}