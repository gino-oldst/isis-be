package st.old.isis.storage.policy.policies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Trie;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.AlertStatusType;
import st.old.isis.enums.MessageType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.services.AlertLogService;
import st.old.isis.storage.services.CommunicationMessageService;
import st.old.isis.storage.services.KeywordService;
import st.old.isis.storage.services.PolicyMapService;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InappropriateLanguageAlgorithm {

    private final AlertLogService alertLogService;

    private final PolicyMapService policyMapService;

    private final KeywordService keywordService;

    private final CommunicationMessageService communicationMessageService;

    public void process(List<CommunicationMessageDto> commMessages, AlertPolicyDto policy, LocalDate importDate) throws IsisException {

        log.info("START - Processing Policy: {} for {} messages", policy.getName(), commMessages.size());

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

        Trie trie = Trie.builder().ignoreCase().addKeywords(keywords).build();

        if(commMessages == null || ListUtils.isNullOrEmpty(commMessages)){
            log.warn("No communication messages found.");
            throw IsisException.notFound(
                    String.format("No communication messages found."),
                    "NotFoundException");
        }

        for(CommunicationMessageDto message: commMessages){

            log.info("Processing inappropriate algorithm for message id {}", message.getId());

            if(message.getMessage() == null) {
                if(message.getType() != null || !message.getType().equals(MessageType.BLOOMBERG_CHAT_ATTACHMENT)){
                    log.error("Message Id {} has a message of NULL value but is from source {}. ", message.getId(), message.getSource());
                }
            } else {
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
                                        .message(message.getMessage())
                                        .policyId(policyId)
                                        .description(null) // TODO Generated alert description
                                        .status(AlertStatusType.OPEN)
                                        .keyword(emit.getKeyword())
                                        .keywordId(keywordDto.getId())
                                        .importDate(importDate)  // For internal use only
                                        .timeSent(message.getTimeSent())
                                        .sender(message.getSender())
                                        .source(message.getSource())
                                        .build();

                                log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , message.getId());
                                alertLogService.create(alert);
                            });
                }
            }

        }


        log.info("COMPLETED - Processing Policy: {} for {} messages", policy.getName(), commMessages.size());

    }

    public void process(AlertPolicyDto policy, LocalDate importDate) throws IsisException {

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

        final var commMessages= communicationMessageService.searchList(
                CommunicationMessageDto.builder().importDate(importDate).build(), Pageable.unpaged());

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

                            log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , message.getId());
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
