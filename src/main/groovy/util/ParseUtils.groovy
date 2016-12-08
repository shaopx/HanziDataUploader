package util

/**
 * Created by SHAOPENGXIANG on 2016/12/1.
 */
class ParseUtils {

    static String getTag(String raw) {
        if (raw.equals('作者')) {
            return 'zuozhe'
        } else if (raw.contains('作品介绍')) {
            return 'jieshao'
        } else if (raw.contains('作者介绍') || raw.contains('作者简介')) {
            return 'zzjj'
        } else if (raw.contains('原文') || raw.contains('正文')) {
            return 'yuanwen'
        } else if (raw.contains('注释')) {
            return 'zhushi'
        } else if (raw.contains('注解')) {
            return 'zhujie'
        } else if (raw.contains('繁体') || raw.contains('对照')) {
            return 'fanti'
        } else if (raw.contains('译文')) {
            return 'yiwen'
        } else if (raw.contains('鉴赏')) {
            return 'shangxi1'
        } else if (raw.contains('赏析')) {
            return 'shangxi2'
        } else if (raw.contains('简析')) {
            return 'shangxi3'
        } else if (raw.contains('简介')) {
            return 'shangxi4'
        } else if (raw.contains('作品评析')) {
            return 'shangxi5'
        } else if (raw.contains('释义')) {
            return 'shiyi'
        } else if (raw.contains('影响')) {
            return 'yingxiang'
        } else if (raw.contains('出处')) {
            return 'chuchu'
        } else if (raw.contains('别名')) {
            return 'bieming'
        } else if (raw.contains('背景')) {
            return 'beijing'
        } else if (raw.contains('作品名称') || raw.contains('中文名')) {
            return 'title'
        } else if (raw.contains('作者')) {
            return 'zuozhe'
        } else if (raw.contains('年代')) {
            return 'niandai'
        } else if (raw.contains('体裁')) {
            return 'ticai'
        } else if (raw.contains('特点')) {
            return 'tedian'
        } else if (raw.contains('外文')) {
            return 'waiwen'
        } else {
            return raw
        }


    }
}
