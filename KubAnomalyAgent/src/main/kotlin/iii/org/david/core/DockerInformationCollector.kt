package iii.org.david.interfaces.core



import iii.org.david.config.Configure
import iii.org.david.interfaces.ContainerInformationCollector
import iii.org.david.interfaces.structure.ContainerHDInformation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import utils.ExternalProcessHandleError


class DockerInformationCollector:ContainerInformationCollector {
    var commandExecutor = ExternalProcessHandleError()
    var logger: Logger = LogManager.getLogger(DockerInformationCollector::class.java.getName())
    override fun init(): Boolean {
        logger.info("DockerInformationCollector init complete")
        return true
    }
    inline fun <reified T> List<T>.foreach(crossinline invoke:(T) -> Unit):
            Unit{
        val size =size
        var i=0
        while (i<size){
            invoke(get(i))
            i++
        }
    }
    override fun getResource(): ArrayList<ContainerHDInformation> {
        val containerResourcesData = ArrayList<ContainerHDInformation>()
        val queryStatusCommand="timeout 4 docker stats --no-stream --format \"{{.Container}}: {{.CPUPerc}} : {{.MemUsage}} : {{.MemPerc}} : {{.NetIO}} : {{.BlockIO}} ::\" "

        try {
            logger.info("Ready to collect container information $queryStatusCommand")
            commandExecutor.exec(queryStatusCommand,true,true,20000)
        }catch (e:Exception){
            logger.error("Start Contaner error: "+e.toString())
        }

        parseResourceDataForTer(commandExecutor.outputMsg,containerResourcesData)


        logger.info("ContainerResourceSize ${containerResourcesData.size}")
        return containerResourcesData
    }
    fun parseResourceDataForTer(data:String,resourceData:ArrayList<ContainerHDInformation>):Boolean{
        val unfilterData = ArrayList<ContainerHDInformation>()
       // logger.info(data)
        val eachContainer = data.split("::")
        eachContainer.forEach {
            val eachInfo = it.split(":")
            var counter = 0
            var containerName  = ""
            var cpu  = ""
            var MEMUSAGE  = ""
            var NETIO  = ""
            var BLOCKIO  = ""
            var MEMUSAGEPercentage = ""
            eachInfo.forEach {
                val content = it.replace(" ","")

                if(content.length>0){
                    when(counter%7){
                        0 -> {
                            containerName =content
                            //loggers.info("CONTAINER ${containerName}")
                        }
                        1 ->{
                            cpu = content
                            // loggers.info("CPU% ${cpu}")
                        }
                        2 -> {
                            MEMUSAGE = content
                            // loggers.info("MEMUSAGE/LIMITMEM% ${MEMUSAGE}")
                        }
                        3 -> {
                            MEMUSAGEPercentage = content
                            // loggers.info("MEMUSAGE/LIMITMEM% ${MEMUSAGEPercentage}")
                        }
                        4 -> {
                            NETIO = content
                            // loggers.info("NETI/O ${NETIO}")
                        }
                        5 -> {
                            BLOCKIO = content
                            //loggers.info("BLOCKI/O ${BLOCKIO}")
                        }
                    }
                    if((counter%6)==5){

                        var eachData = ContainerHDInformation(iii.org.david.config.Configure.getDavidAgentIP(),containerName,cpu,MEMUSAGE,MEMUSAGEPercentage,NETIO,BLOCKIO)
                        //logger.info("Each Data $eachData")

                        if(!containerName.contains("CONTAINER")){

                            unfilterData.add(eachData)
                        }
                    }
                    counter ++
                }
            }
        }
        //Filter same container data
        unfilterData.forEach outer@{
            if(finddata(it.containerName,resourceData)){

            }else{
               if(it.containerName !in Configure.AgentID){
                   resourceData.add(it)
               }
            }
        }
        return true
    }

    fun finddata(dataWantToFind:String,dataList:ArrayList<ContainerHDInformation>):Boolean{
        var result = false
        dataList.forEach {
            if(it.containerName.contains(dataWantToFind)){
                result =true
            }
        }
        return result
    }
    override fun getIP(): String {

        //Configure.getDavidAgentIP()
        //read From ConfigFile
       /* val getHostIPCommand = "/sbin/ip route|awk '/default/ { print \$3 }'"
        try {
            commandExecutor.exec(getHostIPCommand,true,true,30000)
        }catch (e:Exception){
            logger.error(e.toString())
        }*/
        return  Configure.getDavidAgentIP()
    }

    override fun deinit(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}