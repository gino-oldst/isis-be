package st.old.isis.storage.specifications;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import st.old.isis.dto.storage.ExternalUserDto;
import st.old.isis.storage.entities.ExternalUser;
import st.old.isis.utils.ListUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ExternalUserSpecification implements Specification<ExternalUser> {

  private static final long serialVersionUID = -7200869136738766556L;

  private final ExternalUserDto filter;

  @Override
  public Predicate toPredicate(
      @NonNull final Root<ExternalUser> root,
      @NonNull final CriteriaQuery<?> query,
      @NonNull final CriteriaBuilder cb) {

    final List<Predicate> predicates = new ArrayList<>();

    final var ids = filter.getIds();
    final var hasListOfIds = !ListUtils.isNullOrEmpty(ids);

    if (hasListOfIds) {
      predicates.add(root.get("id").in(ids));
    }

    final var externalIds = filter.getExternalIds();
    final var hasListOfExtIds = !ListUtils.isNullOrEmpty(externalIds);

    if (hasListOfExtIds) {
      predicates.add(root.get("externalId").in(externalIds));
    }

    final var sources = filter.getSources();
    final var hasListOfSources = !ListUtils.isNullOrEmpty(sources);

    if (hasListOfSources) {
      predicates.add(root.get("source").in(sources));
    }

    final var email = filter.getEmail();

    if (email != null) {
      predicates.add(cb.equal(root.get("email"), email));
    }

    final var sortPredicates = new ArrayList<Predicate>();

    final var likeEmail = filter.getLikeEmail();

    if (likeEmail != null) {
      sortPredicates.add(cb.like(root.get("email"), likeEmail + "%"));
    }

    final var likeFirstname = filter.getLikeFirstName();

    if (likeFirstname != null) {
      sortPredicates.add(cb.like(root.get("firstName"), likeFirstname + "%"));
    }

    final var likeLastname = filter.getLikeLastName();

    if (likeLastname != null) {
      sortPredicates.add(cb.like(root.get("lastName"), likeLastname + "%"));
    }

    if (!sortPredicates.isEmpty()) {
      predicates.add(cb.or(sortPredicates.toArray(Predicate[]::new)));
    }

    return cb.and(predicates.toArray(Predicate[]::new));
  }
}
