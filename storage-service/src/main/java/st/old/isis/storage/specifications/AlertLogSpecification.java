package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.utils.ListUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AlertLogSpecification implements Specification<AlertLog> {

    private static final long serialVersionUID = 4861133374602285598L;

    private final AlertLogDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<AlertLog> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        if (filter.getCommMessageId() != null) {
            predicates.add(cb.equal(root.get("commMessageId"), filter.getCommMessageId()));
        }

        if (!ListUtils.isNullOrEmpty(filter.getCommMessageIds())) {
            predicates.add(root.get("commMessageId").in(filter.getCommMessageIds()));
        }

        if (filter.getKeyword() != null) {
            predicates.add(cb.like(root.get("keyword"), "%"+filter.getKeyword()+"%"));
        }

        if (!ListUtils.isNullOrEmpty(filter.getKeywordIds()) ) {
            predicates.add(root.get("keywordId").in(filter.getKeywordIds()));
        }

        if (filter.getDescription() != null) {
            predicates.add(cb.like(root.get("message"), "%"+filter.getDescription()+"%"));
        }

        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (!ListUtils.isNullOrEmpty(filter.getStatusTypes()) ) {
            predicates.add(root.get("status").in(filter.getStatusTypes()));
        }

        if (filter.getDateCreatedFromRange() != null && filter.getDateCreatedToRange() != null) {
            predicates.add(cb.between(root.get("dateCreated"), filter.getDateCreatedFromRange(), filter.getDateCreatedToRange()));
        }

        if (filter.getSender() != null) {
            predicates.add(cb.equal(root.get("sender"), filter.getSender()));
        }

        if (filter.getSource() != null) {
            predicates.add(cb.equal(root.get("source"), filter.getSource()));
        }

        if (!ListUtils.isNullOrEmpty(filter.getSources())) {
            predicates.add(root.get("source").in(filter.getSources()));
        }

        if (!ListUtils.isNullOrEmpty(filter.getSenders())) {
            predicates.add(root.get("sender").in(filter.getSenders()));
        }

        if (filter.getTimeSentFromRange() != null && filter.getTimeSentToRange() != null) {
            predicates.add(cb.between(root.get("timeSent"), filter.getTimeSentFromRange(), filter.getTimeSentToRange()));
        }

        if (filter.getEscalation() != null) {
            predicates.add(cb.equal(root.get("escalation"), filter.getEscalation()));
        }

        if (filter.getEscalation() != null) {
            predicates.add(cb.equal(root.get("escalation"), filter.getEscalation()));
        }

        if (!ListUtils.isNullOrEmpty(filter.getEscalations())) {
            predicates.add(root.get("escalation").in(filter.getEscalations()));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
