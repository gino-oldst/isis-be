package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.AlertPolicyDto;
import st.old.isis.storage.entities.AlertPolicy;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class AlertPolicySpecification implements Specification<AlertPolicy> {

    private static final long serialVersionUID = 6047460486681931662L;

    private final AlertPolicyDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<AlertPolicy> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
