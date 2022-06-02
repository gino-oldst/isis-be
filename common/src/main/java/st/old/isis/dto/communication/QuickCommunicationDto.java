package st.old.isis.dto.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import st.old.isis.dto.CsvList;
import st.old.isis.dto.Dto;
import st.old.isis.dto.list.ListDto;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.enums.AlertStatusType;
import st.old.isis.utils.CsvUtil;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickCommunicationDto implements Dto, CsvList {

  private static final long serialVersionUID = -1184001885573580486L;

  private CommunicationMessageDto message;

  private ListDto<AlertLogDto> alerts;

  //    private ListDto<CommunicationRecipientDto> recipients;

  private LocalDateTime dateFrom;

  private LocalDateTime dateTo;

  private List<String> senders;

  private List<String> keywords;

  private List<String> keywordIds;

  private List<String> sources;

  private List<String> recipients;

  private List<AlertStatusType> statusTypes;

  public static List<String> csvHeaders() {
    return List.of("messageId", "status", "dateTime", "source", "sender", "recipients");
  }

  @Override
  public List<String> toCsvStringList() {

    return alerts.getData().stream()
        .map(
            alert ->
                CsvUtil.mapToCsv(
                    List.of(
                        alert.getCommMessageId(),
                        alert.getStatus().name(),
                        alert.getTimeSent().toString(),
                        alert.getSource(),
                        alert.getSender(),
                        Arrays.toString(alert.getRecipients().toArray()))))
        .collect(Collectors.toList());
  }
}
