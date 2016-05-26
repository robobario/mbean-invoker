package de.adscale;

import java.util.List;

public class OperationConfig {

    private List<MbeanRun> mbeansToRun;

    public List<MbeanRun> getMbeansToRun() {
        return mbeansToRun;
    }

    public void setMbeansToRun(List<MbeanRun> mbeansToRun) {
        this.mbeansToRun = mbeansToRun;
    }
}
