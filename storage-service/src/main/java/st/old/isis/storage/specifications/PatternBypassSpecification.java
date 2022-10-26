package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.AlertLogDto;
import st.old.isis.dto.storage.PatternBypassDto;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.PatternBypass;
import st.old.isis.utils.ListUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PatternBypassSpecification implements Specification<PatternBypass> {

    private static final long serialVersionUID = 4861133374602285598L;

    private final PatternBypassDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<PatternBypass> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        if (filter.getPatternType() != null) {
            predicates.add(cb.equal(root.get("patternType"), filter.getPatternType()));
        }

        if (filter.getBypassType() != null) {
            predicates.add(cb.equal(root.get("bypassType"), filter.getBypassType()));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
