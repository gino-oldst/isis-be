package st.old.isis.storage.specifications;



import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.EscalationHistoryDto;
import st.old.isis.storage.entities.EscalationHistory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class EscalationHistorySpecification implements Specification<EscalationHistory> {

    private final EscalationHistoryDto filter;

    public EscalationHistorySpecification(EscalationHistoryDto filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<EscalationHistory> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getAlertId() != null) {
            predicates.add(cb.equal(root.get("alertId"), filter.getAlertId()));
        }

        if (filter.getEscalatedFrom() != null) {
            predicates.add(cb.equal(root.get("escalatedFrom"), filter.getEscalatedFrom()));
        }


        if (filter.getEscalatedTo() != null) {
            predicates.add(cb.equal(root.get("escalatedTo"), filter.getEscalatedTo()));
        }

        if (filter.getDateCreatedFromRange() != null && filter.getDateCreatedToRange() != null) {
            predicates.add(cb.between(root.get("dateCreated"), filter.getDateCreatedFromRange(), filter.getDateCreatedToRange()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
