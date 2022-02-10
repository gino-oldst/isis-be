package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.NotificationLogDto;
import st.old.isis.storage.entities.NotificationLog;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class NotificationLogSpecification implements Specification<NotificationLog> {

    private static final long serialVersionUID = 4857490383708533985L;

    private final NotificationLogDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<NotificationLog> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
