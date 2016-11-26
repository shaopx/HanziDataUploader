import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by SHAOPENGXIANG on 2016/11/26.
 */


def reg = ~/\B-\B/
println reg.matcher("asdfasfdasdf - adsfasdfsadf").matches()

def text ="\t　　作者：唐&middot;白居易&nbsp;</p>"

def matcher = text =~ ~/作者[:：]?唐?(&middot;)?(.+)\)?(?=&nbsp;)(唐诗赏析)?/
println matcher.getClass().getName()
println matcher.size()
matcher.each {
    println it.getClass().getName()
    println it
    println it.size()
    println it[1]
}


//Pattern pattern = Pattern.compile("<.+?>");
//Matcher matchermj = pattern.matcher("<a href=\"index.html\">主页</a>");
//matchermj.each {
//    println it.getClass().getName()
//    println it
//}
//String string = matchermj.replaceAll("");
//System.out.println(string);
//
//pattern = Pattern.compile("href=\"(.+?)\"");
//matcher = pattern.matcher("<a href=\"index.html\">主页</a>");
////matcher.each {
////    println it.getClass().getName()
////    println it
////}
//if(matcher.find()){
//    System.out.println(matcher.group(0));
//}
//
//
//pattern = Pattern.compile("(?<=<TITLE>).*(?=</TITLE>)");
//matcher = pattern.matcher("<head><TITLE>这是title text</TITLE></head>");
////matcher.each {
////    println it.getClass().getName()
////    println it
////}
//if(matcher.find()){
//    System.out.println(matcher.group(0));
//}