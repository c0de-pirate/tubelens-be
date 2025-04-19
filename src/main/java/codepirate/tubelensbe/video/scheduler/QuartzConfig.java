package codepirate.tubelensbe.video.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail apiCallJobDetail() {
        return JobBuilder.newJob(ApiCallJob.class)
                .withIdentity("apiCallJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger apiCallJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(apiCallJobDetail())
                .withIdentity("apiCallTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }
}