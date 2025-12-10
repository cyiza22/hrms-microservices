package api.example.leaveservice.observer;

import api.example.leaveservice.entity.Leave;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingObserver implements LeaveObserver {

    @Override
    public void onLeaveStatusChanged(Leave leave) {
        log.info("Leave status changed: ID={}, EmployeeID={}, Status={}",
                leave.getId(), leave.getEmployeeId(), leave.getStatus());
    }

    @Override
    public String getObserverName() {
        return "LoggingObserver";
    }
}