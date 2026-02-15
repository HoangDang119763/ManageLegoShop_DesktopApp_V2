package DTO;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class RolePermissionDTO {
    private int roleId;
    private int permissionId;

    public RolePermissionDTO(int roleId, int permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }

    public RolePermissionDTO(RolePermissionDTO other) {
        if (other != null) {
            this.roleId = other.roleId;
            this.permissionId = other.permissionId;
        }
    }
}
