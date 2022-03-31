package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.MessageActivityLogDto;
import st.old.isis.storage.entities.MessageActivityLog;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MessageActivityLogSpecification implements Specification<MessageActivityLog> {

  private static final long serialVersionUID = -4578831824060839634L;

  private final MessageActivityLogDto filter;

  @Override
  public Predicate toPredicate(
      @NonNull final Root<MessageActivityLog> root,
      @NonNull final CriteriaQuery<?> query,
      @NonNull final CriteriaBuilder cb) {

    final List<Predicate> predicates = new ArrayList<>();

    final var messageId = filter.getMessageId();

    if (messageId != null) {
      predicates.add(cb.equal(root.get("messageId"), messageId));
    }

    final var type = filter.getType();

    if (messageId != null) {
      predicates.add(cb.equal(root.get("type"), type));
    }

    final var dateCreatedFrom = filter.getDateCreatedFrom();
    final var dateCreatedTo = filter.getDateCreatedTo();

    if (dateCreatedFrom != null && dateCreatedTo != null) {
      predicates.add(
          cb.between(
              root.get("dateCreated"),
              LocalDateTime.of(dateCreatedFrom, LocalTime.MIN),
              LocalDateTime.of(dateCreatedTo, LocalTime.MAX)));
    }

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
