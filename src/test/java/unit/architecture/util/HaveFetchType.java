package unit.architecture.util;

import static com.tngtech.archunit.lang.ConditionEvent.createMessage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static unit.architecture.util.RelationshipAnnotations.anyRelationshipAnnotation;
import static unit.architecture.util.RelationshipAnnotations.relationshipAnnotations;
import static unit.architecture.util.RelationshipAnnotations.relationshipAnnotationsString;

import java.util.Optional;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;

import jakarta.persistence.FetchType;

public class HaveFetchType extends ArchCondition<JavaMember> {
    private final FetchType requiredFetchType;

    private HaveFetchType(FetchType requiredFetchType) {
        super("have fetch type " + requiredFetchType);
        this.requiredFetchType = requiredFetchType;
    }

    public static HaveFetchType haveFetchType(FetchType fetchType) {
        return new HaveFetchType(fetchType);
    }

    @Override
    public void check(JavaMember item, ConditionEvents events) {
        if (!item.isAnnotatedWith(anyRelationshipAnnotation)) {
            events.add(violated(item, createMessage(item,
                "is not annotated with one of " + relationshipAnnotationsString)));
            return;
        }

        final JavaAnnotation<?> relationship = relationshipAnnotations.stream()
            .map(Class::getName)
            .map(item::tryGetAnnotationOfType)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow();

        final var fetch = relationship.tryGetExplicitlyDeclaredProperty("fetch")
            .filter(JavaEnumConstant.class::isInstance)
            .map(JavaEnumConstant.class::cast)
            .filter(c -> c.getDeclaringClass().isEquivalentTo(FetchType.class))
            .map(c -> FetchType.valueOf(c.name()))
            .orElseThrow();

        if (fetch != requiredFetchType) {
            events.add(violated(item, createMessage(item, "has fetchType different from %s (%s)"
                .formatted(requiredFetchType, fetch))));
        }
    }
}
