package de.adscale;

import com.google.gson.Gson;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

public class MbeanInvoker {

    public static void main(String[] args) {
        Options options = new Options();
        try {
            Config conf = parseOptions(args, options);
            invokeAll(conf);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void invokeAll(Config conf) throws FileNotFoundException {
        MbeanRunConfig mbeanRunConfig = getMbeanRunConfig(conf);
        try {
            for (MbeanRun mbeanRun : mbeanRunConfig.getMbeansToRun()) {
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
                Object invoke = remote.invoke(new ObjectName(mbeanRun.getObjectName()), mbeanRun.getMethodName(), new Object[] {}, new String[] {});
                System.out.println(mbeanRun.getObjectName() + ":" + mbeanRun.getMethodName() + " result = " + Objects.toString(invoke));
            }
            catch (Exception e) {
                System.out.println(mbeanRun.getObjectName() + ":" + mbeanRun.getMethodName() + " on " + app + " failed [ " + e.getMessage() + " ]");
            }
        }
    }


    private static List<App> getApplicationsMatchingSpecifier(MbeanRunConfig mbeanRunConfig, MbeanRun run) {
        return mbeanRunConfig.getApplications().stream().filter(a -> matches(a, run)).collect(Collectors.toList());
    }


    private static boolean matches(App a, MbeanRun run) {
        return run.getAppSpecifierPattern().matcher(a.getAppName()).matches();
    }


    private static MbeanRunConfig getMbeanRunConfig(Config conf) throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(new File(conf.getAppConfigurationFile())), MbeanRunConfig.class);
    }


    private static Config parseOptions(String[] args, Options options) throws ParseException {
        options.addOption("f", "app-configuration-file", true, "configuration file containing app information");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String appConfigFile = cmd.getOptionValue("app-configuration-file", "");
            return new Config(appConfigFile);
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


        Config(String appConfigurationFile) {
            if (appConfigurationFile == null) {
                throw new IllegalArgumentException();
            }
            this.appConfigurationFile = appConfigurationFile;
        }


        public String getAppConfigurationFile() {
            return appConfigurationFile;
        }
    }
}
