package st.old.isis.storage.policy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.enums.AlertPolicyStatus;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.policies.InappropriateLanguagePolicy;
import st.old.isis.storage.services.AlertPolicyService;
import st.old.isis.storage.services.DataImportRecordService;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyProcessorService {

    private final AlertPolicyService alertPolicyService;

    private final InappropriateLanguagePolicy inappropriateLanguagePolicy;
    
    private final DataImportRecordService dataImportRecordService;

    @Audited(event = "PROCESS_ALL_PENDING_IMPORT_DATA")
    public void processAllPendingImportData() throws IsisException {

        final var listOfDataImportRecords = dataImportRecordService.findByStatus(ProcessedStatus.PENDING_PROCESS);

        log.info("START - Processing Data Imported Records  - Size : {}", listOfDataImportRecords.getData().size());

        for (DataImportRecordDto rec : listOfDataImportRecords.getData()) {

            log.info("Processing Imported Record By {}", rec.getImportDate());
            processAllPoliciesByImportDate(rec.getImportDate());

            // Set status of data import to COMPLETED
            rec.setProcessedStatus(ProcessedStatus.COMPLETED);
            dataImportRecordService.update(rec);
        }

        log.info("COMPLETE - Processing Data Imported Records  - Size : {}", listOfDataImportRecords.getData().size());

    }

    public void processAllPoliciesByImportDate(LocalDate importDate) throws IsisException {

        log.info("START - Processing All Policies by Import Date: {}", importDate);

        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        for(AlertPolicyDto policy: listOfPolicies.getData()){
            processPolicy(policy, importDate);
        }

        log.info("COMPLETED - Processing All Policies by Import Date: {}", importDate);
    }

    public void processPolicy(@NonNull AlertPolicyDto filter, LocalDate importDate) throws IsisException {

        final var policyRecord = alertPolicyService.getById(filter.getId());

        if (policyRecord == null) {
            log.warn("Alert Policy Record with Internal Code {}  does not exist", filter.getInternalCode());
            throw IsisException.notFound("No policy record found.", "NotFoundException");
        }

        if (policyRecord.getStatus() != AlertPolicyStatus.ACTIVE){
            log.warn("Behavior Record is currently IN-ACTIVE");
            throw IsisException.conflict("Behavior Record is currently IN-ACTIVE.", "StatusException");
        }

        switch (policyRecord.getInternalCode()) {
            case "INAPPROPRIATE_LANGUAGE":
                inappropriateLanguagePolicy.process(policyRecord, importDate);
                break;
            default:
                break;
        }


    }



}
