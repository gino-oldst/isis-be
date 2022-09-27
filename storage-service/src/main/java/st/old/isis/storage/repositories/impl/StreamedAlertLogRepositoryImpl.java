package st.old.isis.storage.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Component;
import st.old.isis.storage.configurations.DatabaseConfiguration;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.entities.AlertLog_;
import st.old.isis.storage.repositories.StreamedAlertLogRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class StreamedAlertLogRepositoryImpl implements StreamedAlertLogRepository {

    private final DatabaseConfiguration dbConfiguration;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Stream<AlertLog> findAll(Specification<AlertLog> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AlertLog> query = cb.createQuery(AlertLog.class);
        Root<AlertLog> root = query.from(AlertLog.class);
        query.where(spec.toPredicate(root, query, cb));

        return em.createQuery(query)
                .setHint(QueryHints.HINT_FETCH_SIZE, dbConfiguration.getBatchSize())
                .getResultStream();
    }

    @Override
    public Stream<String> findDistinctMessageIds(Specification<AlertLog> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<AlertLog> root = query.from(AlertLog.class);
        query.where(spec.toPredicate(root, query, cb));

        query.select(root.get(AlertLog_.commMessageId)).distinct(true);

        return em.createQuery(query)
                .setHint(QueryHints.HINT_FETCH_SIZE, dbConfiguration.getBatchSize())
                .getResultStream();
    }

    @Override
    public Page<Tuple> findDistinctMessageIdsPaged(Specification<AlertLog> spec, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = cb.createQuery(Tuple.class);
        Root<AlertLog> tupleRoot = tupleQuery.from(AlertLog.class);
        tupleQuery.where(spec.toPredicate(tupleRoot, tupleQuery, cb));
        tupleQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), tupleRoot, cb));

        tupleQuery.multiselect(tupleRoot.get(AlertLog_.commMessageId), tupleRoot.get(AlertLog_.timeSent), tupleRoot.get(AlertLog_.source), tupleRoot.get(AlertLog_.sender));
        List<Tuple> resultAlerts = em.createQuery(tupleQuery).setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

        // Create Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AlertLog> alertRootCount = countQuery.from(AlertLog.class);
        countQuery.select(cb.countDistinct(alertRootCount.get(AlertLog_.commMessageId)))
                .where(spec.toPredicate(tupleRoot, tupleQuery, cb));

        // Fetches the count of all Books as per given criteria
        Long count = em.createQuery(countQuery).getSingleResult();

        Page<Tuple> result = new PageImpl<>(resultAlerts, pageable, count);

        return result;

    }

}
