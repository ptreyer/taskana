package pro.taskana;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenDependency;
import org.jboss.shrinkwrap.resolver.impl.maven.coordinate.MavenDependencyImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pro.taskana.simplehistory.rest.models.TaskHistoryEventListResource;
import pro.taskana.task.rest.models.TaskRepresentationModel;

/**
 * This test class is configured to run with postgres DB if you want to run it with h2 it is needed.
 * to change data source configuration at project-defaults.yml.
 */
@RunWith(Arquillian.class)
public class TaskanaWildflyWithSimpleHistoryEnabled extends AbstractAccTest {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TaskanaWildflyWithSimpleHistoryEnabled.class);

  @Deployment(testable = false)
  public static Archive<?> createTestArchive() {

    String applicationPropertyFile = "application.properties";
    String dbType = System.getProperty("db.type");
    if (dbType != null && !dbType.isEmpty()) {
      applicationPropertyFile = "application-" + dbType + ".properties";
    }

    LOGGER.info(
        "Running with db.type '{}' and using property file '{}'", dbType, applicationPropertyFile);

    MavenCoordinate simpleHistoryCoordinate =
        MavenCoordinates.createCoordinate(
            "pro.taskana.simplehistory",
            "taskana-simplehistory-rest-spring",
            DEPENDENCY_VERSION,
            PackagingType.JAR,
            null);

    MavenDependency simpleHistoryDependency =
        new MavenDependencyImpl(simpleHistoryCoordinate, ScopeType.TEST, false);

    File[] files =
        Maven.resolver()
            .loadPomFromFile("pom.xml")
            .importRuntimeDependencies()
            .addDependency(simpleHistoryDependency)
            .resolve()
            .withTransitivity()
            .asFile();

    return ShrinkWrap.create(WebArchive.class, "taskana.war")
        .addPackages(true, "pro.taskana")
        .addAsResource("taskana.properties")
        .addAsResource(applicationPropertyFile, "application.properties")
        .addAsResource("taskana-test.ldif")
        .addAsWebInfResource("int-test-web.xml", "web.xml")
        .addAsWebInfResource("int-test-jboss-web.xml", "jboss-web.xml")
        .addAsLibraries(files);
  }

  @Test
  @RunAsClient
  public void should_WriteHistoryEventIntoDatabase() {

    ResponseEntity<TaskHistoryEventListResource> getHistoryEventsResponse =
        performGetHistoryEventsRestCall();
    assertThat(getHistoryEventsResponse.getBody()).isNotNull();
    assertThat(getHistoryEventsResponse.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(getHistoryEventsResponse.getBody().getContent()).hasSize(0);

    ResponseEntity<TaskRepresentationModel> responseCreateTask = performCreateTaskRestCall();
    assertThat(HttpStatus.CREATED).isEqualTo(responseCreateTask.getStatusCode());
    assertThat(responseCreateTask.getBody()).isNotNull();

    getHistoryEventsResponse = performGetHistoryEventsRestCall();

    assertThat(getHistoryEventsResponse.getBody()).isNotNull();
    assertThat(getHistoryEventsResponse.getBody().getLink(IanaLinkRelations.SELF)).isNotNull();
    assertThat(getHistoryEventsResponse.getBody().getContent()).hasSize(1);
  }

}