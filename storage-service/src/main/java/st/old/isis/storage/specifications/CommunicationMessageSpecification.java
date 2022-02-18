package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.CommunicationMessageDto;
import st.old.isis.storage.entities.CommunicationMessage;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommunicationMessageSpecification implements Specification<CommunicationMessage> {

    private final CommunicationMessageDto filter;

    @Override
    public Predicate toPredicate(@NonNull final Root<CommunicationMessage> root,
                                 @NonNull final CriteriaQuery<?> query,
                                 @NonNull final CriteriaBuilder cb) {

        final List<Predicate> predicates = new ArrayList<>();

        if(filter.getChainLinkId() != null){
            predicates.add(cb.equal(root.get("chainLinkId"), filter.getChainLinkId()));
        }

        predicates.add(cb.equal(root.get("deleted"), filter.isDeleted()));

        return cb.and(predicates.toArray(Predicate[]::new));
    }
}
