package com.fitness.model;

/**
 * Concrete subclass representing a System Administrator.
 * Demonstrates:
 * - Inheritance from User
 * - Method Overriding (Polymorphism)
 * - Specialized fields (department, adminLevel, canManageAdmins)
 */
public class AdminUser extends User {
    private String department;
    private int adminLevel; // 1 = Moderator, 2 = Senior, 3 = SuperAdmin
    private boolean canManageAdmins;

    public AdminUser() {
        super();
        setRole(Role.ADMIN);
    }

    public AdminUser(Integer id, String name, String email, String passwordHash,
                     String department, int adminLevel, boolean canManageAdmins) {
        super(id, name, email, passwordHash, Role.ADMIN);
        this.department = department;
        this.adminLevel = adminLevel;
        this.canManageAdmins = canManageAdmins;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
    }

    public boolean isCanManageAdmins() {
        return canManageAdmins;
    }

    public void setCanManageAdmins(boolean canManageAdmins) {
        this.canManageAdmins = canManageAdmins;
    }

    @Override
    public String getDashboardUrl() {
        return "/dashboard?view=admin";
    }

    @Override
    public String getProfileSummary() {
        return String.format("Admin [Dept: %s | Level: %d | SuperPrivileges: %b]",
                department, adminLevel, canManageAdmins);
    }
}
