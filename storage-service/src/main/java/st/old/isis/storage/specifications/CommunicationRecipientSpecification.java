package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.CommunicationRecipientDto;
import st.old.isis.storage.entities.CommunicationRecipient;
import st.old.isis.utils.ListUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommunicationRecipientSpecification implements Specification<CommunicationRecipient> {

    private static final long serialVersionUID = 4527114378830445569L;

    private final CommunicationRecipientDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<CommunicationRecipient> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        if(filter.getChainLinkId() != null) {
            predicates.add(cb.equal(root.get("chainLinkId"), filter.getChainLinkId()));
        }

        if(!ListUtils.isNullOrEmpty(filter.getChains())) {
            predicates.add(root.get("chainLinkId").in(filter.getChains()));
        }

        if(filter.getRecipient() != null) {
            predicates.add(cb.equal(root.get("recipient"), filter.getRecipient()));
        }

        if(!ListUtils.isNullOrEmpty(filter.getRecipients())) {
            predicates.add(root.get("recipient").in(filter.getRecipients()));
        }

        if(filter.getDateFrom() != null && filter.getDateTo() != null) {
            predicates.add(cb.between(root.get("dateImported"), filter.getDateFrom(), filter.getDateTo()));
        }

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
