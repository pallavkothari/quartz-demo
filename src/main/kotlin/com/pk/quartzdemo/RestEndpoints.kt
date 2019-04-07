package com.pk.quartzdemo

import org.quartz.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.validation.Valid


@RestController
class RestEndpoints {

    internal val logger = LoggerFactory.getLogger(RestEndpoints::class.java)

    @Autowired
    internal val scheduler: Scheduler? = null

    @RequestMapping("/")
    fun index(): String {
        return "Hello! Try scheduling something at /schedule"
    }

    @RequestMapping("/schedule", method = [RequestMethod.POST])
    fun schedule(@Valid @RequestBody req: ScheduledNotification): ResponseEntity<ScheduledResponse> {
        logger.info("input: $req")

        val zonedDateTime = ZonedDateTime.of(req.dateTime, req.zone)

        if (zonedDateTime.isBefore(ZonedDateTime.now())) {
            val scheduleEmailResponse = ScheduledResponse(
                false,
                "dateTime must be after current time", "", ""
            )
            return ResponseEntity.badRequest().body(scheduleEmailResponse)
        }

        val jobDetail = buildJobDetail(req)
        val trigger = buildJobTrigger(jobDetail, zonedDateTime)

        scheduler?.scheduleJob(jobDetail, trigger)

        val scheduleEmailResponse = ScheduledResponse(
            true,
            "Notification scheduled successfully!",
            jobDetail.key.name, jobDetail.key.group
        )

        return ResponseEntity.ok(scheduleEmailResponse)
    }

    /**
     * builds a quarts Trigger
     */
    private fun buildJobTrigger(jobDetail: JobDetail, zonedDateTime: ZonedDateTime): Trigger {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity(jobDetail.key.name, "default-triggers")
            .withDescription("Logging Trigger")
            .startAt(Date.from(zonedDateTime.toInstant()))
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
            .build()
    }

    /**
     * builds a quartz jobDetail
     */
    private fun buildJobDetail(req: ScheduledNotification): JobDetail {
        val jobDataMap = JobDataMap()

        jobDataMap["msg"] = req.msg

        return JobBuilder.newJob(LoggingJob::class.java)
            .withIdentity(UUID.randomUUID().toString(), "default-jobs")
            .withDescription("Default Jobs")
            .usingJobData(jobDataMap)
            .storeDurably()
            .build()
    }
}

/**
 * This implementation of a quartz job just logs the payload (looks for an entry called 'msg' in the jobDataMap)
 */
@Component
class LoggingJob: QuartzJobBean() {
    val logger = LoggerFactory.getLogger(LoggingJob::class.java)

    override fun executeInternal(context: JobExecutionContext) {
        logger.info("Executing Job with key ${context.jobDetail.key}")

        val jobDataMap = context.mergedJobDataMap

        val msg = jobDataMap.getString("msg")

        logger.info("Here's your msg : $msg")
    }
}

/**
 * model for a ScheduledNotificationRequest
 */
data class ScheduledNotification(val msg: String, val dateTime: LocalDateTime, val zone: ZoneId)

/**
 * model for the response
 */
data class ScheduledResponse(val success: Boolean, val msg: String, val jobId: String, val jobGroup: String)
