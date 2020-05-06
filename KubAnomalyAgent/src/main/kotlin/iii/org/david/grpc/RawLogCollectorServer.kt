package iii.org.david.grpc

import com.beust.klaxon.Klaxon
import com.google.protobuf.Empty
import iii.org.david.pool.RawLogNamePool
import interfaces.BooleanStrut
import interfaces.ListRawLog
import interfaces.RawLogCollectorServiceGrpc
import io.grpc.stub.StreamObserver
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths

class RawLogCollectorServer:RawLogCollectorServiceGrpc.RawLogCollectorServiceImplBase() {
    private val logger: Logger = LogManager.getLogger(RawLogCollectorServer::class.java.getName())
    override fun init(request: Empty?, responseObserver: StreamObserver<BooleanStrut>?) {
        super.init(request, responseObserver)
    }

    override fun getRawLog(request: Empty?, responseObserver: StreamObserver<ListRawLog>?) {
        val response = ListRawLog.newBuilder()

        while (RawLogNamePool.queueSize()!=0){
            val rawlog = RawLogNamePool.getLogOutPool()
            logger.info("Read and Parse RawLog")
            if(Files.exists(Paths.get(rawlog))){
               var rawLogs= Files.readAllLines(Paths.get(rawlog))
                rawLogs.forEach{
                    var json = Klaxon().parse<JSONObject>(it)
                    json!!.get("")
                }
            logger.info("Delete Monitor Log")


                Files.delete(Paths.get(rawlog))
            }
            logger.info("Delete Monitor Log Complete")
            response.addRawLog(rawlog)
        }

        responseObserver!!.onNext(response.build())
        responseObserver.onCompleted()

    }

}