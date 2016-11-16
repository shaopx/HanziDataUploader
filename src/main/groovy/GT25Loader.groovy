import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import org.cyberneko.html.parsers.SAXParser
import groovy.util.XmlSlurper
import static Utils.*

/**
 * Created by Administrator on 2016/11/15.
 */
class GT25Loader {

    def type = "h25"
    def cookie = 'JSESSIONID=CDE720CE3C272CB9D73C9E4DDAAF51E7;comment=false;tradsimp=true;'
    def http = new HTTPBuilder()

    def rootPath = '/h25/'
    def indexPath = 'READ/書籍庫/二十五史'

    def targetPaths = [indexPath]

    def dir = "c:/dev/data/poem/" + type + "/";

    def builder = new JsonBuilder()

    void initEnv() {
        def file = new File(dir);
        if (!file.exists()) {
            file.mkdirs()
        }
    }

    def perform() {
        while (targetPaths.size() > 0) {
            def localPaths = targetPaths.clone();
            targetPaths.clear();

            for (path in localPaths) {
                requestPoem(path);
            }
        }
    }


    def requestPoem(targetPath) {
        println 'request ' + targetPath
        try {
            //http://202.106.125.44:8082/h25/READ/書籍庫/二十五史/史記/史記卷一 五帝本紀第一
            http.request('http://202.106.125.44:8082', GET, TEXT) { req ->
                uri.path = rootPath + targetPath
//                uri.query = [a: '1', e: '1', u: uid, b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Cookie = cookie
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    def htmlText = reader.text
                    //println htmlText
                    handleHtml(targetPath, htmlText)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            errorText(dir, targetPath, "target:" + targetPath + ", error:" + ex.getMessage());
        }
    }


    def handleHtml(String targetPath, String text) {

        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(text)

        page."**".findAll { it.name() == 'A' && it.@class == '' }.each {
            // doSomething with each entry
            //println it.text() + "==>" + it.@href.text()
            targetPaths.add(it.@href.text())
        }

        def docDivNode = page.depthFirst().find { it.name() == 'DIV' && it.@class == 'doc' };
        if (docDivNode) {
            def title = docDivNode.H1?.text()
            println "title:" + title

            def content = docDivNode.DIV?.DIV?.P?.text()
            if (title && content) {
                println "content:" + content.substring(0, 64)
                def fname = title + ".txt"
                fname = formatString(fname)
                saveToFile(dir, targetPath, fname, content)
            }
        }


    }


    public static void main(args) {
        new GT25Loader().perform()
    }
}
