package st.old.isis.storage.policy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.DataImportRecordDto;
import st.old.isis.dto.storage.PolicyProcessorDto;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.policy.policies.InappropriateLanguageAlgorithm;
import st.old.isis.storage.services.AlertPolicyService;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.DataImportRecordService;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyProcessorService {

    private final AlertPolicyService alertPolicyService;

    private final InappropriateLanguageAlgorithm inappropriateLanguagePolicy;

    private final DataImportRecordService dataImportRecordService;

    private final CommunicationMessageService communicationMessageService;

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

    public void processPolicyByDateRange(@NonNull String policyId,
                                         @NonNull LocalDate dateFrom,
                                         @NonNull LocalDate dateTo) throws IsisException {

        final var policyDto = alertPolicyService.getActiveById(policyId);

        log.info("START - Processing Policy {} by Date From: {} to Date To: {}", policyDto.getName(), dateFrom, dateTo);

        final var listOfDates= dateFrom.datesUntil(dateTo.plusDays(1)).collect(Collectors.toList());

        for(LocalDate targetDate: listOfDates){
            processPolicy(policyDto, targetDate);
        }

        log.info("COMPLETED - Processing Policy {} by Date From: {} to Date To: {}", policyDto.getName(), dateFrom, dateTo);
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

        log.info("STARTED - Processing Policy Id {} and Import Date {}", filter.getId(), importDate);

        final var policyRecord = alertPolicyService.getActiveById(filter.getId());

        final var commMessages= communicationMessageService.searchList(
                CommunicationMessageDto.builder().importDate(importDate).build(), Pageable.unpaged());

        if(commMessages == null || ListUtils.isNullOrEmpty(commMessages.getData())){
            log.warn("No communication messages found for date: {}.", importDate);
        } else {
            switch (policyRecord.getInternalCode()) {
                case "INAPPROPRIATE_LANGUAGE":
                    inappropriateLanguagePolicy.process(commMessages.getData(), policyRecord, importDate);
                    break;
                default:
                    break;
            }
        }

        log.info("COMPLETED - Processing Policy Id {} and Import Date {}", filter.getId(), importDate);

    }

    public void processPolicyIdByFilter(PolicyProcessorDto filter) throws IsisException {

        log.info("STARTED - Processing Policy By Filter {}", filter);

        final var policyRecord = alertPolicyService.getActiveById(filter.getPolicyId());

        final var messageFilter = CommunicationMessageDto.builder()
                .importDate(filter.getImportDate())
                .source(filter.getSource())
                .build();

        final var commMessages= communicationMessageService.searchList(messageFilter, Pageable.unpaged());

        if(commMessages == null || ListUtils.isNullOrEmpty(commMessages.getData())){
            log.warn("No communication messages found for date: {}.", filter.getImportDate());
            throw IsisException.notFound("No communication messages found for date: " + filter.getImportDate(), "NotFoundException");
        } else {
            switch (policyRecord.getInternalCode()) {
                case "INAPPROPRIATE_LANGUAGE":
                    inappropriateLanguagePolicy.process(commMessages.getData(), policyRecord, filter.getImportDate());
                    break;
                default:
                    break;
            }
        }

        log.info("COMPLETED - Processing Policy By Filter {} for {} of messages", filter, commMessages.getData().size());
    }

}
