package iii.org.david.core



import com.beust.klaxon.Klaxon
import iii.org.david.interfaces.structure.ContainerLog
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.File
import java.io.InputStream

import java.util.concurrent.ForkJoinPool
import kotlin.collections.ArrayList


class DAVIDLogProcessor  {
    var logger: Logger = LogManager.getLogger(DAVIDLogProcessor::class.java)
    var pools = ForkJoinPool()
    var start: Int = 0
    var end: Int = 0
    var threshold = 8000 // every 8000 line we create a new line
    var filterList = ArrayList<String>()
    var lineList = ArrayList<String>()
    var dockerclient = DockerClient()
    var resultArray = ArrayList<ContainerLog>()
    val jobs = arrayListOf<Job>()
    lateinit var testingArray: ArrayList<Int>

    fun init(linelist:ArrayList<String>):Boolean{

        this.lineList = linelist
       // pool = ForkJoinPool()
        var processors = Runtime.getRuntime().availableProcessors()
        threshold = lineList.size/processors
        logger.info("DAVIDLogProcessor init Complete")
        if(threshold <= 1){
            threshold = 2
        }
        return true
    }
    fun inittest(testarray:ArrayList<Int>){
        this.testingArray = testarray
        var processors = Runtime.getRuntime().availableProcessors()
        println("processors $processors")
        threshold = testingArray.size/processors
        println("Threshold $threshold")
        println("testingArraySize ${testingArray.size}")
    }

    fun divideToSmallArray() = runBlocking{
        var subarrayList = ArrayList<MutableList<String>>()
        var size = lineList.size
        var startIndex=0
        var endindex=threshold
        //Divide big array to small array
        while (true){
            println("StartIndex $startIndex")
            println("endindex $endindex")
            println("size $size")
            if (size-endindex<=0){
                subarrayList.add(lineList.subList(startIndex,lineList.size))
                break
            }
            subarrayList.add(lineList.subList(startIndex,endindex))
            startIndex=endindex
            endindex = threshold+startIndex


        }
        computes(subarrayList)
        jobs.forEach { it.join() }
    }
    fun computes(subarrayList: ArrayList<MutableList<String>>){


        subarrayList.forEach {
            jobs+= GlobalScope.launch  {
                resultArray.addAll(parseLog(it))

            }

        }

        println("localresult Size ${resultArray.size}")

    }


    //fun parseLog

    fun parseLog(linelist:MutableList<String>):ArrayList<ContainerLog>{
        var results = ArrayList<ContainerLog>()
        linelist.forEach{

            if(it.contains("Events")){
                logger.info("End of Log")
                return results
            }else if(!it.last().toString().equals("}")){
                logger.info("Not JSON Data Pass")
                println("Not JSON Data Pass")
            }else if(!it.first().toString().equals("{")){

            }else{

                val result = Klaxon().parse<ContainerLog>(it)
                val ContainerID :String= result!!.output.substring(result.output.indexOf("ID=") + 3, result.output.indexOf("ID=") + 16)
                var davidagentContainerID =""
                val resultContainer = dockerclient.listContainerID(false)
                resultContainer.forEach{
                    var davidagent = it.names.find { it.contains("davidagent") }
                    if(davidagent!=null){
                        davidagentContainerID = it.id
                    }
                }
                //print(ContainerID)
                if(ContainerID.contains(davidagentContainerID)){

                }else{
                    results.add(result)
                }
            }
        }
        return results
    }
    suspend fun sequentialsublist(sublist:MutableList<Int>):ArrayList<Int>{
        var result= ArrayList<Int>()

        sublist.forEach {
            result.add(it*it*it)
        }
        return result
    }
    fun sequential():ArrayList<Int>{
        var result= ArrayList<Int>()

        testingArray.forEach {
            result.add(it*it*it)
        }
        return result
    }

}



fun main(args: Array<String>) {
    val lineList = ArrayList<String>()
    val startTime3 = System.currentTimeMillis()
        val inputStream: InputStream = File("/home/peter/DockerScanSystem/DAVIDAgent/1521548520578_API.json").inputStream()
        //lineList = Files.re

        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

    val endTime3 = System.currentTimeMillis()
    println("Spend ${endTime3-startTime3} ms")
    /*var testarray = arrayListOf<Int>(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16)
    var testdatsize = 65000000

    while (testdatsize>0){
        val random = Random()
        testarray.add(random.nextInt())
        testdatsize--
    }*/
    var test = DAVIDLogProcessor()
    test.init(lineList)
    val startTime = System.currentTimeMillis()
    test.divideToSmallArray()
    val endTime = System.currentTimeMillis()
    println("Spend: ${endTime-startTime} ms")

    val startTime1 = System.currentTimeMillis()
    var resultseq=test.parseLog(lineList)
    val endTime1 = System.currentTimeMillis()
    println("Spend: ${endTime1-startTime1} ms")
    println("result size: ${resultseq.size}")

    //test.computes()
    var result = test.resultArray

    println("result size: ${result.size}")

    /*

    jobs += launch(Unconfined) { // not confined -- will work with main thread
        println("      'Unconfined': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(coroutineContext) { // context of the parent, runBlocking coroutine
        println("'coroutineContext': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(CommonPool) { // will get dispatched to ForkJoinPool.commonPool (or equivalent)
        println("      'CommonPool': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs += launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
        println("          'newSTC': I'm working in thread ${Thread.currentThread().name}")
    }
    jobs.forEach { it.join() }*/
}