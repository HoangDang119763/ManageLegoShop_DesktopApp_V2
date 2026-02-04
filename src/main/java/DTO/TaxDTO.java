package DTO;

public class TaxDTO {
    private int id;
    private int employeeId;
    private int numDependents;

    public TaxDTO() {
    }

    public TaxDTO(int id, int employeeId, int numDependents) {
        this.id = id;
        this.employeeId = employeeId;
        this.numDependents = numDependents;
    }

    public TaxDTO(TaxDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.numDependents = other.numDependents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getNumDependents() {
        return numDependents;
    }

    public void setNumDependents(int numDependents) {
        this.numDependents = numDependents;
    }

    @Override
    public String toString() {
        return "TaxDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", numDependents=" + numDependents +
                '}';
    }
}
