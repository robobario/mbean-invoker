package de.adscale;

import java.util.List;

public class MbeanRunConfig {
    private List<App> applications;

    private List<MbeanRun> mbeansToRun;

    public List<App> getApplications() {
        return applications;
    }

    public void setApplications(List<App> applications) {
        this.applications = applications;
    }

    public List<MbeanRun> getMbeansToRun() {
        return mbeansToRun;
    }

    public void setMbeansToRun(List<MbeanRun> mbeansToRun) {
        this.mbeansToRun = mbeansToRun;
    }
}
