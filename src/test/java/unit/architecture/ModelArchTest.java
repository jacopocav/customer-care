package unit.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.members;
import static jakarta.persistence.FetchType.LAZY;
import static unit.architecture.util.HaveFetchType.haveFetchType;
import static unit.architecture.util.RelationshipAnnotations.anyRelationshipAnnotation;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import io.jacopocav.customercare.CustomerCareApplication;
import io.jacopocav.customercare.model.CommonModel;
import jakarta.persistence.Entity;

@AnalyzeClasses(packagesOf = CustomerCareApplication.class)
public class ModelArchTest {
    @ArchTest
    static final ArchRule allEntitiesShouldExtendCommonModel =
        classes().that().areAnnotatedWith(Entity.class)
            .should().beAssignableTo(CommonModel.class);

    @ArchTest
    static final ArchRule relationshipsBetweenEntitiesShouldAlwaysBeLazy =
        members().that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and().areAnnotatedWith(anyRelationshipAnnotation)
            .should(haveFetchType(LAZY));
}
