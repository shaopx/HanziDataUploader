import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.HTML
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.Method.GET

/**
 * Created by shaopengxiang on 2016/11/11.
 */
class BaiduDataLoader {


    public void load(){
        def http = new HTTPBuilder('https://www.baidu.com/s?wd=%E6%AC%A7%E9%98%B3%E4%BF%AE&rsv_spt=1&rsv_iqid=0xa33693f300024d11&issp=1&f=8&rsv_bp=0&rsv_idx=2&ie=utf-8&tn=baiduhome_pg&rsv_enter=1&rsv_sug3=2&rsv_n=2')

        def html = http.get( path : '/s', query : [wd:'%E6%AC%A7%E9%98%B3%E4%BF%AE'] )

        assert html instanceof groovy.util.slurpersupport.GPathResult
        assert html.HEAD.size() == 1
        assert html.BODY.size() == 1

        println html
        println html.BODY
    }

    public static void main(String[] args) throws Exception {
        BaiduDataLoader loader = new BaiduDataLoader();
        loader.load();
    }
}
