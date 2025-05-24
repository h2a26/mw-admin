package org.hein.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hein.utils.AuditableEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "role_id"})
}, indexes = {
        @Index(name = "idx_user_role_user_id", columnList = "user_id"),
        @Index(name = "idx_user_role_role_id", columnList = "role_id"),
        @Index(name = "idx_user_role_active", columnList = "active"),
        @Index(name = "idx_user_role_valid_to", columnList = "valid_to")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * When this role was assigned to the user
     */
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    /**
     * Start date when this role becomes active for the user
     */
    @Column(name = "valid_from")
    private LocalDateTime validFrom;
    
    /**
     * End date when this role expires for the user (null means no expiration)
     */
    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /**
     * User who assigned this role
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    private User assignedBy;

    /**
     * Reason for role assignment
     */
    @Column(name = "assignment_reason", length = 500)
    private String assignmentReason;

    /**
     * Status of the role assignment (PENDING, ACTIVE, REJECTED, REVOKED, EXPIRED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private UserRoleStatus status;
    
    /**
     * User who approved this role assignment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;
    
    /**
     * When this role assignment was approved
     */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Whether this role assignment is currently active
     */
    @Builder.Default
    private boolean active = true;

    /**
     * Whether to inherit permissions from parent roles
     */
    @Builder.Default
    @Column(name = "inherit_permissions")
    private boolean inheritPermissions = true;
    
    /**
     * Optional restrictions or limitations on this role assignment
     * Could be a JSON string with specific constraints
     */
    @Column(name = "restrictions", length = 1000)
    private String restrictions;

    /**
     * Notes provided during approval
     */
    @Column(name = "approver_notes", length = 500)
    private String approverNotes;
    
    /**
     * Reason provided for rejecting the role assignment
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
    
    /**
     * When this role assignment was rejected
     */
    @Column(name = "rejection_date")
    private LocalDateTime rejectionDate;
    
    /**
     * User who revoked this role assignment (if revoked before expiry)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;
    
    /**
     * When this role assignment was revoked
     */
    @Column(name = "revocation_date")
    private LocalDateTime revocationDate;
    
    /**
     * Reason for revoking the role assignment
     */
    @Column(name = "revocation_reason", length = 500)
    private String revocationReason;
    
    /**
     * Constructor for creating a basic user-role relationship
     */
    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
        this.assignedAt = LocalDateTime.now();
        this.validFrom = LocalDateTime.now();
        this.status = UserRoleStatus.ACTIVE;
    }
    
    /**
     * Check if this role assignment is currently valid based on validation dates
     */
    @Transient
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        boolean validTimeFrame = (validFrom == null || !now.isBefore(validFrom)) && 
                    (validTo == null || !now.isAfter(validTo));
        return active && validTimeFrame && status == UserRoleStatus.ACTIVE;
    }

    @PrePersist
    protected void onPrePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
        
        if (validFrom == null) {
            validFrom = LocalDateTime.now();
        }
        
        if (status == null) {
            status = UserRoleStatus.PENDING;
        }
    }
    
    /**
     * Approve this role assignment
     */
    public void approve(User approver, String notes) {
        this.status = UserRoleStatus.ACTIVE;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.approverNotes = notes;
    }
    
    /**
     * Reject this role assignment
     */
    public void reject(User approver, String reason) {
        this.status = UserRoleStatus.REJECTED;
        this.approvedBy = approver;
        this.rejectionDate = LocalDateTime.now();
        this.rejectionReason = reason;
        this.active = false;
    }
    
    /**
     * Revoke this role assignment before its expiry date
     */
    public void revoke(User revoker, String reason) {
        this.status = UserRoleStatus.REVOKED;
        this.revokedBy = revoker;
        this.revocationDate = LocalDateTime.now();
        this.revocationReason = reason;
        this.active = false;
    }
    
    /**
     * Mark this role assignment as expired (called by the system)
     */
    public void markExpired() {
        this.status = UserRoleStatus.EXPIRED;
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole userRole)) return false;
        return user != null && role != null &&
               user.equals(userRole.user) &&
               role.equals(userRole.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, role);
    }
}