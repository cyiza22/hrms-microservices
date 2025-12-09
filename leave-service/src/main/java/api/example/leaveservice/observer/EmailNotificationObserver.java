package api.example.leaveservice.observer;

import api.example.leaveservice.entity.Leave;
import api.example.leaveservice.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer for Email Notifications
 * Sends emails when leave status changes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationObserver implements LeaveObserver {

    private final EmailNotificationService emailService;

    @Override
    public void onLeaveStatusChanged(Leave leave) {
        try {
            log.info("Email observer triggered for leave ID: {}", leave.getId());

            switch (leave.getStatus()) {
                case APPROVED:
                    emailService.sendLeaveApprovalEmail(leave);
                    break;
                case REJECTED:
                    emailService.sendLeaveRejectionEmail(leave);
                    break;
                case PENDING:
                    emailService.sendLeaveApplicationEmail(leave);
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());
        }
    }

    @Override
    public String getObserverName() {
        return "EmailNotificationObserver";
    }
}