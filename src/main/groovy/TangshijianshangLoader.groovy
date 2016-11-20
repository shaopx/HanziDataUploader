import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

import static Utils.errorText
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET

/**
 * Created by Administrator on 2016/11/20.
 */
class TangshijianshangLoader {

    def rootDir = "c:/dev/data/poem/tagnshijianshang/";

    def links = []

    void perform() {
        try {
            requestIndexs()

            for (def link in links[0..0])
                requestLink(link)
        } catch (ex) {
            ex.printStackTrace()
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            def exception = sw.toString();
            errorText(rootDir, "root", "error:" + exception);
        }
    }

    def requestIndexs() {
        try {
            def http = new HTTPBuilder()
            http.request('http://www.eywedu.net/', GET, TEXT) { req ->
                uri.path = "/tangshi/"
//                uri.query = [a: '1', e: '0', u: uid+"", b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->
                    def htmlStr = reader.text
                    //println htmlStr
                    handleIndexHtml(htmlStr)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            def exception = sw.toString();
            errorText(rootDir, "index", "error:" + exception);
        }
    }

    def requestLink(link) {
        try {
            def http = new HTTPBuilder()
            http.request('http://www.eywedu.net/', GET, TEXT) { req ->
                uri.path = "/tangshi/" + link
//                uri.query = [a: '1', e: '0', u: uid+"", b: '4']
                headers.'User-Agent' = "Mozilla/5.0 Firefox/3.0.4"
                headers.Accept = 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                headers.Connection = 'keep-alive'
//                headers.'content-type' = "text/html; charset=gb2312"

                response.success = { resp, reader ->
                    def htmlStr = new String(reader.text.toString().getBytes("UTF-8"));
                    println htmlStr
                    handleHtml(htmlStr)
                }

                response.'404' = {
                    println 'Not found'
                }
            }
        } catch (ex) {
            ex.printStackTrace()
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            def exception = sw.toString();
            errorText(rootDir, link, "error:" + exception);
        }
    }

    def handleIndexHtml(htmlStr) {
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(htmlStr)

        links = page."**".findAll {
            it.name() == 'A' && it.@href != 'index.html' && it.@href.toString().endsWith("htm")
        }.collect() {
            it.@href
        }

        println links
    }

    def handleHtml(htmlStr) {
        def parser = new SAXParser()

        def page = new XmlSlurper(parser).parseText(htmlStr)

        def table = page."**".findAll { it.name() == 'TABLE' && it.@id != 'table4' }

        println table
    }

    static void main(args) {
        new TangshijianshangLoader().perform()
    }
}
