package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.MessageActivityLogDto;
import st.old.isis.storage.entities.MessageActivityLog;
import st.old.isis.utils.ListUtils;

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

    if (filter.getMessageId() != null) {
      predicates.add(cb.equal(root.get("messageId"), filter.getMessageId()));
    }

    if (filter.getType() != null) {
      predicates.add(cb.equal(root.get("type"), filter.getType()));
    }

    if (filter.getExecutorId() != null) {
      predicates.add(cb.equal(root.get("executorId"), filter.getExecutorId()));
    }

    if (!ListUtils.isNullOrEmpty(filter.getExecutorIds())) {
      predicates.add(root.get("executorId").in(filter.getExecutorIds()));
    }

    if (filter.getDateCreatedFrom() != null && filter.getDateCreatedTo() != null) {
      predicates.add(
          cb.between(
              root.get("dateCreated"),
              LocalDateTime.of(filter.getDateCreatedFrom(), LocalTime.MIN),
              LocalDateTime.of(filter.getDateCreatedTo(), LocalTime.MAX)));
    }

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
