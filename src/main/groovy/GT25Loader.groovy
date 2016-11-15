import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import org.cyberneko.html.parsers.SAXParser
import groovy.util.XmlSlurper

/**
 * Created by Administrator on 2016/11/15.
 */
class GT25Loader {

    def type = "h25"
    def cookie = 'JSESSIONID=4076423FD12B776EE46A1FCDC83F0408'
    def http = new HTTPBuilder()

    def dir = "c:/dev/data/poem/" + type + "/";

    def builder = new JsonBuilder()

    def perform() {

        def file = new File(dir);
        if (!file.exists()) {
            file.mkdirs()
        }

        for (i in 1..1) {
            requestPoem(i);
        }

    }


    def requestPoem(uid) {
        try {
            //http://202.106.125.44:8082/h25/READ/書籍庫/二十五史/史記/史記卷一 五帝本紀第一
            http.request('http://202.106.125.44:8082', GET, TEXT) { req ->
                uri.path = "/h25/READ/書籍庫/二十五史"
                uri.query = [a: '1', e: '1', u: uid, b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Cookie = cookie
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    def htmlText = reader.text
                    println htmlText
                    handleHtml(uid, htmlText)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            errorText(uid, "uid:" + uid + ", error:" + ex.getMessage());
        }
    }


    def handleHtml(int uid, String text) {

        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(text)

        page."**".findAll { it.@class.toString().contains("book") }.each {
            // doSomething with each entry
            println it.text() + "==>" + it.A[0].@href.text()
        }

//        println page.depthFirst().find { it.text() == '元史'}
//        println page.depthFirst().find { it.text().contains('明史')}

//        def poemFileName = uid + "_" + poemAuthor + "_" + poemTitle + ".json";

//        builder.poem{
//            n poemTitle
//            a poemAuthor
//            r poemRemark
//            c poemContent
//            x poemContentExplain
//            b poemAuthorDesc
//        }
//
//        saveToFile(uid, poemFileName, JsonOutput.prettyPrint(builder.toString()))


    }

    def formatString(String str) {
        return str.replaceAll("，", ",").replaceAll("。", ".").
                replace("<span class=\"comment\">", "(").
                replace("</span>", ")").replace("\t", "").replace("{", "(").replace("}", ")").replace("\\", "_").replace("/", "");
    }

    def errorText(int uid, String text) {
        System.err.println("error")
        saveToFile(uid, "error_" + uid + ".txt", text);
    }

    def getFileSubDir(int uid) {
        for (int i = 1000; i < 100000; i += 1000) {
            if (uid < i) {
                return dir + "/" + i + "/";
            }
        }
        return dir + "/big/";
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
        new GT25Loader().perform()
    }
}
