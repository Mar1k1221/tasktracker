package homework1;

import com.example.tasktracker.model.TaskStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskStatusTest {

     @ParameterizedTest(name="{0} -> {1} переход разрешен/запрещен")
     @CsvSource({
             "NEW,IN_PROGRESS,true",
             "NEW,CANCELLED,true",
             "IN_PROGRESS,DONE,true",
             "IN_PROGRESS,CANCELLED,true",
             "IN_PROGRESS,NEW,true",
             "DONE,IN_PROGRESS,true",
             "DONE,CANCELLED,true",
             "CANCELLED,NEW,false",
             "DONE,NEW,false",
             "NEW,DONE,false"
     })
    public void test_status_transition(TaskStatus previous, TaskStatus to,boolean status_result ){
         boolean result = previous.status_transition(to);
         assertEquals(result,status_result);
     }

}