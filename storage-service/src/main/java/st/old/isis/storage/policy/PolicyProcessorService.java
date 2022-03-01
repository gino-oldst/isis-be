package st.old.isis.storage.policy;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.AlertPolicyStatus;
import st.old.isis.enums.AlertStatusType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.*;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyProcessorService {

    private final AlertLogService alertLogService;

    private final AlertPolicyService alertPolicyService;

    private final PolicyMapService policyMapService;

    private final KeywordService keywordService;

    private final CommunicationMessageService communicationMessageService;

    public void processAllPendingImportData() throws IsisException {

        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            throw IsisException.notFound("No Policy Records Found", "NotFoundException");
        }

        //TODO Check list of data import table for PENDING_PROCESS
        /*
        final var listOfDataImportRecords = dataImportedRecordService.findByStatus(BehaviourProcessedStatus.PENDING_PROCESS);

        final var listOfDataImportRecords = dataImportedRecordService.findByStatus(BehaviourProcessedStatus.PENDING_PROCESS);

        log.info("Processing Data Imported Records  - Size : {}", listOfDataImportRecords.getData().size());

        for (DataImportRecordDto rec : listOfDataImportRecords.getData()) {

            log.info("Processing Imported Record By {}", rec.getDateImported());
            for (BehaviourDto behaviourDto : listOfBehaviors.getData()) {
                processPolicies(behaviourDto.getInternalCode(), rec.getDateImported());
            }

            rec.setBehaviourProcessedStatus(BehaviourProcessedStatus.COMPLETED);
            dataImportedRecordService.updateRecord(rec);

        }
        log.info("Processing Data Imported Records  - Size : {} - Completed", listOfDataImportRecords.getData().size());

        */

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
                processInappropriateLanguageAlgorithm(policyRecord, importDate);
                break;
            default:
                break;
        }


    }

    private void processInappropriateLanguageAlgorithm(AlertPolicyDto policy, LocalDate importDate) throws IsisException {

        log.info("START - Processing Policy: {}", policy.getName());

        final var policyId= policy.getId();

        final var listPolicyMap = policyMapService.search(PolicyMapDto.builder().policyId(policyId).build());

        if(listPolicyMap == null || ListUtils.isNullOrEmpty(listPolicyMap.getData())){
            log.warn("No policy map keywords found for policy {}", policyId);
            throw IsisException.notFound(
                    String.format("No policy map keywords found for policy [%s]", policyId),
                    "NotFoundException");
        }

        final var keywordsMap = generateKeywordMap(listPolicyMap.getData());

        // Generate keyword set for trie
        final var keywords = keywordsMap.entrySet().stream()
                .map(k -> k.getValue().getWord())
                .collect(Collectors.toSet());

        Trie trie = Trie.builder().onlyWholeWords().ignoreCase().addKeywords(keywords).build();

        final var commMessages= communicationMessageService.search(
                CommunicationMessageDto.builder().importDate(importDate).build());

        if(commMessages == null || ListUtils.isNullOrEmpty(commMessages.getData())){
            log.warn("No communication messages found from import date {}", importDate);
            throw IsisException.notFound(
                    String.format("No communication messages found from import date [%s]", importDate),
                    "NotFoundException");
        }

        for(CommunicationMessageDto message: commMessages.getData()){

            final var emits= trie.parseText(message.getMessage());

            if(!emits.isEmpty()) {
                emits.stream()
                        .forEach( emit -> {

                            final var keywordDto= keywordsMap.get(emit.getKeyword());

                            if(keywordDto == null){
                                log.warn("No keywords found in keywords map for {}", emit.getKeyword());
                                return;
                            }

                            final var alert= AlertLogDto.builder()
                                    .commChainLinkId(message.getChainLinkId())
                                    .commMessageId(message.getId())
                                    .policyId(policyId)
                                    .description(null) // TODO Generated alert description
                                    .status(AlertStatusType.OPEN)
                                    .keyword(emit.getKeyword())
                                    .keywordId(keywordDto.getId())
                                    .build();

                            log.info("Alert Entry Found - Keyword: {} in Message: {}", emit.getKeyword() , message.getMessage());

                            alertLogService.create(alert);

                        });
            }
        }

        log.info("COMPLETED - Processing Policy: {}", policy.getName());

    }

    private Map<String, KeywordDto> generateKeywordMap(List<PolicyMapDto> listPolicyMap) {
        return listPolicyMap.stream()
                .map(p -> {
                    try {
                        return keywordService.getById(p.getKeywordId());
                    } catch (IsisException e) {
                        log.warn(e.getMessage());
                        return null;
                    }
                })
                .filter(word -> word != null)
                .collect(Collectors.toMap(KeywordDto::getWord, Function.identity()));
    }

}
