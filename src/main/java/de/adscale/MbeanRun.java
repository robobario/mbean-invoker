package de.adscale;

import static com.google.common.base.Splitter.on;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MbeanRun {

    private String objectName;

    private String methodName;

    private List<String> appSpecifiers;

    private List<String> args;

    private List<Pattern> compiledPatterns;


    public String getObjectName() {
        return objectName;
    }


    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }


    public String getMethodName() {
        return methodName;
    }


    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }


    public List<String> getArgs() {
        return args;
    }


    public void setArgs(List<String> args) {
        this.args = args;
    }


    public List<String> getAppSpecifier() {
        return appSpecifiers;
    }


    public void setAppSpecifier(List<String> appSpecifiers) {
        this.appSpecifiers = appSpecifiers;
    }


    public Stream<Pattern> getAppSpecifierPatterns() {
        if (compiledPatterns != null) {
            return compiledPatterns.stream();
        }
        List<Pattern> compiled = Lists.newArrayList();
        for (String appSpecifier : appSpecifiers) {
            List<String> quoted = ImmutableList.copyOf(on("*").split(appSpecifier)).stream().map(Pattern::quote).collect(Collectors.toList());
            String join = Joiner.on(".*").join(quoted);
            compiled.add(Pattern.compile(join));
        }
        this.compiledPatterns = compiled;
        return compiled.stream();
    }


    @Override
    public String toString() {
        return objectName + ":" + methodName + "(" + Joiner.on(",").join(args) + ")";
    }
}
