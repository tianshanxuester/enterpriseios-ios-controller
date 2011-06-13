package com.enterpriseios.push.web.wbxml.parser;

import org.eclipse.jetty.util.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/07/12
 * Time: 10:00:35
 * To change this template use File | Settings | File Templates.
 */
public class CharsetMappings {
    private static final Map<Integer, String> mibEnumToCharset = new HashMap<Integer, String>();

    static {
        mibEnumToCharset.put(3, "US-ASCII");
        mibEnumToCharset.put(4, "ISO-8859-1");
        mibEnumToCharset.put(106, "UTF-8");
    }

    public static String getCharset(int mibEnum) {
        String ret = mibEnumToCharset.get(mibEnum);
        if (ret == null) {
            Log.info("unsupported character set detected. fallback to 'UTF-8'");
            ret = "UTF-8";
        }
        return ret;
    }
}
