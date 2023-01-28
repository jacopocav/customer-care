package unit.architecture.util;

import static java.util.stream.Collectors.joining;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RelationshipAnnotations {
    public static final Set<Class<? extends Annotation>> relationshipAnnotations = Set.of(
        OneToMany.class,
        OneToOne.class,
        ManyToOne.class,
        ManyToMany.class
    );
    public static final String relationshipAnnotationsString = relationshipAnnotations.stream()
        .map(Class::getSimpleName)
        .collect(joining(", ", "[", "]"));

    public static final DescribedPredicate<JavaAnnotation<?>> anyRelationshipAnnotation =
        new DescribedPredicate<>("one of " + relationshipAnnotationsString) {
            @Override
            public boolean test(JavaAnnotation<?> annotation) {
                final var type = annotation.getRawType();
                return relationshipAnnotations.stream()
                    .anyMatch(type::isEquivalentTo);
            }
        };
}
