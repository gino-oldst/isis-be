package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.storage.entities.AlertLog;

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

        if (filter.getKeyword() != null) {
            predicates.add(cb.like(root.get("keyword"), "%"+filter.getKeyword()+"%"));
        }

        if (filter.getDescription() != null) {
            predicates.add(cb.like(root.get("message"), "%"+filter.getDescription()+"%"));
        }

        if (filter.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getDateCreatedFromRange() != null && filter.getDateCreatedToRange() != null) {
            predicates.add(cb.between(root.get("dateCreated"), filter.getDateCreatedFromRange(), filter.getDateCreatedToRange()));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
