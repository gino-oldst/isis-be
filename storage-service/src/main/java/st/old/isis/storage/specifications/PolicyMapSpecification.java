package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.PolicyMapDto;
import st.old.isis.storage.entities.PolicyMap;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PolicyMapSpecification implements Specification<PolicyMap> {

    private static final long serialVersionUID = 5531469358824329961L;

    private final PolicyMapDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<PolicyMap> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
