package projekt.gui.scene.tableObjects;

import java.awt.*;

public class problemArchetypeEntrys {
    private String title;
    private String param1;
    private String param2;

    public  problemArchetypeEntrys(String title, String param1, String param2){
        this.title = title;
        this.param1 = param1;
        this.param2 = param2;
    }

    public String getParam1() {
        return param1;
    }

    public String getParam2() {
        return param2;
    }

    public String getTitle() {
        return title;
    }
}
