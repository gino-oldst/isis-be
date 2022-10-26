package st.old.isis.storage.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import st.old.isis.dto.storage.PolicyProcessorDto;
import st.old.isis.exception.IsisException;

@RestController
@RequestMapping(path = "/batch-policy")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job job;

    @PostMapping(path = "/start")
    public void startBatch(@RequestBody PolicyProcessorDto dto) throws IsisException {

        if(dto.getDateFrom() == null ||
                dto.getDateTo() == null||
                dto.getSource() == null) {
            throw IsisException.badRequest("Missing policy processor parameters", "SearchFilterException");
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .addString("dateFrom", dto.getDateFrom().toString())
                .addString("dateTo", dto.getDateTo().toString())
                .addString("source", dto.getSource())
                .toJobParameters();
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {

            e.printStackTrace();
        }
    }
}