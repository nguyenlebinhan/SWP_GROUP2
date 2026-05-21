package model;

public class RolePermissionItem {
    private final Permission permission;
    private final boolean granted;

    public RolePermissionItem(Permission permission, boolean granted) {
        this.permission = permission;
        this.granted = granted;
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isGranted() {
        return granted;
    }
}
