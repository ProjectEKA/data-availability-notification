package in.projecteka.datanotificationsubscription.common.model;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum HIType {

    OP_CONSULTATION("OPConsultation"),
    DIAGNOSTIC_REPORT("DiagnosticReport"),
    PRESCRIPTION("Prescription"),
    IMMUNIZATION_RECORD("ImmunizationRecord"),
    DISCHARGE_SUMMARY("DischargeSummary"),
    HEALTH_DOCUMENT_RECORD("HealthDocumentRecord"),
    WELLNESS_RECORD("WellnessRecord");
    private final String resourceType;

    HIType(String value) {
        resourceType = value;
    }

    @JsonValue
    public String getValue() {
        return resourceType;
    }

    public HIType findByValue(String input) {
        return Arrays.stream(HIType.values())
                .filter(hiType -> hiType.resourceType.equals(input))
                .findAny().get();
    }

}
