package acceptance.events.workbasket;

import static org.assertj.core.api.Assertions.assertThat;

import acceptance.AbstractAccTest;
import acceptance.security.JaasExtension;
import acceptance.security.WithAccessId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import pro.taskana.simplehistory.impl.SimpleHistoryServiceImpl;
import pro.taskana.spi.history.api.events.workbasket.WorkbasketHistoryEvent;
import pro.taskana.spi.history.api.events.workbasket.WorkbasketHistoryEventType;
import pro.taskana.workbasket.api.WorkbasketService;
import pro.taskana.workbasket.api.models.WorkbasketAccessItem;

@ExtendWith(JaasExtension.class)
class CreateHistoryEventOnWorkbasketAccessItemsSetAccTest extends AbstractAccTest {

  private final WorkbasketService workbasketService = taskanaEngine.getWorkbasketService();
  private final SimpleHistoryServiceImpl historyService = getHistoryService();

  @WithAccessId(user = "admin")
  @Test
  void should_CreateWorkbasketAccessItemsUpdatedHistoryEvent_When_AccessItemsAreSet()
      throws Exception {

    final String workbasketId = "WBI:100000000000000000000000000000000004";
    final String accessId1 = "peter";
    final String accessId2 = "claudia";
    final String accessId3 = "sven";

    List<WorkbasketHistoryEvent> events =
        historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).isEmpty();

    List<WorkbasketAccessItem> newItems = new ArrayList<>();

    WorkbasketAccessItem newWorkbasketAccessItem =
        workbasketService.newWorkbasketAccessItem(workbasketId, accessId1);
    WorkbasketAccessItem newWorkbasketAccessItem2 =
        workbasketService.newWorkbasketAccessItem(workbasketId, accessId2);
    WorkbasketAccessItem newWorkbasketAccessItem3 =
        workbasketService.newWorkbasketAccessItem(workbasketId, accessId3);

    newItems.add(newWorkbasketAccessItem);
    newItems.add(newWorkbasketAccessItem2);
    newItems.add(newWorkbasketAccessItem3);

    workbasketService.setWorkbasketAccessItems(workbasketId, newItems);

    events = historyService.createWorkbasketHistoryQuery().workbasketIdIn(workbasketId).list();

    assertThat(events).hasSize(1);

    String eventType = events.get(0).getEventType();

    assertThat(eventType)
        .isEqualTo(WorkbasketHistoryEventType.WORKBASKET_ACCESS_ITEMS_UPDATED.getName());
  }
}