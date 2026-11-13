package com.crm.crm_backend.model;

public enum ProductCategory {
    SECURITY_HARDWARE("Security Hardware"),
    SOFTWARE_LICENSE("Software License"),
    CYBERSECURITY_AUDIT("Cybersecurity Audit"),
    ENTERPRISE_ERP("Enterprise ERP"),
    TRAINING_AND_CERTIFICATION("Training and Certification"),
    MAINTENANCE_AND_SUPPORT("Maintenance and Support"),
    OTHER("Other");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
