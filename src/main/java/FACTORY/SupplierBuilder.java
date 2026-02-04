package FACTORY;

import DTO.SupplierDTO;
import INTERFACE.Builder;

public class SupplierBuilder implements Builder<SupplierDTO> {
    private int id;
    private int statusId;
    private String name;
    private String phone;
    private String address;
    private String email;

    public SupplierBuilder id(int id) {
        this.id = id;
        return this;
    }

    public SupplierBuilder statusId(int statusId) {
        this.statusId = statusId;
        return this;
    }

    public SupplierBuilder name(String name) {
        this.name = name;
        return this;
    }

    public SupplierBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public SupplierBuilder address(String address) {
        this.address = address;
        return this;
    }

    public SupplierBuilder email(String email) {
        this.email = email;
        return this;
    }

    @Override
    public SupplierDTO build() {
        return new SupplierDTO(id, name, phone, address, email, statusId);
    }
}
