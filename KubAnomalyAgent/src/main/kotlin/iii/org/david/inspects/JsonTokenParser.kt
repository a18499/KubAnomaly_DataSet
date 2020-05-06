package iii.org.david.inspects

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.async.ByteArrayFeeder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.TokenBuffer
import iii.org.david.inspects.interfaces.LoggerReceiver

import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.IOException



/**
 * Created by jimmyhuang on 2018/7/25.
 */
@Deprecated("This is going to deprecated")
open class JsonTokenParser(receiver: LoggerReceiver){


    var logger : Logger = LogManager.getLogger(JsonTokenParser::class.java)
    var mapper = ObjectMapper()
    var jsonParser: JsonParser
    var jsonFeeder: ByteArrayFeeder
    var objectDepth:Int = 0
    var arrayDepth:Int = 0

    private var tokenizeArrayElements: Boolean = false
    private var tokenBuffer: TokenBuffer? = null

    init {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
        mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true)
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        val factory = mapper.factory
        jsonParser = factory.createNonBlockingByteArrayParser()
        jsonFeeder = jsonParser.nonBlockingInputFeeder as ByteArrayFeeder
        tokenBuffer = TokenBuffer(jsonParser)

        /*launch {
            while (true) {
                val list = doJsonParser()
                if(!list.isEmpty()) {
                    val result = list.map {
                        val node = mapper.readTree(it.asParser()) as JsonNode
                        //logger.info("-> ${node}")
                        node.toString()
                    }.toMutableList()
                    receiver.processNewLines(ArrayList(result))
                }
                //receiver.processNewLines(list.map {(mapper.readTree(it.asParser()) as JsonNode)})
                //Thread.sleep(200)
            }
        }*/
    }
    private fun doJsonParser(): ArrayList<TokenBuffer>{
        val list = ArrayList<TokenBuffer>()
        try {
            do {
                if(list.size > 10){
                    break
                }
                val token = jsonParser.nextToken()
                if ((token == JsonToken.NOT_AVAILABLE) ||
                        (token == null && jsonParser.nextToken() == null)) {
                    break
                }
                this.updateDepth(token)
                if(this.tokenizeArrayElements)
                    this.processTokenArray(token, list)
                else
                    this.processTokenNormal(token, list)
                //Thread.sleep(5)
            }while (true)
        }catch (e:Exception){
            //logger.error("Parse -> $e")
        }
        finally {
            return list
        }
    }
    fun feedData(byteArray: ByteArray){
        while(!jsonFeeder.needMoreInput()) {
            //list.addAll(doJsonParser())
            Thread.sleep(50)
        }
        jsonFeeder.feedInput(byteArray, 0, byteArray.size)
    }

    private fun updateDepth(token: JsonToken) {
        when (token) {
            JsonToken.START_OBJECT -> this.objectDepth++
            JsonToken.END_OBJECT -> this.objectDepth--
            JsonToken.START_ARRAY -> this.arrayDepth++
            JsonToken.END_ARRAY -> this.arrayDepth--
        }
    }

    @Throws(IOException::class)
    private fun processTokenNormal(token: JsonToken, result: ArrayList<TokenBuffer>) {
        this.tokenBuffer!!.copyCurrentEvent(this.jsonParser)

        if ((token.isStructEnd || token.isScalarValue) &&
                this.objectDepth === 0 && this.arrayDepth === 0) {

            result.add(this.tokenBuffer!!)
            this.tokenBuffer = TokenBuffer(this.jsonParser)

        }

    }

    @Throws(IOException::class)
    private fun processTokenArray(token: JsonToken, result: MutableList<TokenBuffer>) {

        if (!isTopLevelArrayToken(token)) {
            this.tokenBuffer!!.copyCurrentEvent(this.jsonParser)
        }

        if (this.objectDepth === 0 &&
                (this.arrayDepth === 0 || this.arrayDepth === 1) &&
                (token == JsonToken.END_OBJECT || token.isScalarValue)) {
            result.add(this.tokenBuffer!!)
            this.tokenBuffer = TokenBuffer(this.jsonParser)
        }
    }

    private fun isTopLevelArrayToken(token: JsonToken): Boolean {
        return this.objectDepth === 0 && (token == JsonToken.START_ARRAY && this.arrayDepth === 1 || token == JsonToken.END_ARRAY && this.arrayDepth === 0)
    }

}