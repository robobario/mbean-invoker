package de.adscale;

import static com.google.common.base.Splitter.on;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MbeanRun {

    private String objectName;

    private String methodName;

    private String appSpecifier;

    private List<String> args;

    private Pattern compile;


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


    public String getAppSpecifier() {
        return appSpecifier;
    }


    public void setAppSpecifier(String appSpecifier) {
        this.appSpecifier = appSpecifier;
    }


    public Pattern getAppSpecifierPattern() {
        if (compile != null) {
            return compile;
        }
        List<String> quoted = ImmutableList.copyOf(on("*").split(appSpecifier)).stream().map(Pattern::quote).collect(Collectors.toList());
        String join = Joiner.on(".*").join(quoted);
        compile = Pattern.compile(join);
        return compile;
    }
}
