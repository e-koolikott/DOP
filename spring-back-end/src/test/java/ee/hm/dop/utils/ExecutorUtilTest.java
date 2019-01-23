package ee.hm.dop.utils;

import org.joda.time.LocalDateTime;
import org.junit.Ignore;
import org.junit.Test;

import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertTrue;

public class ExecutorUtilTest {

    @Ignore
    @Test
    public void getInitialDelayExecuteTomorrow() {
        LocalDateTime now = now();
        int hourOfDayToExecute = now.getHourOfDay() - 1;
        if(hourOfDayToExecute == -1){hourOfDayToExecute = 23;}
        LocalDateTime expectedExecutionTime = now.withHourOfDay(hourOfDayToExecute).withMinuteOfHour(0)
                .withSecondOfMinute(0).withMillisOfSecond(0).plusDays(1);

        int delay = (int) ExecutorUtil.getInitialDelay(hourOfDayToExecute);
        LocalDateTime firstExecution = now.plusMillis(delay);

        assertTrue(Math.abs(firstExecution.toDate().getTime() - expectedExecutionTime.toDate().getTime()) < 100);
    }

    //TODO: This fails in the evening, ex 23.45
    @Ignore
    @Test
    public void getInitialDelayExecuteToday() {
        LocalDateTime now = now();
        int hourOfDayToExecute = (now.getHourOfDay() + 1) % 23;
        LocalDateTime expectedExecutionTime = now.withHourOfDay(hourOfDayToExecute).withMinuteOfHour(0)
                .withSecondOfMinute(0).withMillisOfSecond(0);

        int delay = (int) ExecutorUtil.getInitialDelay(hourOfDayToExecute);
        LocalDateTime firstExecution = now.plusMillis(delay);

        assertTrue(Math.abs(firstExecution.toDate().getTime() - expectedExecutionTime.toDate().getTime()) < 2000);
    }
}
