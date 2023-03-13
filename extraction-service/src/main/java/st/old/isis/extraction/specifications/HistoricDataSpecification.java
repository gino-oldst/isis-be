package st.old.isis.extraction.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.extraction.entities.HistoricData;
import st.old.isis.dto.storage.HistoricDataDto;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class HistoricDataSpecification implements Specification<HistoricData> {

    private final HistoricDataDto filter;
    
    @Override
    public Predicate toPredicate(@NonNull Root<HistoricData> root, 
                                @NonNull CriteriaQuery<?> query, 
                                @NonNull CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("expired"), filter.isExpired()));

        return cb.and(predicates.toArray(Predicate[]::new));
    }
    
}
