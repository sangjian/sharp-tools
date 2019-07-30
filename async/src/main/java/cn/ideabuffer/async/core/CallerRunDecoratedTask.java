package cn.ideabuffer.async.core;

/**
 * @author sangjian.sj
 * @date 2019/07/27
 */
public abstract class CallerRunDecoratedTask implements Runnable {

    private boolean callerRun;

    public boolean isCallerRun() {
        return callerRun;
    }

    public void setCallerRun(boolean callerRun) {
        this.callerRun = callerRun;
    }

}
