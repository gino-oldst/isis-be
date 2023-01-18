package st.old.isis.storage.policy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.ProcessedStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.policy.policies.InappropriateLanguageAlgorithm;
import st.old.isis.storage.services.AlertPolicyService;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.DataImportRecordService;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    // TODO - processAllPendingImportDataViaOpenSearch
    @Audited(event = "PROCESS_ALL_PENDING_IMPORT_DATA_VIA_OPEN_SEARCH")
    public void processAllPendingImportDataViaOpenSearch() throws IsisException {

        // Get Pending Records from the values made in ingestion
        final var listOfDataImportRecords = dataImportRecordService.findByStatus(ProcessedStatus.PENDING_PROCESS);

        log.info("START - Processing Data Imported Records  - Size : {}", listOfDataImportRecords.getData().size());

        for (DataImportRecordDto rec : listOfDataImportRecords.getData()) {

            log.info("Processing Imported Record By {}", rec.getImportDate());

            // TODO @processAllPoliciesByImportDateViaOpenSearch
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

        // Gets The Policy
        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        for(AlertPolicyDto policy: listOfPolicies.getData()){
            // Actually Processes the policy
            processPolicy(policy, importDate);
        }

        log.info("COMPLETED - Processing All Policies by Import Date: {}", importDate);
    }

    // TODO - processAllPoliciesByImportDateViaOpenSearch
    public void processAllPoliciesByImportDateViaOpenSearch(LocalDate importDate) throws IsisException {

        log.info("START - Processing All Policies by Import Date: {}", importDate);

        // Gets The Policy
        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        // Loop Through Each Policy
        for(AlertPolicyDto policy: listOfPolicies.getData()){
            // Actually Processes the policy
            // TODO @processPolicyViaOpenSearch
            processPolicy(policy, importDate);
        }

        log.info("COMPLETED - Processing All Policies by Import Date: {}", importDate);
    }

    public void processPolicy(@NonNull AlertPolicyDto filter, LocalDate importDate) throws IsisException {

        log.info("STARTED - Processing Policy Id {} and Import Date {}", filter.getId(), importDate);

        final var policyRecord = alertPolicyService.getActiveById(filter.getId());

        // TODO on a New ProcessPolicy with OpenSearch
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

    //TODO - processPolicyViaOpenSearch
    public void processPolicyViaOpenSearch(@NonNull AlertPolicyDto filter, LocalDate importDate) throws IsisException {

        log.info("STARTED - Processing Policy Id {} and Import Date {}", filter.getId(), importDate);

        final var policyRecord = alertPolicyService.getActiveById(filter.getId());

        // TODO on a New ProcessPolicy with OpenSearch... Search by Date Range
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

    public BatchAlertNotificationDto processSingleMessageByAllPolicies(@NonNull CommunicationMessage message) throws IsisException {

        log.debug("STARTED - Processing All Policies by Message: {}", message.getId());

        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        for(AlertPolicyDto policy: listOfPolicies.getData()){
            if(message == null){
                log.warn("No communication message for processing.", message);
            } else {
                switch (policy.getInternalCode()) {
                    case "INAPPROPRIATE_LANGUAGE":
                        return inappropriateLanguagePolicy.processSingle(message, policy, message.getImportDate());
                    default:
                        return null;
                }
            }
        }

        return null;
    }

    public BatchAlertNotificationDto processSingleMessageByAllPolicies(@NonNull Map<AlertPolicyDto, List<KeywordDto>> policyMap,
                                                                       @NonNull CommunicationMessage message) throws IsisException {

        log.debug("STARTED - Processing All Policies by Message: {}", message.getId());

        int totalAlertSize = 0;

        for(AlertPolicyDto policy: policyMap.keySet()){
            if(message == null){
                log.warn("No communication message for processing.", message);
            } else if(message.getBypassType() != null) {
                log.debug("Message bypassed with id {} and bypass type {}", message.getId(), message.getBypassType());
            } else {
                switch (policy.getInternalCode()) {
                    case "INAPPROPRIATE_LANGUAGE":
                        final var itrBatchAlert = inappropriateLanguagePolicy.processSingleWithKeywords(message, policy, policyMap.get(policy), message.getImportDate());
                        if(itrBatchAlert.getCount() > 0) totalAlertSize += itrBatchAlert.getCount();
                        break;
                    default:
                        throw IsisException.notFound("Policy's internal code was not found. " + policy, "NotFoundException");
                }
            }
        }

        return BatchAlertNotificationDto.builder()
                .commChainLinkId(message.getChainLinkId())
                .commMessageId(message.getId())
                .message(message.getMessage())
                .count(totalAlertSize)
                .build();
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
