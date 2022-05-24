package st.old.isis.storage.specifications;

import antlr.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.utils.ListUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommunicationMessageSpecification implements Specification<CommunicationMessage> {

  private static final long serialVersionUID = 919526411281861134L;

  private final CommunicationMessageDto filter;

  @Override
  public Predicate toPredicate(
      @NonNull final Root<CommunicationMessage> root,
      @NonNull final CriteriaQuery<?> query,
      @NonNull final CriteriaBuilder cb) {

    final List<Predicate> predicates = new ArrayList<>();

    if (filter.getId() != null && !filter.getId().isEmpty()) {
      predicates.add(cb.like(root.get("id"), "%" + filter.getId() + "%"));
    }

    if (filter.getChainLinkId() != null) {
      predicates.add(cb.equal(root.get("chainLinkId"), filter.getChainLinkId()));
    }

    if (!ListUtils.isNullOrEmpty(filter.getChains())) {
      predicates.add(root.get("chainLinkId").in(filter.getChains()));
    }

    if (filter.getDateFrom() != null && filter.getDateTo() != null) {
      predicates.add(cb.between(root.get("importDate"), filter.getDateFrom(), filter.getDateTo()));
    }

    if (filter.getSender() != null) {
      predicates.add(cb.equal(root.get("sender"), filter.getSender()));
    }

    if (!ListUtils.isNullOrEmpty(filter.getSenders())) {
      predicates.add(root.get("sender").in(filter.getSenders()));
    }

    if (filter.getImportDate() != null) {
      predicates.add(cb.equal(root.get("importDate"), filter.getImportDate()));
    }

    if (filter.getSource() != null) {
      predicates.add(cb.equal(root.get("source"), filter.getSource()));
    }

    if (!ListUtils.isNullOrEmpty(filter.getSources())) {
      predicates.add(root.get("source").in(filter.getSources()));
    }

    if (filter.getType() != null) {
      predicates.add(cb.equal(root.get("type"), filter.getType()));
    }

    if (filter.getSubject() != null) {
      predicates.add(
          cb.like(cb.upper(root.get("subject")), "%" + filter.getSubject().toUpperCase() + "%"));
    }

    if (filter.getDateSentFrom() != null && filter.getDateSentTo() != null) {
      predicates.add(
          cb.between(root.get("timeSent"), filter.getDateSentFrom(), filter.getDateSentTo()));
    }

    predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
