package ewm.events.repository.specification;

import ewm.events.model.Event;
import ewm.events.model.State;
import ewm.participationRequest.model.ParticipationRequest;
import ewm.participationRequest.model.ParticipationStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
/*
Тут прописываем спецификацию для наших фильтров, для пояснения скажу:
root - по сути тоже самое, что и FROM, т.е. с помощью него мы получаем поля
query - представляет сам каркас SQL-запроса
cb или же CriteriaBuilder - используется для созданий выраженией, условий, оператов и функций

Каждая Specification возвращает Predicate (условие),
которое затем добавляется в итоговый SQL-запрос.

далее мы в EventsServiceImpl используем конструктор для этой самой спецификации

т.е. вместо JPQL запросов мы прописываем запросы тут и вместо огромного кода, просто создаем конструктор
*/
public class EventSpecification {

    public static Specification<Event> published() {
        return (root, query, cb) ->
                cb.equal(root.get("state"), State.PUBLISHED);
    }

    public static Specification<Event> hasText(String text) {

        return (root, query, cb) -> {

            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + text.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> hasCategories(List<Long> categories) {

        return (root, query, cb) -> {

            if (categories == null || categories.isEmpty()) {
                return cb.conjunction();
            }

            return root.get("category")
                    .get("id")
                    .in(categories);
        };
    }

    public static Specification<Event> hasPaid(Boolean paid) {

        return (root, query, cb) -> {

            if(paid == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("paid"), paid);
        };
    }

    public static Specification<Event> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, cb) -> {
            if (onlyAvailable == null || !onlyAvailable) {
                return cb.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ParticipationRequest> requestRoot = subquery.from(ParticipationRequest.class);
            subquery.select(cb.count(requestRoot))
                    .where(
                            cb.equal(requestRoot.get("event"), root),
                            cb.equal(requestRoot.get("status"), ParticipationStatus.CONFIRMED)
                    );

            return cb.or(
                    cb.equal(root.get("participantLimit"), 0),
                    cb.lessThan(subquery, root.get("participantLimit"))
            );
        };
    }

    public static Specification<Event> dateAfter(LocalDateTime start) {

        return (root, query, cb) -> {

            if (start == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(root.get("eventDate"), start);
        };
    }

    public static Specification<Event> dateBefore(LocalDateTime end) {

        return (root, query, cb) -> {

            if (end == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("eventDate"), end);
        };
    }

    public static Specification<Event> hasUser(List<Long> users) {
        return (root, query, cb) -> {
            if (users == null || users.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("initiator").get("id").in(users);
        };
    }

    public static Specification<Event> hasStates(List<State> states) {

        return (root, query, cb) -> {

            if (states == null || states.isEmpty()) {
                return cb.conjunction();
            }

            return root.get("state")
                    .in(states);
        };
    }
}

