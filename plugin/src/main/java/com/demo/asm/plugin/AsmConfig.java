package com.demo.asm.plugin;

import java.util.List;

public class AsmConfig {
    /**
     * 过滤包含此字符串的类
     */
    public String[] filterContainsClass;
    /**
     * 过滤以此字符串开头的类
     */
    public String[] filterStartWithClass;

    public List<String> filterClassNameList;
    public String filterClassNameListFile;

    public String[] injectMethodPairList;
    public boolean isDebug;
}
