package com.app.fresy.connection.response;

public class RespShipMethod {

    public Long id;
    public String title;
    public boolean enabled;
    public String method_id;
    public Settings settings;

    public class Settings {
        public SubSettings title;
        public SubSettings tax_status;
        public SubSettings cost;
    }

}
