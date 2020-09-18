package in.projecteka.dataavailabilitynotification.common.Model;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class HeartbeatResponse {
    LocalDateTime timeStamp;
    Status status;
    Error error;
}
