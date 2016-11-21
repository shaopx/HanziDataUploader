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
                requestLinkJava(link)
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
                headers.'Accept-Language' = 'en-US,en;q=0.5'
                headers.Connection = 'keep-alive'

                response.success = { resp, reader ->

//                    println reader.text.getClass().getName();
                    def htmlStr = reader.text

//                    println htmlStr
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

    def requestLinkJava(link) {
        URL getUrl = new URL("http://www.eywedu.net/tangshi/001.htm");
        HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
        // 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
        // 服务器
        connection.connect();
        // 取得输入流，并使用Reader读取

//        InputStream input = connection.getInputStream();
//        File outFile = new File("bytes.data");
//        System.out.println("outFile:"+outFile.getAbsolutePath());
//        OutputStream output = new FileOutputStream(outFile);
//        byte[] buffer = new byte[12*1024];
//        int readed = 0;
//        while((readed= input.read(buffer))>0){
//            output.write(buffer, 0, readed);
//        }
//        output.close();

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));

        System.out.println("=============================");
        System.out.println("Contents of get request");
        System.out.println("=============================");
        String lines;
        StringBuilder sb = new StringBuilder()
        while ((lines = reader.readLine()) != null) {
            //lines = new String(lines.getBytes(), "utf-8");
            System.out.println(lines);
            sb.append(lines)
        }
        reader.close();
        // 断开连接
        connection.disconnect();

        handleHtml(sb.toString())
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
                    //def htmlStr = new String(reader.text.toString().getBytes("UTF-8"));
//                    def htmlStr = reader.text;
                    InputStreamReader isr = reader;
                    println "requestLink isr:" + isr.getClass().getName();
                    println "requestLink getEncoding:" + isr.getEncoding();
                    def htmlStr = reader.text
                    //def htmlStr = new String(reader.text.getBytes("UTF8"), "gb2312");

//                    def newStr = new String(htmlStr.getBytes("gb2312"), "UTF-8");

                    println htmlStr
                    //handleHtml(htmlStr)

                    new File("temp_utf8.txt").withWriter {
                        it.println(htmlStr)
                    }
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

        def title = page.depthFirst().find { it.name() == 'TITLE'}.text()
        println "title:"+title
        def author = title.subString(0, )

        def table = page."**".findAll { it.name() == 'TABLE' && it.@id == 'table4' }

        println table
    }

    static void main(args) {
        new TangshijianshangLoader().perform()
    }
}
