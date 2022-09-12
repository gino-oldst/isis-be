package st.old.isis.storage.policy.policies;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import st.old.isis.dto.storage.*;
import st.old.isis.enums.AlertStatusType;
import st.old.isis.enums.MessageType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.services.*;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    private final CommunicationRecipientService communicationRecipientService;

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

        final var keywordsMap = new TreeMap<String, KeywordDto>(String.CASE_INSENSITIVE_ORDER);
        keywordsMap.putAll(generateKeywordMap(listPolicyMap.getData()));

        // Generate keyword set for trie
        final var keywords = keywordsMap.entrySet().stream()
                .map(k -> k.getValue().getWord())
                .collect(Collectors.toSet());

        Trie trie = Trie.builder().onlyWholeWordsWhiteSpaceSeparated().ignoreCase().addKeywords(keywords).build();

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

                                //Disregard if within disclaimer text
                                if(checkifEmitWithinDisclaimer(emit, message.getMessage())) {
                                    log.info("Keyword found {} in messageId {} but within disclaimer text. No alerts flagged", emit.getKeyword(), message.getId());
                                } else {
                                    final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(message.getChainLinkId()).build());

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
                                            .recipients(recipients.getData().stream().map(CommunicationRecipientDto::getRecipient).collect(Collectors.toList()))
                                            .build();

                                    log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , message.getId());
                                    alertLogService.create(alert);
                                }


                            });
                }
            }

        }


        log.info("COMPLETED - Processing Policy: {} for {} messages", policy.getName(), commMessages.size());

    }

    private boolean checkifEmitWithinDisclaimer(Emit emit, String message) {

        /*
        *   Reuters Disclaimer Text
        *
        *   Ex. [[:Conversations will be recorded and may be monitored by the participants and their employers, and may be disclosed to governmental or other regulatory bodies in compliance with applicable laws.You acknowledge and agree that any interactive
        *       features within Eikon, Eikon Messenger, Refinitiv Messenger, Refinitiv Workspace or any other services which are not advertised to be trade execution venues must not be used to conduct or execute any transactions or trades under any circumstances.:]]
        *
        * */
        if(message.matches("^(.*)[\\[\\[\\:\\:\\]\\]]([\\s\\S]*)$")) {

            int disclaimerStart = message.indexOf("[[:");
            int disclaimerEnd = message.indexOf(":]]");

            return emit.getStart() > disclaimerStart && emit.getEnd() < disclaimerEnd;
        }

        return false;

    }

    public BatchAlertNotificationDto processSingle(CommunicationMessage commMessage, AlertPolicyDto policy, LocalDate importDate) throws IsisException {

        log.debug("STARTED - Processing Policy: {} for {}", policy.getName(), commMessage);

        List<AlertLog> alertResults = new ArrayList<>();

        if(commMessage == null){
            log.warn("No communication message found.");
            throw IsisException.notFound(
                    String.format("No communication message found."),
                    "NotFoundException");
        }

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

        Trie trie = Trie.builder().onlyWholeWordsWhiteSpaceSeparated().ignoreCase().addKeywords(keywords).build();

        if(commMessage.getMessage() == null) {
            if(commMessage.getType() != null || !commMessage.getType().equals(MessageType.BLOOMBERG_CHAT_ATTACHMENT)){
                log.error("Message Id {} has a message of NULL value but is from source {}. ", commMessage.getId(), commMessage.getSource());
            }
        } else {
            final var emits= trie.parseText(commMessage.getMessage());

            if(!emits.isEmpty()) {
                emits.stream()
                        .forEach( emit -> {

                            final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(commMessage.getChainLinkId()).build());

                            final var keywordDto= keywordsMap.get(emit.getKeyword());

                            if(keywordDto == null){
                                log.warn("No keywords found in keywords map for {}", emit.getKeyword());
                                return;
                            }

                            final var alert= AlertLogDto.builder()
                                    .commChainLinkId(commMessage.getChainLinkId())
                                    .commMessageId(commMessage.getId())
                                    .message(commMessage.getMessage())
                                    .policyId(policyId)
                                    .description(null) // TODO Generated alert description
                                    .status(AlertStatusType.OPEN)
                                    .keyword(emit.getKeyword())
                                    .keywordId(keywordDto.getId())
                                    .importDate(importDate)  // For internal use only
                                    .timeSent(commMessage.getTimeSent())
                                    .sender(commMessage.getSender())
                                    .source(commMessage.getSource())
                                    .recipients(recipients.getData().stream().map(CommunicationRecipientDto::getRecipient).collect(Collectors.toList()))
                                    .build();

                            log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , commMessage.getId());

                            final var result = alertLogService.create(alert);

                            alertResults.add(alertLogService.mapToEntity(result));
                        });
            }
        }

        log.debug("COMPLETED - Processing Policy: {} for {}", policy.getName(), commMessage);

        return BatchAlertNotificationDto.builder()
                .commChainLinkId(commMessage.getChainLinkId())
                .commMessageId(commMessage.getId())
                .message(commMessage.getMessage())
                .count(alertResults.size())
                .build();

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

        Trie trie = Trie.builder().onlyWholeWordsWhiteSpaceSeparated().ignoreCase().addKeywords(keywords).build();

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
