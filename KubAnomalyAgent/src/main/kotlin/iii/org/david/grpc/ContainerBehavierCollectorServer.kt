package iii.org.david.grpc

import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.dockerjava.api.model.Container
import com.google.protobuf.Empty
import iii.org.david.config.Configure
import iii.org.david.core.DockerBehavierCollector
import iii.org.david.core.DockerClient

import iii.org.david.interfaces.structure.ContainerLog
import iii.org.david.pool.RawLogNamePool
import interfaces.*
import io.grpc.stub.StreamObserver

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import utils.ExternalProcessHandleError
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeoutException


class ContainerBehavierCollectorServer:ContainerBehaviorCollectServiceGrpc.ContainerBehaviorCollectServiceImplBase() {
    val logger : Logger = LogManager.getLogger(ContainerBehavierCollectorServer::class.java.getName())
    var containerBehaiverCollector = DockerBehavierCollector()
    var dockerclient = DockerClient()
    var falco = ExternalProcessHandleError()
    lateinit var logName:String
    var normalizeLog = ArrayList<ContainerLog>()
    override fun init(request: StringStruct, responseObserver: StreamObserver<BooleanStrut>) {
        var uuid = UUID.randomUUID()
        containerBehaiverCollector.init(uuid.toString())
        logger.info("ContainerBehavierCollectorServer init Complete")
        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }

    override fun collect(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        logger.info("Reset Log")
        normalizeLog.clear()
        logger.info("Begin Collect")
        logger.info("Looger will start record 10 sec")
        logName = UUID.randomUUID().toString()+".json"
        try {
            falco.exec("timeout 15 falco -A > "+logName,true,true,10.toLong()*1000)
        }catch (e: TimeoutException){
            logger.error("TimeoutException: "+e.toString())
        }

        logger.error("Error Message "+falco.errorMsg)
        logger.error("OutPut Message "+falco.outputMsg)

        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        logger.info("Log Normalizeing")
        logNormalizer()
        logger.info("Log Size: ${normalizeLog.size}")
        logger.info("Collect Complete")
        logger.info("Put Log Name into pool")
        RawLogNamePool.putLogIntoPool(logName)
        /*logger.info("Delete Monitor Log")
        if(Files.exists(Paths.get(logName))){
            Files.delete(Paths.get(logName))
        }
        logger.info("Delete Monitor Log Complete")*/
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()
    }

    override fun killMonitorProcess(request: Empty?, responseObserver: StreamObserver<BooleanStrut>?) {

    }

    override fun getMonitorLogs(request: Empty, responseObserver: StreamObserver<ListContainerLog>) {

        val result = normalizeLog

        val response = ListContainerLog.newBuilder()

        if (result.isEmpty()){
            logger.info("Data is Empty")
        }else{

            result.forEach outer@{
                //Filter Agent Rawlog with mutiple containerID
                var eachContianerLog = it
                Configure.AgentID.forEach {
                    if(eachContianerLog.containerid.contains(it)){

                    }else{
                        var objectMapper = ObjectMapper()
                        var containerLogString = objectMapper.writeValueAsString(it)
                        response.addContainerLog(containerLogString)
                        return@outer
                    }
                }
            }
        }
        responseObserver.onNext(response.build())
        responseObserver.onCompleted()
    }

    override fun deinit(request: Empty, responseObserver: StreamObserver<BooleanStrut>) {
        logger.info("Deinit has no function")
        val reponse :BooleanStrut = BooleanStrut.newBuilder().setScucces(true).build()
        responseObserver.onNext(reponse)
        responseObserver.onCompleted()

    }

    override fun discoveryLiveContainer(request: Empty, responseObserver: StreamObserver<ContainerList>) {
        val result = dockerclient.listContainerID(false)
        val filterdresult:ArrayList<Container> = ArrayList()
        logger.info("discoveryLiveContainer list containers ${result.size}")
        val response = ContainerList.newBuilder()
        try {
            result.forEach{
                var flag = true
                it.names.forEach {
                    if(it.contains("composedasbydockerfile_scheduler_1")||it.contains("composedasbydockerfile_activemq_1")
                            ||it.contains("composedasbydockerfile_analyzer_1")
                            ||it.contains("composedasbydockerfile_containermonitor_1")
                            ||it.contains("composedasbydockerfile_dbagent_1")
                            ||it.contains("composedasbydockerfile_mongodb_1")
                            ||it.contains("composedasbydockerfile_restservice_1")
                            ||it.contains("davidui_webservice_1")
                            ||it.contains("davidui_mongodb_1")
                            ||it.contains("davidagent")){

                        flag = false
                    }
                }
                if (flag){
                    filterdresult.add(it)
                    var objectmapper = ObjectMapper()
                    var containerstring = objectmapper.writeValueAsString(it)
                    response.addContainer(containerstring)
                }
            }
        }catch (e:Exception){
            logger.error("discoveryLiveContainer error $e")
        }
        logger.info("valid container num ${filterdresult.size}")
        responseObserver.onNext(response.build())
        responseObserver.onCompleted()
    }

    fun logNormalizer():Boolean{

        if(Files.exists(Paths.get(logName))){
            var rawdata = Files.readAllLines(Paths.get(logName))
            try {
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
                        logger.error(e.toString())
                    }
                }
            }catch (e:Exception){
                logger.error("Unknow Exception $e")
            }

        }else {
            logger.error("File Not Found")
        }
        logger.info("Complete normalize deleteLogFile")
       /* if(Files.exists(Paths.get(logName))){
            Files.delete(Paths.get(logName))
        }*/

        return true
    }

}