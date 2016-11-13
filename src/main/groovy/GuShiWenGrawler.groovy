import edu.uci.ics.crawler4j.crawler.Page
import edu.uci.ics.crawler4j.parser.HtmlParseData
import edu.uci.ics.crawler4j.url.WebURL

/**
 * Created by Administrator on 2016/11/13.
 */
class GuShiWenGrawler extends GroovyTextCrawler {

    def domain = "gushiwen.org";
    def domainPattern = ~".*$domain";

    def tags = "首页, 诗文, 名句, 典籍, 作者, 随便看看, 我的收藏, app下载, 先秦, 两汉, 魏晋, 南北朝, 隋代, 唐代, 五代, 宋代, 金朝, 元代, 明代, 清代, 唐代, 类型, 不限, 写景, 咏物, 春天, 夏天, 秋天, 冬天, 写雨, 写雪, 写风, 写花, 梅花, 荷花, 菊花, 柳树, 月亮, 山水, 写山, 写水, 长江, 黄河, 儿童, 写鸟, 写马, 田园, 边塞, 地名, 抒情, 爱国, 离别, 送别, 思乡, 思念, 爱情, 励志, 哲理, 闺怨, 悼亡, 写人, 老师, 母亲, 友情, 战争, 读书, 惜时, 婉约, 豪放, 诗经, 民谣, 节日, 春节, 元宵节, 寒食节, 清明节, 端午节, 七夕节, 中秋节, 重阳节, 忧国忧民, 咏史怀古, 宋词精选, 小学古诗, 初中古诗, 高中古诗, 小学文言文, 初中文言文, 高中文言文, 古诗十九首, 唐诗三百首, 古诗三百首, 宋词三百首, 古文观止, 更多>>, 朝代, 不限, 先秦, 两汉, 魏晋, 南北朝, 隋代, 唐代, 五代, 宋代, 金朝, 元代, 明代, 清代, 形式, 不限, 诗, 词, 曲, 文言文".tokenize(', ')
    def endTags = "写翻译,  写赏析,  纠错,  下载, 评分：, 很差较差还行推荐力荐, 参考翻译,  写翻译, 译文及注释".tokenize(', ');
    static def storagePath = 'd:/data/crawler/gushiwen/'

    static File file = new File(storagePath);
    static {
        if(!file.exists()) file.mkdirs();
    }


    @Override
    boolean shouldVisit(Page referringPage, WebURL url) {
        //System.out.println('GuShiWenGrawler should visit :' + url)
        String href = url.getURL().toLowerCase();
        return !(href =~ FILTERS) && href =~ domainPattern;
//        println "flag:"+flag
    }

    @Override
    void visit(Page page) {
        try {
            def url = page.getWebURL();
            println 'GuShiWenGrawler visit :' + url
            if (page.getParseData() instanceof HtmlParseData) {

                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                String text = htmlParseData.getText();
                String html = htmlParseData.getHtml();
                def title = htmlParseData.getTitle();

                String fileName = MD5Calculator.calculateMD5(url.toString())
                new File(storagePath + fileName+".txt").withPrintWriter { printWriter ->
                    printWriter.println(url);
                    printWriter.println(title);
                    printWriter.println(text)
                }

//                if (text != null && text.trim().length() > 0)
//                    readLines(url, text, new Object(){
//                        void call(Article article){
//                            if(article==null) return;
//                            println "title:"+article.title;
//                            println "author:"+article.author;
//                            println "chaodai:"+article.chaodai;
//                            println "content:"+article.content;
//                            println "yiwen:"+article.yiwen;
//                        }
//                    });
            }
        } catch (ex) {
            ex.printStackTrace()

        }


    }


    def readLines(url, text, callback) {
        Article article = new Article();
        article.url = url;
        println 'read lines :' + text.getClass().getName()


        def lines = text.tokenize('\n')
        println 'read lines :' + lines

        boolean init = true;
        boolean yuanwenStarted = false;
        for (line in lines) {
            if (tags.contains(line) && init) {
                continue;
            }
            if (line.contains("© 2016 古诗文网")) {
                continue;
            }

            init = false;


            if (yuanwenStarted) {
                article.data = line;
                yuanwenStarted = false;
            }

            if (line == "原文：") {
                yuanwenStarted = true;
            } else if (line.startsWith("作者：") && line.length() > 3) {
                article.author = line.substring(3);
            } else if (line.startsWith("朝代：") && line.length() > 3) {
                article.chaodai = line.substring(3);
            } else if (line.startsWith("译文　") && line.length() > 4) {
                article.yiwen = line.substring(4);
            } else if (article.title == null) {
                article.title = line;
            } else if (endTags.contains(line)) {
                continue;
            }
            println line;
        }

        callback(article);
    }


    public static void main(String[] args) {
        def domain = "gushiwen.org";
        def domainPattern = ~".*$domain";
        println 'domainPattern:' + domainPattern;
        def href = "http://so.gushiwen.org/regexp/regexp-syntax.jpg";

        if (href =~ domainPattern) {
            println 'groovy matched!'
        }

    }
}
