package pro.taskana.monitor.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.taskana.TaskanaEngineConfiguration;
import pro.taskana.common.api.TaskanaEngine;
import pro.taskana.common.internal.InternalTaskanaEngine;
import pro.taskana.monitor.api.SelectedItem;
import pro.taskana.monitor.api.TaskTimestamp;
import pro.taskana.monitor.api.reports.ClassificationCategoryReport;
import pro.taskana.monitor.api.reports.header.TimeIntervalColumnHeader;
import pro.taskana.monitor.api.reports.item.MonitorQueryItem;
import pro.taskana.task.api.TaskCustomField;
import pro.taskana.task.api.TaskState;

/** Unit Test for CategoryBuilderImpl. */
@ExtendWith(MockitoExtension.class)
class ClassificationClassificationCategoryReportBuilderImplTest {

  @InjectMocks private MonitorServiceImpl cut;

  @Mock private InternalTaskanaEngine internalTaskanaEngineMock;

  @Mock private TaskanaEngine taskanaEngineMock;

  @Mock private TaskanaEngineConfiguration taskanaEngineConfiguration;

  @Mock private MonitorMapper monitorMapperMock;

  @BeforeEach
  void setup() {
    when(internalTaskanaEngineMock.getEngine()).thenReturn(taskanaEngineMock);
  }

