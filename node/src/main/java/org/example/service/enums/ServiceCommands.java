package org.example.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    CANCEL("/cancel"),
    REGISTRATION("/registration"),
    START("/start");
    private final String value;

    ServiceCommands(String value){
        this.value = value;
    }
@Override
    public String toString(){
        return value;
    }

    public static Object fromValue(String v) {
        for(ServiceCommands c: ServiceCommands.values()){
            if(c.value.equals(v)){
                return c;
            }
        }
        return null;
    }


}
