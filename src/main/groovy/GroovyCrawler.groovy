import edu.uci.ics.crawler4j.crawler.Page
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.url.WebURL

/**
 * Created by Administrator on 2016/11/13.
 */
class GroovyCrawler extends GroovyTextCrawler{

    def crawlDomain = "gushiwen.org";

    @Override
    boolean shouldVisit(Page referringPage, WebURL url) {
        println 'GroovyCrawler should visit :'+url
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches() && href.contains(crawlDomains);
    }

    @Override
    void visit(Page page) {
        println 'GroovyCrawler visit :'+page.getWebURL()
        super.visit(page)
    }
}
