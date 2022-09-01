package st.old.isis.storage.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import st.old.isis.storage.configurations.DatabaseConfiguration;
import st.old.isis.storage.entities.AlertLog;
import st.old.isis.storage.repositories.StreamedAlertLogRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class StreamedAlertLogRepositoryImpl implements StreamedAlertLogRepository {

  private final DatabaseConfiguration dbConfiguration;

  @PersistenceContext private EntityManager em;

  @Override
  public Stream<AlertLog> findAll(@NonNull Specification<AlertLog> spec) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<AlertLog> query = cb.createQuery(AlertLog.class);
    Root<AlertLog> root = query.from(AlertLog.class);
    query.where(spec.toPredicate(root, query, cb));

    return em.createQuery(query)
        .setHint(QueryHints.HINT_FETCH_SIZE, dbConfiguration.getBatchSize())
        .getResultStream();
  }

}
