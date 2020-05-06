package iii.org.david.agent

import com.fasterxml.jackson.databind.ObjectMapper
import iii.org.david.agent.interfaces.AgentWorker
import iii.org.david.config.Configure
import iii.org.david.core.DockerBehavierCollector
import iii.org.david.inspects.BehaviorLogger


import iii.org.david.interfaces.core.DockerInformationCollector
import interfaces.ContainerListWithIp
import interfaces.DavidCommunicatorServiceGrpc
import interfaces.ListContainerHDInformationWithIp
import interfaces.ListRawLogStringWithIp
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/**
 * Created by jimmyhuang on 2018/7/23.
 */
class LoggerCollector(private val masterPort:String,
                      private val agentAddress:String):AgentWorker{

    var logger : Logger = LogManager.getLogger(LoggerCollector::class.java.name)
    private val dockerBehavierCollector = DockerBehavierCollector() //Log Data
    private val dockerInformationCollector = DockerInformationCollector()//HD Information

    //private var behaviorCount:Long = 0
    private var behaviorCountMean:Long = 0
    private val behaviorLogger = BehaviorLogger{
        logger.info("recive Log Size -> ${it.size}")
        //behaviorCount += it.size
        behaviorCountMean += it.size
        if(checkConnection() && (!isFinish)) {
            try {
                //it.forEach { logger.error(" -> $it") }
                handleBehaviorChunk(ArrayList(it))
            }catch (e: Exception){
                logger.error("Handle Chunked error -> $e")
                e.stackTrace.forEach { logger.error("\t $it") }
                if(!checkConnection()){
                    connectToMaster()
                }
            }
        }

    }

    val objectMapper = ObjectMapper()
    private var isFinish:Boolean = false
    private var isReconnect:Boolean = false
    private var behaviorLoggerJob:Job
    private lateinit var channelForDAVIDMaster : ManagedChannel
    private lateinit var davidCommunicatorServiceGrpcStub: DavidCommunicatorServiceGrpc.DavidCommunicatorServiceBlockingStub
    init {
        while(!connectToMaster()){
            logger.error("Connect to David Master error.")
            logger.error("Waiting 20s to reconnect." )
            Thread.sleep(20000)
        }
        behaviorLoggerJob = GlobalScope.async {
            behaviorLogger.run()
        }
    }

    //David Center
    private fun connectToMaster(): Boolean {

        val masterAddress = Configure.getDAVIDMasterIP()
        logger.info("David master address -> $masterAddress:${this.masterPort}")

        //ContainerBehaviorCollectServiceBlockingStub ManagedChannelBuilder.forAddress(hostname,port).usePlaintext(true).maxInboundMessageSize(1024*1024*1024).build()
        try {
            channelForDAVIDMaster = ManagedChannelBuilder
                    .forAddress(masterAddress, this.masterPort.toInt()).usePlaintext(true)
                    .maxInboundMessageSize(1024 * 1024 * 1024).build()
            davidCommunicatorServiceGrpcStub = DavidCommunicatorServiceGrpc.newBlockingStub(channelForDAVIDMaster)

        } catch (e: Exception) {
            logger.error("Unknown Exception $e")
            e.stackTrace.forEach { logger.error("\t $it") }
            return false
        }
        logger.info("connectToMaster complete")
        return true

    }

    fun handleBehaviorChunk(list:ArrayList<String>){
        logger.info("Begin to collect behavior data")
        val logDatagName = System.currentTimeMillis().toString()+".json"
        dockerBehavierCollector.init(logDatagName)
        dockerBehavierCollector.collect()
        val rawLogData = dockerBehavierCollector.rawLogList

        logger.info("Total Raw Log Size: ${rawLogData.size}")
        val chunkdataOfLogs = list.asSequence().chunked(250).toMutableList()
        logger.info("Total chunk ${chunkdataOfLogs.size} need to send ")

        chunkdataOfLogs.forEach {
            val rawLogStruct = ListRawLogStringWithIp.newBuilder()
            it.forEach {
                //val rawLogs = objectMapper.writeValueAsString(it)
                rawLogStruct.addRawlogstring(it)
            }
            rawLogStruct.ip = Configure.getDavidAgentIP()
            val listRawLog = rawLogStruct.build()
            davidCommunicatorServiceGrpcStub.sendRawLogDataToMaster(listRawLog)

        }
        logger.info("Send LogDataToMaster Complete")
        //dockerBehavierCollector.rawLogList.clear()
        //logger.info("Clear RawLogList")
        chunkdataOfLogs.clear()
    }

    private fun handleHDResourceRsultStruct(){

        logger.info("Collect HDResourceRsultStruct")
        val HDResourceRsultStruct = ListContainerHDInformationWithIp.newBuilder().clear().clearContainerHDInformation()
        val HDResourceRsult = dockerInformationCollector.getResource()

        logger.info("HDResourceRsult Size ${HDResourceRsult.size}")
        HDResourceRsult.map { objectMapper.writeValueAsString(it) }.forEach {
            HDResourceRsultStruct.addContainerHDInformation(it)
        }
        HDResourceRsultStruct.ip = this.agentAddress
        val HDDStructList =  HDResourceRsultStruct.build()
        davidCommunicatorServiceGrpcStub.sendHDInformationToMaster(HDDStructList)
        logger.info("Send HDInformationToMaster Complete")

    }
    private fun handleContainerList(){
        logger.info("Collect live container")
        val containerStruct = ContainerListWithIp.newBuilder()
        val containers = dockerBehavierCollector.discoveryLiveContainer()
        logger.info("ContainerSize ${containers.size}")
        if (containers.isNotEmpty()){
            containers.map { objectMapper.writeValueAsString(it) }
                    .forEach { containerStruct.addContainer(it) }
        }
        containerStruct.ip = this.agentAddress
        val listContainer =  containerStruct.build()
        davidCommunicatorServiceGrpcStub.discoveryLiveContainer(listContainer)
    }

    private fun checkConnection():Boolean{
        if(isReconnect)
            return false

        if(channelForDAVIDMaster.isTerminated
                || channelForDAVIDMaster.isShutdown){
            logger.info("Reconnect to Master")
            return false
        }
        return true
    }
    private fun isFinish():Boolean{
        if(isFinish){
            logger.info("DAVIDCollector is goning to shutdown")
            return true
        }
        return false
    }

    override fun run() {

        if(Configure.getVMVersion()){
            Timer().schedule(delay = 0, period = 6000) {
                if(checkConnection()) {
                    handleHDResourceRsultStruct()
                    if (isFinish()) this.cancel()
                }
            }
            Timer().schedule(delay = 0, period = 6000) {
                if(checkConnection()) {
                    handleContainerList()
                    if (isFinish()) this.cancel()
                }
            }
        }


        val period = 6
        Timer().schedule(delay = 0, period = period*1000.toLong()) {
          /*  if(checkConnection()) {
                handleContainerList()
                if (isFinish()) this.cancel()
            }*/
            val mean = behaviorCountMean/period
            behaviorCountMean = 0
            logger.info("BehaviorLoggerJob -> ${behaviorLoggerJob.isActive}")
            logger.info("BehaviorLoggerJob Mean Count -> $mean")
            //logger.info("BehaviorLoggerJob Behavior Count -> $behaviorCount")
        }
        //Behavior Monitor

        //Error with reconnection
        Timer().schedule(delay = 0, period = 30000) {
            if(!checkConnection()){
                isReconnect = true
                logger.error("LoggerCollection Connection lost.")
                logger.error("Start reconnection.")
                connectToMaster()
                isReconnect = false
            }
        }
    }

    override fun shutdown() {
        isFinish = true
    }


}