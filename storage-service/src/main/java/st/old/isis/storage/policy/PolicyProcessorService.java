package st.old.isis.storage.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.enums.AlertPolicyStatus;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AlertPolicyService;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.KeywordService;
import st.old.isis.storage.services.PolicyMapService;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyProcessorService {

    private final AlertPolicyService alertPolicyService;

    private final PolicyMapService policyMapService;

    private final KeywordService keywordService;

    private final CommunicationMessageService communicationMessageService;

    public void processAllPendingImportData() throws IsisException {

        final var listOfPolicies = alertPolicyService.search(AlertPolicyDto.builder().build());

        if (listOfPolicies == null || ListUtils.isNullOrEmpty(listOfPolicies.getData())) {
            log.warn("No Policy Records Found");
            return;
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

        processBehaviours("INAPPROPRIATE_LANGUAGE", LocalDate.parse("2022-02-24"));

    }

    public void processBehaviours(String internalCode, LocalDate importDate) throws IsisException {

        final var policyRecord = alertPolicyService.getByInternalCode(internalCode);

        if (policyRecord == null) {
            log.warn("Alert Policy Record with Internal Code {}  does not exist", internalCode);
            return;
        }

        if (policyRecord.getStatus() != AlertPolicyStatus.ACTIVE){
            log.warn("Behavior Record is currently IN-ACTIVE");
            return;
        }

        switch (policyRecord.getInternalCode()) {
            //TODO Create enum for alert policy internal codes
            case "INAPPROPRIATE_LANGUAGE":
                processInappropriateLanguageAlgorithm(policyRecord.getId(), importDate);
                break;
            default:
                break;
        }


    }

    private void processInappropriateLanguageAlgorithm(String policyId, LocalDate importDate) {

        final var listPolicyMap = policyMapService.search(PolicyMapDto.builder().policyId(policyId).build());

        if(listPolicyMap == null || ListUtils.isNullOrEmpty(listPolicyMap.getData())){
            log.warn("No policy map keywords found for policy {}", policyId);
            return;
        }

        final var keywords = generateListOfKeywords(listPolicyMap.getData());

        Trie trie = Trie.builder().onlyWholeWords().ignoreCase().addKeywords(keywords).build();

        final var commMessages= communicationMessageService.search(
                CommunicationMessageDto.builder().importDate(importDate).build());

        if(commMessages == null || ListUtils.isNullOrEmpty(commMessages.getData())){
            log.warn("No communication messages found from import date {}", importDate);
            return;
        }

        final var flaggedMessages= new HashMap<>();

        for(CommunicationMessageDto message: commMessages.getData()){

            final var emits= trie.parseText(message.getMessage());

            if(!emits.isEmpty()) {
                flaggedMessages.put(message, emits);
            }

        }

        System.out.println(flaggedMessages);

    }

    private Set<String> generateListOfKeywords(List<PolicyMapDto> listPolicyMap) {
        return listPolicyMap.stream()
                .map(p -> {
                    try {
                        var keyword= keywordService.getById(p.getKeywordId());
                        return keyword.getWord();
                    } catch (IsisException e) {
                        log.warn(e.getMessage());
                        return null;
                    }
                })
                .filter(word -> word != null)
                .collect(Collectors.toSet());
    }

}