  @Test
  void testGetTotalNumbersOfCategoryReport() throws Exception {
    final List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    final List<TaskState> states = List.of(TaskState.CLAIMED, TaskState.READY);
    final List<String> categories = List.of("EXTERN");
    final List<String> domains = List.of("DOMAIN_A");
    final List<String> classificationIds = List.of("L10000");
    final List<String> excludedClassificationIds = List.of("L20000");
    Map<TaskCustomField, String> customAttributeFilter = new HashMap<>();
    customAttributeFilter.put(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A");

    List<MonitorQueryItem> expectedResult = new ArrayList<>();
    MonitorQueryItem monitorQueryItem = new MonitorQueryItem();
    monitorQueryItem.setKey("EXTERN");
    monitorQueryItem.setNumberOfTasks(1);
    expectedResult.add(monitorQueryItem);
    when(monitorMapperMock.getTaskCountOfCategories(
            any(),
            eq(workbasketIds),
            eq(states),
            eq(categories),
            eq(domains),
            eq(TaskTimestamp.DUE),
            eq(classificationIds),
            eq(excludedClassificationIds),
            eq(customAttributeFilter)))
        .thenReturn(expectedResult);

    final ClassificationCategoryReport actualResult =
        cut.createClassificationCategoryReportBuilder()
            .workbasketIdIn(workbasketIds)
            .stateIn(states)
            .classificationCategoryIn(categories)
            .domainIn(domains)
            .classificationIdIn(classificationIds)
            .excludedClassificationIdIn(excludedClassificationIds)
            .customAttributeFilterIn(customAttributeFilter)
            .buildReport();

    verify(internalTaskanaEngineMock).openConnection();
    verify(internalTaskanaEngineMock, times(2)).getEngine();
    verify(taskanaEngineMock).checkRoleMembership(any());
    verify(taskanaEngineMock).getWorkingDaysToDaysConverter();
    verify(monitorMapperMock)
        .getTaskCountOfCategories(any(), any(), any(), any(), any(), any(), any(), any(), any());
    verify(internalTaskanaEngineMock).returnConnection();
    verifyNoMoreInteractions(
        internalTaskanaEngineMock,
        taskanaEngineMock,
        monitorMapperMock,
        taskanaEngineConfiguration);

    assertThat(actualResult).isNotNull();
    assertThat(actualResult.getRow("EXTERN").getTotalValue()).isOne();
    assertThat(actualResult.getSumRow().getTotalValue()).isOne();
  }

  @Test
  void testGetCategoryReportWithReportLineItemDefinitions() throws Exception {
    final List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    final List<TaskState> states = List.of(TaskState.CLAIMED, TaskState.READY);
    final List<String> categories = List.of("EXTERN");
    final List<String> domains = List.of("DOMAIN_A");
    final List<String> classificationIds = List.of("L10000");
    final List<String> excludedClassificationIds = List.of("L20000");
    Map<TaskCustomField, String> customAttributeFilter = new HashMap<>();
    customAttributeFilter.put(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A");
    final List<TimeIntervalColumnHeader> columnHeaders =
        List.of(new TimeIntervalColumnHeader(0, 0));

    final List<MonitorQueryItem> expectedResult = new ArrayList<>();
    MonitorQueryItem monitorQueryItem = new MonitorQueryItem();
    monitorQueryItem.setKey("EXTERN");
    monitorQueryItem.setAgeInDays(0);
    monitorQueryItem.setNumberOfTasks(1);
    expectedResult.add(monitorQueryItem);
    when(monitorMapperMock.getTaskCountOfCategories(
            any(),
            eq(workbasketIds),
            eq(states),
            eq(categories),
            eq(domains),
            eq(TaskTimestamp.DUE),
            eq(classificationIds),
            eq(excludedClassificationIds),
            eq(customAttributeFilter)))
        .thenReturn(expectedResult);

    final ClassificationCategoryReport actualResult =
        cut.createClassificationCategoryReportBuilder()
            .workbasketIdIn(workbasketIds)
            .stateIn(states)
            .classificationCategoryIn(categories)
            .domainIn(domains)
            .classificationIdIn(classificationIds)
            .excludedClassificationIdIn(excludedClassificationIds)
            .customAttributeFilterIn(customAttributeFilter)
            .withColumnHeaders(columnHeaders)
            .buildReport();

    verify(internalTaskanaEngineMock).openConnection();
    verify(internalTaskanaEngineMock, times(2)).getEngine();
    verify(taskanaEngineMock).checkRoleMembership(any());
    verify(taskanaEngineMock).getWorkingDaysToDaysConverter();
    verify(monitorMapperMock)
        .getTaskCountOfCategories(any(), any(), any(), any(), any(), any(), any(), any(), any());
    verify(internalTaskanaEngineMock).returnConnection();
    verifyNoMoreInteractions(
        internalTaskanaEngineMock,
        taskanaEngineMock,
        monitorMapperMock,
        taskanaEngineConfiguration);

    assertThat(actualResult).isNotNull();
    assertThat(actualResult.getRow("EXTERN").getTotalValue()).isOne();
    assertThat(actualResult.getRow("EXTERN").getCells()[0]).isOne();
    assertThat(actualResult.getSumRow().getTotalValue()).isOne();
  }

  @Test
  void testListTaskIdsOfCategoryReportForSelectedItems() throws Exception {
    final List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    final List<TaskState> states = List.of(TaskState.CLAIMED, TaskState.READY);
    final List<String> categories = List.of("EXTERN");
    final List<String> domains = List.of("DOMAIN_A");
    final List<String> classificationIds = List.of("L10000");
    final List<String> excludedClassificationIds = List.of("L20000");
    final Map<TaskCustomField, String> customAttributeFilter = new HashMap<>();
    customAttributeFilter.put(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A");
    final List<TimeIntervalColumnHeader> columnHeaders =
        List.of(new TimeIntervalColumnHeader(0, 0));

    List<SelectedItem> selectedItems = List.of(new SelectedItem("EXTERN", null, 1, 5));

    List<String> expectedResult = List.of("TKI:000000000000000000000000000000000001");
    when(monitorMapperMock.getTaskIdsForSelectedItems(
            any(),
            eq(workbasketIds),
            eq(states),
            eq(categories),
            eq(domains),
            eq(classificationIds),
            eq(excludedClassificationIds),
            eq(customAttributeFilter),
            eq(null),
            eq("CLASSIFICATION_CATEGORY"),
            eq(TaskTimestamp.DUE),
            eq(selectedItems),
            eq(false)))
        .thenReturn(expectedResult);

    final List<String> actualResult =
        cut.createClassificationCategoryReportBuilder()
            .workbasketIdIn(workbasketIds)
            .stateIn(states)
            .classificationCategoryIn(categories)
            .domainIn(domains)
            .classificationIdIn(classificationIds)
            .excludedClassificationIdIn(excludedClassificationIds)
            .customAttributeFilterIn(customAttributeFilter)
            .withColumnHeaders(columnHeaders)
            .listTaskIdsForSelectedItems(selectedItems, TaskTimestamp.DUE);

    verify(internalTaskanaEngineMock).openConnection();
    verify(internalTaskanaEngineMock, times(2)).getEngine();
    verify(taskanaEngineMock).checkRoleMembership(any());
    verify(taskanaEngineMock).getWorkingDaysToDaysConverter();
    verify(monitorMapperMock)
        .getTaskIdsForSelectedItems(
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            eq(TaskTimestamp.DUE),
            any(),
            eq(false));
    verify(internalTaskanaEngineMock).returnConnection();
    verifyNoMoreInteractions(
        internalTaskanaEngineMock,
        taskanaEngineMock,
        monitorMapperMock,
        taskanaEngineConfiguration);

    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @Test
  void testListTaskIdsForSelectedItemsIsEmptyResult() throws Exception {
    List<String> result =
        cut.createClassificationCategoryReportBuilder()
            .listTaskIdsForSelectedItems(
                List.of(new SelectedItem("BLA", null, 0, 0)), TaskTimestamp.DUE);
    assertThat(result).isNotNull();
  }

  @Test
  void testListCustomAttributeValuesForCustomAttributeName() throws Exception {
    final List<String> workbasketIds = List.of("WBI:000000000000000000000000000000000001");
    final List<TaskState> states = List.of(TaskState.CLAIMED, TaskState.READY);
    final List<String> categories = List.of("EXTERN");
    final List<String> domains = List.of("DOMAIN_A");
    final List<String> classificationIds = List.of("L10000");
    final List<String> excludedClassificationIds = List.of("L20000");
    Map<TaskCustomField, String> customAttributeFilter = new HashMap<>();
    customAttributeFilter.put(TaskCustomField.CUSTOM_1, "Geschaeftsstelle A");
    final List<TimeIntervalColumnHeader> columnHeaders =
        List.of(new TimeIntervalColumnHeader(0, 0));

    List<String> expectedResult = List.of("Geschaeftsstelle A");
    when(monitorMapperMock.getCustomAttributeValuesForReport(
            workbasketIds,
            states,
            categories,
            domains,
            classificationIds,
            excludedClassificationIds,
            customAttributeFilter,
            null,
            TaskCustomField.CUSTOM_1))
        .thenReturn(expectedResult);

    final List<String> actualResult =
        cut.createClassificationCategoryReportBuilder()
            .workbasketIdIn(workbasketIds)
            .stateIn(states)
            .classificationCategoryIn(categories)
            .domainIn(domains)
            .classificationIdIn(classificationIds)
            .excludedClassificationIdIn(excludedClassificationIds)
            .customAttributeFilterIn(customAttributeFilter)
            .withColumnHeaders(columnHeaders)
            .listCustomAttributeValuesForCustomAttributeName(TaskCustomField.CUSTOM_1);

    verify(internalTaskanaEngineMock).openConnection();
    verify(internalTaskanaEngineMock, times(2)).getEngine();
    verify(taskanaEngineMock).checkRoleMembership(any());
    verify(taskanaEngineMock).getWorkingDaysToDaysConverter();
    verify(monitorMapperMock)
        .getCustomAttributeValuesForReport(
            any(), any(), any(), any(), any(), any(), any(), any(), any());
    verify(internalTaskanaEngineMock).returnConnection();
    verifyNoMoreInteractions(
        internalTaskanaEngineMock,
        taskanaEngineMock,
        monitorMapperMock,
        taskanaEngineConfiguration);

    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @Test
  void testListCustomAttributeValuesForCustomAttributeNameIsEmptyResult() throws Exception {
    List<String> result =
        cut.createClassificationCategoryReportBuilder()
            .workbasketIdIn(List.of("DieGibtsSicherNed"))
            .listCustomAttributeValuesForCustomAttributeName(TaskCustomField.CUSTOM_1);
    assertThat(result).isNotNull();
  }
}
