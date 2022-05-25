package st.old.isis.storage.repositories.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import st.old.isis.storage.configurations.DatabaseConfiguration;
import st.old.isis.storage.entities.CommunicationMessage;
import st.old.isis.storage.repositories.StreamedCommunicationMessageRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class CommunicationMessageRepositoryImpl implements StreamedCommunicationMessageRepository {

  private final DatabaseConfiguration dbConfiguration;

  @PersistenceContext private EntityManager em;

  @Override
  public Stream<CommunicationMessage> findAll(@NonNull Specification<CommunicationMessage> spec) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<CommunicationMessage> query = cb.createQuery(CommunicationMessage.class);
    Root<CommunicationMessage> root = query.from(CommunicationMessage.class);
    query.where(spec.toPredicate(root, query, cb));

    return em.createQuery(query)
        .setHint(QueryHints.HINT_FETCH_SIZE, dbConfiguration.getBatchSize())
        .getResultStream();
  }
}
