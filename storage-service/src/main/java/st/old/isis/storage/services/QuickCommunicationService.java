package st.old.isis.storage.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import st.old.isis.annotations.Audited;
import st.old.isis.dto.communication.QuickCommunicationDto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.KeywordDto;
import st.old.isis.exception.IsisException;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.repositories.AlertLogRepository;
import st.old.isis.storage.repositories.StreamedAlertLogRepository;
import st.old.isis.storage.specifications.AlertLogSpecification;
import st.old.isis.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuickCommunicationService {

    private final AlertLogRepository alertLogRepository;
    private final AlertLogService alertLogService;
    private final KeywordService keywordService;
    private final StreamedAlertLogRepository streamedAlertLogRepository;

    @Audited(event = "SEARCH_PAGED_QUICK_COMMUNICATION")
    public ListDto<QuickCommunicationDto> search(@NonNull @Audited final QuickCommunicationDto filter, @Audited Pageable pageable) throws IsisException {

        final var quickCommList = filterAlerts(filter, pageable);

        return pagedResults(quickCommList, pageable);
    }

    private List<QuickCommunicationDto> filterAlerts(@NonNull final QuickCommunicationDto filter, Pageable pageable) throws IsisException {
        // Remove status sorting for now
        final var sortByStatus = pageable.getSort().by("status");

        Sort newSort = Sort.by(pageable.getSort()
                .get()
                .filter(order -> !order.getProperty().equals("status"))
                .collect(Collectors.toList()));

        List<Object[]> distinctMessageIds = alertLogRepository.findDistinctMessagesOrderBy(newSort);

        if (ListUtils.isNullOrEmpty(distinctMessageIds)) {
            throw IsisException.notFound("No messages found for indicated filter", "NotFoundException");
        }

        final var keyword =
                keywordService.search(KeywordDto.builder().build()).getData().stream()
                        .collect(Collectors.toMap(KeywordDto::getId, Function.identity()));

        final List<QuickCommunicationDto> quickCommList = new ArrayList<>();

        final var filterDateTimeFrom = filter.getDateFrom();
        final var filterDateTimeTo = filter.getDateTo();

        distinctMessageIds.forEach(obj -> {

            String messageId = (String) obj[0];

            // Retrieve alerts for that specific message
            final var alerts = alertLogService.search(AlertLogDto.builder()
                    .keywordIds(filter.getKeywordIds())
                    .statusTypes(filter.getStatusTypes())
                    .sources(filter.getSources())
                    .senders(filter.getSenders())
                    .timeSentFromRange(filterDateTimeFrom)
                    .timeSentToRange(filterDateTimeTo)
                    .commMessageId(messageId).build(), sortByStatus);

            var filteredAlerts = alerts.getData();

            if (!ListUtils.isNullOrEmpty(filter.getRecipients())) {
                filteredAlerts = alerts.getData().stream().filter(alert -> {
                    String joinString = String.join(",", alert.getRecipients());
                    return filter.getRecipients().stream().allMatch(r -> joinString.contains(r));
                }).collect(Collectors.toList());
            }

            if (!ListUtils.isNullOrEmpty(filteredAlerts)) {

                filteredAlerts
                        .forEach(alert -> {
                            final var key = keyword.get(alert.getKeywordId());
                            if (key != null) {
                                alert.setAlertName("Alert " + key.getNumber());
                            }
                        });
//                    final var chainLinkId = alerts.getData().get(0).getCommChainLinkId();

                // Retrieve the recipients
//                    final var recipients = communicationRecipientService.search(CommunicationRecipientDto.builder().chainLinkId(chainLinkId).build());

                final var filteredListDto = ListDto.<AlertLogDto>builder()
                        .data(filteredAlerts)
                        .totalNoOfRecords(filteredAlerts.size())
                        .build();

                final var quickComm = QuickCommunicationDto.builder()
                        .alerts(filteredListDto)
//                            .recipients(recipients)
                        .build();

                quickCommList.add(quickComm);
            }

        });

        return quickCommList;
    }

    @Transactional(readOnly = true)
    public ListDto<QuickCommunicationDto> getFilteredAlerts(@NonNull final QuickCommunicationDto filter, Pageable pageable) throws IsisException {

        log.info("STARTED: getFilteredAlerts - {}", filter);

        // Remove status sorting for now
        final var sortByStatus = pageable.getSort().by("status");

        Sort newSort = Sort.by(pageable.getSort()
                .get()
                .filter(order -> !order.getProperty().equals("status"))
                .collect(Collectors.toList()));

        final var pageSize = pageable.getPageSize();
        final var pageNumber = pageable.getPageNumber();

        final var keyword =
                keywordService.search(KeywordDto.builder().build()).getData().stream()
                        .collect(Collectors.toMap(KeywordDto::getId, Function.identity()));

        final List<QuickCommunicationDto> quickCommList = new ArrayList<>();
        int totalDistinctMessageCount = 0;

        final var filterDateTimeFrom = filter.getDateFrom();
        final var filterDateTimeTo = filter.getDateTo();

        AlertLog current = null;

        final var alertFilter = AlertLogDto.builder()
                .keywordIds(filter.getKeywordIds())
                .statusTypes(filter.getStatusTypes())
                .sources(filter.getSources())
                .senders(filter.getSenders())
                .timeSentFromRange(filterDateTimeFrom)
                .timeSentToRange(filterDateTimeTo).build();

        final var spec = new AlertLogSpecification(alertFilter);

        try (final var distinctMessageIds = streamedAlertLogRepository.findAll(spec).filter(distinctByKey(id -> id.getCommMessageId()))) {
            final var itr = distinctMessageIds.iterator();
            current = itr.next();

            int counter = 0;

            while (quickCommList.size() < pageSize && itr.hasNext()) {
                totalDistinctMessageCount++;
                counter++;
                if (counter > pageSize * pageNumber) {
                    String messageId = (String) current.getCommMessageId();

                    // Retrieve alerts for that specific message
                    final var alerts = alertLogService.search(AlertLogDto.builder()
                            .keywordIds(filter.getKeywordIds())
                            .statusTypes(filter.getStatusTypes())
                            .sources(filter.getSources())
                            .senders(filter.getSenders())
                            .timeSentFromRange(filterDateTimeFrom)
                            .timeSentToRange(filterDateTimeTo)
                            .commMessageId(messageId).build(), sortByStatus);

                    var filteredAlerts = alerts.getData();

                    if (!ListUtils.isNullOrEmpty(filter.getRecipients())) {
                        filteredAlerts = alerts.getData().stream().filter(alert -> {
                            String joinString = String.join(",", alert.getRecipients());
                            return filter.getRecipients().stream().allMatch(r -> joinString.contains(r));
                        }).collect(Collectors.toList());
                    }

                    if (!ListUtils.isNullOrEmpty(filteredAlerts)) {

                        filteredAlerts
                                .forEach(alert -> {
                                    final var key = keyword.get(alert.getKeywordId());
                                    if (key != null) {
                                        alert.setAlertName("Alert " + key.getNumber());
                                    }
                                });

                        final var filteredListDto = ListDto.<AlertLogDto>builder()
                                .data(filteredAlerts)
                                .totalNoOfRecords(filteredAlerts.size())
                                .build();

                        final var quickComm = QuickCommunicationDto.builder()
                                .alerts(filteredListDto)
                                .build();

                        quickCommList.add(quickComm);
                    }
                }
            }
        };

        return ListDto.<QuickCommunicationDto>builder()
                .data(quickCommList)
                .totalPages(totalDistinctMessageCount / pageSize)
                .totalNoOfRecords(totalDistinctMessageCount)
                .totalCurrentElements(quickCommList.size())
                .build().success();

    }

    @Transactional(readOnly = true)
        public ListDto<QuickCommunicationDto> pagedFilteredAlerts(@NonNull final QuickCommunicationDto filter, Pageable pageable) throws IsisException {

        log.info("STARTED: getFilteredAlerts - {}", filter);

        // Remove status sorting for now
        final var sortByStatus = pageable.getSort().by("status");

        final var keyword =
                keywordService.search(KeywordDto.builder().build()).getData().stream()
                        .collect(Collectors.toMap(KeywordDto::getId, Function.identity()));

        final List<QuickCommunicationDto> quickCommList = new ArrayList<>();

        final var filterDateTimeFrom = filter.getDateFrom();
        final var filterDateTimeTo = filter.getDateTo();

        final var alertFilter = AlertLogDto.builder()
                .keywordIds(filter.getKeywordIds())
                .policyIds(filter.getPolicyIds())
                .statusTypes(filter.getStatusTypes())
                .sources(filter.getSources())
                .senders(filter.getSenders())
                .escalations(filter.getEscalations())
                .timeSentFromRange(filterDateTimeFrom)
                .timeSentToRange(filterDateTimeTo).build();

        final var spec = new AlertLogSpecification(alertFilter);

        final var distinctMessageIds = streamedAlertLogRepository.findDistinctMessageIdsPaged(spec, pageable);

        distinctMessageIds.getContent().stream().forEach(messageId -> {

            alertFilter.setCommMessageId((String) messageId.get(0));

            // Retrieve alerts for that specific message
            final var alerts = alertLogService.search(alertFilter, sortByStatus);

            var filteredAlerts = alerts.getData();

            if (!ListUtils.isNullOrEmpty(filter.getRecipients())) {
                filteredAlerts = alerts.getData().stream().filter(alert -> {
                    String joinString = String.join(",", alert.getRecipients());
                    return filter.getRecipients().stream().allMatch(r -> joinString.contains(r));
                }).collect(Collectors.toList());
            }

            if (!ListUtils.isNullOrEmpty(filteredAlerts)) {

                filteredAlerts
                        .forEach(alert -> {
                            final var key = keyword.get(alert.getKeywordId());
                            if (key != null) {
                                alert.setAlertName("Alert " + key.getNumber());
                            }
                        });

                final var filteredListDto = ListDto.<AlertLogDto>builder()
                        .data(filteredAlerts)
                        .totalNoOfRecords(filteredAlerts.size())
                        .build();

                final var quickComm = QuickCommunicationDto.builder()
                        .alerts(filteredListDto)
                        .build();

                quickCommList.add(quickComm);
            }


        });

        return ListDto.<QuickCommunicationDto>builder()
                .data(quickCommList)
                .totalPages(distinctMessageIds.getTotalPages())
                .totalNoOfRecords(distinctMessageIds.getTotalElements())
                .totalCurrentElements(distinctMessageIds.getNumberOfElements())
                .build().success();
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private ListDto<QuickCommunicationDto> pagedResults(List<QuickCommunicationDto> quickCommList, Pageable pageable) {
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = (pageable.getPageSize() * (pageable.getPageNumber() + 1)) > quickCommList.size() ? quickCommList.size() : pageable.getPageSize() * (pageable.getPageNumber() + 1);

        final var totalSize = quickCommList.size();
        final var pageSize = pageable.getPageSize();
        var totalPages = totalSize / pageSize + ((totalSize % pageSize > 0) ? 1 : 0);

        if (start > end) {
            return ListDto.<QuickCommunicationDto>builder()
                    .totalNoOfRecords(totalSize)
                    .totalCurrentElements(0)
                    .totalPages(totalPages)
                    .data(new ArrayList<>())
                    .build()
                    .success();
        }

        final var finalQuickComm = quickCommList.subList(start, end);

        return ListDto.<QuickCommunicationDto>builder()
                .data(finalQuickComm)
                .totalPages(totalPages)
                .totalNoOfRecords(totalSize)
                .totalCurrentElements(finalQuickComm.size())
                .build().success();
    }

    public List<QuickCommunicationDto> downloadAlerts(@NonNull final QuickCommunicationDto filter) throws IsisException {
        return filterAlerts(filter, Pageable.unpaged());
    }
}
