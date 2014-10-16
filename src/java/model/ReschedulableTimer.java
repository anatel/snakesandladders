/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Anat
 */
public class ReschedulableTimer extends Timer {

    private Runnable task;
    private TimerTask timerTask;

    public void schedule(Runnable runnable, long delay) {
        task = runnable;
        timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        schedule(timerTask, delay);
    }

    public void reschedule(Runnable runnable, long delay) {
        task = runnable;
        timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        schedule(timerTask, delay);
    }

    public void reschedule(long delay) {

        timerTask.cancel();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        schedule(timerTask, delay);
    }
    
    public void cancelTimer()
    {
         timerTask.cancel();
    }
}
