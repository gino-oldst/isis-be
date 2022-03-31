package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.storage.entities.CommunicationMessage;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class RecentMessageSpecification implements Specification<CommunicationMessage> {

  private static final long serialVersionUID = 919526411281861134L;

  private final String chainLinkId;
  private final LocalDateTime timeSent;
  private final boolean greaterThan;

  @Override
  public Predicate toPredicate(
      @NonNull final Root<CommunicationMessage> root,
      @NonNull final CriteriaQuery<?> query,
      @NonNull final CriteriaBuilder cb) {

    final List<Predicate> predicates = new ArrayList<>();

    predicates.add(cb.equal(root.get("chainLinkId"), chainLinkId));

    if (greaterThan) {
      predicates.add(cb.greaterThan(root.get("timeSent"), timeSent));
      query.orderBy(cb.asc(root.get("timeSent")));
    }

    if (!greaterThan) {
      predicates.add(cb.lessThan(root.get("timeSent"), timeSent));
      query.orderBy(cb.desc(root.get("timeSent")));
    }

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
