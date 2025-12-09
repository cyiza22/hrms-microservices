package api.example.leaveservice.observer;

import api.example.leaveservice.entity.Leave;

/**
 * Observer Pattern Interface
 * Purpose: Define contract for objects that want to be notified of leave changes
 * This demonstrates the Observer Design Pattern from the curriculum
 */
public interface LeaveObserver {
    /**
     * Called when a leave status changes
     * @param leave The leave that was modified
     */
    void onLeaveStatusChanged(Leave leave);

    /**
     * Returns the name of this observer for logging
     */
    String getObserverName();
}