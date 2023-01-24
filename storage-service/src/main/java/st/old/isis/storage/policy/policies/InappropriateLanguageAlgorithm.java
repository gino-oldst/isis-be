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
import st.old.isis.enums.SourceType;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.services.*;
import st.old.isis.utils.ListUtils;

import java.time.LocalDate;
import java.util.*;
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

    private static final List<String> DISCLAIMER_BYPASS_KEYWORDS = Arrays.asList("confidential", "info", "privacy", "privileged", "privilégié");
    private static final List<String> DISCLAIMER_INDICATORS = Arrays.asList("disclaimer", "copyright", "disclosure");

    // TODO - CREATE OPENSEARCH IMPPLEMENTATION OF @processViaOpenSearch, List Must be changed to the DTO of OpenSearch
    public void processViaOpenSearch(List<CommunicationMessageDto> commMessages, AlertPolicyDto policy, LocalDate importDate) throws IsisException {

        log.debug("START - Processing Policy: {} for {} messages", policy.getName(), commMessages.size());

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

            log.debug("Processing inappropriate algorithm for message id {}", message.getId());

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
                                if(!checkifBypassEmit(emit, message, message.getSource())) {
                                    final var keywordDto= keywordsMap.get(emit.getKeyword().toLowerCase());

                                    if(keywordDto == null){
                                        log.warn("No keywords found in keywords map for {}", emit.getKeyword());
                                        return;
                                    }

                                    final var alert= AlertLogDto.builder()
                                            .commChainLinkId(message.getChainLinkId())
                                            .commMessageId(message.getId()) // TODO - hit ID from the hits item ie. searchResponse.hits().hits().get(i)._id;
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
                                            .recipients(message.getRecipients())
                                            .build();

                                    log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , message.getId());
                                    alertLogService.create(alert);
                                }
                            });
                }
            }

        }


        log.debug("COMPLETED - Processing Policy: {} for {} messages", policy.getName(), commMessages.size());

    }

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

            log.debug("Processing inappropriate algorithm for message id {}", message.getId());

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
                                if(!checkifBypassEmit(emit, message, message.getSource())) {
                                    final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(message.getChainLinkId()).build());

                                    final var keywordDto= keywordsMap.get(emit.getKeyword().toLowerCase());

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

    private static boolean checkifBypassEmit(Emit emit, CommunicationMessageDto messageDto, String source) {

        final String message = messageDto.getMessage();

        /*
        *   Reuters Disclaimer Text
        *
        *   Ex. [[:Conversations will be recorded and may be monitored by the participants and their employers, and may be disclosed to governmental or other regulatory bodies in compliance with applicable laws.You acknowledge and agree that any interactive
        *       features within Eikon, Eikon Messenger, Refinitiv Messenger, Refinitiv Workspace or any other services which are not advertised to be trade execution venues must not be used to conduct or execute any transactions or trades under any circumstances.:]]
        *
        * */
        if(message.matches("^[\\[\\[\\:\\:\\]\\]]([\\s\\S]*)$")) {

            int disclaimerStart = message.indexOf("[[:");
            int disclaimerEnd = message.indexOf(":]]");

            if(emit.getStart() > disclaimerStart && emit.getEnd() < disclaimerEnd){
                log.debug("Keyword found {} in messageId {} but within REUTERS disclaimer text. No alerts flagged", emit.getKeyword(), messageDto.getId());
                return true;
            }

        } else if (message.matches("^[*][*][*]([\\s\\S]*)Disclaimer:([\\s\\S]*)$")) {

            /*
            *   Bloomberg Chat Disclaimer Text
            *
            *  Ex. *** CLIENT NAME THAT (MARIANA UFP LLP) Firm Disclaimer: The contents of this message are confidential and intended for the addressee only. It may not be copied or reproduced and is not an investment recommendation as defined in MAR. It is issued
            *       for information and marketing purposes only and is not and should not be regarded as an offer to sell or buy any security or financial product. Emails and messages may not be secure or virus-free. We cannot accept responsibility for this message's
            *       completeness or accuracy, and it should not be relied upon as such. Mariana Capital Sales Limited (FRN 649237) is an appointed representative of Mariana UFP LLP which is authorised & regulated by the Financial Conduct Authority (FRN 551170).
            *
            * */
            log.debug("Keyword found {} in messageId {} but within BLOOOMBERG CHAT disclaimer text. No alerts flagged", emit.getKeyword(), messageDto.getId());
            return true;
        } else if(source.equals(SourceType.BLOOMBERG_CHAT.name()) && message.contains("BACKFILL CONTENT AUTOGENERATED FROM HISTORY")) {
            log.debug("Keyword found {} in messageId {} but within BACKFILL CONTENT text. No alerts flagged", emit.getKeyword(), messageDto.getId());
            return true;
        }else  if ((source.equals(SourceType.BLOOMBERG_MAIL.name()) || source.equals(SourceType.BLOOMBERG_CHAT.name())) && ListUtils.containsItemsFromList(emit.getKeyword(), DISCLAIMER_BYPASS_KEYWORDS)){

            /*
            *   Bypass messages with emits that are included in the disclaimer bypass keywords AND the message contains indicators which may identify it as a disclaimer
            * */

            if(ListUtils.containsItemsFromList(message, DISCLAIMER_INDICATORS)){
                log.debug("Keyword found {} in messageId {} but identified as BYPASS BLOOMBERG CHAT/MAIL disclaimer text. No alerts flagged", emit.getKeyword(), messageDto.getId());
                return true;
            }

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

                            if(!checkifBypassEmit(emit, communicationMessageService.mapToResource(commMessage), commMessage.getSource())) {
                                final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(commMessage.getChainLinkId()).build());

                                final var keywordDto= keywordsMap.get(emit.getKeyword().toLowerCase());

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
                            }
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

    public BatchAlertNotificationDto processSingleWithKeywords(CommunicationMessage commMessage,
                                                               AlertPolicyDto policy,
                                                               List<KeywordDto> keywordsList,
                                                               LocalDate importDate) throws IsisException {

        log.debug("STARTED - Processing Policy: {} for {}", policy.getName(), commMessage);

        List<AlertLog> alertResults = new ArrayList<>();

        if(commMessage == null){
            log.warn("No communication message found.");
            throw IsisException.notFound(
                    String.format("No communication message found."),
                    "NotFoundException");
        }

        final var policyId= policy.getId();

        if(keywordsList == null || ListUtils.isNullOrEmpty(keywordsList)){
            log.warn("No policy map keywords found for policy {}", policyId);
            throw IsisException.notFound(
                    String.format("No policy map keywords found for policy [%s]", policyId),
                    "NotFoundException");
        }

        // Generate keyword set for trie
        final var keywords = keywordsList.stream()
                .map(k -> k.getWord())
                .collect(Collectors.toSet());

        Trie trie = Trie.builder().onlyWholeWordsWhiteSpaceSeparated().ignoreCase().addKeywords(keywords).build();

        if(commMessage.getMessage() == null) {
            if(commMessage.getType() != null || !commMessage.getType().equals(MessageType.BLOOMBERG_CHAT_ATTACHMENT)){
                log.debug("Message Id {} has a message of NULL value but is from source {}. ", commMessage.getId(), commMessage.getSource());
            }
        } else {
            final var emits= trie.parseText(commMessage.getMessage());

            if(!emits.isEmpty()) {
                emits.stream()
                        .forEach( emit -> {

                            if(!checkifBypassEmit(emit, communicationMessageService.mapToResource(commMessage), commMessage.getSource())) {
                                final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(commMessage.getChainLinkId()).build());

                                final var keywordDto= keywordsList.stream().filter(k -> k.getWord().equalsIgnoreCase(emit.getKeyword())).findFirst();

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
                                        .keywordId(keywordDto.get().getId())
                                        .importDate(importDate)  // For internal use only
                                        .timeSent(commMessage.getTimeSent())
                                        .sender(commMessage.getSender())
                                        .source(commMessage.getSource())
                                        .recipients(recipients.getData().stream().map(CommunicationRecipientDto::getRecipient).collect(Collectors.toList()))
                                        .build();

                                log.info("Inappropriate Language Entry Found - Keyword: {} in Message Id: {}", emit.getKeyword() , commMessage.getId());

                                final var result = alertLogService.create(alert);

                                alertResults.add(alertLogService.mapToEntity(result));
                            }
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

                            final var keywordDto= keywordsMap.get(emit.getKeyword().toLowerCase());

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
                .collect(Collectors.toMap(e -> e.getWord().toLowerCase(), Function.identity()));
    }

}
