package codepirate.tubelensbe.video.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail videoSyncJobDetail() {
        return JobBuilder.newJob(VideoSyncJob.class)
                .withIdentity("videoSyncJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger videoSyncTrigger() {
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMinutes(1)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(videoSyncJobDetail())
                .withIdentity("videoSyncTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}