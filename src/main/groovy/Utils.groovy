import groovy.util.slurpersupport.NodeChild

/**
 * Created by shaopengxiang on 2016/11/15.
 */
class Utils {

    static String getFileSubDir(rootdir, uid) {
        if (uid instanceof Integer) {
            for (int i = 1000; i < 1000000; i += 1000) {
                if (uid < i) {
                    return rootdir + "/" + i + "/";
                }
            }
            return rootdir + "/big/";
        } else {
            return rootdir + "/" + uid + "/";
        }
    }

    static void saveToFile(rootdir, uid, fn, content) {
        def subDir = getFileSubDir(rootdir, uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withPrintWriter { printWriter ->
            printWriter.append(content)
        }

        if (!fn.startsWith("error")) {
            new File(subDir, "error_" + uid + ".txt").delete()
        }

    }


    static String formatException(Throwable ex){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        def exception = sw.toString();
    }

    static void errorText(rootdir, uid, text) {
        uid = uid.toString()
        def errorFileName = formatString(uid+"")
        System.err.println("error" + errorFileName)
        saveToFile(rootdir, uid, "error_" + errorFileName + ".txt", text);
    }

    static String formatString(String str) {
        return str.replaceAll("，", ",").replaceAll("。", ".").
                replace("<span class=\"comment\">", "(").
                replace("</span>", ")").replace("\t", "").replace("{", "(").replace("}", ")").replace("\\", "_").replace("/", "");
    }

    static String getPContent(pNode, includeSpan=true) {
        //println "pNode class:"+pNode.getClass().getName()
        def mnode = pNode;
        if (pNode instanceof NodeChild) {
            //groovy.util.slurpersupport.NodeChild
            mnode = pNode.getAt(0);
            //println "mnode class:"+mnode.getClass().getName()
        }


        def result = ""
        for (leefnode in mnode.children()) {
//                    println 'leefnode.class:' + leefnode.getClass().getName()
            if (leefnode instanceof String) {
                result += leefnode
            } else if (includeSpan && leefnode instanceof groovy.util.slurpersupport.Node) {
//                        println 'leefnode.name:' + leefnode.name()
                result += "<" + leefnode.name().toLowerCase()


                def attrs = leefnode.attributes()
                attrs.each { k, v ->
                    result += " " + k + " = '" + v + "'"
                }
                result += ">" + leefnode.children()[0] + "</" + leefnode.name().toLowerCase() + ">"
                //result += "<" + leefnode.name() + " class = '" + leefnode.attributes()['class'] + "'>" + leefnode.children()[0] + "</" + leefnode.name() + ">"
            }
        }

        return result;
    }


    public static String getUrlTextContent(String rootDir, String url){
        try {

            URL getUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            // 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
            // 服务器
            connection.connect();
            // 取得输入流，并使用Reader读取

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "GBK"));
            String lines;
            StringBuilder sb = new StringBuilder()
            while ((lines = reader.readLine()) != null) {
                //lines = new String(lines.getBytes(), "utf-8");
                //System.out.println(lines);
                sb.append(lines+"\n")
            }
            reader.close();
            // 断开连接
            connection.disconnect();

           return sb.toString();
        } catch (Throwable ex) {
            println "exception when request" + url
            ex.printStackTrace()
            def exception = formatException(ex);
            errorText(rootDir, url, "error:" + exception);
            //errorLinks.add(link)
        }
    }

    public static void main(args) {
        def subDir = getFileSubDir('d:/dev', 3457)
        println subDir
    }
}
