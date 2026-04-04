package com.quietchatter.batch.adaptor.`in`.batch

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class DailyReportScheduler(
    private val jobLauncher: JobLauncher,
    private val dailyReportJob: Job
) {
    private val log = LoggerFactory.getLogger(DailyReportScheduler::class.java)

    // 매일 새벽 1시에 실행 (한국 시간 기준)
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    fun runDailyReportJob() {
        val targetDate = LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        log.info("Scheduled Daily Report Job starting for date: {}", targetDate)
        
        try {
            val jobParameters = JobParametersBuilder()
                .addString("targetDate", targetDate)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters()
            
            jobLauncher.run(dailyReportJob, jobParameters)
        } catch (e: Exception) {
            log.error("Failed to run scheduled Daily Report Job", e)
        }
    }
}
