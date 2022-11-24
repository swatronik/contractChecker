package src.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import src.util.Produces;

@Controller
@Configuration
@EnableScheduling
@EnableAsync
public class PeriodCheckController {

    private static ThreadPoolTaskScheduler scheduler;
    private static String cronTime = "";//"0 15 9-17 * * MON-FRI"

    private static class TaskRecheck implements Runnable {
        @Override
        public void run() {
            QueryController.refreshAllQuery();
            System.out.println("Run Schedule!!!");
        }
    }

    @Bean
    public static void initScheduler() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.initialize();
    }



    public static void setCronPeriod(String cron) {
        cronTime = cron;
        initScheduler();
        scheduler.schedule(new TaskRecheck(), new CronTrigger(cronTime));
        //scheduler.schedule(new TaskRecheck(), new CronTrigger(cronTime));
    }

    public static String getCronPeriod() {
        return cronTime;
    }

    @GetMapping(
            value = "/setPeriodRun/{newPeriod}",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> setPeriodRun(@PathVariable("newPeriod") String newPeriod) {
        setCronPeriod(newPeriod);

        return ResponseEntity
                .ok()
                .body(null);
    }

    @GetMapping(
            value = "/getPeriodRun",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getPeriodRun() {
        return ResponseEntity
                .ok()
                .body(getCronPeriod());
    }
}
