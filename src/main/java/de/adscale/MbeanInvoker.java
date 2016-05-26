package de.adscale;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class MbeanInvoker {

    public static void main(String[] args) {
        Options options = new Options();
        try {
            Config conf = parseOptions(args, options);
            if(Objects.equals(conf.getOperation(), "")){
                MbeanRunConfig mbeanRunConfig = getMbeanRunConfig(conf);
                System.out.println("provide an operation with -o from :");
                mbeanRunConfig.getOperations().keySet().forEach(System.out::println);
            }else {
                invokeAll(conf);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void invokeAll(Config conf) throws FileNotFoundException {
        MbeanRunConfig mbeanRunConfig = getMbeanRunConfig(conf);
        try {
            String operation = conf.getOperation();
            OperationConfig config = mbeanRunConfig.getOperations().get(operation);
            for (MbeanRun mbeanRun : config.getMbeansToRun()) {
                invoke(mbeanRun, mbeanRunConfig);
            }
        }
        finally {
            mbeanRunConfig.getApplications().forEach(App::close);
        }
    }


    private static void invoke(MbeanRun mbeanRun, MbeanRunConfig conf) {
        List<App> apps = getApplicationsMatchingSpecifier(conf, mbeanRun);
        for (App app : apps) {
            try {
                MBeanServerConnection remote = app.connection();
                Object invoke = doInvoke(mbeanRun, remote);
                System.out.println(app + ":" + mbeanRun + " result = " + Objects.toString(invoke));
            }
            catch (Exception e) {
                System.out.println(app + ":" + mbeanRun + " on " + app + " failed [ " + e.getMessage() + " ]");
            }
        }
    }


    private static Object doInvoke(MbeanRun mbeanRun, MBeanServerConnection remote) throws Exception {
        ObjectName name = new ObjectName(mbeanRun.getObjectName());
        MBeanInfo mBeanInfo = remote.getMBeanInfo(name);
        Optional<MBeanOperationInfo> info = ImmutableList.copyOf(mBeanInfo.getOperations()).stream().filter(i -> matches(mbeanRun, i)).findFirst();
        if (!info.isPresent()) {
            throw new RuntimeException("no signature match found");
        }
        MBeanOperationInfo mBeanOperationInfo = info.get();
        int paramCount = mBeanOperationInfo.getSignature().length;
        List<Object> params = new ArrayList<>(paramCount);
        List<String> paramClasses = new ArrayList<>(paramCount);
        for (int i = 0; i < mBeanOperationInfo.getSignature().length; i++) {
            String type = mBeanOperationInfo.getSignature()[i].getType();
            params.add(convert(type, mbeanRun.getArgs().get(i)));
            paramClasses.add(type);
        }
        return remote.invoke(name, mBeanOperationInfo.getName(), params.toArray(), paramClasses.toArray(new String[paramCount]));
    }


    private static Object convert(String type, String s) {
        switch (type) {
            case "String":
            case "java.lang.String":
                return s;
            case "int":
            case "java.lang.Integer":
                return Integer.parseInt(s);
            case "long":
            case "java.lang.Long":
                return Long.parseLong(s);
            case "boolean":
            case "java.lang.Boolean":
                return Boolean.parseBoolean(s);
        }
        return null;
    }


    private static boolean matches(MbeanRun mbeanRun, MBeanOperationInfo info) {
        boolean isMethodMatch = info.getName().equals(mbeanRun.getMethodName());
        boolean isParameterMatch = info.getSignature().length == mbeanRun.getArgs().size();
        return isMethodMatch && isParameterMatch;
    }


    private static List<App> getApplicationsMatchingSpecifier(MbeanRunConfig mbeanRunConfig, MbeanRun run) {
        return mbeanRunConfig.getApplications().stream().filter(a -> matches(a, run)).collect(Collectors.toList());
    }


    private static boolean matches(App a, MbeanRun run) {
        return run.getAppSpecifierPatterns().anyMatch(p -> p.matcher(a.getAppName()).matches());
    }


    private static MbeanRunConfig getMbeanRunConfig(Config conf) throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(new File(conf.getAppConfigurationFile())), MbeanRunConfig.class);
    }


    private static Config parseOptions(String[] args, Options options) throws ParseException {
        options.addOption("f", "app-configuration-file", true, "configuration file containing app information");
        options.addOption("o", "operation", true, "operation to run");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String appConfigFile = cmd.getOptionValue("app-configuration-file", "");
            String operation = cmd.getOptionValue("operation", "");
            return new Config(appConfigFile, operation);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java mbean-invoker.jar", options);
            System.exit(1);
            return null; // ugly ugly ugly
        }
    }

    private static class Config {

        private final String appConfigurationFile;
        private String operation;


        Config(String appConfigurationFile, String operation) {
            this.operation = operation;
            if (appConfigurationFile == null) {
                throw new IllegalArgumentException();
            }
            this.appConfigurationFile = appConfigurationFile;
        }


        public String getAppConfigurationFile() {
            return appConfigurationFile;
        }

        public String getOperation() {
            return operation;
        }
    }
}
