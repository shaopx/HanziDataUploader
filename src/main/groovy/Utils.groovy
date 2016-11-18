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
            printWriter.println(content)
        }

        if (!fn.startsWith("error")) {
            new File(subDir, "error_" + uid + ".txt").delete()
        }

    }


    static void errorText(rootdir, uid, text) {

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

    public static void main(args) {
        def subDir = getFileSubDir('d:/dev', 3457)
        println subDir
    }
}
