import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.POST
import com.mongodb.BasicDBObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.bson.Document
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import groovyx.net.http.HttpURLClient
import static groovyx.net.http.ContentType.JSON

/**
 * Created by shaopengxiang on 2016/11/15.
 */
class GuShiWenOrgDataLoader {
    def rootDir = "c:/dev/data/gushiwen.org/";
    def http = new HTTPBuilder('http://app.gushiwen.org/')

    def perform() {
        for (i in 27258..73208){
            postData(i);
//            sleep(1000);
        }

    }

    def postData(id) {
        try {

            http.request(POST, JSON) {
                uri.path = '/api/shiwen/view.aspx'
                requestContentType = URLENC
                body = [id: id, token: 'gswapi']

                response.success = { resp, json ->
//                println "POST response status: ${json}"
                    def fn = id + '_gushiwen.json'
                    try {
                        fn = id

                        if (json != null && json.tb_gushiwen != null && json.tb_gushiwen.author != null) {
                            def author = json.tb_gushiwen.author
                            if (!"null".equalsIgnoreCase(author))
                                fn += "_" + author;
                        }
                        if (json != null && json.tb_gushiwen != null && json.tb_gushiwen.nameStr != null) {
                            def nameStr = json.tb_gushiwen.nameStr
                            if (!"null".equalsIgnoreCase(nameStr))
                                fn += "_" + nameStr;
                        }
                        fn += ".json"
                        fn = formatString(fn)
                    } catch (ex) {
                        ex.printStackTrace()
                    }
                    saveToFile(id, fn, json.toString());
                    println 'save file:' + fn
                }
            }
        } catch (eex) {
            eex.printStackTrace()
            errorText(uid, "uid:" + uid + ", error:" + ex.getMessage());
        }
    }

    def errorText(int uid, String text) {
        System.err.println("error")
        saveToFile(uid, "error_" + uid + ".txt", text);
    }

    def formatString(String str) {
        return str.replaceAll("，", ",").replaceAll("。", ".").
                replace("<span class=\"comment\">", "(").
                replace("</span>", ")").replace("\t", "").replace("{", "(").replace("}", ")").replace("\\", "_").replace("/", "");
    }

    def getFileSubDir(int uid) {
        for (int i = 1000; i < 100000; i += 1000) {
            if (uid < i) {
                return rootDir + "/" + i + "/";
            }
        }
        return rootDir + "/big/";
    }

    def saveToFile(uid, fn, content) {
        def subDir = getFileSubDir(uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withPrintWriter { printWriter ->
            printWriter.println(content)
        }

        new File(subDir, "error_" + uid + ".txt").delete()
    }

    public static void main(args) {
        new GuShiWenOrgDataLoader().perform()
    }
}
