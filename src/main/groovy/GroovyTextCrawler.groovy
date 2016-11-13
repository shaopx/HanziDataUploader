import edu.uci.ics.crawler4j.crawler.WebCrawler

import java.util.regex.Pattern

/**
 * Created by Administrator on 2016/11/13.
 */
class GroovyTextCrawler extends WebCrawler{

    static FILTERS = ~".*(.(css|js|gif|jpg|png|mp3|mp3|zip|gz))\$";
//    final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
//            + "|png|mp3|mp3|zip|gz))\$");

    public static void main(String[] args){
        def dddd = ~"hello";
        def excludedPattern = ~".*(/.(css|js|gif|jpg))\$";
        println 'dddd:'+dddd
        println ''+excludedPattern

        def href ="http://www.runoob.com/regexp/regexp-syntax.jpg";
        println 'matched:'+FILTERS.matcher(href).matches()
        if(href=~FILTERS){
            println 'groovy matched!'
        }

    }
}
