package util

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
            if (uid)
                return rootdir + "/" + uid + "/";
            return rootdir+"/"
        }
    }

    static void saveToFile(rootdir, uid, fn, content) {
        def subDir = getFileSubDir(rootdir, uid);
        def file = new File(subDir);
        if (!file.exists()) {
            file.mkdirs()
        }

        new File(subDir, fn).withWriter('UTF-8') { printWriter ->
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
        errorFileName= formatString(errorFileName)
        errorFileName = errorFileName.replace(":", "")
        errorFileName = errorFileName.replace("/", "_")
        errorFileName = errorFileName.replace("\\", "_")
        errorFileName = errorFileName.replace("<","")
        errorFileName = errorFileName.replace(">","")
        errorFileName = errorFileName.replace("=","")
        System.err.println("error:" + errorFileName)
        saveToFile(rootdir, '', "error_" + errorFileName + ".txt", text);
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

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
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


    private static int min(int one, int two, int three) {
        int min = one;
        if(two < min) {
            min = two;
        }
        if(three < min) {
            min = three;
        }
        return min;
    }

    public static int ld(String str1, String str2) {
        if(str1==null){
            str1="";
        }

        if(str2==null){
            str2="";
        }

        int[][] d;    //矩阵
        int n = str1.length();
        int m = str2.length();
        int i;    //遍历str1的
        int j;    //遍历str2的
        char ch1;    //str1的
        char ch2;    //str2的
        int temp;    //记录相同字符,在某个矩阵位置值的增量,不是0就是1
        if(n == 0) {
            return m;
        }
        if(m == 0) {
            return n;
        }
        d = new int[n+1][m+1];
        for(i=0; i<=n; i++) {    //初始化第一列
            d[i][0] = i;
        }
        for(j=0; j<=m; j++) {    //初始化第一行
            d[0][j] = j;
        }
        for(i=1; i<=n; i++) {    //遍历str1
            ch1 = str1.charAt(i-1);
            //去匹配str2
            for(j=1; j<=m; j++) {
                ch2 = str2.charAt(j-1);
                if(ch1 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                //左边+1,上边+1, 左上角+temp取最小
                d[i][j] = min(d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1]+temp);
            }
        }
        return d[n][m];
    }

    public static double sim(String str1, String str2) {
        int ld = ld(str1, str2);
        return 1 - (double) ld / Math.max(str1.length(), str2.length());
    }

    public static void main(args) {
        String str1 = "普安建阴题壁";
        String str2 = "普安剑阴题壁";
        System.out.println("ld="+ld(str1, str2));
        System.out.println("sim="+sim(str1, str2));
    }
}
