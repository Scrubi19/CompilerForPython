package CodeGenerator;

public class Register {
    private String name;
    private boolean value;

    public Register(String name, boolean value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmployment(boolean value) {
        this.value = value;
    }
}