package st.old.isis.storage.specifications;


import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.AssignToUserHistoryDto;
import st.old.isis.storage.entities.AssignToUserHistory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class AssignToUserHistorySpecification implements Specification<AssignToUserHistory> {

    private final AssignToUserHistoryDto filter;

    public AssignToUserHistorySpecification(AssignToUserHistoryDto filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<AssignToUserHistory> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getAlertId() != null) {
            predicates.add(cb.equal(root.get("alertId"), filter.getAlertId()));
        }

        if (filter.getAssignedFrom() != null) {
            predicates.add(cb.equal(root.get("assignedFrom"), filter.getAssignedFrom()));
        }

        if (filter.getAssignedTo() != null) {
            predicates.add(cb.equal(root.get("assignedTo"), filter.getAssignedTo()));
        }

        if (filter.getDateCreatedFromRange() != null && filter.getDateCreatedToRange() != null) {
            predicates.add(cb.between(root.get("dateCreated"), filter.getDateCreatedFromRange(), filter.getDateCreatedToRange()));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
