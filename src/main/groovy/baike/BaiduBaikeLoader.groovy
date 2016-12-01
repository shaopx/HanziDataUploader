package baike
/**
 * Created by SHAOPENGXIANG on 2016/11/28.
 */
import org.cyberneko.html.parsers.SAXParser

import static util.Utils.*

class BaiduBaikeLoader {
    def rootDir = "c:/dev/data/poem/baidu/";

    void perform() {
        requestWord('于塞北春日思归')
    }

    def requestWord(word) {
        def text = Utils.getUrlTextContent(rootDir, "http://baike.baidu.com/item/" + word);
        //println text

        def parser = new SAXParser()
        def page = new XmlSlurper(parser).parseText(text)

        def tags = page."**".findAll {
            it.name() == 'DIV' && it.@class == 'para-title level-2'
        }

        def engdTags = page."**".findAll {
            it.name() == 'DL' && it.@class == 'lemma-reference collapse nslog-area log-set-param'
        }
        println 'engdTags:' + engdTags.toString()

        def divs = page."**".findAll {
            it.name() == 'DIV' || it.name() == 'DL'
        }

        def divMap = [:]
        def divChilds = []
        def lastDivTag = null
        divs.each { div ->

            if (div in tags || div in engdTags) {
                if (lastDivTag) {
                    println div.toString()+":    key:"+lastDivTag+"--->"+divChilds.toString()
                    divMap.put(lastDivTag, divChilds)
                    divChilds.clear()
                }


                lastDivTag = div
                if (div in engdTags) {
                    lastDivTag = null
                }
            } else {
                if (lastDivTag)
                    divChilds << div
            }

        }

        println '=============================='
//        divMap.each { key,value ->
//            println key+">>>"+value.toString()
//        }

        divMap.each { it->
            println it.value
        }
        //println divMap.toString()

    }

    static void main(args) {
        new BaiduBaikeLoader().perform()
    }


}
