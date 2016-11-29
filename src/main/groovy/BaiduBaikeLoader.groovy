/**
 * Created by SHAOPENGXIANG on 2016/11/28.
 */
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovyx.net.http.HTTPBuilder
import org.cyberneko.html.parsers.SAXParser

import java.util.regex.Matcher
import java.util.regex.Pattern

import static Utils.errorText
import static Utils.formatException
import static Utils.errorText
import static Utils.formatString
import static Utils.saveToFile
import static Utils.*
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET



class BaiduBaikeLoader {
    def rootDir = "c:/dev/data/poem/baidu/";
    void perform() {
        requestWord('测试')
    }

    def requestWord(word){
        def text = getUrlTextContent(rootDir, "http://baike.baidu.com/item/温泉庄卧病寄杨七炯");
        println text
    }

    static void main(args) {
        new BaiduBaikeLoader().perform()
    }


}
